package org.guangwenz.akka.db.connpool

import java.sql.Connection

import akka.actor.{Actor, ActorLogging}
import com.jolbox.bonecp.{BoneCP, BoneCPConfig}

/**
  * Created by shrek.zhou on 9/8/16.
  */

object ConnectionPool {

  sealed trait DbCmd

  case object ShutdownConnectionPool extends DbCmd

  case class GetDbConnection(reqId: String) extends DbCmd

  case class PrintDbStats(reqId: String) extends DbCmd

  case class DbConnectionRetrieved(reqId: String, conn: Connection)

  case class GetDbConnectionException(reqId: String, reason: String) extends Exception

}

class ConnectionPool(className: Option[String] = None, url: Option[String] = None, username: Option[String] = None, password: Option[String] = None) extends Actor with ActorLogging {

  import ConnectionPool._

  var connectionPool: Either[String, BoneCP] = Left("Not initialized yet")

  override def postStop(): Unit = {
    super.postStop()

    connectionPool match {
      case Left(excep) =>
        log.warning("connection Pool is not created, last reason is {}", excep)
        connectionPool = Left("Not initialized")
      case Right(boneCP) =>
        log.info("Shutting down BoneCP pool")
        boneCP.shutdown()
        connectionPool = Left("Not initialized")
    }
  }

  override def preStart(): Unit = {
    super.preStart()
    try {
      Class.forName(className.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.className")))

      val config = new BoneCPConfig()
      config.setJdbcUrl(url.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.url")))
      config.setUsername(username.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.username")))
      config.setPassword(password.getOrElse(context.system.settings.config.getString("guangwenz.akka.db.jdbc.password")))
      config.setMinConnectionsPerPartition(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.min-connections-per-partition"))
      config.setMaxConnectionsPerPartition(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.max-connections-per-partition"))
      config.setPartitionCount(context.system.settings.config.getInt("guangwenz.akka.db.jdbc.partition-count"))
      connectionPool = Right(new BoneCP(config))
    } catch {
      case ex: Exception =>
        log.error("ERROR to initialize boneCP with error {}", ex.getMessage, ex)

    }
  }

  def receive: PartialFunction[Any, Unit] = {
    case GetDbConnection(reqId) =>
      connectionPool match {
        case Right(boneCP) =>
          sender ! DbConnectionRetrieved(reqId, boneCP.getConnection)
        case Left(reason) =>
          sender ! GetDbConnectionException(reqId, reason)
      }

    case PrintDbStats(reqId) =>
      connectionPool match {
        case Right(boneCP) =>
          log.info(s"CacheHitRatio:${boneCP.getStatistics.getCacheHitRatio}")
          log.info(s"CacheHits:${boneCP.getStatistics.getCacheHits}")
          log.info(s"CacheMiss:${boneCP.getStatistics.getCacheMiss}")
          log.info(s"ConnectionsRequested:${boneCP.getStatistics.getConnectionsRequested}")
          log.info(s"ConnectionWaitTimeAvg:${boneCP.getStatistics.getConnectionWaitTimeAvg}")
          log.info(s"CumulativeConnectionWaitTime:${boneCP.getStatistics.getCumulativeConnectionWaitTime}")
          log.info(s"CumulativeStatementExecutionTime:${boneCP.getStatistics.getCumulativeStatementExecutionTime}")
          log.info(s"StatementExecuteTimeAvg:${boneCP.getStatistics.getStatementExecuteTimeAvg}")
          log.info(s"StatementPrepareTimeAvg:${boneCP.getStatistics.getStatementPrepareTimeAvg}")
          log.info(s"StatementsCached:${boneCP.getStatistics.getStatementsCached}")
          log.info(s"StatementsExecuted:${boneCP.getStatistics.getStatementsExecuted}")
          log.info(s"StatementsPrepared:${boneCP.getStatistics.getStatementsPrepared}")
          log.info(s"TotalCreatedConnections:${boneCP.getStatistics.getTotalCreatedConnections}")
          log.info(s"TotalFree:${boneCP.getStatistics.getTotalFree}")
          log.info(s"TotalLeased:${boneCP.getStatistics.getTotalLeased}")
          sender ! "Done"
        case Left(reason) =>
          log.warning("No connection pool created")
          sender ! reason
      }
    case ShutdownConnectionPool =>
      connectionPool match {
        case Right(boneCP) =>
          log.info("Shutting down connection pool")
          boneCP.shutdown()
        case Left(reason) =>
          log.info("connection pool is not created, last error {}", reason)
      }
  }
}
