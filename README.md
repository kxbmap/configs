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
libraryDependencies += "com.github.kxbmap" %% "configs" % "0.5.0"
```

Quick Start
-----------

```scala
import com.typesafe.config.ConfigFactory
import configs.ConfigReader
```

Result type of get a value from config is `configs.Result`.
If get successfully, returns `configs.Result.Success`, if not `configs.Result.Failure`:

```scala
val config = ConfigFactory.parseString("foo = 42")
```
```scala
val foo = ConfigReader[Int].read(config, "foo")
// foo: configs.Result[Int] = Success(value = 42)

foo.valueOrElse(0)
// res0: Int = 42

val missing = ConfigReader[Int].read(config, "missing")
// missing: configs.Result[Int] = Failure(
//   error = ConfigError(
//     head = Exceptional(
//       throwable = com.typesafe.config.ConfigException$Missing: String: 1: No configuration setting found for key 'missing',
//       paths = List("missing")
//     ),
//     tail = Vector()
//   )
// )

missing.valueOrElse(0)
// res1: Int = 0
```

Import `configs.syntax._` provides extension methods for `Config`:

```scala
import configs.syntax._
```
```scala
config.get[Int]("foo")
// res2: configs.Result[Int] = Success(value = 42)
```

`get[Option[A]]` will return success with value `None` if path is not exists:

```scala
config.get[Option[Int]]("missing")
// res3: configs.Result[Option[Int]] = Success(value = None)

config.getOrElse("missing", 0) // Alias for config.get[Option[Int]]("missing").map(_.getOrElse(0))
// res4: configs.Result[Int] = Success(value = 0)
```

You can get a case class value out of the box:

```scala
import scala.concurrent.duration.FiniteDuration

case class MyConfig(foo: String, bar: Int, baz: List[FiniteDuration])
```
```scala
val config = ConfigFactory.parseString("""
  my-config {
    foo = My config value
    bar = 123456
    baz = [1h, 2m, 3s]
  }
  """)
```
```scala
config.get[MyConfig]("my-config")
// res5: configs.Result[MyConfig] = Success(
//   value = MyConfig(
//     foo = "My config value",
//     bar = 123456,
//     baz = List(1 hour, 2 minutes, 3 seconds)
//   )
// )
```

If failed, `Result` accumulates error messages:

```scala
val config = ConfigFactory.parseString("""
  my-config {
    bar = 2147483648
    baz = [aaa, bbb, ccc]
  }
  """)
```
```scala
val result = config.get[MyConfig]("my-config")
// result: configs.Result[MyConfig] = Failure(
//   error = ConfigError(
//     head = Exceptional(
//       throwable = com.typesafe.config.ConfigException$Missing: String: 2: No configuration setting found for key 'foo',
//       paths = List("my-config", "foo")
//     ),
//     tail = Vector(
//       Exceptional(
//         throwable = com.typesafe.config.ConfigException$WrongType: String: 2: bar has type out-of-range value 2147483648 rather than int (32-bit integer),
//         paths = List("my-config", "bar")
//       ),
//       Exceptional(
//         throwable = com.typesafe.config.ConfigException$BadValue: String: 4: Invalid value at '0': No number in duration value 'aaa',
//         paths = List("my-config", "baz", "0")
//       ),
//       Exceptional(
//         throwable = com.typesafe.config.ConfigException$BadValue: String: 4: Invalid value at '1': No number in duration value 'bbb',
//         paths = List("my-config", "baz", "1")
//       ),
//       Exceptional(
//         throwable = com.typesafe.config.ConfigException$BadValue: String: 4: Invalid value at '2': No number in duration value 'ccc',
//         paths = List("my-config", "baz", "2")
//       )
//     )
//   )
// )

result.failed.foreach { error =>
  error.messages.foreach(println)
}
// [my-config.foo] String: 2: No configuration setting found for key 'foo'
// [my-config.bar] String: 2: bar has type out-of-range value 2147483648 rather than int (32-bit integer)
// [my-config.baz.0] String: 4: Invalid value at '0': No number in duration value 'aaa'
// [my-config.baz.1] String: 4: Invalid value at '1': No number in duration value 'bbb'
// [my-config.baz.2] String: 4: Invalid value at '2': No number in duration value 'ccc'
```

You can get a value without key using `extract`:

```scala
val config = ConfigFactory.parseString("""
  foo = My config value
  bar = 123456
  baz = [1h, 2m, 3s]
  """)
```
```scala
config.extract[MyConfig]
// res7: configs.Result[MyConfig] = Success(
//   value = MyConfig(
//     foo = "My config value",
//     bar = 123456,
//     baz = List(1 hour, 2 minutes, 3 seconds)
//   )
// )
```

You may use the `~` operator to combine multiple results and apply a function with the results passed as arguments, this is useful when you want to construct a complex case class from several config extractors.

```scala
case class ServiceConfig(name: String, port: Int, hosts: List[String])

val config = ConfigFactory.parseString(
  """
    |name = "foo"
    |port = 9876
    |hosts = ["localhost", "foo.com"]
  """.stripMargin)
```
```scala
(
  config.get[String]("name") ~
  config.get[Int]("port") ~
  config.get[List[String]]("hosts")
)(ServiceConfig) // Alternatively (name, port, hosts) => ServerConfig(name, port, posts)
// res8: configs.Result[ServiceConfig] = Success(
//   value = ServiceConfig(
//     name = "foo",
//     port = 9876,
//     hosts = List("localhost", "foo.com")
//   )
// )
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

```scala
sealed trait Tree
case class Branch(value: Int, left: Tree, right: Tree) extends Tree
case object Leaf extends Tree
```

You can get an ADT value from config:

```scala
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

```scala
config.get[Tree]("tree")
// res9: configs.Result[Tree] = Success(
//   value = Branch(
//     value = 42,
//     left = Leaf,
//     right = Branch(value = 123, left = Leaf, right = Leaf)
//   )
// )
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

```scala
import com.example.MyBean

implicit val myBeanConfigReader: ConfigReader[MyBean] =
  ConfigReader.deriveBean[MyBean]
```

And then you can get Java Beans value:

```scala
val config = ConfigFactory.parseString("""
  int-value = 42
  string-list = [foo, bar, baz]
  locale-to-duration {
    ja_JP = 42ms
    en_US = 123s
  }
  """)
```
```scala
config.extract[MyBean]
// res11: configs.Result[MyBean] = Success(
//   value = MyBean(intValue=42, stringList=[foo, bar, baz], localeToDuration={en_US=PT2M3S, ja_JP=PT0.042S})
// )
```


License
-------

Copyright 2013-2016 Tsukasa Kitachi

Apache License, Version 2.0
