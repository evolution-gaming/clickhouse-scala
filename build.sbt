name := "clickhouse-scala-core"

version := "0.0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.testcontainers" % "testcontainers" % "1.4.2" % "test",
  "ru.yandex.clickhouse" % "clickhouse-jdbc" % "0.1.34" % "test"
)