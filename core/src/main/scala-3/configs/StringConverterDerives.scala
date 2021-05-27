package configs

trait StringConverterDerives:
  given [A <: Enumeration#Value]: StringConverter[A] = ???
