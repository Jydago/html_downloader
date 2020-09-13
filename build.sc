// build.sc
import mill._, scalalib._

object wget extends ScalaModule {
  def scalaVersion = "2.13.3"

  override def ivyDeps = Agg(
    ivy"com.lihaoyi::os-lib:0.7.1",
    ivy"org.asynchttpclient:async-http-client:2.12.1",
    ivy"org.jsoup:jsoup:1.13.1"
  )

}