package com.typesafe.sbt.dart

import sbt._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.SbtWeb._
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys.baseDirectory
import sbt.Keys.unmanagedResourceDirectories
import sbt.Keys.resourceGenerators
import sbt.Keys.resourceDirectories

import sbt.Keys.state
import play.PlayRunHook
import java.net.InetSocketAddress

import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.web.Import._

import com.typesafe.sbt.packager.universal.Keys._
//import play.twirl.sbt.Import._

object Import {

  object DartKeys {
    val dart2js = TaskKey[Unit]("dart2js", "Build dart js applications.")

    val dartWeb = SettingKey[File]("dart-web", "Dart web directory.")
  }

}

object SbtDart extends Plugin with DartProcessor {

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport.DartKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    dart2js := {
      runPub(baseDirectory.value, (target in dart2js).value.absolutePath, List()).get.exitValue()
    },
    dist <<= dist dependsOn (dart2js),
    stage <<= stage dependsOn (dart2js),
    target in dart2js := webTarget.value / dart2js.key.label,

    unmanagedResourceDirectories in Assets <+= (target in dart2js)(base => base / "web"),

    playRunHooks <+= (baseDirectory, target in dart2js).map { (base, output) => Pub(base, output.absolutePath) },

    resourceDirectories in Compile <+= baseDirectory / "web",

    resourceGenerators in Assets <+= dart2jsCompiler,
    

    dartWeb in dart2js := baseDirectory.value / "web")

  def dartSources: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
      println("Dart sources copy")
      val build = (target in dart2js).value
      val src = (dartWeb in dart2js).value
      val watch = (base: File) => base ** "*"

      watch(src).get.map(f => (f, f.relativeTo(src))).filter {
        case (f, Some(r)) => true
        case _ => false
      } map (e => (e._1, e._2.get.toString()))

  }
  def dartbuiltFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>
      println("Dart2js ...")
      val build = (target in dart2js).value
      runCommand(pubExePath + " build --output " + build.absolutePath)

      val watch = (base: File) => base ** "*"

      val o = watch(build);
      val oo = o.get
      val ret = oo.map(f => (f, f.relativeTo(build / "web"))).filter {
        case (f, Some(r)) => true
        case _ => false
      } map (e => (e._1, e._2.get.toString()))
      ret

  }

  private def runPub(base: sbt.File, output: String, args: List[String]) = {
    println(s"Will run: pub --gruntfile=$output $args in ${base.getPath}")
    if (System.getProperty("os.name").startsWith("Windows")) {
      val process: ProcessBuilder = Process("cmd" :: "/c" :: "pub.exe" :: "build" :: "--output=" + output :: args, base)
      println(s"Will run: ${process.toString} in ${base.getPath}")
      Some(process.run)
    } else {
      val process: ProcessBuilder = Process("pub" :: "build" :: "--output=" + output :: args, base)
      println(s"Will run: ${process.toString} in ${base.getPath}")
      Some(process.run)
    }
  }

  object Pub {
    def apply(base: File, output: String): PlayRunHook = {

      object PubProcess extends PlayRunHook {

        var process: Option[Process] = None

        override def afterStarted(addr: InetSocketAddress): Unit = {
          process = runPub(base, output, Nil)
        }

        override def afterStopped(): Unit = {
          process.map(p => p.destroy())
          process = None
        }
      }

      PubProcess
    }
  }

  def Dart2jsCompiler(name: String,
    watch: File => PathFinder,
    proc: DartProcessor): sbt.Def.Initialize[sbt.Task[Seq[java.io.File]]] = {
    (state, baseDirectory) map { (state, base) =>
      {
        state.log.info("OOOOOOOO")
        Nil
      }
    }
  }

  val dart2jsCompiler = Dart2jsCompiler("dart" + "-js-compiler",
    src => (src ** "*") --- (src ** "*.dart.*") --- (src ** "out" ** "*"),
    null)

}
