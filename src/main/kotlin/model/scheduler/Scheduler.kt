package scheduler

import model.population.Population
import utils.ModelNode

interface Scheduler {
    fun select(population: Population): Pair<ModelNode, ModelNode>
}
