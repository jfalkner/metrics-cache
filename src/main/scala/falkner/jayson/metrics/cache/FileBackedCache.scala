package falkner.jayson.metrics.cache

import java.nio.file.{Files, Path}

import falkner.jayson.metrics.Metrics
import falkner.jayson.metrics.io.JSON
import spray.json.{JsObject, JsValue}

import scala.util.{Failure, Success, Try}
import collection.JavaConverters._
import spray.json._


object FileBackedCache {

  def apply(p: Path): FileBackedCache = new FileBackedCache {
    override val cache = p
  }
}

/**
  * Simple, no-config file-backed cache
  */
trait FileBackedCache extends Cache {

  val cache: Path

  lazy val namespaces = list.map(_.getFileName.toString)

  protected def path(ns: String, key: String): Path = path(ns, key, "json")

  protected def path(ns: String, key: String, ext: String): Path = cache.resolve(s"$ext/$ns/$key.$ext")

  protected def json(ns: String, key: String): JsObject =
    new String(Files.readAllBytes(path(ns, key))).parseJson.asJsObject

  protected def list(): List[Path] = cache.resolve("json") match {
    case p if Files.exists(p) => FileUtil.list(p)
    case _ => Nil
  }

  override def put(ns: String, key: String, metrics: Metrics) = JSON(path(ns, key), metrics)

  override def put(ns: String, key: String, json: JsObject) = {
    Files.createDirectories(path(ns, key).getParent)
    Files.write(path(ns, key), json.toString.getBytes)
  }

  override def query(key: String): Map[String, JsValue] =
    namespaces.flatMap(ns =>
      Try(json(ns, key)) match {
        case Success(vals) => Some(vals)
        case Failure(t) => None
      }).flatMap(ms => ms.fields.toSeq).toMap
}

// TODO: has to be an API in scala that does this
object FileUtil {

  def list(p: Path, isDir: Boolean = true): List[Path] = {
    val ds = Files.list(p)
    try {
      ds.iterator.asScala.toList.filter(f => Files.isDirectory(f) == isDir)
    } finally {
      ds.close
    }
  }

  def listFiles(p: Path): List[Path] = list(p, false)

}