package configs.testutil.instance

import scalaprops.Gen

extension [A](gen: Gen[A])
  inline def widen[X >: A] = gen.asInstanceOf[Gen[X]]
