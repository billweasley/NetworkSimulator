package scheduler

import model.population.Population
import model.population.populationProtocols.PopulationProtocol
import model.population.shapeConstruction.ShapeConstructingPopulation
import utils.ModelNode
import java.util.*

class RandomScheduler : Scheduler {
    private val randomGenerator = Random()
    override fun select(population: Population): Pair<ModelNode, ModelNode> {
        val boundary: Int?
        when (population) {
            is PopulationProtocol -> {
                boundary = population.size()
            }
            is ShapeConstructingPopulation -> {
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