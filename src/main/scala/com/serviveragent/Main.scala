package com.serviveragent

import akka.actor.typed.ActorSystem

@main def main(): Unit =
  ActorSystem(actortest.Module.createModule(), "ActorSystem")
