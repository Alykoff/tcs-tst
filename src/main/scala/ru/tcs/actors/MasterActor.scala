package ru.tcs.actors

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import ru.tcs.utils.Messages._
import ru.tcs.utils.Messages.MasterCommandMessages._
import ru.tcs.utils.Messages.MasterWorkerMessages._
import ru.tcs.utils.Utils._
import ru.tcs.utils.Constants._

import scala.concurrent.duration._
import scala.language.postfixOps

class MasterActor extends Actor with ActorLogging {
  import context._

  var members = Vector.empty[AnyRef]
  val cluster = Cluster(context.system)
  val selfAddress = cluster.selfAddress.copy()

  cluster.join(selfAddress)

  override def preStart(): Unit = cluster.subscribe(self,
    classOf[MemberUp],
    classOf[MemberRemoved],
    classOf[MemberEvent],
    classOf[UnreachableMember]
  )

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = receive(Vector.empty, DEFAULT_TICK_INTERVAL)

  def receive(addresses: Vector[Address], interval: FiniteDuration): Receive = {
    // === cluster events messages ===
    case state: CurrentClusterState =>
      log.info("Get CurrentClusterState: {}", state.members)
      val initMembers = state.members.toVector.map(_.address).filter(_ != selfAddress)
      if (initMembers.isEmpty) become(receive(initMembers, interval))

    case MemberUp(member)
      if member.hasRole(WORKER_ROLE) =>
        log.info("Member is Up: {}", member.address)
        val newAddresses = addresses :+ member.address
        selectWorker(member.address) ! ChangeNodesInterval(interval)
        become(receive(newAddresses, interval))

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      val newAddresses = addresses.filterNot(_ == member.address)
      become(receive(newAddresses, interval))

    // ==== commands messages ===
    case RemoveNode(address) =>
      addresses.find(_ == address).foreach{ case adr =>
        cluster.leave(adr)
        log.info(s"Removing member: $address")
        selectWorker(address) ! Stop
      }

    case AddNode(address) =>
      addresses.find(_ == address).getOrElse{
        val newAddresses = addresses :+ address
        selectWorker(address) ! Join(selfAddress)
//        cluster.join(address)
        become(receive(newAddresses, interval))
      }

    case ChangeNodesInterval(newInterval) =>
      log.info(s"New interval: $newInterval")
      addresses.foreach { case address =>
        selectWorker(address) ! ChangeInterval(newInterval)
      }
      become(receive(addresses, newInterval))

  }
}
