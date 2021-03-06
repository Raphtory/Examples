package com.raphtory.examples.test.actors

import com.raphtory.core.actors.Router.GraphBuilder
import com.raphtory.core.model.communication.{GraphUpdate, _}
import spray.json._

/**
  * The Graph Manager is the top level actor in this system (under the stream)
  * which tracks all the graph partitions - passing commands processed by the 'command processor' actors
  * to the correct partition
  */
/**
  * The Command Processor takes string message from Kafka and translates them into
  * the correct case Class which can then be passed to the graph manager
  * which will then pass it to the graph partition dealing with the associated vertex
  */
class RandomGraphBuilder extends GraphBuilder[String]{

  //************* MESSAGE HANDLING BLOCK

   override def parseTuple(tuple:String) = {
    val command    = tuple.asInstanceOf[String]
    val parsedOBJ  = command.parseJson.asJsObject //get the json object
    val commandKey = parsedOBJ.fields //get the command type
    if (commandKey.contains("VertexAdd"))
      sendUpdate(vertexAdd(parsedOBJ.getFields("VertexAdd").head.asJsObject))
    //else if(commandKey.contains("VertexUpdateProperties")) vertexUpdateProperties(parsedOBJ.getFields("VertexUpdateProperties").head.asJsObject)
    else if (commandKey.contains("VertexRemoval"))
      sendUpdate(vertexRemoval(parsedOBJ.getFields("VertexRemoval").head.asJsObject))
    else if (commandKey.contains("EdgeAdd"))
      sendUpdate(edgeAdd(parsedOBJ.getFields("EdgeAdd").head.asJsObject)) //if addVertex, parse to handling function
    //   else if(commandKey.contains("EdgeUpdateProperties")) edgeUpdateProperties(parsedOBJ.getFields("EdgeUpdateProperties").head.asJsObject)
    else if (commandKey.contains("EdgeRemoval"))
      sendUpdate(edgeRemoval(parsedOBJ.getFields("EdgeRemoval").head.asJsObject))
  }

  def vertexAdd(command: JsObject) = {
    val msgTime = command.fields("messageID").toString().toLong
    val srcId   = command.fields("srcID").toString().toInt //extract the srcID
    if (command.fields.contains("properties")) { //if there are properties within the command
      var properties = Properties() //create a vertex map
      //command.fields("properties").asJsObject.fields.foreach( pair => {  //add all of the pairs to the map
      //   properties = properties updated (pair._1, pair._2.toString())
      // })
      //send the srcID and properties to the graph manager
      VertexAddWithProperties(msgTime, srcId, properties)
    } else
      VertexAdd(msgTime, srcId)
    // if there are not any properties, just send the srcID
  }

//  def vertexUpdateProperties(command:JsObject):Unit={
//    val msgTime = command.fields("messageID").toString().toLong
//    val srcId = command.fields("srcID").toString().toInt //extract the srcID
//    var properties = Properties() //create a vertex map
//    //command.fields("properties").asJsObject.fields.foreach( pair => {properties = properties updated (pair._1,pair._2.toString())})
//    sendGraphUpdate(VertexUpdateProperties(msgTime,srcId,properties)) //send the srcID and properties to the graph parition
//  }

  def vertexRemoval(command: JsObject):GraphUpdate = {
    val msgTime = command.fields("messageID").toString().toLong
    val srcId   = command.fields("srcID").toString().toInt //extract the srcID
    VertexDelete(msgTime, srcId)
  }

  def edgeAdd(command: JsObject):GraphUpdate = {
    val msgTime = command.fields("messageID").toString().toLong
    val srcId   = command.fields("srcID").toString().toInt //extract the srcID
    val dstId   = command.fields("dstID").toString().toInt //extract the dstID
    if (command.fields.contains("properties")) { //if there are properties within the command
      var properties = Properties() //create a vertex map
//      command.fields("properties").asJsObject.fields.foreach( pair => { //add all of the pairs to the map
//        properties = properties updated (pair._1,pair._2.toString())
//      })
      EdgeAddWithProperties(msgTime, srcId, dstId, properties)
    } else EdgeAdd(msgTime, srcId, dstId)
  }

//  def edgeUpdateProperties(command:JsObject):Unit={
//    val msgTime = command.fields("messageID").toString().toLong
//    val srcId = command.fields("srcID").toString().toInt //extract the srcID
//    val dstId = command.fields("dstID").toString().toInt //extract the dstID
//    var properties =Properties() //create a vertex map
//    //command.fields("properties").asJsObject.fields.foreach( pair => {properties = properties updated (pair._1,pair._2.toString())})
//    sendGraphUpdate(EdgeUpdateProperties(msgTime,srcId,dstId,properties))//send the srcID, dstID and properties to the graph manager
//  }

  def edgeRemoval(command: JsObject):GraphUpdate = {
    val msgTime = command.fields("messageID").toString().toLong
    val srcId   = command.fields("srcID").toString().toInt //extract the srcID
    val dstId   = command.fields("dstID").toString().toInt //extract the dstID
    EdgeDelete(msgTime, srcId, dstId)
  }

}
