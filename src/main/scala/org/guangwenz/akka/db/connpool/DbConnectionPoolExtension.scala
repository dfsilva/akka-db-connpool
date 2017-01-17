package org.guangwenz.akka.db.connpool

import java.sql.Connection
import java.util.UUID

import akka.actor.{ActorRef, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.guangwenz.akka.db.connpool.ConnectionPool.{DbConnectionRetrieved, GetDbConnection, ShutdownConnectionPool}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by shrek.zhou on 9/8/16.
  */
object DbConnectionPoolExtension extends ExtensionId[DbConnectionPoolExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): DbConnectionPoolExtension = new DbConnectionPoolExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = DbConnectionPoolExtension
}

class DbConnectionPoolExtension(system: ExtendedActorSystem) extends Extension {

  private val connectionPool = system.actorOf(Props(new ConnectionPool()).withDispatcher("db-access-dispatcher"), "guangwenz-util-db")
  private implicit val timeout = Timeout(1.minutes)
  private val requests = Map()

  system.registerOnTermination {
    connectionPool ! ShutdownConnectionPool
  }

  def getConnection: Option[Connection] = {
    val future = connectionPool.ask(GetDbConnection(UUID.randomUUID().toString)).mapTo[DbConnectionRetrieved]
    try {
      Some(Await.result(future, 30.seconds).conn)
    } catch {
      case ex: Exception => None
    }
  }

  def connect(handler: (Either[String, Connection]) => Unit): Unit = {
    implicit val executor = system.dispatcher
    val future = connectionPool.ask(GetDbConnection(UUID.randomUUID().toString)).mapTo[DbConnectionRetrieved]
    try {
      handler(Right(Await.result(future, 30.seconds).conn))
    } catch {
      case exception: Exception =>
        handler(Left(exception.getMessage))
    }
  }

  def getConnectionPoolActor: Option[ActorRef] = {
    Some(connectionPool)
  }
}
