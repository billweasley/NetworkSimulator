package model.population.populationProtocols

import model.population.LinklessPopulation
import model.shared.ModelNode
import scheduler.RandomScheduler
import scheduler.Scheduler
import java.util.concurrent.ConcurrentHashMap

open class PopulationProtocol(private val scheduler: Scheduler = RandomScheduler(),
                              private val interactFunction: (ModelNode, ModelNode) -> Boolean,
                              private val symbols: Set<String>,
                              private val initialStates: Map<String, Int>) : LinklessPopulation {

    override val statisticsMap = symbols.map { it -> Pair(it, if (initialStates.containsKey(it)) initialStates[it]!! else 0) }.toMap(ConcurrentHashMap())

    constructor(another: PopulationProtocol) :
            this(scheduler = another.scheduler, interactFunction = another.interactFunction,
                    symbols = another.symbols,
                    initialStates = another.initialStates)

    override val nodes = ModelNode.createMultipleNodes(symbols, initialStates)

    override fun size(): Int {
        return nodes.size
    }

    @Synchronized override fun interact(): Triple<Boolean, ModelNode, ModelNode> {
        val selected = scheduler.select(this)
        val oriFirst = String(selected.first.state.currentState.toCharArray())
        val oriSecond = String(selected.second.state.currentState.toCharArray())
        val res = Triple(interactFunction.invoke(selected.first, selected.second),selected.first,selected.second)
        if (res.first){
            statisticsMap[oriFirst] = statisticsMap[oriFirst]!! - 1
            statisticsMap[oriSecond] = statisticsMap[oriSecond]!! - 1
            statisticsMap[res.second.state.currentState] = statisticsMap[res.second.state.currentState]!! + 1
            statisticsMap[res.third.state.currentState] = statisticsMap[res.third.state.currentState]!! + 1
        }
        return res
    }
}

