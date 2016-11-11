package com.pacb.itg.metrics.cache

import java.nio.file.{Files, Path, Paths}

import falkner.jayson.metrics._
import falkner.jayson.metrics.io.CSV
import falkner.jayson.metrics.io.CSV.Chunk

import collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import jfalkner.logs.Logs

import scala.collection.mutable.Map

// cache files are looked up by (key, version) and path has many lines of CSV with index being the row for this entry
case class Entry(key: String, version: String, value: Path, index: Int)

case class Key(key: String, version: String)
case class Value(p: Path, index: Int)

object MetricsCache {

  def apply(cacheDir: String): MetricsCache = new MetricsCache(Paths.get(cacheDir))

  def apply(cacheDir: Path): MetricsCache = new MetricsCache(cacheDir)
}

class MetricsCache(val cacheDir: Path) extends Logs {

  override val logsPath = Files.createDirectories(cacheDir.resolve("logs"))

  val entryLog = make[Entry](".entries.csv")
  entryLog.squash()

  lazy val cache: Map[Key, Value] = Map(entryLog.load().map(e => (Key(e.key, e.version), Value(e.value, e.index))).toList :_ *)

  def getOrElse(key: String, version: String, c: => Chunk): Chunk = getOrElse(Key(key, version), c)

  def getOrElse(key: Key, chunk: => Chunk): Chunk = cache.get(key) match {
    case Some(hit) => Try(Files.readAllLines(hit.p).asScala.toList) match {
      case Success(lines) =>
        //println(s"Matched: $key. Returning cached chunk.")
        CSV(lines, hit.index)
      case Failure(t) =>
        //println(s"Failed converting cached chunk for: $key. Returning default chunk.")
        chunk
    }
    case _ =>
      //println(s"Failed to match: $key. Returning default chunk.")
      chunk
  }

  def updateIfNotEqual(key: String, m: => Metrics): Chunk = updateIfNotEqual(key, m.version, CSV(m))

  def updateIfNotEqual(key: String, version: String, chunk: Chunk): Chunk = cache.get(Key(key, version)) match {
    case Some(hit) =>
      val lines = Files.readAllLines(hit.p).asScala.toList
      if (CSV(lines, hit.index).all != chunk.all) makeEntry(key, version, chunk) else chunk
    case _ => makeEntry(key, version, chunk)
  }

  def makeEntry(key: String, version: String, c: Chunk): Chunk = {
    val k = Key(key, version)
    cache.get(k) match {
      case Some(hit) =>
        val lines = Files.readAllLines(hit.p).asScala.toList.patch(hit.index, Seq(c.values), 1)
        Files.write(hit.p, lines.mkString("\n").getBytes)
      case None =>
        val p = latestBlock(version)
        if (!Files.exists(p)) Files.createFile(p)
        val index = Files.readAllLines(p).asScala.toList match {
          case lines if lines.isEmpty =>
            Files.write(p, c.all.getBytes)
            1
          case lines =>
            Files.write(p, (lines ++ Seq(c.values)).mkString("\n").getBytes)
            lines.size
        }
        cache(k) = Value(p, index)
        entryLog.log(Entry(k.key, k.version, p, index))
    }
    c
  }

  // default NFS rsize matches /pbi/dept/itg. find via `mount -l | grep itg`. Must be smaller to use just one block transfer
  def latestBlock(version: String, sizeLimit: Long = 65536 - 1024): Path = {
    val versionCache = cacheDir.resolve(version)
    Files.createDirectories(versionCache)
    val sorted = Files.list(versionCache).iterator.asScala.toList.sortWith(_.getFileName.toString > _.getFileName.toString)
    sorted.isEmpty match {
      case true => versionCache.resolve("0.csv")
      case _ => Files.size(sorted.head) > sizeLimit match {
        case true => versionCache.resolve(s"${sorted.head.getFileName.toString.split('.').head.toInt + 1 }.csv")
        case _ => sorted.head
      }
    }
  }
}