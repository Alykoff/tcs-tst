package ru.tcs.actors

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.util.Timeout
import ru.tcs.utils.Messages._
import ru.tcs.utils.Utils._
import ru.tcs.utils.Constants._

import scala.concurrent.duration._
import scala.language.postfixOps

class WorkerActor extends Actor with ActorLogging {
  import context._

  var count = 0 // may be become
  val uuid = java.util.UUID.randomUUID.toString
  val cluster = Cluster(system)
  val selfAddress = cluster.selfAddress.copy()
  val SCHEDULER_TICK_TIMEOUT = DEFAULT_TIMEOUT
  val SCHEDULER_DROP_COUNTER_TIMEOUT = DEFAULT_TIMEOUT

  cluster.subscribe(self,
    classOf[MemberUp],
    classOf[MemberRemoved],
    classOf[MemberEvent],
    classOf[UnreachableMember]
  )
  schedulerDropCounter()

  def schedulerTick(interval: FiniteDuration = DEFAULT_TICK_INTERVAL) =
    system.scheduler.schedule(interval, interval) {
      implicit val timeout = Timeout(SCHEDULER_TICK_TIMEOUT)
      self ! InnerWorkerMessages.Tick
    }

  def schedulerDropCounter() =
    system.scheduler.schedule(DROP_COUNTER_INTERVAL, DROP_COUNTER_INTERVAL) {
      implicit val timeout = Timeout(SCHEDULER_DROP_COUNTER_TIMEOUT)
      self ! InnerWorkerMessages.DropCounter
    }

  def dropCounter() = count = 0

  def incCounter() = count = count + 1

  def createMsg = uuid + System.currentTimeMillis

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive = receive(Nil, schedulerTick(), cluster)

  def receive(addresses: List[Address], scheduler: Cancellable, cluster: Cluster): Receive = {
    // === cluster events messages ===
    case state: CurrentClusterState =>
      log.info("receive CurrentClusterState: {}", state.members)
      val allAddresses = state.members.map(_.address)
      val addressesWithoutMe = allAddresses.filter(_ != selfAddress).toList
      become(
        receive(addressesWithoutMe, scheduler, cluster)
      )

    case MemberUp(member)
      if (member.address != selfAddress) && member.roles.contains(WORKER_ROLE) =>
        val newAddresses = addresses :+ member.address
        log.info("MemberUp, {}", member.address)
        become(
          receive(newAddresses, scheduler, cluster)
        )

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      val newAddresses = addresses.filterNot(_ == member.address)
      become(
        receive(newAddresses, scheduler, cluster)
      )

    // ==== master commands messages ===
    case MasterWorkerMessages.Join(address) =>
      cluster.joinSeedNodes(Vector(address))

    case MasterWorkerMessages.ChangeInterval(interval) =>
      log.info("Change interval: {}", interval)
      scheduler.cancel()
      val newScheduler = schedulerTick(interval)
      become(
        receive(addresses, newScheduler, cluster)
      )

    case MasterWorkerMessages.Stop =>
      log.info("Current actor is stopped! Buy-buy!")
      scheduler.cancel()
      stop(self)

    // ==== messages from other workers ===
    case WorkerMessages.Message(msg) =>
      incCounter()
      log.info("counter: {}", count)

    // ==== inner messages ===
    case InnerWorkerMessages.Tick =>
      val msg = createMsg
//      context.actorSelection("../work") ! WorkerMessages.Message(msg)
      addresses foreach { address =>
        selectWorker(address) ! WorkerMessages.Message(msg)
      }

    case InnerWorkerMessages.DropCounter => {
      dropCounter()
    }
  }
}
