package presentation.generator

import org.graphstream.algorithm.generator.Generator

interface SimulationGenerator: Generator{
    var countOfSelectWithoutInteraction: Int
    val terminateTheshold: Int
    val requireLayoutAlgorithm: Boolean
    fun shouldTerminate(): Boolean{
        return false
    }

    fun restart()
}