package tutorials

import akka.actor._

case object PingMsg

case object PongMsg

case object StopMsg

case object StartMsg

class Ping(maxNumber: Int, pong: ActorRef) extends Actor {
  var counter = maxNumber
  def receive = {
    case StartMsg =>
      counter -= 1; println("ping")
      pong ! PingMsg
    case PongMsg =>
      counter -= 1; println("ping")
      if (counter < 0) {
        sender ! StopMsg
        println("ping stopped")
        context.stop(self)
      } else {
        sender ! PingMsg
      }
  }
}

class Pong extends Actor {
  def receive = {
    case PingMsg =>
      println("pong")
      sender ! PongMsg
    case StopMsg =>
      println("pong stopped")
      context.stop(self)
  }
}

object PingPong extends App {
  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  val ping = system.actorOf(Props(new Ping(5, pong)), name = "ping")
  ping ! StartMsg
}