package falkner.jayson.metrics.cache

import java.nio.file.{Files, Path, Paths}

import falkner.jayson.metrics._
import org.specs2.matcher.MatchResult
import spray.json.JsValue
import org.specs2.mutable.Specification


class FileBackedCacheSpec extends Specification {

  "MetricCache" should {
    "Correctly populate a cache" in {
      withCleanup { cacheDir =>
        val m1 = new TestMetrics("Foo", 1)
        val m2 = new MoreTestMetrics("Bar")
        val mc = FileBackedCache(cacheDir)
        mc.put("test", "example/key", m1)
        mc.put("test2", "example/key", m2)

        def strip(v: JsValue): String = v.toString.stripPrefix("\"").stripSuffix("\"")

        val results = mc.query("example/key")
        Set("Test", "Test2") mustEqual results.keySet
        Set("String", "NumExact") mustEqual results("Test").asJsObject.fields.keySet
        "Foo" mustEqual strip(results("Test").asJsObject.fields("String"))
        "1" mustEqual strip(results("Test").asJsObject.fields("NumExact"))
        Set("More") mustEqual results("Test2").asJsObject.fields.keySet
        "Bar" mustEqual strip(results("Test2").asJsObject.fields("More"))
      }
    }
  }

  def withCleanup(f: (Path) => MatchResult[Any]): MatchResult[Any] = {
    val basePath = Files.createTempDirectory("MetricsCache")
    try {
      f(basePath)
    }
    finally {
      Seq(
        "json/test/example/key.json",
        "json/test2/example/key.json",
        "json/test/example",
        "json/test2/example",
        "json/test",
        "json/test2",
        "json"
      ).map(s => basePath.resolve(s)).map(p => Files.delete(p))
      Files.delete(basePath)
    }
  }
}

class TestMetrics(sv: String, nv: Int) extends Metrics {
  override val namespace = "Test"
  override val version = "_"
  override lazy val values: List[Metric] = List(
    Str("String", sv),
    Num("NumExact", nv)
  )
}

class MoreTestMetrics(sv: String) extends Metrics {
  override val namespace = "Test2"
  override val version = "_"
  override lazy val values: List[Metric] = List(
    Str("More", sv)
  )
}
