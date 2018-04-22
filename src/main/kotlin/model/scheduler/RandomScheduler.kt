package model.scheduler

import model.population.LinkedPopulation
import model.population.Population
import model.population.populationProtocols.PopulationProtocol
import model.shared.ModelNode
import java.util.*

class RandomScheduler : Scheduler {
    private val randomGenerator = Random()
    override fun select(population: Population): Pair<ModelNode, ModelNode> {
        val boundary: Int?
        when (population) {
            is PopulationProtocol -> {
                boundary = population.size()
            }
            is LinkedPopulation -> {
                boundary = population.numOfNode()
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
        val indexOfInitiator = randomGenerator.nextInt(boundary)
        var indexOfReceiver = randomGenerator.nextInt(boundary)
        while (indexOfInitiator == indexOfReceiver) indexOfReceiver = randomGenerator.nextInt(boundary)
        return Pair(population.nodes[indexOfInitiator], population.nodes[indexOfReceiver])

    }
}