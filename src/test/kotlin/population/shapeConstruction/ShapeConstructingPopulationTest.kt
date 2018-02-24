package population.shapeConstruction

import org.junit.Test
import scheduler.RandomScheduler
import utils.InteractionFunctions
import utils.Node

class ShapeConstructingPopulationTest {
    val simpleGlobalLineConstructor = ShapeConstructingPopulation(
            initialStates = mapOf(Pair("q0", 10)),
            symbols = setOf("q0", "q1", "l", "w", "q2"),
            scheduler = RandomScheduler(),
            interactFunction = { nodeA, nodeB, map ->
                InteractionFunctions.simpleGlobalLineFunc(nodeA, nodeB, map)
            },
            numOfNodes = 10
    )

    @Test
    fun interact() {

        var i = 1000000
        while (i-- > 0) {
            simpleGlobalLineConstructor.interact()
        }
        println(simpleGlobalLineConstructor.nodes.map { node -> node.state.currentState })


        var j = 0
        val index = HashMap<Node, Int>()
        val list = simpleGlobalLineConstructor.adjacencyList.toList()
        list.forEach({ pair -> index[pair.first] = j++ })
        list.forEach({ pair ->
            val set = pair.second
            set.forEach { node -> println(" ${index[pair.first]} connected to ${index[node]}") }
        })

    }

}