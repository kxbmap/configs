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
libraryDependencies += "com.github.kxbmap" %% "configs" % "0.4.3"
```

configs version 0.4+ only support Java 8. If you need to Java 7, please check [0.3.x](https://github.com/kxbmap/configs/tree/v0.3.x-java7).

Quick Start
-----------

```tut:silent
import com.typesafe.config.ConfigFactory
import configs.Configs
```

Result type of get a value from config is `configs.Result`.
If get successfully, returns `configs.Result.Success`, if not `configs.Result.Failure`:

```tut:silent
val config = ConfigFactory.parseString("foo = 42")
```
```tut
val result = Configs[Int].get(config, "foo")

result.valueOrElse(0)

val result = Configs[Int].get(config, "missing")

result.valueOrElse(0)
```

Import `configs.syntax._` provides extension methods for `Config`:

```tut:silent
import configs.syntax._
```
```tut
config.get[Int]("foo")
```

`get[Option[A]]` will return success with value `None` if path is not exists:

```tut
config.get[Option[Int]]("missing")

config.getOrElse("missing", 0) // Alias for config.get[Option[Int]]("missing").map(_.getOrElse(0))
```

You can get a case class value out of the box:

```tut:silent
import scala.concurrent.duration.FiniteDuration

case class MyConfig(foo: String, bar: Int, baz: List[FiniteDuration])
```
```tut:silent
val config = ConfigFactory.parseString("""
  my-config {
    foo = My config value
    bar = 123456
    baz = [1h, 2m, 3s]
  }
  """)
```
```tut
config.get[MyConfig]("my-config")
```

If failed, `Result` accumulates error messages:

```tut:silent
val config = ConfigFactory.parseString("""
  my-config {
    bar = 2147483648
    baz = [aaa, bbb, ccc]
  }
  """)
```
```tut
val result = config.get[MyConfig]("my-config")

result.valueOr { error =>
  error.messages.foreach(println)
  MyConfig("", 0, Nil)
}
```

You can get a value without key using `extract`:

```tut:silent
val config = ConfigFactory.parseString("""
  foo = My config value
  bar = 123456
  baz = [1h, 2m, 3s]
  """)
```
```tut
config.extract[MyConfig]
```

Supported types
---------------

configs can get many type values from config.
It is provided by type class `Configs`.

There are a number of built-in `Configs` instances:

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
* classes that have public constructors
* ADTs (sealed trait + classes/objects). See [ADTs support](#adts-support)
* Java Beans. See [Java Beans support](#java-beans-support)

In this list, `A` means any type that is `Configs` instance. And `S` means any type that is `FromString` instance.


### ADTs support

If there is such an ADT:

```tut:silent
sealed trait Tree
case class Branch(value: Int, left: Tree, right: Tree) extends Tree
case object Leaf extends Tree
```

You can get an ADT value from config:

```tut:silent
val config = ConfigFactory.parseString("""
  tree = {
    type = Branch
    value = 42
    left = Leaf
    right {
      type = Branch
      value = 123
      left = Leaf
      right = { type = Leaf }
    }
  }
  """)
```

```tut
config.get[Tree]("tree")
```


### Java Beans support

If there is Java Beans class like the follows:

```java
@lombok.Data
public class MyBean {
    private int intValue;
    private java.util.List<String> stringList;
    private java.util.Map<java.util.Locale, java.time.Duration> localeToDuration;
}
```

Then you define `Configs` instance using `deriveBean` macro:

```tut:silent
implicit val myBeanConfigs: Configs[MyBean] =
  Configs.deriveBean[MyBean]
```

And then you can get Java Beans value:

```tut:silent
val config = ConfigFactory.parseString("""
  int-value = 42
  string-list = [foo, bar, baz]
  locale-to-duration {
    ja_JP = 42ms
    en_US = 123s
  }
  """)
```
```tut
config.extract[MyBean]
```


License
-------

Copyright 2013-2016 Tsukasa Kitachi

Apache License, Version 2.0
