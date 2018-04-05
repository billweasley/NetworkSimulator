package presentation.generator

import model.population.gridNetworkConstruction.GridNetworkConstructingPopulation
import model.population.gridNetworkConstruction.MarkedSet
import model.shared.LocallyCoordinatedModelNode
import model.shared.Port
import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.implementations.AbstractGraph
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.graph.implementations.SingleNode
import org.graphstream.stream.SourceBase
import java.util.*

fun main(args: Array<String>){
   // GridNetworkGenerator().display()
}

class GridNetworkGenerator(var population:GridNetworkConstructingPopulation,
                           val maxTimes: Long = 1000000,
                           val fastRes: Boolean= false,
                           val preExecutedSteps: Int = 0,
                           nameOfPopulation: String = "",
                           val graph: SingleGraph = SingleGraph(nameOfPopulation),
                           private val styleSheet: String = "node {fill-mode: none; fill-color: rgba(255,0,0,0);text-background-mode:plain;text-alignment:right;text-size: 10px;size: 5px;}" +
                                   "node.important {fill-mode: plain; fill-color: black;text-size: 30px; size: 10px;}" +
                                   "node.marked {fill-color: red;}" +
                                   "edge.marked {fill-color: red;}") : SourceBase(), SimulationGenerator {

    override var countOfSelectWithoutInteraction: Int = 0
    override val terminateTheshold: Int = 1000
    override val requireLayoutAlgorithm = true
    private val random = Random()
    private var count = 0
    init {
        if(fastRes && preExecutedSteps < 0)
            throw IllegalArgumentException("The fast forwarding requires a non-negative value.")
        graph.addAttribute("ui.stylesheet", styleSheet)
        addSink(graph)
    }

    private fun addNodeOnGraph(id: String,x: Double, y: Double, degreeOfRotation: Double = 0.0){
        graph.setNodeFactory({str, _ ->
            MySingleNode(graph as AbstractGraph, str)
        })
        val up = graph.addNode<SingleNode>("$id | u")
        val down = graph.addNode<SingleNode>("$id | d")
        val left = graph.addNode<SingleNode>("$id | l")
        val right = graph.addNode<SingleNode>("$id | r")
        graph.setNodeFactory({str, graph ->
            val res = GridNode(graph as AbstractGraph,str,x,y,up,down,left,right,degreeOfRotation)
            res.setAttribute("ui.class","important")
            res
        })
        graph.addNode<GridNode>(id)
    }


    @Synchronized
    override fun shouldTerminate(): Boolean{
        return countOfSelectWithoutInteraction > terminateTheshold
    }
    fun display(){
        this.begin()
        val viewer  = graph.display(false)
        viewer.defaultView.camera.setAutoFitView(true)
        while (count < maxTimes){
            this.nextEvents()
        }
    }
    private fun sleepWith(millisecond: Long){
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
        if (fastRes){
            for(i in 0..preExecutedSteps){
                val result = population.interact()
            }
        }
        val numOfNode = population.numOfNode()
        val setOfCoordinate = mutableSetOf<Pair<Int,Int>>()
        for (i in 0..(numOfNode - 1)){
            var pair = Pair(random.nextInt(numOfNode),random.nextInt(numOfNode))
            while(setOfCoordinate.contains(pair))
                pair = Pair(random.nextInt(numOfNode),random.nextInt(numOfNode))
            val theNode = population.nodes[i]
            addNodeOnGraph(theNode.index.toString(),pair.first.toDouble(),pair.second.toDouble())
        }
        for(group in population.groupOfNodes.values){
            initialConnectNodesInRepresentation(group)
        }
    }
    private fun updateRotationInBatch(toMoveNode: GridNode,degreeOfTransfer: Double){
        dfsForBatchRotation(toMoveNode,degreeOfTransfer, mutableSetOf())
    }

    private fun movePosInBatch(toMoveNode: GridNode,newPos: Pair<Double,Double>){
        val ori = Pair(Toolkit.nodePosition(toMoveNode)[0],Toolkit.nodePosition(toMoveNode)[1])
        val diffVector = Pair(newPos.first - ori.first, newPos.second - ori.second)
        dfsForBatchMovement(toMoveNode,diffVector, mutableSetOf())
    }
    private fun dfsForBatchMovement(node: GridNode, diffVector: Pair<Double,Double>,visited: MutableSet<GridNode>): MutableSet<GridNode>{
        val ori = Pair(Toolkit.nodePosition(node)[0],Toolkit.nodePosition(node)[1])
        node.setPos(ori.first + diffVector.first, ori.second + diffVector.second)
        visited.add(node)
        node.downPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchMovement(neighbor,diffVector,visited))
            }
        })
        node.upPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchMovement(neighbor,diffVector,visited))
            }
        })
        node.leftPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchMovement(neighbor,diffVector,visited))
            }
        })
        node.rightPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchMovement(neighbor,diffVector,visited))
            }
        })
        return visited
    }

    private fun dfsForBatchRotation(node: GridNode, degreeOfTransfer: Double,visited: MutableSet<GridNode>): MutableSet<GridNode>{
        node.updateRotation(degreeOfTransfer)
        visited.add(node)
        node.downPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchRotation(neighbor,degreeOfTransfer,visited))
            }
        })
        node.upPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchRotation(neighbor,degreeOfTransfer,visited))
            }
        })
        node.leftPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchRotation(neighbor,degreeOfTransfer,visited))
            }
        })
        node.rightPort.getNeighborNodeIterator<GridNode>().forEachRemaining({neighbor ->
            if (!visited.contains(neighbor) && neighbor != node){
                visited.addAll(dfsForBatchRotation(neighbor,degreeOfTransfer,visited))
            }
        })
        return visited
    }


    private fun initialConnectNodesInRepresentation(set: MarkedSet<LocallyCoordinatedModelNode>){
       for (modelNode in set){
           val rep = graph.getNode<GridNode>(modelNode.index)
           if (modelNode.right != null){
               val another = modelNode.right
               val anotherRep = graph.getNode<GridNode>(another!!.index)
               val anotherPort = when(modelNode){
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
               updateRotationInBatch(toMoveNode,degreeOfTransfer)
               val destinationPos =
                       toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.RIGHT else anotherPort)
               movePosInBatch(toMoveNode,destinationPos)
           }
           if (modelNode.left != null){
               val another = modelNode.left
               val anotherRep = graph.getNode<GridNode>(another!!.index)
               val anotherPort = when(modelNode){
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
               updateRotationInBatch(toMoveNode,degreeOfTransfer)
               val destinationPos =
                       toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.LEFT else anotherPort)
               movePosInBatch(toMoveNode,destinationPos)

           }
           if (modelNode.up != null){
               val another = modelNode.up
               val anotherRep = graph.getNode<GridNode>(another!!.index)
               val anotherPort = when(modelNode){
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
               updateRotationInBatch(toMoveNode,degreeOfTransfer)
               val destinationPos =
                       toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.UP else anotherPort)
               movePosInBatch(toMoveNode,destinationPos)
           }
           if(modelNode.down != null){
               val another = modelNode.down
               val anotherRep = graph.getNode<GridNode>(another!!.index)
               val anotherPort = when(modelNode){
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
               val degreeOfRequiredForGridNodeInStandard = toFixedNode.getRotation() - Port.DOWN.degree - anotherPort!!.degree
               //D(transfer) = D(required) - D(originOfToMove)
               val degreeOfTransfer = degreeOfRequiredForGridNodeInStandard - toMoveNode.getRotation()
               updateRotationInBatch(toMoveNode,degreeOfTransfer)
               val destinationPos =
                       toFixedNode.getOppositeConnectionCenterCoordinate(if (toFixedNode == rep) Port.DOWN else anotherPort)
               movePosInBatch(toMoveNode,destinationPos)
           }
       }
    }
    override fun nextEvents(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class MySingleNode( graph: AbstractGraph,  id:String): SingleNode(graph,id)