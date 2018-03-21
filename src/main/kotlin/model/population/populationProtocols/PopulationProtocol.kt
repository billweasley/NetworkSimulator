package model.population.populationProtocols

import model.population.LinklessPopulation
import scheduler.Scheduler
import utils.ModelNode

open class PopulationProtocol(private val scheduler: Scheduler,
                              private val interactFunction: (ModelNode, ModelNode) -> Boolean,
                              symbols: Set<String>,
                              initialStates: Map<String, Int>) : LinklessPopulation {

    override val nodes = ModelNode.createMultipleNodes(symbols, initialStates)

    override fun size(): Int {
        return nodes.size
    }

    override fun interact(): Triple<Boolean,ModelNode, ModelNode> {
        val selected = scheduler.select(this)
        return Triple(interactFunction.invoke(selected.first, selected.second),selected.first,selected.second);

    }
}

