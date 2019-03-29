package onextent.akka.naviblob.azure

import akka.actor.Props
import com.typesafe.scalalogging.LazyLogging
import onextent.akka.naviblob.azure.storage._

object TextBlobConnector extends LazyLogging {
  val name: String = "TextBlobConnector"
  def props(implicit config: BlobConfig) =
    Props(new TextBlobConnector())
}

class TextBlobConnector(implicit config: BlobConfig)
    extends BlobConnector[String]
    with LazyLogging {

  def createIterator(path: String): Iterator[String] = {

    if (path.startsWith("._") || path.endsWith("crc"))
      new Iterator[String] { // empty reader for non-data file
        override def hasNext: Boolean = false
        override def next(): String = ""
      } else new TextStreamReader(path).read()
  }

}
