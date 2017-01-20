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

  protected def path(ns: String, key: String): Path = path(ns, key, "json")

  protected def path(ns: String, key: String, ext: String): Path = cache.resolve(s"$ext/$ns/$key.$ext")

  protected def json(ns: String, key: String): JsObject =
    new String(Files.readAllBytes(path(ns, key))).parseJson.asJsObject

  protected def list(): Iterator[Path] = cache.resolve("json") match {
    case p if Files.exists(p) => Files.list(p).iterator.asScala.filter(p => Files.isDirectory(p))
    case _ => Nil.iterator
  }

  override def put(ns: String, key: String, metrics: Metrics) = JSON(path(ns, key), metrics)

  override def put(ns: String, key: String, json: JsObject) = {
    Files.createDirectories(path(ns, key).getParent)
    Files.write(path(ns, key), json.toString.getBytes)
  }

  override def query(key: String): Map[String, JsValue] =
    list.map(_.getFileName.toString).flatMap(ns =>
      Try(json(ns, key)) match {
        case Success(vals) => Some(vals)
        case Failure(t) => None
      }).flatMap(ms => ms.fields.toSeq).toMap
}
