package presentation.populationProtocol

import model.population.shapeConstruction.ShapeConstructingPopulation
import org.graphstream.algorithm.generator.Generator
import org.graphstream.graph.Edge
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.SourceBase
import scheduler.RandomScheduler
import utils.InteractionFunctions
import java.util.*



fun main(args: Array<String>) {
    val simpleGlobalLineConstructor = ShapeConstructingPopulation(
            scheduler = RandomScheduler(),
            interactFunction = { nodeA, nodeB, map ->
                InteractionFunctions.simpleGlobalLineFunc(nodeA, nodeB, map)
            },
            symbols = setOf("q0", "q1", "l", "w", "q2"),
            initialStates = mapOf(Pair("q0", 10))
    )

    val cycleCoverConstructor = ShapeConstructingPopulation(
            scheduler = RandomScheduler(),
            interactFunction = { nodeA, nodeB, map ->
                InteractionFunctions.cycleCoverFunc(nodeA, nodeB, map)
            },
            symbols = setOf("q0", "q1", "q2"),
            initialStates = mapOf(Pair("q0", 10))
    )
    val globalStarConstructor = ShapeConstructingPopulation(
            scheduler = RandomScheduler(),
            interactFunction = { nodeA, nodeB, map ->
                InteractionFunctions.globalStarFunc(nodeA, nodeB, map)
            },
            symbols = setOf("c", "p"),
            initialStates = mapOf(Pair("c", 100))
    )

    val styleSheet = "node {" +
            "	fill-color: black;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}" +
            "edge.marked {" +
            "	fill-color: red;" +
            "}"

    val populationProtocolGenerator = ShapeConstructorGenerator(
            globalStarConstructor,
            1000000,
            false,
            540000,
            "Test",
            styleSheet = styleSheet
    )

    populationProtocolGenerator.display()
}


class ShapeConstructorGenerator(val population: ShapeConstructingPopulation,
                                  val maxTimes: Long = -1,
                                  val fastRes: Boolean= false,
                                  val preExecutedSteps: Int = 0,
                                  nameOfPopulation: String,
                                  styleSheet: String? = null): SourceBase(), Generator {

    private var count = 0
    private val graph: Graph = SingleGraph(nameOfPopulation)
    private val random = Random()
     init {
        if(fastRes && preExecutedSteps < 0)
            throw IllegalArgumentException("The fast forwarding requires a non-negative value.")
        if (styleSheet != null)
            graph.addAttribute("ui.stylesheet", styleSheet)
        addSink(graph)
    }

    fun display(){

        graph.display()
        this.begin()
        while (count < maxTimes){
            this.nextEvents()
        }
        this.end()
    }


    override fun end() {}
    override fun begin() {
        if (fastRes){
            for(i in 0..preExecutedSteps){
                population.interact()
            }
        }
        val positions = HashSet<String>()
        for(node in population.nodes ){
            graph.addNode<Node>(node.index.toString())
            graph.getNode<Node>(node.index.toString())?.addAttribute("ui.label",node.state.currentState)
            var x = random.nextInt(Math.sqrt(population.nodes.size.toDouble()).toInt())
            var y = random.nextInt(Math.sqrt(population.nodes.size.toDouble()).toInt())
            while (positions.contains("$x | $y")){
                x = random.nextInt(population.nodes.size)
                y = random.nextInt(population.nodes.size)
            }
            positions.add("$x | $y")
            graph.getNode<Node>(node.index.toString())?.addAttribute("xy",x,y)
        }
        for (node in population.nodes){
            val neighbors = population.adjacencyList[node]
            if (neighbors != null){
                for (neighbor in neighbors){
                  if (!graph.getNode<Node>(node.index.toString()).hasEdgeBetween(graph.getNode<Node>(neighbor.index.toString())))  {
                      graph.addEdge<Edge>(
                              node.index.toString()+"|"+ neighbor.index.toString(),
                              graph.getNode<Node>(node.index.toString()),
                              graph.getNode<Node>(neighbor.index.toString())
                      )
                  }
                }
            }
        }
    }


    override fun nextEvents(): Boolean {
        val (isInteracted, firstNode, secondNode) = population.interact()
        if (isInteracted){
            count ++

            val firstNodeRep = graph.getNode<Node>(firstNode.index) as Node
            val secondNodeRep = graph.getNode<Node>(secondNode.index) as Node
            val firstOriLabel = firstNodeRep.getAttribute<String>("ui.label")
            val secondOriLabel = secondNodeRep.getAttribute<String>("ui.label")

            firstNodeRep.setAttribute("ui.class","marked")
            secondNodeRep.setAttribute("ui.class","marked" )
            firstNodeRep.setAttribute("ui.label",firstOriLabel+ "->" + firstNode.state.currentState)
            secondNodeRep.setAttribute("ui.label",secondOriLabel + "->"+ secondNode.state.currentState)

            if (population.adjacencyList[firstNode]?.contains(secondNode)!!){
                if (!firstNodeRep.hasEdgeBetween(secondNodeRep))
                    graph.addEdge<Edge>(firstNode.index.toString() + "|"+secondNode.index.toString(), firstNodeRep,secondNodeRep)
            }else{
                if (firstNodeRep.hasEdgeBetween(secondNodeRep)){
                    firstNodeRep.getEdgeBetween<Edge>(secondNodeRep)
                            .setAttribute("ui.class","marked" )
                    sleepWith(500)
                    graph.removeEdge<Edge>(firstNodeRep,secondNodeRep)
                }
            }
            sleepWith(500)
            graph.getNode<Node>(firstNode.index).setAttribute("ui.label",firstNode.state.currentState)
            graph.getNode<Node>(secondNode.index).setAttribute("ui.label",secondNode.state.currentState)
            graph.getNode<Node>(firstNode.index).removeAttribute("ui.class" )
            graph.getNode<Node>(secondNode.index).removeAttribute("ui.class" )

            sleepWith(500)
        }

        return isInteracted
    }


    private fun sleepWith(millisecond: Long){
        try {
            Thread.sleep(millisecond)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


}