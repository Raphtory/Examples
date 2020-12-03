
import com.raphtory.RaphtoryGraph
import examples.networkx.{networkxGraphBuilder, networkxSpout}
import algorithms.DegreeBasic

object testDeploy extends App{
  val source  = new networkxSpout()
  val builder = new networkxGraphBuilder()
  val RG = RaphtoryGraph[String](source,builder)
  val arguments = Array[String]()

  RG.viewQuery(DegreeBasic(), 3L, arguments)
}
