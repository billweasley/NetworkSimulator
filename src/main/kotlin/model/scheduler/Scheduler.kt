package scheduler

import model.population.Population
import model.shared.ModelNode

interface Scheduler {
    fun select(population: Population): Pair<ModelNode, ModelNode>
}
