package presentation.generator.concrete

import model.population.gridNetworkConstruction.GridNetworkConstructingPopulation
import model.population.gridNetworkConstruction.MarkedSet
import model.shared.LocallyCoordinatedModelNode
import model.shared.Port
import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.AbstractGraph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.implementations.SingleNode
import presentation.generator.GridNode
import presentation.generator.SimulationGenerator
import shared.GridNetworkConstructingFunctions

class MySingleNode(graph: AbstractGraph, id: String) : SingleNode(graph, id)

fun main(args: Array<String>) {

    val spanningSquarePopulation = GridNetworkConstructingPopulation(interactFunction = {firstPair, secondPair -> GridNetworkConstructingFunctions.squareGridNetworkFunc(firstPair,secondPair)}

    , symbols = setOf("Lu", "q0", "q1", "Lr", "Ld", "Ll", "Lu"),
            initialStates = mapOf(Pair("q0", 25), Pair("Lu", 1))
    )
    GridNetworkGenerator(spanningSquarePopulation).display()
}


class GridNetworkGenerator(override var population: GridNetworkConstructingPopulation,
                           override val maxTimes: Long = 10000000,
                           override val fastRes: Boolean = false,
                           override val preExecutedSteps: Long = 1000000,
                           override val nameOfPopulation: String = "",
                           override val graph: SingleGraph = SingleGraph(nameOfPopulation),
                           private val styleSheet: String = "" +
                                   "node {fill-mode: none; fill-color: rgba(255,0,0,0);text-background-mode:plain;text-alignment:right;text-size: 10px;size: 5px;}" +
                                   "node.important {fill-mode: plain; fill-color: black;text-size: 20px; size: 10px;}" +
                                   "node.importantMarked {fill-mode: plain; fill-color: red;text-size: 20px; size: 10px;}" +
                                   "node.marked {fill-color: red;}" +
                                   "edge.marked {fill-color: red;}") : SimulationGenerator() {

    override val terminateThreshold: Int = 1000 * population.numOfNode()
    override val requireLayoutAlgorithm = false

    init {
        if (fastRes && preExecutedSteps < 0)
            throw IllegalArgumentException("The fast forwarding requires a non-negative value.")
        addSink(graph)
        graph.addAttribute("ui.stylesheet", styleSheet)
        val numOfNode = population.numOfNode()
        val setOfCoordinate = mutableSetOf<Pair<Int, Int>>()
        for (i in 0..(numOfNode - 1)) {
            var pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            while (setOfCoordinate.contains(pair))
                pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            val theNode = population.nodes[i]
            addNodeOnGraph(theNode.index.toString(), pair.first.toDouble(), pair.second.toDouble(),theNode.rotationDegree,state = theNode.state.currentState)
        }
    }

    private fun addNodeOnGraph(id: String, x: Double, y: Double, degreeOfRotation: Double = 0.0, state: String) {
        graph.setNodeFactory({ str, _ ->
            MySingleNode(graph as AbstractGraph, str)
        })
        val up = graph.addNode<SingleNode>("$id | u")
        val down = graph.addNode<SingleNode>("$id | d")
        val left = graph.addNode<SingleNode>("$id | l")
        val right = graph.addNode<SingleNode>("$id | r")
        println("addNode1")
        graph.setNodeFactory({ str, graph ->
            val res = GridNode(graph as AbstractGraph, str, x, y, up, down, left, right, degreeOfRotation)
            res.setAttribute("ui.class", "important")
            res
        })
        println("addNode2")
        graph.addNode<GridNode>(id)
        graph.getNode<GridNode>(id)?.addAttribute("ui.label",state)

    }


    @Synchronized
    override fun shouldTerminate(): Boolean {
        return countOfSelectWithoutInteraction > terminateThreshold ||  count >= maxTimes
    }

    override fun display() {
        graph.display(false)

        this.begin()
        while (count < maxTimes) {
            this.nextEvents()
        }
    }

    override fun restart() {
        countOfSelectWithoutInteraction = 0
        count = 0
        population = GridNetworkConstructingPopulation(population)
        graph.clear()
        graph.addAttribute("ui.stylesheet", styleSheet)
        val numOfNode = population.numOfNode()
        val setOfCoordinate = mutableSetOf<Pair<Int, Int>>()
        for (i in 0..(numOfNode - 1)) {
            var pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            while (setOfCoordinate.contains(pair))
                pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            val theNode = population.nodes[i]
            println("r1")
            addNodeOnGraph(theNode.index.toString(), pair.first.toDouble(), pair.second.toDouble(),theNode.rotationDegree,state = theNode.state.currentState)
            println("r2")
        }

    }

    override fun begin() {
        if (fastRes) {
            for (i in 0..preExecutedSteps) {
                population.interact()
            }
        }

        for (group in population.groupOfNodes.values) {
            initialConnectNodesInRepresentation(group)
        }
    }

    private fun updateRotationInBatch(toMoveNode: GridNode, destinationDegree: Double) {
        val set = dfsForUpdateRotationInBatch(toMoveNode, mutableSetOf())
        toMoveNode.updateRotation(destinationDegree - toMoveNode.getRotation())
        population.nodes[toMoveNode.id.toInt()].rotationDegree = toMoveNode.getRotation()
        val ori = Pair(Toolkit.nodePosition(toMoveNode)[0], Toolkit.nodePosition(toMoveNode)[1])
        for(node in set){
            if (node != toMoveNode){
                node.updateRotation( destinationDegree - node.getRotation())
                population.nodes[node.id.toInt()].rotationDegree = node.getRotation()

            }
        }
    }
    private fun dfsForUpdateRotationInBatch(node: GridNode, visited: MutableSet<GridNode>): MutableSet<GridNode> {
        if(!visited.contains(node)){
            visited.add(node)
            val rightCentre = node.getOppositeConnectionCenterNode(Port.RIGHT)
            if (rightCentre != null) visited.addAll(dfsForUpdateRotationInBatch(rightCentre,visited))
            val leftCentre = node.getOppositeConnectionCenterNode(Port.LEFT)
            if (leftCentre != null) visited.addAll(dfsForUpdateRotationInBatch(leftCentre,visited))
            val upCentre = node.getOppositeConnectionCenterNode(Port.UP)
            if (upCentre != null) visited.addAll(dfsForUpdateRotationInBatch(upCentre,visited))
            val downCentre = node.getOppositeConnectionCenterNode(Port.DOWN)
            if (downCentre != null) visited.addAll(dfsForUpdateRotationInBatch(downCentre,visited))
        }
        return visited
    }

    //Have to be used after updateRotationInBatch
    private fun movePosInBatch(toMoveNode: GridNode, newPos: Pair<Double, Double>){
        toMoveNode.setPos(newPos.first,newPos.second)
        dfsForMovePosInBatch(toMoveNode, mutableSetOf())
    }
    private fun dfsForMovePosInBatch(node: GridNode, visited: MutableSet<GridNode>): MutableSet<GridNode> {
        if(!visited.contains(node)){
            visited.add(node)
            val rightCentre = node.getOppositeConnectionCenterNode(Port.RIGHT)
            if (rightCentre != null){
                val rightNewPos =node.getOppositeConnectionCenterCoordinate(Port.RIGHT)
                rightCentre.setPos(rightNewPos.first,rightNewPos.second)
                visited.addAll(dfsForMovePosInBatch(rightCentre,visited))
            }
            val leftCentre = node.getOppositeConnectionCenterNode(Port.LEFT)
            if (leftCentre != null){
                val leftNewPos =node.getOppositeConnectionCenterCoordinate(Port.LEFT)
                leftCentre.setPos(leftNewPos.first,leftNewPos.second)
                visited.addAll(dfsForMovePosInBatch(leftCentre,visited))
            }
            val upCentre = node.getOppositeConnectionCenterNode(Port.UP)
            if (upCentre != null){
                val upNewPos = node.getOppositeConnectionCenterCoordinate(Port.UP)
                upCentre.setPos(upNewPos.first,upNewPos.second)
                visited.addAll(dfsForMovePosInBatch(upCentre,visited))
            }
            val downCentre = node.getOppositeConnectionCenterNode(Port.DOWN)
            if (downCentre != null){
                val downNewPos = node.getOppositeConnectionCenterCoordinate(Port.DOWN)
                downCentre.setPos(downNewPos.first,downNewPos.second)
                visited.addAll(dfsForMovePosInBatch(downCentre,visited))
            }
        }
        return visited
    }

    private fun initialConnectNodesInRepresentation(set: MarkedSet<LocallyCoordinatedModelNode>) {
        val isVisited = mutableSetOf<LocallyCoordinatedModelNode>()
        for (modelNode in set) {
            val thisRep = graph.getNode<Node>("${modelNode.index}") as GridNode
            for (thisPort in Port.values()){
                if (modelNode.getPort(thisPort) != null && isVisited.contains(modelNode.getPort(thisPort)!!)){
                    val anotherModelNode = modelNode.getPort(thisPort)!!
                    val anotherRep = graph.getNode<Node>("${anotherModelNode.index}") as GridNode
                    var anotherPort: Port? = null
                    for (thatPort in Port.values()){
                        if (anotherModelNode.getPort(thatPort) == modelNode){
                            anotherPort = thatPort
                            break
                        }
                    }
                    thisRep.setAttribute("ui.label",modelNode.state.currentState)
                    anotherRep.setAttribute("ui.label",anotherModelNode.state.currentState)
                    val thisInteractNodeRep = thisRep.getInteractPortNode(thisPort)
                    val anotherInteractNodeRep = anotherRep.getInteractPortNode(anotherPort!!)
                    updateRotationInBatch(anotherRep,thisRep.getRotation())
                    movePosInBatch(anotherRep, thisRep.getOppositeConnectionCenterCoordinate(thisPort))
                    graph.addEdge<Edge>(
                            if (thisInteractNodeRep.id < anotherInteractNodeRep.id) thisInteractNodeRep.id + anotherInteractNodeRep.id else thisInteractNodeRep.id + anotherInteractNodeRep.id,
                            thisInteractNodeRep, anotherInteractNodeRep
                    )

                }
            }
            isVisited.add(modelNode)
        }
    }
    @Synchronized
    override fun nextEvents(): Boolean {
        val (res, firstPairOfNode, secondPairOfNode) = population.interact()
        val isInteracted = res.first
        val shouldActivate = res.second
        if (isInteracted) {
            count++
            countOfSelectWithoutInteraction = 0
            println("n1")
            val firstModelNode = firstPairOfNode.first
            val firstModelPort = firstPairOfNode.second

            val secondModelNode = secondPairOfNode.first
            val secondModelPort = secondPairOfNode.second
            println("n2")
            val firstRep = graph.getNode<Node>("${firstModelNode.index}") as GridNode
            val secondRep = graph.getNode<Node>("${secondModelNode.index}") as GridNode
            println("n3")
            firstRep.setAttribute("ui.class", "importantMarked")
            secondRep.setAttribute("ui.class", "importantMarked")
            firstRep.setAttribute("ui.label",firstModelNode.state.currentState)
            secondRep.setAttribute("ui.label",secondModelNode.state.currentState)
            println("n4")
            val getFirstInteractNodeRep = firstRep.getInteractPortNode(firstModelPort)
            val getSecondInteractNodeRep = secondRep.getInteractPortNode(secondModelPort)
            println("n5")
            val firstInteractEdge = firstRep.getInteractEdge(firstModelPort)
            val secondInteractEdge = secondRep.getInteractEdge(secondModelPort)
            println("n6")
            firstInteractEdge.setAttribute("ui.class", "marked")
            secondInteractEdge.setAttribute("ui.class", "marked")
            sleepWith(500)

            if (shouldActivate) {
                val secondNewPos = firstRep.getOppositeConnectionCenterCoordinate(firstModelPort)
                println("Rotating. Before: ${secondRep.getRotation()}")
                println("Rotating. Before refer: ${firstRep.getRotation()}")
                updateRotationInBatch(secondRep,firstRep.getRotation())
                movePosInBatch(secondRep, secondNewPos)
                sleepWith(1000)
                println("n7")
                graph.addEdge<Edge>(
                        if (getFirstInteractNodeRep.id < getSecondInteractNodeRep.id) getFirstInteractNodeRep.id + getSecondInteractNodeRep.id else getSecondInteractNodeRep.id + getFirstInteractNodeRep.id,
                        getFirstInteractNodeRep, getSecondInteractNodeRep
                )
                println("n8")
                sleepWith(1000)
                firstInteractEdge.removeAttribute("ui.class")
                secondInteractEdge.removeAttribute("ui.class")
                firstRep.setAttribute("ui.class", "important")
                secondRep.setAttribute("ui.class", "important")
            } else {
                graph.removeEdge<Edge>(getFirstInteractNodeRep, getSecondInteractNodeRep)
                println("n9")
                sleepWith(1000)
                firstInteractEdge.removeAttribute("ui.class")
                secondInteractEdge.removeAttribute("ui.class")
                firstRep.setAttribute("ui.class", "important")
                secondRep.setAttribute("ui.class", "important")
            }
        } else {
            countOfSelectWithoutInteraction++
        }
        return isInteracted
    }

}

