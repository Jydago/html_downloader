// build.sc
import mill._, scalalib._

object app extends ScalaModule {
  def scalaVersion = "2.13.3"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.7.1",
    ivy"org.asynchttpclient:async-http-client:2.12.1",
    ivy"org.jsoup:jsoup:1.13.1"
  )

  object test extends Tests {
    override def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.0",
      ivy"org.scalamock::scalamock:5.0.0"
    )
    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }

}