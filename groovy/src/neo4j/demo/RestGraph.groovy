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
 * Created by hariharan kumar on 4/28/14.
 */

class RestGraphDemo {

    GraphDatabaseAPI dbObj

    public enum RelTypes implements RelationshipType
    {
        friend,brother,
    }

    RestGraphDemo() {
        dbObj = new RestGraphDatabase("http://localhost:7474/db/data")
    }

    void createNode() {
        org.neo4j.graphdb.Node node = dbObj.createNode()
        node.setProperty("name", "rose")
    }

    void createRelationShip() {
        org.neo4j.graphdb.Node dinesh = dbObj.getNodeById(1)
        org.neo4j.graphdb.Node kiki = dbObj.getNodeById(4)
        println(dinesh.getProperty("name"))
        println(kiki.getProperty("name"))
        dinesh.createRelationshipTo(kiki, {
            return "friend"
        } as RelationshipType)
    }

    void deleteRelationShip() {
        org.neo4j.graphdb.Node dinesh = dbObj.getNodeById(1)
        Iterable relationShips =
                dinesh.getRelationships({ return "friend" } as RelationshipType, Direction.BOTH)
        relationShips.each { relation ->
            if ("kiki".equals(relation.endNode.getProperty("name"))) relation.delete()
        }
    }

    void selectNode() {
        org.neo4j.graphdb.Node node = dbObj.getNodeById(1)
        println(node.getProperty("name"))
    }

    void shutDown() {
        dbObj.shutdown()
    }

    // Here Conditional traversal starts exploring all possible paths and as well shortest path
    void getFriendsByBreadthFirst()
    {
        org.neo4j.graphdb.Node ganesh = dbObj.getNodeById(0)
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships( { return "friend" } as RelationshipType, Direction.OUTGOING )
                .evaluator( Evaluators.excludeStartPosition() );
        td.traverse(ganesh).nodes().each { node -> println(node.getProperty("name")) }
    }

    void getFriendsByDepthFirst()
    {
        org.neo4j.graphdb.Node ganesh = dbObj.getNodeById(0)
        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships( RelTypes.friend, Direction.OUTGOING )
                .evaluator( Evaluators.excludeStartPosition() );
        td.traverse(ganesh).nodes().each { node -> println(node.getProperty("name")) }
    }

    void getFamilyByDepthFirst()
    {
        org.neo4j.graphdb.Node ganesh = dbObj.getNodeById(0)
        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships( { return "brother" } as RelationshipType, Direction.OUTGOING )
                .evaluator( Evaluators.excludeStartPosition() );
        td.traverse(ganesh).nodes().each { node -> println(node.getProperty("name")) }

    }

    // Finding effective path along with the weightage
    void findShortestPathsBetweenStartAndEndNode() {
        org.neo4j.graphdb.Node startNode = dbObj.getNodeById(0)
        org.neo4j.graphdb.Node endNode = dbObj.getNodeById(35)
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
                Traversal.expanderForTypes( RelTypes.friend, Direction.OUTGOING ), 15 )
        Iterable<Path> paths = finder.findAllPaths( startNode, endNode )
        paths.each { path ->
            path.nodes().each {
                node -> println(node.getProperty("name"))
            }
        }
    }

    void findAllPathsBetweenStartAndEndNode() {
        org.neo4j.graphdb.Node startNode = dbObj.getNodeById(0)
        org.neo4j.graphdb.Node endNode = dbObj.getNodeById(35)
        PathFinder<Path> finder = GraphAlgoFactory.allSimplePaths(
                Traversal.expanderForTypes(RelTypes.friend, Direction.OUTGOING), 15)
        Iterable<Path> paths = finder.findAllPaths(startNode, endNode)
        paths.eachWithIndex { Path path, int i ->
            println("Starting with index " + i)
            path.nodes().each {
                node -> println(node.getProperty("name"))
            }
        }
    }

    void findAllPathsBetweenStartAndEndNodeWithWeights() {
        org.neo4j.graphdb.Node startNode = dbObj.getNodeById(0)
        org.neo4j.graphdb.Node endNode = dbObj.getNodeById(35)
        PathFinder<Path> finder = GraphAlgoFactory.allSimplePaths(
                Traversal.expanderForTypes(RelTypes.friend, Direction.OUTGOING), 15)
        Iterable<Path> paths = finder.findAllPaths(startNode, endNode)
        paths.eachWithIndex { Path path, int i ->
            println("Starting with index " + i)
            path.nodes().each {
                node -> println(node.getProperty("name"))
            }
        }
    }

    // Calculate the weight
    void cheapestPathUsingDijkstra() {
        org.neo4j.graphdb.Node startNode = dbObj.getNodeById(0)
        org.neo4j.graphdb.Node endNode = dbObj.getNodeById(35)
        PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(
                Traversal.expanderForTypes( RelTypes.friend, Direction.BOTH ), "cost" )
        WeightedPath path = finder.findSinglePath( startNode, endNode )
        // Get the weight for the found path
        println(path.weight())
    }
}
restGraphDemo = new neo4j.demo.RestGraphDemo()
restGraphDemo.createNode()
//restGraphDemo.createRelationShip()
//restGraphDemo.deleteRelationShip()
restGraphDemo.selectNode()
println("Friends Breadth First")
restGraphDemo.getFriendsByBreadthFirst()
println("Friends depth First")
restGraphDemo.getFriendsByDepthFirst()
println("Family depth First")
restGraphDemo.getFamilyByDepthFirst()
restGraphDemo.findShortestPathsBetweenStartAndEndNode()
restGraphDemo.findAllPathsBetweenStartAndEndNode()
restGraphDemo.findAllPathsBetweenStartAndEndNodeWithWeights()
restGraphDemo.cheapestPathUsingDijkstra()