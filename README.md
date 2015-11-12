configs
=======

[![Build Status](https://travis-ci.org/kxbmap/configs.svg?branch=master)](https://travis-ci.org/kxbmap/configs)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.kxbmap/configs_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.kxbmap/configs_2.11)
[![Scaladoc](http://javadoc-badge.appspot.com/com.github.kxbmap/configs_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.github.kxbmap/configs_2.11)

Scala wrapper for [Typesafe config](https://github.com/typesafehub/config)

Usage
-----

For Java 8 (depends config 1.3.x):
```scala
libraryDependencies += "com.github.kxbmap" %% "configs" % "0.3.0"
```

For Java 7 (depends config 1.2.x):
```scala
libraryDependencies += "com.github.kxbmap" %% "configs-java7" % "0.3.0"
```

Examples
--------

preparation of each example:
```scala
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.ConfigFactory

val config = ConfigFactory.load()
```

### case class

```scala
case class CaseClassSetting(
  foo: Int,
  bar: String,
  baz: List[java.util.Locale]
)
```

```hocon
setting.foo = 42
setting.bar = hello
setting.baz = [ja_JP, en_US]
```

```scala
scala> config.get[CaseClassSetting]("setting")
res1: CaseClassSetting = CaseClassSetting(42,hello,List(ja_JP, en_US))
```

### sealed trait and case class/object

```scala
import com.github.kxbmap.configs.Configs

sealed trait SealedSetting

case object SealedObject extends SealedSetting
case class SealdCaseClass(value: Long) extends SealedSetting

// Need to define Configs[SealedSetting]
implicit val sealedSettingConfigs: Configs[SealedSetting] =
  Configs.of[SealedSetting]
```

```hocon
foo = SealedObject
bar {
  value = 42
}
```

```scala
scala> config.get[SealedSetting]("foo")
res3: SealedSetting = SealedObject

scala> config.get[SealedSetting]("bar")
res4: SealedSetting = SealdCaseClass(42)
```

### Java beans

```java
public class BeanSetting {
  private Integer foo;
  private String bar;
  private java.util.List<java.util.Locale> baz;
  // omit setters and getters
}
```

```scala
import com.github.kxbmap.configs.Configs

// Define Configs[BeanSetting]
implicit val beanSettingConfigs: Configs[BeanSetting] =
  Configs.bean[BeanSetting]
```

```hocon
setting.foo = 42
setting.bar = hello
setting.baz = [ja_JP, en_US]
```

```scala
scala> config.get[BeanSetting]("setting")
res1: BeanSetting = BeanSetting@56dc0deb

scala> (res1.getFoo, res1.getBar, res1.getBaz)
res2: (Integer, String, java.util.List[java.util.Locale]) = (42,hello,[ja_JP, en_US])
```


License
-------

Apache License, Version 2.0
