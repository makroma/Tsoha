name := "Tsoha"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

libraryDependencies += "postgresql" % "postgresql" % "9.1-901.jdbc4"


