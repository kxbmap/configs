package com.github.kxbmap.configs

import com.typesafe.config.ConfigException
import org.scalatest.FunSpec

class CatchCondSpec extends FunSpec {

  describe("CatchCond") {

    describe("missing") {
      it ("should returns true if apply to ConfigException.Missing") {
        assert(CatchCond.missing(new ConfigException.Missing("")))
      }

      it ("should returns false if otherwise") {
        assert(!CatchCond.missing(new ConfigException.Generic("")))
      }
    }

    describe("configException") {
      it ("should returns true if apply to ConfigException") {
        assert(CatchCond.configException(new ConfigException.Generic("")))
      }

      it ("should returns false if otherwise") {
        assert(!CatchCond.missing(new UnsupportedOperationException()))
      }
    }

    describe("nonFatal") {
      it ("should returns true if apply to non fatal exception") {
        assert(CatchCond.nonFatal(new UnsupportedOperationException()))
      }

      it ("should returns false if otherwise") {
        assert(!CatchCond.nonFatal(new NotImplementedError()))
      }
    }
  }
}
