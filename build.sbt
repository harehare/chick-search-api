name := "test"

version := "0.1"

scalaVersion := "2.12.6"

val http4sVersion = "0.19.0-M1"
val elastic4sVersion = "6.3.6"
val circeVersion = "0.9.1"

lazy val commonSettings = Def.settings(
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-language:higherKinds",
    "-unchecked",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint:adapted-args",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",
    "-Xlint:delayedinit-select",
    "-Xlint:doc-detached",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload",
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:implicits",
    "-Ywarn-unused:imports",
    "-Ywarn-unused:locals",
    "-Ywarn-unused:params",
    "-Ywarn-unused:patvars",
    "-Ywarn-unused:privates",
    "-Ywarn-value-discard",
  ),
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
  scalafmtVersion := "1.4.0",
  scalafmtShowDiff in scalafmt := true,
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(core, algolia, elasticsearch, solr, mysql)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"            % "1.2.0",
      "io.circe"                   %% "circe-core"           % circeVersion,
      "io.circe"                   %% "circe-generic"        % circeVersion,
      "io.circe"                   %% "circe-generic-extras" % circeVersion,
      "io.circe"                   %% "circe-java8"          % circeVersion,
      "io.circe"                   %% "circe-parser"         % circeVersion,
      "org.typelevel"              %% "cats-effect"          % "1.0.0-RC2",
      "org.typelevel"              %% "mouse"                % "0.17",
      "org.http4s"                 %% "http4s-blaze-client"  % http4sVersion,
      "org.http4s"                 %% "http4s-blaze-server"  % http4sVersion,
      "org.http4s"                 %% "http4s-circe"         % http4sVersion,
      "org.http4s"                 %% "http4s-dsl"           % http4sVersion,
      "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.0",
      "ch.qos.logback"             %  "logback-classic"      % "1.2.3",
    ),
  )

lazy val algolia = project
  .in(file("algolia"))
  .settings(commonSettings)
  .dependsOn(core % "compile->compile; test->test")
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(
    name := "chick-algolia-api",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.algolia"    %% "algoliasearch-scala"  % "[1,)",
      "ch.qos.logback" %  "logback-classic"      % "1.2.3",
      "org.json4s"     %% "json4s-jackson"       % "3.6.0",
    ),
    dockerBaseImage := "java:8-jdk-alpine",
  )

lazy val elasticsearch = project
  .in(file("elasticsearch"))
  .settings(commonSettings)
  .dependsOn(core % "compile->compile; test->test")
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(
    name := "chick-elasticsearch-api",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-circe" % elastic4sVersion,
    ),
    dockerBaseImage := "java:8-jdk-alpine",
  )

lazy val solr = project
  .in(file("solr"))
  .settings(commonSettings)
  .dependsOn(core % "compile->compile; test->test")
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(
    name := "chick-solr-api",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "com.github.takezoe" %% "solr-scala-client" % "0.0.19"
    ),
    dockerBaseImage := "java:8-jdk-alpine",
  )

lazy val mysql = project
  .in(file("mysql"))
  .settings(commonSettings)
  .dependsOn(core % "compile->compile; test->test")
  .enablePlugins(JavaAppPackaging, AshScriptPlugin, DockerPlugin)
  .settings(
    name := "chick-mysql-api",
    version := "1.0.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "0.5.3",
      "mysql" %  "mysql-connector-java" % "5.1.45"
    ),
    dockerBaseImage := "java:8-jdk-alpine",
  )
