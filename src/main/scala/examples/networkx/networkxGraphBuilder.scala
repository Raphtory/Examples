package examples.networkx

import com.raphtory.core.actors.Router.GraphBuilder
import com.raphtory.core.model.communication._

class networkxGraphBuilder extends GraphBuilder[String]{

  override def parseTuple(record: String) = {
      val dp = record.split(";").map(_.trim)
      val properties = dp(2).replaceAll("^\\{|\\}$", "").split(",")
        .map(_.split(":"))
        .map { case Array(key, value) => (key.trim()-> value.trim()) }.toMap
      val srcClusterId = assignID(dp(0))//dp(0).toLong
      val dstClusterId = assignID(dp(1))//dp(1).toLong
      val time = properties("'t'").toLong


    sendUpdate(
      VertexAddWithProperties(
        msgTime = time,
        srcID = srcClusterId,
        Properties(StringProperty("Word", dp.head))
      )
    )
    sendUpdate(
      VertexAddWithProperties(
        msgTime = time,
        srcID = dstClusterId,
        Properties(StringProperty("Word", dp(1)))
      )
    )
    sendUpdate(
      EdgeAddWithProperties(msgTime = time,
          srcID = srcClusterId,
          dstID = dstClusterId,
          Properties(LongProperty("weight", properties("'weight'").toLong))
      )
    )

//    sendUpdate(
//        EdgeAddWithProperties(msgTime = time,
//          srcID = dstClusterId,
//          dstID = srcClusterId ,
//          Properties(LongProperty("weight", properties("'weight'").toLong))
//        )
//      )

    }
}
