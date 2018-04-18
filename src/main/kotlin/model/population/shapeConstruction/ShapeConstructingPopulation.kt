package model.population.shapeConstruction

import model.population.LinkedPopulation
import model.shared.ModelNode
import scheduler.RandomScheduler
import scheduler.Scheduler
import java.util.concurrent.ConcurrentHashMap

class ShapeConstructingPopulation(private val scheduler: Scheduler = RandomScheduler(),
                                  private val interactFunction: (ModelNode, ModelNode, Map<ModelNode, HashSet<ModelNode>>) -> Pair<Boolean, Boolean>,
                                  private val symbols: Set<String>, //(isInteracted, ,New state for link)
                                  private val initialStates: Map<String, Int>) : LinkedPopulation {

    constructor(another: ShapeConstructingPopulation) :
            this(scheduler = another.scheduler, interactFunction = another.interactFunction,
                    symbols = another.symbols,
                    initialStates = another.initialStates)

    override val nodes = ModelNode.createMultipleNodes(symbols, initialStates)
    val adjacencyList = ConcurrentHashMap<ModelNode, HashSet<ModelNode>>()
    val statictisMap = symbols.map { it -> Pair(it, if (initialStates.containsKey(it)) initialStates[it]!! else 0) }.toMap(ConcurrentHashMap())


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

    @Synchronized override fun interact(): Triple<Boolean, ModelNode, ModelNode> {
        val selected = scheduler.select(this)
        val nodeA = selected.first
        val nodeB = selected.second
        val oriFirst = String(selected.first.state.currentState.toCharArray())
        val oriSecond = String(selected.second.state.currentState.toCharArray())
        val result = interactFunction.invoke(nodeA, nodeB, adjacencyList)
        val afterFirst = String(selected.first.state.currentState.toCharArray())
        val afterSecond = String(selected.second.state.currentState.toCharArray())
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
            statictisMap[oriFirst] = statictisMap[oriFirst]!! - 1
            statictisMap[oriSecond] = statictisMap[oriSecond]!! - 1
            statictisMap[afterFirst] = statictisMap[afterFirst]!! + 1
            statictisMap[afterSecond] = statictisMap[afterSecond]!! + 1
        }
        return Triple(hasInteracted,nodeA,nodeB)
    }

}
