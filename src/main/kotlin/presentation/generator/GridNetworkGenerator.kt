package presentation.generator

import model.population.gridNetworkConstruction.GridNetworkConstructingPopulation
import model.population.gridNetworkConstruction.MarkedSet
import model.shared.LocallyCoordinatedModelNode
import model.shared.Port
import model.shared.State
import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.AbstractGraph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.implementations.SingleNode
import org.graphstream.stream.SourceBase
import java.util.*

fun isThePattern(str: String, pattern: String): Boolean = pattern.toRegex().matches(str)

fun isThePatternPair(pair: Pair<String, String>, patternPair: Pair<String, String>): Boolean {
    return isThePattern(pair.first, patternPair.first) && isThePattern(pair.second, patternPair.second)
}

fun isTheTriple(triple: Triple<Pair<State, Port>, Pair<State, Port>, Boolean>, patternTriple: Triple<Pair<String, Port>, Pair<String, Port>, Boolean>): Boolean {
    return isThePatternPair(Pair(triple.first.first.currentState, triple.second.first.currentState),
            Pair(patternTriple.first.first, patternTriple.second.first))
            && triple.first.second == patternTriple.first.second
            && triple.second.second == patternTriple.second.second
            && triple.third == patternTriple.third
}

infix fun Triple<Pair<State, Port>, Pair<State, Port>, Boolean>.match(patternTriple: Triple<Pair<String, Port>, Pair<String, Port>, Boolean>): Boolean = isTheTriple(this, patternTriple)


fun main(args: Array<String>) {

    val spanningSquarePopualtion = GridNetworkConstructingPopulation(interactFunction = { firstPair: Pair<LocallyCoordinatedModelNode, Port>, secondPair: Pair<LocallyCoordinatedModelNode, Port> ->
        val firstModelNode = firstPair.first
        val secondModelNode = secondPair.first
        val isConnected = firstModelNode.getPort(firstPair.second) == secondModelNode
        //println("${firstModelNode.state} ${firstPair.second} ${secondModelNode.state} ${secondPair.second} $isConnected")
        val givenState =
                Triple(Pair(firstModelNode.state.currentState, firstPair.second), Pair(secondModelNode.state.currentState, secondPair.second), isConnected)
        val transferredState = when(givenState) {
            Triple(Pair("Ll", Port.LEFT), Pair("q1", Port.RIGHT), false) -> Triple("Ld", "q1", true)
            Triple(Pair("Lu", Port.UP), Pair("q0", Port.DOWN), false) -> Triple("q1", "Lr", true)
            Triple(Pair("Lr", Port.RIGHT), Pair("q0", Port.LEFT), false) -> Triple("q1", "Ld", true)
            Triple(Pair("Ld", Port.DOWN), Pair("q0", Port.UP), false) -> Triple("q1", "Ll", true)
            Triple(Pair("Ll", Port.LEFT), Pair("q0", Port.RIGHT), false) -> Triple("q1", "Lu", true)
            Triple(Pair("Lu", Port.UP), Pair("q1", Port.DOWN), false) -> Triple("Ll", "q1", true)
            Triple(Pair("Lr", Port.RIGHT), Pair("q1", Port.LEFT), false) -> Triple("Lu", "q1", true)
            Triple(Pair("Ld", Port.DOWN), Pair("q1", Port.UP), false) -> Triple("Lr", "q1", true)
            else -> null
        }
        //println("is null? ${transferredState == null}")
        if (transferredState == null) Triple(false, Pair("",""),isConnected) else{
            Triple(true, Pair(transferredState.first,transferredState.second), transferredState.third)
        }

    }, symbols = setOf("Lu", "q0", "q1", "Lr", "Ld", "Ll", "Lu"), initialStates = mapOf(Pair("q0", 35), Pair("Lu", 1)))
    GridNetworkGenerator(spanningSquarePopualtion).display()
}

/*val transferredState = when {
givenState == Triple(Pair("i", Port.DOWN), Pair("q0", Port.UP), false)
-> Triple("i1", "q0", true)
givenState == Triple(Pair("e", Port.DOWN), Pair("q0", Port.UP), false)
-> Triple("e1", "e1", true)
givenState match Triple(Pair("i[12]", Port.RIGHT), Pair("i[12]", Port.LEFT), false)
-> Triple((givenState.first.first + 1).toString(), (givenState.second.first + 1).toString(), true)
givenState == Triple(Pair("i1", Port.RIGHT), Pair("e1", Port.LEFT), false)
-> Triple("i2", "e2", true)
givenState == Triple(Pair("i2", Port.RIGHT), Pair("e1", Port.LEFT), false)
-> Triple("i3", "e2", true)
givenState == Triple(Pair("e1", Port.RIGHT), Pair("i1", Port.LEFT), false)
-> Triple("e2", "i2", true)
givenState == Triple(Pair("e1", Port.RIGHT), Pair("i2", Port.LEFT), false)
-> Triple("e2", "i3", true)
givenState == Triple(Pair("i3", Port.UP), Pair("i1", Port.DOWN), true)
-> Triple("i", "i", false)
givenState == Triple(Pair("e2", Port.UP), Pair("e1", Port.DOWN), true)
-> Triple("e", "e", false)
else -> null
}*/


