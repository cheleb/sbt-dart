package com.typesafe.sbt.dart

import sbt._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import sbt.Keys._

object Import {

  object DartKeys {
    val dartbuild = TaskKey[Pipeline.Stage]("dart-build", "Build dart applications.")
  }

}

object SbtDart extends AutoPlugin with DartProcessor{

  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport.DartKeys._

  override def projectSettings: Seq[Setting[_]] = Seq(
    dartbuild := dartbuiltFiles.value,
    pipelineStages <+= dartbuild
  )

  def dartbuiltFiles: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
      mappings =>
      val targetDir = webTarget.value / dartbuild.key.label
      val build = sourceDirectory.value / "build"
      runCommand(pubExePath + " build")
      
      val watch = (base: File) => base  ** "*"
      
      watch(build).get.map(f=>(f, f.relativeTo(build).toString()))
      
  }
}
