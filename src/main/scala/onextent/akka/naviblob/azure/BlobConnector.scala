package onextent.akka.naviblob.azure

import akka.actor.Actor
import com.sksamuel.avro4s.{Decoder, SchemaFor}
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.naviblob.akka.{NoMore, Pull}
import onextent.akka.naviblob.azure.storage.{BlobConfig, BlobPaths}

import scala.annotation.tailrec

abstract class BlobConnector[T >: Null: Decoder: SchemaFor](
    implicit config: BlobConfig)
    extends Actor
    with LazyLogging {

  val pathsIterator: Iterator[String] = new BlobPaths().toList.iterator

  val firstPath: String = pathsIterator.next()
  logger.debug(s"reading from first path $firstPath")


  def createIterator(path: String): Iterator[T]

  var readerIterator: Iterator[T] = createIterator(firstPath)

  override def receive: Receive = {

    case _: Pull =>
      @tailrec
      def pull(): Unit = {
        if (readerIterator.hasNext) {
          // read one from the current file
          sender() ! readerIterator.next()
        } else {
          // open next file and read one
          if (!pathsIterator.hasNext) {
            // all files in original path spec have been processed
            sender() ! NoMore()
          } else {
            val nextPath = pathsIterator.next()
            logger.debug(s"reading from next path $nextPath")
            readerIterator = createIterator(nextPath)
            pull() // recurse until you find one readable
          }
        }
      }
      pull()

    case x => logger.error(s"I don't know how to handle ${x.getClass.getName}")

  }

}
