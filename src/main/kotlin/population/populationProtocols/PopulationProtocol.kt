package population.populationProtocols

import population.LinklessPopulation
import scheduler.Scheduler
import utils.Node

open class PopulationProtocol(private val scheduler: Scheduler,
                              private val numOfNodes: Int,
                              private val interactFunction: (Node, Node) -> Boolean,
                              symbols: Set<String>,
                              initialStates: Map<String, Int>) : LinklessPopulation {

    override val nodes = Node.createMultipleNodes(symbols, initialStates, numOfNodes)

    override fun size(): Int {
        return numOfNodes
    }

    override fun interact(): Boolean {

        val selected = scheduler.select(this)
        return interactFunction.invoke(selected.first, selected.second)

    }
}

