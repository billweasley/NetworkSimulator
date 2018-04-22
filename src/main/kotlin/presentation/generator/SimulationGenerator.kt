package presentation.generator

import model.population.Population
import org.graphstream.algorithm.generator.Generator
import org.graphstream.graph.Graph
import org.graphstream.stream.SourceBase
import java.util.*

abstract class SimulationGenerator : Generator, SourceBase() {
    abstract val terminateThreshold: Int
    abstract val requireLayoutAlgorithm: Boolean
    abstract val nameOfPopulation: String
    abstract val maxTimes: Long
    abstract val fastRes: Boolean
    abstract val preExecutedSteps: Long
    protected val random = Random()
    @Volatile
    var countOfSelectWithoutInteraction = 0
    @Volatile
    var count = 0
    abstract val graph: Graph
    abstract val population: Population
    abstract fun shouldTerminate(): Boolean
    abstract fun restart()
    abstract fun display()
    override fun end() {}
    protected fun sleepWith(millisecond: Long) {
        try {
            Thread.sleep(millisecond)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}