package com.thecookiezen.chat

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.thecookiezen.chat.Server._

class Server extends Actor {

  override def receive: Receive = open(List[(String, ActorRef)]())

  def open(clients: List[(String, ActorRef)]): Receive = {
    case Connect(username) => {
      sendMessageToUsers(
        SystemMessage(
          s"${LocalDateTime.now()}: User $username joined the channel."),
        clients)
      context.become(open((username, sender) :: clients))
    }
    case BroadCast(msg) => {
      val username = clients.filter(_._2 == sender).head._1
      sendMessageToUsers(UserMessage(username, msg), clients)
    }
    case Disconnect() => {
      val user = clients.filter(_._2 == sender).head
      sendMessageToUsers(
        SystemMessage(
          s"${LocalDateTime.now()}: User ${user._1} disconnected from channel."),
        clients)
      user._2 ! PoisonPill
    }
    case Close() => {
      sendMessageToUsers(
        SystemMessage(s"${LocalDateTime.now()}: Channel is now closed."),
        clients)
      context.become(closed)
    }
  }

  def closed: Receive = {
    case Open() => {
      context.become(open(List[(String, ActorRef)]()))
    }
    case _ => sender() ! SystemMessage("Channel is closed.")
  }

  private def sendMessageToUsers(message: Message,
                                 clients: List[(String, ActorRef)]) {
    clients.foreach(_._2 ! message)
  }
}

object Server {

  case class Connect(username: String)

  case class Disconnect()

  case class BroadCast(message: String)

  case class Send(message: String)

  case class Open()

  case class Close()

  sealed abstract class Message()

  case class SystemMessage(content: String) extends Message

  case class UserMessage(username: String, content: String) extends Message

}
