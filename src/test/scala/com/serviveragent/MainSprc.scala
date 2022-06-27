package com.serviveragent

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class MainSpec extends AnyFlatSpec with should.Matchers:
  "helloworld" should "" in {
    com.serviveragent.helloWorld === "hello world!"
  }
