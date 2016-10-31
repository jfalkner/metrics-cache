package com.pacb.itg.metrics.cache

import java.nio.file.{Files, Path, Paths}
import java.time.Instant

import falkner.jayson.metrics._
import falkner.jayson.metrics.io.CSV
import falkner.jayson.metrics.io.CSV.Chunk

import collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import jfalkner.logs.Logs

import scala.collection.mutable.Map

case class Entry(key: String, version: String, value: Path)

case class Key(key: String, version: String)

object MetricsCache {

  def apply(cacheDir: String): MetricsCache = new MetricsCache(Paths.get(cacheDir))

  def apply(cacheDir: Path): MetricsCache = new MetricsCache(cacheDir)
}

class MetricsCache(val cacheDir: Path) extends Logs {

  override val logsPath = Files.createDirectories(cacheDir.resolve("logs"))

  val entryLog = make[Entry](".entries.csv")

  lazy val cache: Map[Key, Path] = Map(entryLog.load().map(e => (Key(e.key, e.version), e.value)).toList :_ *)

  def getOrElse(key: String, version: String, c: => Chunk): Chunk = getOrElse(Key(key, version), c)

  def getOrElse(key: Key, chunk: => Chunk): Chunk = cache.get(key) match {
    case Some(hit) => Try(Files.readAllLines(hit).asScala.toList) match {
      case Success(lines) =>
        //println(s"Matched: $key. Returning cached chunk.")
        CSV(lines)
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
    case Some(hit) => Try(Files.readAllLines(hit).asScala.toList) match {
      case Success(lines) => if (CSV(lines).all != chunk.all) makeEntry(key, version, chunk) else chunk
      case Failure(t) => makeEntry(key, version, chunk)
    }
    case _ => makeEntry(key, version, chunk)
  }

  def makeEntry(key: String, m: Metrics): Chunk = makeEntry(key, m.version, CSV(m))

  def makeEntry(key: String, version: String, c: Chunk): Chunk = {
    val ts = Instant.now().toString()
    val p = Files.write(Files.createDirectories(cacheDir.resolve(version)).resolve(ts), c.all.getBytes)
    cache(Key(key, version)) = p
    entryLog.log(Entry(key, version, p))
    c
  }
}

