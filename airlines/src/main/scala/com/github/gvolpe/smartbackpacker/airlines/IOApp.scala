package com.github.gvolpe.smartbackpacker.airlines

import cats.effect.IO

trait IOApp {
  def start(args: List[String]): IO[Unit]
  def main(args: Array[String]): Unit = start(args.toList).unsafeRunSync()
}