import org.jetbrains.sbtidea.JbrPlatform

lazy val scala213           = "2.13.10"
lazy val scalaPluginVersion = "2023.2.5"
lazy val pluginVersion      = "2023.2.1" + sys.env.get("PLUGIN_BUILD_NUMBER").fold(".0")(v => s".$v")

ThisBuild / intellijPluginName := "intellij-metals"
ThisBuild / intellijBuild := "232"
ThisBuild / jbrInfo := AutoJbr(explicitPlatform = Some(JbrPlatform.osx_aarch64))

Global / intellijAttachSources := true

addCommandAlias("fmt", "scalafmtAll")
addCommandAlias("check", "scalafmtCheckAll")

(Global / javacOptions) := Seq("--release", "17")

resolvers += "jitpack" at "https://jitpack.io"

lazy val root =
  newProject("intellij-metals", file("."))
    .enablePlugins(SbtIdeaPlugin)
    .settings(
      patchPluginXml := pluginXmlOptions { xml =>
        xml.version = version.value
        xml.changeNotes = sys.env.getOrElse(
          "PLUGIN_CHANGE_NOTES",
          s"""<![CDATA[
        <b>What's new?</b>
        <ul>
        </ul>
        ]]>"""
        )
      }
    )

def newProject(projectName: String, base: File): Project =
  Project(projectName, base).settings(
    name := projectName,
    scalaVersion := scala213,
    version := pluginVersion,
    libraryDependencies ++= Seq(
      "com.github.ballerina-platform" % "lsp4intellij"    % "0.95.1",
      "com.novocode"                  % "junit-interface" % "0.11" % Test
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-s", "-a", "+c", "+q"),
    intellijPlugins := Seq(
      "com.intellij.java".toPlugin,
      s"org.intellij.scala:$scalaPluginVersion".toPlugin
    )
  )
