configs
=======

A Scala wrapper for Typesafe config


Usage
-----
```
// sample.conf
db.default.driver="org.h2.Driver"
db.default.url="jdbc:h2:mem:default"
db.default.user="kxbmap"
db.default.password="secret"

db.secondary.driver="org.h2.Driver"
db.secondary.url="jdbc:h2:mem:secondary"

db.break.driver="org.h2.Driver"
db.break.url="jdbc:h2:mem:break"
db.break.user=[kxbmap]
```
```scala
import com.github.kxbmap.configs._
import com.typesafe.config.ConfigFactory
import scala.util.Try

case class DBConfig(driver: String,
                    url: String,
                    user: Option[String],
                    password: Option[String])

implicit val DBConfigs: Configs[DBConfig] = Configs { c =>
  DBConfig(
    c.get[String]("driver"),
    c.get[String]("url"),
    c.orMissing[String]("user"),
    c.orMissing[String]("password")
  )
}

val config = ConfigFactory.load("sample")

val default = config.get[DBConfig]("db.default")
// default: DBConfig = DBConfig(org.h2.Driver,jdbc:h2:mem:default,Some(kxbmap),Some(secret))

val break = config.opt[DBConfig]("db.break")
// break: Option[DBConfig] = None
// Note: Equivalent to `config.get[Option[DBConfig]]("db.break")`

val dbs = config.get[Map[Symbol, Try[DBConfig]]]("db")
// dbs: Map[Symbol,scala.util.Try[DBConfig]] = Map(
//   'default -> Success(DBConfig(org.h2.Driver,jdbc:h2:mem:default,Some(kxbmap),Some(secret))),
//   'secondary -> Success(DBConfig(org.h2.Driver,jdbc:h2:mem:secondary,None,None)),
//   'break -> Failure(com.typesafe.config.ConfigException$WrongType: sample.conf: 11: user has type LIST rather than STRING)
// )
```
