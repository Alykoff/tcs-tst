package ru.tcs.utils

import scala.concurrent.duration._

object Constants {
  val ACTOR_SYSTEM_NAME = "ClusterSystem"
  val USER_ROOT_PATH = "user"
  val MANAGER_ROLE = "manager"
  val MANAGER_NAME = "man"
  val WORKER_ROLE = "worker"
  val WORKER_NAME = "work"
  val DEFAULT_PORT = "0"
  val DROP_COUNTER_INTERVAL = 1.second
  val DEFAULT_TICK_INTERVAL = 100.millisecond
  val DEFAULT_TIMEOUT = 5.second
 }
