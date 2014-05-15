package neo4j.demo

import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphalgo.PathFinder
import org.neo4j.graphalgo.WeightedPath
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Path
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.traversal.Evaluators
import org.neo4j.graphdb.traversal.TraversalDescription
import org.neo4j.kernel.GraphDatabaseAPI
import org.neo4j.kernel.Traversal
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * Created by hariharank12 on 15/5/14.
 */
class Neo4jPageFlow {

  type VertexOut = scala.collection.immutable.Map[String, Map[String, String]]
  type NodesAndRef = scala.collection.immutable.Seq[(java.lang.String, List[java.lang.String], List[java.lang.String])]

  def getInputXml:scala.xml.Elem = <bank>
    <page name="login"><body><form><input name="key" type="text">ccc</input></form></body>
      <outlinks>
        <page ref="summary" route="success"></page>
        <page ref ="invalid" route="error"></page>
      </outlinks>
    </page>
    <page name="summary"><body>balance</body></page>
    <page name="invalid"><body>credentials</body></page>
  </bank>

  val actualKey = "ccc"

  def mapTwoList(a: List[String], b:List[String]) = a.zip(b)

  def tupleListToMap(input: List[(String, String)]) = input.toMap

  def transformListToMap(input: NodesAndRef): VertexOut = {

    def transformListToMapInner(input: NodesAndRef, acc: VertexOut): VertexOut = {
      if (input.isEmpty) acc
      else transformListToMapInner(input.tail, acc ++ Map(input.head._1 -> tupleListToMap(mapTwoList(input.head._2.head.split(",").toList, input.head._3.head.split(",").toList))))
    }
    transformListToMapInner(input, Map())
  }

  def getNodesAndReferences(filterStatus: Boolean):VertexOut = {

    val nodeWithReferenceList:NodesAndRef = (getInputXml \ "page").
      map(x => (x \ "@name", (x \ "outlinks"))).filter(x => {if(filterStatus) filterStatus else !(x._2.isEmpty) }).map(x => (x._1.text, List((x._2 \\ "@ref").mkString(",")), List((x._2 \\ "@route").mkString(","))))

    transformListToMap(nodeWithReferenceList)
  }

  val nodesAndRefMap = getNodesAndReferences(true)

  val getGraphNodes = (getInputXml \ "page").map(_ \ "@name")

  val getFormNode = (getInputXml \ "page").map(x => (x \ "@name", x \\ "form")).filter(x => !(x._2.isEmpty)).map(x => (x._1, x._2.text))

  //  val createNode =

}

object Neo4jPageFlow {

  def main(args: Array[String]): Unit = {
    val pageFlow = new Neo4jPageFlow
    val nodesAndTheirRef = pageFlow getNodesAndReferences(true)
    println(nodesAndTheirRef)
    val nodesAndTheirRefWithoutEmpty = pageFlow getNodesAndReferences(false)
    println(nodesAndTheirRefWithoutEmpty)

  }

}
