package model.population.shapeConstruction

import model.population.LinkedPopulation
import scheduler.Scheduler
import utils.ModelNode
import java.util.concurrent.ConcurrentHashMap

class ShapeConstructingPopulation(private val scheduler: Scheduler,
                                  private val interactFunction:
                                  (ModelNode, ModelNode, Map<ModelNode, HashSet<ModelNode>>) -> Pair<Boolean, Boolean>,
                                  symbols: Set<String>, //(isInteracted, ,New state for link)
                                  initialStates: Map<String, Int>) : LinkedPopulation {

    override val nodes = ModelNode.createMultipleNodes(symbols, initialStates)
    val adjacencyList = ConcurrentHashMap<ModelNode, HashSet<ModelNode>>()

    init {
        nodes.forEach({ node -> adjacencyList[node] = HashSet() })
    }

    @Volatile
    private var numOfEdges = 0

    private fun activateEdge(u: ModelNode, v: ModelNode) {

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

    private fun deactivateEdge(u: ModelNode, v: ModelNode) {
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

    private fun isEdgeActivated(u: ModelNode, v: ModelNode): Boolean {
        return adjacencyList[u]?.contains(v)!!
    }

    override fun numOfEdge(): Int {
        return numOfEdges
    }

    override fun numOfNode(): Int {
        return nodes.count()
    }

    override fun interact(): Triple<Boolean, ModelNode, ModelNode> {
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
        return Triple(hasInteracted,nodeA,nodeB)
    }

}
