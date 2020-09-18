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
      ivy"com.lihaoyi::utest:0.7.2",
      ivy"org.mockito::mockito-scala:1.15.0"

    )
    def testFrameworks = Seq("utest.runner.Framework")
  }

}