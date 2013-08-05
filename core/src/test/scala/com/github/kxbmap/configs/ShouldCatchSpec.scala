package com.github.kxbmap.configs

import com.typesafe.config.ConfigException
import org.scalatest.FunSpec

class ShouldCatchSpec extends FunSpec {
  
  describe("ShouldCatch") {

    describe("missing") {
      it ("should returns true if apply to ConfigException.Missing") {
        assert(ShouldCatch.missing(new ConfigException.Missing("")))
      }
  
      it ("should returns false if otherwise") {
        assert(!ShouldCatch.missing(new ConfigException.Generic("")))
      }
    }
  
    describe("configException") {
      it ("should returns true if apply to ConfigException") {
        assert(ShouldCatch.configException(new ConfigException.Generic("")))
      }
  
      it ("should returns false if otherwise") {
        assert(!ShouldCatch.missing(new UnsupportedOperationException()))
      }
    }
  
    describe("nonFatal") {
      it ("should returns true if apply to non fatal exception") {
        assert(ShouldCatch.nonFatal(new UnsupportedOperationException()))
      }
  
      it ("should returns false if otherwise") {
        assert(!ShouldCatch.nonFatal(new NotImplementedError()))
      }
    }
  }
}
