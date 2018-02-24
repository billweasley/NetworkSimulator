package population.shapeConstruction

import population.LinkedPopulation
import scheduler.Scheduler
import utils.Node
import java.util.concurrent.ConcurrentHashMap

class ShapeConstructingPopulation(private val scheduler: Scheduler,
                                  private val numOfNodes: Int,
                                  private val interactFunction:
                                  (Node, Node, Map<Node, HashSet<Node>>) -> Pair<Boolean, Boolean>, //(isInteracted, ,New state for link)
                                  symbols: Set<String>,
                                  initialStates: Map<String, Int>) : LinkedPopulation {

    override val nodes = Node.createMultipleNodes(symbols, initialStates, numOfNodes)
    val adjacencyList = ConcurrentHashMap<Node, HashSet<Node>>()

    init {
        nodes.forEach({ node -> adjacencyList[node] = HashSet() })
    }

    @Volatile
    private var numOfEdges = 0

    private fun activateEdge(u: Node, v: Node) {

        if (adjacencyList.containsKey(u) && adjacencyList.containsKey(v)) {
            if (adjacencyList[u]?.contains(v)!!
                    && adjacencyList[v]?.contains(u)!!) {
                return
            }
            adjacencyList[v]?.add(u)
            adjacencyList[u]?.add(v)
            numOfEdges++
        }
    }

    private fun deactivateEdge(u: Node, v: Node) {
        if (adjacencyList.containsKey(u) && adjacencyList.containsKey(v)) {
            if (!adjacencyList[u]?.contains(v)!!
                    && !adjacencyList[v]?.contains(u)!!) {
                return
            }
            adjacencyList[v]?.remove(u)
            adjacencyList[u]?.remove(v)
            numOfEdges--
        }
    }

    private fun isEdgeActivated(u: Node, v: Node): Boolean {
        return adjacencyList[u]?.contains(v)!!
    }

    override fun numOfEdge(): Int {
        return numOfEdges
    }

    override fun numOfNode(): Int {
        return numOfNodes
    }

    override fun interact(): Boolean {
        val selected = scheduler.select(this)
        val nodeA = selected.first
        val nodeB = selected.second
        val result = interactFunction.invoke(nodeA, nodeB, adjacencyList)

        val hasInteracted = result.first
        val shouldBeActivated = result.second
        if (hasInteracted) {
            if (shouldBeActivated) {
                if (!isEdgeActivated(nodeA, nodeB))
                    activateEdge(nodeA, nodeB)
            } else {
                if (isEdgeActivated(nodeA, nodeB))
                    deactivateEdge(nodeA, nodeB)
            }
        }
        return hasInteracted
    }

}
