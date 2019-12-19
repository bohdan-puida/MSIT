package wumpus

import Game._
import akka.actor._

import scala.util.Random

object Game {

  sealed trait Room

  case object Empty extends Room

  case object WumpusInside extends Room

  case object BottomLessPit extends Room

  case object ArrowInside extends Room

  sealed trait GameState

  case object GameOver extends GameState

  case class GameRunning(arrows: Int, room: Room) extends GameState {
    println("Arrows: " + arrows)
    if (room == Empty) {
      println("Room is empty, game is going")
    }
    else if (room == WumpusInside) {
      println("Wumpus inside room, game over :(")
    }
    else if (room == BottomLessPit) {
      println("You've got in Pit, game over :(")
    }
    else if (room == ArrowInside) {
      println("You are hit, game is going")
    }
  }


  def checkRoom(game: GameRunning): GameState = game.room match {
    case Empty => game
    case WumpusInside => GameOver
    case BottomLessPit => GameOver
    case ArrowInside => game.copy(arrows = game.arrows + 1, room = Empty)
    case _ => ???
  }
}

class WumpusGame extends Actor {
  val rooms = Array(Empty, WumpusInside, BottomLessPit, ArrowInside)

  def state(room: Room, arrows: Int = 5): GameRunning = GameRunning(arrows, room)

  def shuffleMe {
    for (x <- 0 until 4) {
      val room = Random.shuffle(rooms.toList).head
      checkRoom(state(room))
    }
  }

  override def receive: Receive = {
    case _ => shuffleMe
  }
}

object StartGame extends App {
  val system = ActorSystem("WumpusActor")
  val wumpusActor = system.actorOf(Props[WumpusGame], name = "WumpusActor")
  wumpusActor ! Empty
  wumpusActor ! WumpusInside
  wumpusActor ! BottomLessPit
  wumpusActor ! ArrowInside
}