class GridNetworkGenerator(var population: GridNetworkConstructingPopulation,
                           val maxTimes: Long = 1000000,
                           val fastRes: Boolean = false,
                           val preExecutedSteps: Int = 0,
                           nameOfPopulation: String = "",
                           val graph: SingleGraph = SingleGraph(nameOfPopulation),
                           private val styleSheet: String = "node {fill-mode: none; fill-color: rgba(255,0,0,0);text-background-mode:plain;text-alignment:right;text-size: 10px;size: 5px;}" +
                                   "node.important {fill-mode: plain; fill-color: black;text-size: 20px; size: 10px;}" +
                                   "node.importantMarked {fill-mode: plain; fill-color: red;text-size: 20px; size: 10px;}" +
                                   "node.marked {fill-color: red;}" +
                                   "edge.marked {fill-color: red;}") : SourceBase(), SimulationGenerator {

    override var countOfSelectWithoutInteraction: Int = 0
    override val terminateTheshold: Int = 1000
    override val requireLayoutAlgorithm = true
    private val random = Random()
    private var count = 0

    init {
        if (fastRes && preExecutedSteps < 0)
            throw IllegalArgumentException("The fast forwarding requires a non-negative value.")
        graph.addAttribute("ui.stylesheet", styleSheet)
        addSink(graph)
        graph.addAttribute("layout.stabilization-limit",0.01)
        graph.addAttribute("layout.force",0.0)
        graph.addAttribute("layout.weight",0.0)
    }

    private fun addNodeOnGraph(id: String, x: Double, y: Double, degreeOfRotation: Double = 0.0, state: String) {
        graph.setNodeFactory({ str, _ ->
            MySingleNode(graph as AbstractGraph, str)
        })
        val up = graph.addNode<SingleNode>("$id | u")
        val down = graph.addNode<SingleNode>("$id | d")
        val left = graph.addNode<SingleNode>("$id | l")
        val right = graph.addNode<SingleNode>("$id | r")
        graph.setNodeFactory({ str, graph ->
            val res = GridNode(graph as AbstractGraph, str, x, y, up, down, left, right, degreeOfRotation)
            res.setAttribute("ui.class", "important")
            res
        })
        graph.addNode<GridNode>(id)
        graph.getNode<GridNode>(id)?.addAttribute("ui.label",state)

    }


    @Synchronized
    override fun shouldTerminate(): Boolean {
        return countOfSelectWithoutInteraction > terminateTheshold
    }

    fun display() {
        this.begin()
        graph.display(false)
        while (count < maxTimes) {
            this.nextEvents()
        }
    }

    private fun sleepWith(millisecond: Long) {
        try {
            Thread.sleep(millisecond)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun restart() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun end() {}

    override fun begin() {
        if (fastRes) {
            for (i in 0..preExecutedSteps) {
                val result = population.interact()
            }
        }
        val numOfNode = population.numOfNode()
        val setOfCoordinate = mutableSetOf<Pair<Int, Int>>()
        for (i in 0..(numOfNode - 1)) {
            var pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            while (setOfCoordinate.contains(pair))
                pair = Pair(random.nextInt(numOfNode), random.nextInt(numOfNode))
            val theNode = population.nodes[i]
            addNodeOnGraph(theNode.index.toString(), pair.first.toDouble(), pair.second.toDouble(),state = theNode.state.currentState)
        }
        for (group in population.groupOfNodes.values) {
            initialConnectNodesInRepresentation(group)
        }
    }

    private fun updateRotationInBatch(toMoveNode: GridNode, degreeOfTransfer: Double) {
        dfsForBatchRotation(toMoveNode, degreeOfTransfer, mutableSetOf())
    }

    private fun movePosInBatch(toMoveNode: GridNode, newPos: Pair<Double, Double>) {
        println("${newPos.first} ${newPos.second}")
        val ori = Pair(Toolkit.nodePosition(toMoveNode)[0], Toolkit.nodePosition(toMoveNode)[1])
        val diffVector = Pair(newPos.first - ori.first, newPos.second - ori.second)
        val set = dfsForBatchMovement(toMoveNode, mutableSetOf())
        println("size ${set.size}")
        for (node in set){
            val oriEach = Pair(Toolkit.nodePosition(node)[0], Toolkit.nodePosition(node)[1])
            node.setPos(oriEach.first + diffVector.first, oriEach.second + diffVector.second)
        }
    }

    private fun dfsForBatchMovement(node: GridNode,  visited: MutableSet<GridNode>): MutableSet<GridNode> {
        if(!visited.contains(node)){
            visited.add(node)
            val rightCentre = node.getOppositeConnectionCenterNode(Port.RIGHT)
            if (rightCentre != null) visited.addAll(dfsForBatchMovement(rightCentre,visited))
            val leftCentre = node.getOppositeConnectionCenterNode(Port.LEFT)
            if (leftCentre != null) visited.addAll(dfsForBatchMovement(leftCentre,visited))
            val upCentre = node.getOppositeConnectionCenterNode(Port.UP)
            if (upCentre != null) visited.addAll(dfsForBatchMovement(upCentre,visited))
            val downCentre = node.getOppositeConnectionCenterNode(Port.DOWN)
            if (downCentre != null) visited.addAll(dfsForBatchMovement(downCentre,visited))
        }
        return visited
    }

    private fun dfsForBatchRotation(node: GridNode, degreeOfTransfer: Double, visited: MutableSet<GridNode>): MutableSet<GridNode> {
        if(!visited.contains(node)) {
            node.updateRotation(degreeOfTransfer)
            visited.add(node)
            val rightCentre = node.getOppositeConnectionCenterNode(Port.RIGHT)
            if (rightCentre != null) visited.addAll(dfsForBatchRotation(rightCentre, degreeOfTransfer, visited))
            val leftCentre = node.getOppositeConnectionCenterNode(Port.LEFT)
            if (leftCentre != null) visited.addAll(dfsForBatchRotation(leftCentre, degreeOfTransfer, visited))
            val upCentre = node.getOppositeConnectionCenterNode(Port.UP)
            if (upCentre != null) visited.addAll(dfsForBatchRotation(upCentre, degreeOfTransfer, visited))
            val downCentre = node.getOppositeConnectionCenterNode(Port.DOWN)
            if (downCentre != null) visited.addAll(dfsForBatchRotation(downCentre, degreeOfTransfer, visited))
        }
        return visited
    }


    private fun initialConnectNodesInRepresentation(set: MarkedSet<LocallyCoordinatedModelNode>) {
        for (modelNode in set) {
            val rep = graph.getNode<Node>(modelNode.index)
            if(rep is GridNode){
                if (modelNode.right != null) {
                    val another = modelNode.right
                    val anotherRep = graph.getNode<GridNode>(another!!.index)
                    val anotherPort = when (modelNode) {
                        another.right -> Port.RIGHT
                        another.left -> Port.LEFT
                        another.up -> Port.UP
                        another.down -> Port.DOWN
                        else -> null
                    }
//D(First Port) + D(Another Port) + (D(RequiredToRotation) - D(FixedOne)) = 0 rad
// where D(x) means the rad degree of x
                    val toMoveNode =
                            if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                                    population.groupOfNodes[another.belongtoSet]!!.size) rep else anotherRep
                    val toFixedNode = if (toMoveNode == rep) anotherRep else rep
                    val degreeOfRequiredForGridNodeInStandard = toFixedNode.getRotation() - Port.RIGHT.degree - anotherPort!!.degree
//D(transfer) = D(required) - D(originOfToMove)
                    val degreeOfTransfer = degreeOfRequiredForGridNodeInStandard - toMoveNode.getRotation()
                    updateRotationInBatch(toMoveNode, degreeOfTransfer)
                    val destinationPos =
                            toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.RIGHT else anotherPort)

                    movePosInBatch(toMoveNode, destinationPos)
                    val toMovePort = if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                            population.groupOfNodes[another.belongtoSet]!!.size) Port.RIGHT else anotherPort
                    val toFixedPort = if (toMoveNode == rep) Port.RIGHT else anotherPort
                    val nodeInteractFixed = toFixedNode.getInteractPortNode(toFixedPort)
                    val nodeInteractMoved = toMoveNode.getInteractPortNode(toMovePort)
                    if (graph.getEdge<Edge>(toFixedNode.id + toMoveNode.id) == null
                            && graph.getEdge<Edge>(toMoveNode.id + toFixedNode.id) == null)
                        graph.addEdge<Edge>(
                                if (nodeInteractFixed.id < nodeInteractMoved.id) toFixedNode.id + toMoveNode.id
                                else toMoveNode.id + toFixedNode.id, nodeInteractFixed, nodeInteractMoved)
                }
                if (modelNode.left != null) {
                    val another = modelNode.left
                    val anotherRep = graph.getNode<GridNode>(another!!.index)
                    val anotherPort = when (modelNode) {
                        another.right -> Port.RIGHT
                        another.left -> Port.LEFT
                        another.up -> Port.UP
                        another.down -> Port.DOWN
                        else -> null
                    }
//D(First Port) + D(Another Port) + (D(RequiredToRotation) - D(FixedOne)) = 0 rad
// where D(x) means the rad degree of x
                    val toMoveNode =
                            if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                                    population.groupOfNodes[another.belongtoSet]!!.size) rep else anotherRep
                    val toFixedNode = if (toMoveNode == rep) anotherRep else rep
                    val degreeOfRequiredForGridNodeInStandard = toFixedNode.getRotation() - Port.LEFT.degree - anotherPort!!.degree
//D(transfer) = D(required) - D(originOfToMove)
                    val degreeOfTransfer = degreeOfRequiredForGridNodeInStandard - toMoveNode.getRotation()
                    updateRotationInBatch(toMoveNode, degreeOfTransfer)
                    val destinationPos =
                            toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.LEFT else anotherPort)
                    movePosInBatch(toMoveNode, destinationPos)
                    val toMovePort = if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                            population.groupOfNodes[another.belongtoSet]!!.size) Port.LEFT else anotherPort
                    val toFixedPort = if (toMoveNode == rep) Port.LEFT else anotherPort
                    val nodeInteractFixed = toFixedNode.getInteractPortNode(toFixedPort)
                    val nodeInteractMoved = toMoveNode.getInteractPortNode(toMovePort)
                    if (graph.getEdge<Edge>(toFixedNode.id + toMoveNode.id) == null
                            && graph.getEdge<Edge>(toMoveNode.id + toFixedNode.id) == null)
                        graph.addEdge<Edge>(
                                if (nodeInteractFixed.id < nodeInteractMoved.id) toFixedNode.id + toMoveNode.id
                                else toMoveNode.id + toFixedNode.id, nodeInteractFixed, nodeInteractMoved)

                }
                if (modelNode.up != null) {
                    val another = modelNode.up
                    val anotherRep = graph.getNode<GridNode>(another!!.index)
                    val anotherPort = when (modelNode) {
                        another.right -> Port.RIGHT
                        another.left -> Port.LEFT
                        another.up -> Port.UP
                        another.down -> Port.DOWN
                        else -> null
                    }
//D(First Port) + D(Another Port) + (D(RequiredToRotation) - D(FixedOne)) = 0 rad
// where D(x) means the rad degree of x
                    val toMoveNode =
                            if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                                    population.groupOfNodes[another.belongtoSet]!!.size) rep else anotherRep
                    val toFixedNode = if (toMoveNode == rep) anotherRep else rep
                    val degreeOfRequiredForGridNodeInStandard = toFixedNode.getRotation() - Port.UP.degree - anotherPort!!.degree
