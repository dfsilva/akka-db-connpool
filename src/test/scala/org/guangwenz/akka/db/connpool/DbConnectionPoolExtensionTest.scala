package org.guangwenz.akka.db.connpool

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, TestKit}
import org.guangwenz.akka.db.connpool.ConnectionPool.PrintDbStats
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

/**
  * Created by shrek.zhou on 10/17/16.
  */
class DbConnectionPoolExtensionTest extends TestKit(ActorSystem("akkaconntest")) with FlatSpecLike with Matchers with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Akka Connection Pool Extension " should "return a connection when asked " in {
    val conn = DbConnectionPoolExtension(system).getConnection
    conn.isDefined shouldBe true
  }
  it should "print db stat" in {
    val actor = DbConnectionPoolExtension(system).getConnectionPoolActor.get
    EventFilter.info(pattern = "TotalLeased:.*", occurrences = 1).intercept {
      actor ! PrintDbStats("test")
    }
  }
  it should "return connection" in {
    DbConnectionPoolExtension(system).connect {
      case Right(connection) =>
        connection.isValid(20) shouldBe true
      case Left(reason) =>
        fail(reason)
    }
  }
}
