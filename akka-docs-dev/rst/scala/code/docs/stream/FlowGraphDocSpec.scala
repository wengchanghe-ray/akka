/**
 * Copyright (C) 2014 Typesafe Inc. <http://www.typesafe.com>
 */
package docs.stream

import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.FlowGraph
import akka.stream.scaladsl.FlowGraphImplicits
import akka.stream.scaladsl.MaterializedMap
import akka.stream.scaladsl.Merge
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Zip
import akka.stream.testkit.AkkaSpec

// TODO replace ⇒ with => and disable this intellij setting
class FlowGraphDocSpec extends AkkaSpec {

  implicit val ec = system.dispatcher

  implicit val mat = FlowMaterializer()

  "build simple graph" in {
    //format: OFF
    //#simple-flow-graph
    val g = FlowGraph { implicit b ⇒
      import FlowGraphImplicits._
      val in = Source(1 to 10)
      val out = Sink.ignore

      val broadcast = Broadcast[Int]
      val merge = Merge[Int]

      val f1 = Flow[Int].map(_ + 10)
      val f3 = Flow[Int].map(_.toString)
      val f2 = Flow[Int].map(_ + 20)

      in ~> broadcast ~> f1 ~> merge
            broadcast ~> f2 ~> merge ~> f3 ~> out
    }
    //#simple-flow-graph
    //format: ON

    //#simple-graph-run
    val map: MaterializedMap = g.run()
    //#simple-graph-run
  }

  "build simple graph without implicits" in {
    //#simple-flow-graph-no-implicits
    val g = FlowGraph { b ⇒
      val in = Source(1 to 10)
      val out = Sink.ignore

      val broadcast = Broadcast[Int]
      val merge = Merge[Int]

      val f1 = Flow[Int].map(_ + 10)
      val f3 = Flow[Int].map(_.toString)
      val f2 = Flow[Int].map(_ + 20)

      b.addEdge(in, broadcast)
        .addEdge(broadcast, f1, merge)
        .addEdge(broadcast, f2, merge)
        .addEdge(merge, f3, out)
    }
    //#simple-flow-graph-no-implicits

    g.run()
  }

  "flow connection errors" in {
    intercept[IllegalArgumentException] {
      //#simple-graph
      FlowGraph { implicit b ⇒
        import FlowGraphImplicits._
        val source1 = Source(1 to 10)
        val source2 = Source(1 to 10)

        val zip = Zip[Int, Int]

        source1 ~> zip.left
        source2 ~> zip.right
        // unconnected zip.out (!) => "must have at least 1 outgoing edge"
      }
      //#simple-graph
    }.getMessage should include("must have at least 1 outgoing edge")
  }

}