//D(transfer) = D(required) - D(originOfToMove)
                    val degreeOfTransfer = degreeOfRequiredForGridNodeInStandard - toMoveNode.getRotation()
                    updateRotationInBatch(toMoveNode, degreeOfTransfer)
                    val destinationPos =
                            toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.UP else anotherPort)
                    movePosInBatch(toMoveNode, destinationPos)
                    val toMovePort = if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                            population.groupOfNodes[another.belongtoSet]!!.size) Port.UP else anotherPort
                    val toFixedPort = if (toMoveNode == rep) Port.UP else anotherPort

                    val nodeInteractFixed = toFixedNode.getInteractPortNode(toFixedPort)
                    val nodeInteractMoved = toMoveNode.getInteractPortNode(toMovePort)
                    if (graph.getEdge<Edge>(toFixedNode.id + toMoveNode.id) == null
                            && graph.getEdge<Edge>(toMoveNode.id + toFixedNode.id) == null)
                        graph.addEdge<Edge>(
                                if (nodeInteractFixed.id < nodeInteractMoved.id) toFixedNode.id + toMoveNode.id
                                else toMoveNode.id + toFixedNode.id, nodeInteractFixed, nodeInteractMoved)
                }
                if (modelNode.down != null) {
                    val another = modelNode.down
                    val anotherRep = graph.getNode<GridNode>(another!!.index)
                    val anotherPort = when (modelNode) {
                        another.right -> Port.RIGHT
                        another.left -> Port.LEFT
                        another.up -> Port.UP
                        another.down -> Port.DOWN
                        else -> null
                    }
//D(First Port) + D(Another Port) + (D(RequiredToRotation) - D(FixedOne)) = 0 rad
// where D(x) means the rad degree of x
                    val toMoveNode =
                            if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                                    population.groupOfNodes[another.belongtoSet]!!.size) rep else anotherRep
                    val toMovePort = if (population.groupOfNodes[modelNode.belongtoSet]!!.size <
                            population.groupOfNodes[another.belongtoSet]!!.size) Port.DOWN else anotherPort
                    val toFixedNode = if (toMoveNode == rep) anotherRep else rep
                    val toFixedPort = if (toMoveNode == rep) Port.DOWN else anotherPort
                    val degreeOfRequiredForGridNodeInStandard = toFixedNode.getRotation() - Port.DOWN.degree - anotherPort!!.degree
