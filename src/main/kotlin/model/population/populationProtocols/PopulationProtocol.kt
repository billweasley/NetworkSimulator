package model.population.populationProtocols

import model.population.LinklessPopulation
import model.shared.ModelNode
import scheduler.RandomScheduler
import scheduler.Scheduler

open class PopulationProtocol(private val scheduler: Scheduler = RandomScheduler(),
                              private val interactFunction: (ModelNode, ModelNode) -> Boolean,
                              private val symbols: Set<String>,
                              private val initialStates: Map<String, Int>) : LinklessPopulation {

    constructor(another: PopulationProtocol) :
            this(scheduler = another.scheduler, interactFunction = another.interactFunction,
                    symbols = another.symbols,
                    initialStates = another.initialStates)

    override val nodes = ModelNode.createMultipleNodes(symbols, initialStates)

    override fun size(): Int {
        return nodes.size
    }

    override fun interact(): Triple<Boolean, ModelNode, ModelNode> {
        val selected = scheduler.select(this)
        return Triple(interactFunction.invoke(selected.first, selected.second),selected.first,selected.second)

    }
}

