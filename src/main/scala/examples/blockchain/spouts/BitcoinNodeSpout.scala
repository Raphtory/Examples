package com.raphtory.spouts.blockchain
//
import java.io.{File, PrintWriter}

import com.raphtory.core.actors.Spout.Spout
import scalaj.http.{Http, HttpRequest}
import spray.json._

import scala.collection.mutable
import scala.language.postfixOps
import scala.sys.process._
//
case class BitcoinTransaction(time: JsValue, block: Int, blockID: JsValue, transaction: JsValue)

class BitcoinNodeSpout extends Spout[BitcoinTransaction]{

  var blockcount    = 1
  val rpcuser       = System.getenv().getOrDefault("BITCOIN_USERNAME", "").trim
  val rpcpassword   = System.getenv().getOrDefault("BITCOIN_PASSWORD", "").trim
  val serverAddress = System.getenv().getOrDefault("BITCOIN_NODE", "").trim
  val id            = "scala-jsonrpc"
  val baseRequest   = Http(serverAddress).auth(rpcuser, rpcpassword).header("content-type", "text/plain")

  override def setupDataSource(): Unit = {}
  override def closeDataSource(): Unit = {}

  val queue = mutable.Queue[Option[BitcoinTransaction]]()

  override def generateData(): Option[BitcoinTransaction] = {
   if(queue.isEmpty)
     getTransactions()
    queue.dequeue()
  }

  def getTransactions(): Unit = {
    try {

    val re        = request("getblockhash", blockcount.toString).execute().body.toString.parseJson.asJsObject
    val blockID   = re.fields("result")
    val blockData = request("getblock", s"$blockID,2").execute().body.toString.parseJson.asJsObject
    val result    = blockData.fields("result")
    val time      = result.asJsObject.fields("time")
    for (transaction <- result.asJsObject().fields("tx").asInstanceOf[JsArray].elements)
      queue += Some(BitcoinTransaction(time, blockcount, blockID, transaction))
    //val time = transaction.asJsObject.fields("time")
    blockcount += 1
  } catch {
      case e: java.net.SocketTimeoutException => queue += None
    }
  }

  //************* MESSAGE HANDLING BLOCK
  def handleDomainMessage(): Unit = {

  }

  def outputScript() = {
    val pw = new PrintWriter(new File("bitcoin.sh"))
    pw.write("""curl --user $1:$2 --data-binary $3 -H 'content-type: text/plain;' $4""")
    pw.close
    "chmod 777 bitcoin.sh" !
  }

  def curlRequest(command: String, params: String): String = {
    //val data = """{"jsonrpc":"1.0","id":"scala-jsonrpc","method":"getblockhash","params":[2]}"""
    val data = s"""{"jsonrpc":"1.0","id":"$id","method":"$command","params":[$params]}"""
    s"bash bitcoin.sh $rpcuser $rpcpassword $data $serverAddress" !!
  }

  def request(command: String, params: String = ""): HttpRequest =
    baseRequest.postData(s"""{"jsonrpc": "1.0", "id":"$id", "method": "$command", "params": [$params] }""")




}


//def request(command: String, params: String = ""): HttpRequest = baseRequest.postData(s"""{"jsonrpc": "1.0", "id":"$id", "method": "$command", "params": [$params] }""")
