package com.pacb.itg.metrics.cache

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._
import falkner.jayson.metrics.io.CSV
import org.apache.commons.io.FileUtils
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import collection.JavaConverters._


/**
  * Placeholder for test.
  */
class MetricsCacheSpec extends Specification {

  "MetricCache" should {
    "Correctly populate a cache" in {
      withCleanup { cacheDir =>
        val m1 = new TestMetrics("Foo", 1)
        val k =  Key("foo", m1.version)
        val mc = MetricsCache(cacheDir)
        val filesBefore = Files.list(cacheDir).iterator().asScala.toList
        // cache entry doesn't exist initially
        mc.cache.contains(k) mustEqual false
        mc.updateIfNotEqual(k.key, m1)
        mc.getOrElse(k, CSV(m1)) mustEqual CSV(m1)
        // cache entry should exist after
        mc.cache.contains(k) mustEqual true
        // there should now be one more file in the cache
        val filesAfter = Files.list(cacheDir).iterator().asScala.toList
        filesBefore.size mustEqual filesAfter.size - 1
        // asking for the same key should hit and cause no changes
        mc.getOrElse(k, throw new Exception("No match!")).all mustEqual CSV(m1).all
        filesAfter mustEqual Files.list(cacheDir).iterator().asScala.toList
        // reload of cache should have the same keys
        val mc2 = MetricsCache(cacheDir)
        mc2.cache mustEqual mc.cache
      }
    }
  }

  def withCleanup(f: (Path) => MatchResult[Any]): MatchResult[Any] = {
    val basePath = Files.createTempDirectory("MetricsCache")
    try {
      f(basePath)
    }
    finally {
      Seq(basePath).foreach(p => FileUtils.deleteDirectory(p.toFile))
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
