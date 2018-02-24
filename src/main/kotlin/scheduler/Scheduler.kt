package scheduler

import population.Population
import utils.Node

interface Scheduler {
    fun select(population: Population): Pair<Node, Node>
}
