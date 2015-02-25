package ru.tcs.utils

import akka.actor.{ActorContext, Address, RootActorPath}
import com.typesafe.config.ConfigFactory

object Utils {
  def getPort(args: Array[String]) = {
    if (args.isEmpty) Constants.DEFAULT_PORT else args(0)
  }

  def configure(args: Array[String], role: String) = {
    val port = getPort(args)
    ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.parseString(s"akka.cluster.roles=[$role]"))
//      .withFallback(ConfigFactory.parseString("""akka.cluster.seed-nodes=["akka.tcp://ClusterSystem@127.0.0.1:2551"]"""))
      .withFallback(ConfigFactory.load())
  }

  def selectWorker(address: Address)(implicit context: ActorContext) =
    context.actorSelection(RootActorPath(address) / Constants.USER_ROOT_PATH / Constants.WORKER_NAME)
}
