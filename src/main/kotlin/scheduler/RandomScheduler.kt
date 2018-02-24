package scheduler

import population.Population
import population.populationProtocols.PopulationProtocol
import population.shapeConstruction.ShapeConstructingPopulation
import utils.Node
import java.util.*

class RandomScheduler : Scheduler {
    private val randomGenerator = Random()
    override fun select(population: Population): Pair<Node, Node> {
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