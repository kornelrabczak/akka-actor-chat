package com.thecookiezen.chat

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef}
import com.thecookiezen.chat.Server._

class Client(val username: String, val server: ActorRef) extends Actor {

  server ! Connect(username)

  override def receive: Receive = {
    case SystemMessage(information) => println(information)
    case Send(msg)                  => server ! BroadCast(msg)
    case UserMessage(source, message) =>
      println(s"${LocalDateTime.now()} $source: $message")
  }
}
