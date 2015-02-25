package ru.tcs.apps

import akka.actor.{ActorSystem, Props}
import ru.tcs.actors.WorkerActor
import ru.tcs.utils.Utils._
import ru.tcs.utils.Constants._

import scala.language.postfixOps

object WorkerApp extends App {
  val config = configure(args, WORKER_ROLE)
  val system = ActorSystem(ACTOR_SYSTEM_NAME, config)
  system.actorOf(Props[WorkerActor], name = WORKER_NAME)
}

