package ru.tcs.utils

import akka.actor.Address

import scala.concurrent.duration.FiniteDuration

object Messages {
  object WorkerMessages {
    final case class Message(msg: String)
  }

  object MasterWorkerMessages {
    final case class Join(seedAddress: Address)
    final case class ChangeInterval(interval: FiniteDuration)
    case object Stop
  }

  object MasterCommandMessages {
    final case class ChangeNodesInterval(interval: FiniteDuration)
    final case class RemoveNode(address: Address)
    final case class AddNode(address: Address)
  }
  
  object InnerWorkerMessages {
    case object DropCounter
    case object Tick
  }
}
