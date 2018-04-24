package population.shapeConstruction

import model.population.shapeConstruction.ShapeConstructingPopulation
import model.scheduler.RandomScheduler
import model.shared.ModelNode
import org.junit.Test
import shared.ShapeConstructionFunctions

class ShapeConstructingPopulationTest {
    val simpleGlobalLineConstructor = ShapeConstructingPopulation(
            scheduler = RandomScheduler(),
            interactFunction = { nodeA, nodeB, map ->
                ShapeConstructionFunctions.globalStarFunc(nodeA, nodeB, map)
            },
            symbols = setOf("q0", "q1", "l", "w", "q2"),
            initialStates = mapOf(Pair("q0", 10))
    )

    @Test
    fun interact() {

        var i = 1000000
        while (i-- > 0) {
            simpleGlobalLineConstructor.interact()
        }
        println(simpleGlobalLineConstructor.nodes.map { node -> node.state.currentState })


        var j = 0
        val index = HashMap<ModelNode, Int>()
        val list = simpleGlobalLineConstructor.adjacencyList.toList()
        list.forEach({ pair -> index[pair.first] = j++ })
        list.forEach({ pair ->
            val set = pair.second
            set.forEach { node -> println(" ${index[pair.first]} connected to ${index[node]}") }
        })

    }

}