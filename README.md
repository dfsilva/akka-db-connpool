Akka extension for db connection pool
=====================================

[![Build Status](https://travis-ci.org/zgwmike/akka-db-connpool.svg?branch=master)](https://travis-ci.org/zgwmike/akka-db-connpool)

What it is
----------
It's an akka extension to create database connection from a preconfigure pool.
it's built on top of bonecp

How to add it to your sbt project.
-------------
add dependency to build.sbt

```scala
"org.guangwenz" %% "akka-db-connpool" % "1.0.1",
```

then add following to the application.conf

```scala
guangwenz.akka.db {
  jdbc {
    className = "${YOUR_JDBC_DRIVER_CLS}"
    url = "${JDBC_URL}"
    username = "${JDBC_USER}"
    password = "${JDBC_PASS}"
  }
}
akka.library-extensions += "org.guangwenz.akka.db.connpool.DbConnectionPoolExtension"
```

check `reference.conf` for more config options.

Example code to grab a connection
----------------------
```scala
import org.guangwenz.akka.db.connpool.DbConnectionPoolExtension

val conn = DbConnectionPoolExtension(system).getConnection
conn match {
    case Some(connection)=>
        //Use it!
    case None=>
        //Oops, something is wrong, check the logs.
}
```
Example code to print the stats
----------------------
```scala
import org.guangwenz.akka.db.connpool.ConnectionPool.PrintDbStats
import org.guangwenz.akka.db.connpool.DbConnectionPoolExtension

val actor = DbConnectionPoolExtension(system).getConnectionPoolActor.get
actor ! PrintDbStats("test")
```

Reference
---------
[BoneCP](http://www.jolbox.com/ "BoneCP")
