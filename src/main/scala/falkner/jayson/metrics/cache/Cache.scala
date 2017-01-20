package falkner.jayson.metrics.cache

import java.io.BufferedWriter
import java.nio.file.{Files, Path}

import falkner.jayson.metrics.Col.value
import falkner.jayson.metrics.{Metrics, View}
import falkner.jayson.metrics.io.CSV
import spray.json.{JsObject, JsValue}

/**
  * Created by jfalkner on 1/19/17.
  */
trait Cache {

  def put(ns: String, key: String, json: JsObject)

  def put(ns: String, key: String, metrics: Metrics)

  def query(key: String): Map[String, JsValue]

  def queriesToCsv(outputCsv: Path, queries: Traversable[String], view: View): Path = {
    val bw = Files.newBufferedWriter(outputCsv)
    bw.write(view.metrics.map(_.name).map(CSV.escape).mkString(",") + "\n")

    queriesToCsv(bw, queries, view)

    bw.flush
    bw.close

    outputCsv
  }

  def queriesToCsv(bw: BufferedWriter, queries: Traversable[String], view: View): Unit =
    queries.foreach(q => bw.write(view.metrics.map(m => value(query(q), m)).mkString(",") + "\n"))
}