//D(transfer) = D(required) - D(originOfToMove)
                    val degreeOfTransfer = degreeOfRequiredForGridNodeInStandard - toMoveNode.getRotation()
                    updateRotationInBatch(toMoveNode, degreeOfTransfer)
                    val destinationPos =
                            toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.DOWN else anotherPort)
                    movePosInBatch(toMoveNode, destinationPos)
                    val nodeInteractFixed = toFixedNode.getInteractPortNode(toFixedPort!!)
                    val nodeInteractMoved = toMoveNode.getInteractPortNode(toMovePort!!)
                    if (graph.getEdge<Edge>(toFixedNode.id + toMoveNode.id) == null
                            && graph.getEdge<Edge>(toMoveNode.id + toFixedNode.id) == null)
                        graph.addEdge<Edge>(
                                if (nodeInteractFixed.id < nodeInteractMoved.id) toFixedNode.id + toMoveNode.id
                                else toMoveNode.id + toFixedNode.id, nodeInteractFixed, nodeInteractMoved)
                }
            }

        }
    }

    override fun nextEvents(): Boolean {
        val (res, firstPairOfNode, secondPairOfNode) = population.interact()
        val isInteracted = res.first
        val shouldActivate = res.second
        if (isInteracted) {
            count++
            countOfSelectWithoutInteraction = 0
            val firstModelNode = firstPairOfNode.first
            val firstModelPort = firstPairOfNode.second

            val secondModelNode = secondPairOfNode.first
            val secondModelPort = secondPairOfNode.second

            val firstRep = graph.getNode<Node>("${firstModelNode.index}") as GridNode
            val secondRep = graph.getNode<Node>("${secondModelNode.index}") as GridNode

            firstRep.setAttribute("ui.class", "importantMarked")
            secondRep.setAttribute("ui.class", "importantMarked")
            firstRep.setAttribute("ui.label",firstModelNode.state.currentState)
            secondRep.setAttribute("ui.label",secondModelNode.state.currentState)

            val getFirstInteractNodeRep = firstRep.getInteractPortNode(firstModelPort)
            val getSecondInteractNodeRep = secondRep.getInteractPortNode(secondModelPort)

            val firstInteractEdge = firstRep.getInteractEdge(firstModelPort)
            val secondInteractEdge = secondRep.getInteractEdge(secondModelPort)
            firstInteractEdge.setAttribute("ui.class", "marked")
            secondInteractEdge.setAttribute("ui.class", "marked")
            sleepWith(1000)

            if (shouldActivate) {
                val secondNewPos = firstRep.getOppositeConnectionCenterCoordinate(firstModelPort)
                movePosInBatch(secondRep, secondNewPos)
                sleepWith(1000)
                graph.addEdge<Edge>(
                        if (getFirstInteractNodeRep.id < getSecondInteractNodeRep.id) getFirstInteractNodeRep.id + getSecondInteractNodeRep.id else getSecondInteractNodeRep.id + getFirstInteractNodeRep.id,
                        getFirstInteractNodeRep, getSecondInteractNodeRep
                )
                sleepWith(1000)
                firstInteractEdge.removeAttribute("ui.class")
                secondInteractEdge.removeAttribute("ui.class")
                firstRep.setAttribute("ui.class", "important")
                secondRep.setAttribute("ui.class", "important")
            } else {
                graph.removeEdge<Edge>(getFirstInteractNodeRep, getSecondInteractNodeRep)
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

class MySingleNode(graph: AbstractGraph, id: String) : SingleNode(graph, id)