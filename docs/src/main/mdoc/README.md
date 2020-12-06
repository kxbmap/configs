configs
=======

[![Build Status](https://travis-ci.org/kxbmap/configs.svg?branch=master)](https://travis-ci.org/kxbmap/configs)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.kxbmap/configs_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.kxbmap/configs_2.11)
[![Scaladoc](http://javadoc-badge.appspot.com/com.github.kxbmap/configs_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.github.kxbmap/configs_2.11)

configs is Scala wrapper for [Typesafe config](https://github.com/typesafehub/config).

Usage
-----

Add the following line to your build file:

```scala
libraryDependencies += "com.github.kxbmap" %% "configs" % "@VERSION@"
```

Quick Start
-----------

```scala mdoc:silent
import com.typesafe.config.ConfigFactory
import configs.ConfigReader
```

Result type of get a value from config is `configs.Result`.
If get successfully, returns `configs.Result.Success`, if not `configs.Result.Failure`:

```scala mdoc:silent
val config = ConfigFactory.parseString("foo = 42")
```
```scala mdoc
val foo = ConfigReader[Int].read(config, "foo")

foo.valueOrElse(0)

val missing = ConfigReader[Int].read(config, "missing")

missing.valueOrElse(0)
```

Import `configs.syntax._` provides extension methods for `Config`:

```scala mdoc:silent
import configs.syntax._
```
```scala mdoc
config.get[Int]("foo")
```

`get[Option[A]]` will return success with value `None` if path is not exists:

```scala mdoc
config.get[Option[Int]]("missing")

config.getOrElse("missing", 0) // Alias for config.get[Option[Int]]("missing").map(_.getOrElse(0))
```

You can get a case class value out of the box:

```scala mdoc:silent
import scala.concurrent.duration.FiniteDuration

case class MyConfig(foo: String, bar: Int, baz: List[FiniteDuration])
```
```scala mdoc:nest:silent
val config = ConfigFactory.parseString("""
  my-config {
    foo = My config value
    bar = 123456
    baz = [1h, 2m, 3s]
  }
  """)
```
```scala mdoc
config.get[MyConfig]("my-config")
```

If failed, `Result` accumulates error messages:

```scala mdoc:nest:silent
val config = ConfigFactory.parseString("""
  my-config {
    bar = 2147483648
    baz = [aaa, bbb, ccc]
  }
  """)
```
```scala mdoc
val result = config.get[MyConfig]("my-config")

result.failed.foreach { error =>
  error.messages.foreach(println)
}
```

You can get a value without key using `extract`:

```scala mdoc:nest:silent
val config = ConfigFactory.parseString("""
  foo = My config value
  bar = 123456
  baz = [1h, 2m, 3s]
  """)
```
```scala mdoc
config.extract[MyConfig]
```

You may use the `~` operator to combine multiple results and apply a function with the results passed as arguments, this is useful when you want to construct a complex case class from several config extractors.

```scala mdoc:nest:silent
case class ServiceConfig(name: String, port: Int, hosts: List[String])

val config = ConfigFactory.parseString(
  """
    |name = "foo"
    |port = 9876
    |hosts = ["localhost", "foo.com"]
  """.stripMargin)
```
```scala mdoc
(
  config.get[String]("name") ~
  config.get[Int]("port") ~
  config.get[List[String]]("hosts")
)(ServiceConfig) // Alternatively (name, port, hosts) => ServerConfig(name, port, posts)
```

Supported types
---------------

configs can get many type values from config.
It is provided by type class `ConfigReader`.

There are a number of built-in `ConfigReader` instances:

* Primitive/Wrapper types
  * `Long`, `Int`, `Short`, `Byte`, `Double`, `Float`, `Char`, `Boolean`
  * `java.lang.`{`Long`, `Integer`, `Short`, `Byte`, `Double`, `Float`, `Character`, `Boolean`}
* Big number types
  * `BigInt`, `BigDecimal`
  * `java.math.`{`BigInteger`, `BigDecimal`}
* String representation types
  * `String`
  * `Symbol`, `java.util.`{`UUID`, `Locale`}
  * `java.io.File`, `java.nio.file.Path`
  * `java.net.`{`URI`, `InetAddress`}
* Duration types
  * `java.time.Duration`
  * `scala.concurrent.duration.`{`Duration`, `FiniteDuration`}
* Config types
  * `com.typesafe.config.`{`Config`, `ConfigValue`, `ConfigList`, `ConfigObject`, `ConfigMemorySize`}
  * `configs.Bytes`
* Enum types
  * Java `enum` types
  * Scala `Enumeration` types
* Collection types
  * `F[A]` (using `CanBuildFrom[Nothing, A, F[A]]`, e.g. `List[String]`, `Seq[Int]`)
  * `M[S, A]` (using `CanBuildFrom[Nothing, (S, A), M[S, A]]`, e.g. `Map[String, Int]`, `TreeMap[UUID, Config]`)
  * `java.util.`{`List[A]`, `Map[S, A]`, `Set[A]`, `Collection[A]`}, `java.lang.Iterable[A]`
  * `java.util.Properties`
* Optional types
  * `Option[A]`
  * `java.util.`{`Optional[A]`, `OptionalLong`, `OptionalInt`, `OptionalDouble`}
* case classes
* ADTs (sealed trait + classes/objects). See [ADTs support](#adts-support)
* Java Beans. See [Java Beans support](#java-beans-support)

In this list, `A` means any type that is `ConfigReader` instance. And `S` means any type that is `StringConverter` instance.


### ADTs support

If there is such an ADT:

```scala mdoc:silent
sealed trait Tree
case class Branch(value: Int, left: Tree, right: Tree) extends Tree
case object Leaf extends Tree
```

You can get an ADT value from config:

```scala mdoc:nest:silent
val config = ConfigFactory.parseString("""
  tree = {
    value = 42
    left = Leaf
    right {
      value = 123
      left = Leaf
      right = Leaf
    }
  }
  """)
```

```scala mdoc
config.get[Tree]("tree")
```

```scala mdoc:invisible
Leaf // Avoid unused warning
```

### Java Beans support

If there is Java Beans class like the follows:

```java
package com.example;

@lombok.Data
public class MyBean {
    private int intValue;
    private java.util.List<String> stringList;
    private java.util.Map<java.util.Locale, java.time.Duration> localeToDuration;
}
```

Then you define `ConfigReader` instance using `deriveBean` macro:

```scala mdoc:silent
import com.example.MyBean

implicit val myBeanConfigReader: ConfigReader[MyBean] =
  ConfigReader.deriveBean[MyBean]
```

And then you can get Java Beans value:

```scala mdoc:nest:silent
val config = ConfigFactory.parseString("""
  int-value = 42
  string-list = [foo, bar, baz]
  locale-to-duration {
    ja_JP = 42ms
    en_US = 123s
  }
  """)
```
```scala mdoc
config.extract[MyBean]
```


License
-------

Copyright 2013-2016 Tsukasa Kitachi

Apache License, Version 2.0
