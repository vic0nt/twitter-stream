package actors

/**
  * Created by vicont on 23.09.2016.
  */
import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.iteratee.{Concurrent, Enumeratee, Enumerator}
import play.api.{Logger, Play}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WS
import play.extras.iteratees.{Encoding, JsonIteratees}

class TwitterStreamer(out: ActorRef) extends Actor {
  def receive = {
    case "subscribe" =>
      Logger.info("Received subscription from a client")
      out ! Json.obj("text" -> "Hello, world!")
  }
}

object TwitterStreamer {

  def props(out: ActorRef) = Props(new TwitterStreamer(out))

  private var broadcastEnumerator: Option[Enumerator[JsObject]] = None

  def connect(): Unit = {
    credentials.map { case (consumerKey, requestToken) =>
      val (iteratee, enumerator) = Concurrent.joined[Array[Byte]]
      val jsonStream: Enumerator[JsObject] = enumerator &>
        Encoding.decode() &>
        Enumeratee.grouped(JsonIteratees.jsSimpleObject)
      val (be, _) = Concurrent.broadcast(jsonStream)
      broadcastEnumerator = Some(be)
      val url = "https://stream.twitter.com/1.1/statuses/filter.json"
      WS
        .url(url)
        .withRequestTimeout(-1)
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "manutd")
        .get { response =>
          Logger.info("Status: " + response.status)
          iteratee
        }.map { _ =>
        Logger.info("Twitter stream closed")
      }
    } getOrElse {
      Logger.error("Twitter credentials missing")
    }
  }

  def credentials: Option[(ConsumerKey, RequestToken)] = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.token")
    tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
  } yield (
    ConsumerKey(apiKey, apiSecret),
    RequestToken(token, tokenSecret)
    )
}