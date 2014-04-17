package com.typesafe.sbt.dart

import sbt._
import java.io._

trait DartSdk {

  def dart2jsExePath = DartSdk.dart2jsExe.getAbsolutePath()

  def pubExePath = DartSdk.pubExe.getAbsolutePath()

  def dartExePath = DartSdk.dartExe.getAbsolutePath()
}

object DartSdk {
  lazy val dartSdk: File = {

    val DART_SDK = System.getenv("DART_SDK")
    if (DART_SDK == null) {
      sys.error("DART_SDK env variable must be defined!")
    } else {
      val dartHome = new File(DART_SDK)
      if (dartHome.exists())
        dartHome
      else
        sys.error(dartHome + " does not exist!")
    }
  }

  lazy val pubExe: File = {
    val path = dartSdk + "/bin/pub"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

  lazy val dart2jsExe: File = {
    val path = dartSdk + "/bin/dart2js"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

  lazy val dartExe: File = {
    val path = dartSdk + "/bin/dart"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

}

trait DartProcessor extends DartSdk {

  def runCommand(in: File, cmd: String, inputFile: File) {
    import scala.sys.process._
    val d2js = Process(cmd, in)

    var stdout = List[String]()
    var stderr = List[String]()
    val exit = d2js ! ProcessLogger((s) => stdout ::= s, (s) => stderr ::= s)

    if (exit != 0) {
      //throw CompilationException(stdout.mkString("\n") + stderr.mkString("\n"), inputFile, None)
      throw new RuntimeException(stdout.mkString("\n") + stderr.mkString("\n"))
    }
  }
  
  def runCommand(cmd: String) {
    import scala.sys.process._
    val d2js = Process(cmd)

    println("Run ")
    
    var stdout = List[String]()
    var stderr = List[String]()
    val exit = d2js ! ProcessLogger((s) => stdout ::= s, (s) => stderr ::= s)

    if (exit != 0) {
      //throw CompilationException(stdout.mkString("\n") + stderr.mkString("\n"), inputFile, None)
      throw new RuntimeException(stdout.mkString("\n") + stderr.mkString("\n"))
    }
  }

  
}
