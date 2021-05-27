package configs

trait StringConverterDerives {

  implicit def enumValueStringConverter[A <: Enumeration]: StringConverter[A#Value] =
    macro macros.StringConverterMacro.enumValueStringConverter[A]

}
