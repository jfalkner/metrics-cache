package falkner.jayson.metrics.cache

import java.nio.file.{Files, Path}

import falkner.jayson.metrics._
//import falkner.jayson.metrics.io.CSV
//import org.apache.commons.io.FileUtils
//import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
//import collection.JavaConverters._


/**
  * Placeholder for test.
  */
class MetricsCacheSpec extends Specification {

  "MetricCache" should {
    "Have a test" in (1 mustEqual 1)
//    "Correctly populate a cache" in {
//      withCleanup { cacheDir =>
//        val m1 = new TestMetrics("Foo", 1)
//        val k =  Key("foo", m1.version)
//        val mc = MetricsCache(cacheDir)
//        val filesBefore = Files.list(cacheDir).iterator().asScala.toList
//        // cache entry doesn't exist initially
//        mc.cache.contains(k) mustEqual false
//        mc.updateIfNotEqual(k.key, m1)
//        mc.getOrElse(k, CSV(m1)) mustEqual CSV(m1)
//        // cache entry should exist after
//        mc.cache.contains(k) mustEqual true
//        // there should now be one more file in the cache
//        val filesAfter = Files.list(cacheDir).iterator().asScala.toList
//        filesBefore.size mustEqual filesAfter.size - 1
//        // asking for the same key should hit and cause no changes
//        mc.getOrElse(k, throw new Exception("No match!")).all mustEqual CSV(m1).all
//        filesAfter mustEqual Files.list(cacheDir).iterator().asScala.toList
//        // reload of cache should have the same keys
//        val mc2 = MetricsCache(cacheDir)
//        mc2.cache mustEqual mc.cache
//      }
//    }
//    "Confirm multiple entries per cache file works" in {
//      withCleanup { cacheDir =>
//        val mc = MetricsCache(cacheDir)
//        val a = new TestMetrics("a", 1)
//        val b = new TestMetrics("b", 2)
//        val bc = CSV(b)
//        val c = new TestMetrics("c", 3)
//        val ka =  Key("ka", a.version)
//        val kb =  Key("kb", b.version)
//        val kc =  Key("kc", c.version)
//        mc.updateIfNotEqual(ka.key, a)
//        mc.updateIfNotEqual(kb.key, b.version, bc)
//        mc.updateIfNotEqual(kc.key, c)
//        Files.list(cacheDir.resolve(a.version)).iterator.asScala.toList.size mustEqual 1
//        mc.getOrElse(ka, null) mustEqual CSV(a)
//        mc.getOrElse(kb, null) mustEqual CSV(b)
//        mc.getOrElse(kc.key, kc.version, null) mustEqual CSV(c)
//        // confirm duplicate entries don't alter the cache
//        mc.updateIfNotEqual(kb.key, b.version, bc).eq(bc) mustEqual true
//        // confirm an in-place update works
//        val b2 = new TestMetrics("b", 22)
//        mc.updateIfNotEqual(kb.key, b2)
//        mc.getOrElse(kb, null) mustEqual CSV(b2)
//        // confirm a reload has the same -- also test String-based constructor
//        val mc2 = MetricsCache(cacheDir.toAbsolutePath.toString)
//        mc2.getOrElse(ka, null) mustEqual CSV(a)
//        mc2.getOrElse(kb, null) mustEqual CSV(b2)
//        mc2.getOrElse(kc, null) mustEqual CSV(c)
//        // confirm block size is working
//        mc.latestBlock(a.version) mustEqual mc.cache(ka).p
//        mc.latestBlock(a.version).getFileName.toString mustEqual "0.csv"
//        mc.latestBlock(a.version, 0).getFileName.toString mustEqual "1.csv"
//        // confirm block name sort is working -- e.g. if 3.csv exists, then 4.csv is made
//        Files.createFile(mc.latestBlock(a.version, 0))
//        mc.latestBlock(a.version, -1).getFileName.toString mustEqual "2.csv"
//        // if a file is deleted that the cache depends on, auto-recover
////        Files.list(cacheDir.resolve(a.version)).iterator.asScala.toList.foreach(f => Files.delete(f))
////        mc.getOrElse(ka, null) mustEqual CSV(a)
//      }
//    }
  }

//  def withCleanup(f: (Path) => MatchResult[Any]): MatchResult[Any] = {
//    val basePath = Files.createTempDirectory("MetricsCache")
//    try {
//      f(basePath)
//    }
//    finally {
//      Seq(basePath).foreach(p => FileUtils.deleteDirectory(p.toFile))
//    }
//  }
}

class TestMetrics(sv: String, nv: Int) extends Metrics {
  override val namespace = "Test"
  override val version = "_"
  override lazy val values: List[Metric] = List(
    Str("String", sv),
    Num("NumExact", nv)
  )
}
