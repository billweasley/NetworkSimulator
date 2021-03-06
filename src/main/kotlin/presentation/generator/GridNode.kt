package presentation.generator

import koma.end
import koma.extensions.get
import koma.extensions.map
import koma.mat
import koma.matrix.Matrix
import model.shared.Port
import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.AbstractGraph
import org.graphstream.graph.implementations.SingleNode

class GridNode(graph: AbstractGraph?, id: String?,
               initialX: Double,
               initialY: Double,
               var upPort: Node,
               var downPort: Node,
               var leftPort: Node,
               var rightPort: Node, initialRotation: Double = 0.0) : SingleNode(graph, id) {
    private val portLength = 0.3
    private val edgeLength = 1
    private var rotation = initialRotation

    init {
        this.setAttribute("xy",initialX,initialY)
        graph?.addEdge<Edge>(this.id + " | u", this,upPort)
        graph?.addEdge<Edge>(this.id + " | d", this,downPort)
        graph?.addEdge<Edge>(this.id + " | r", this,rightPort)
        graph?.addEdge<Edge>(this.id + " | l", this,leftPort)
        bindNodeToPort(upPort,Port.UP)
        bindNodeToPort(downPort,Port.DOWN)
        bindNodeToPort(leftPort,Port.LEFT)
        bindNodeToPort(rightPort,Port.RIGHT)
    }

     fun centeredRotate(degree: Double, x: Double, y: Double): Pair<Double,Double> {
        val rad = degree * Math.PI / 180
        // Ref: https://www.zhihu.com/question/52027040
        // IN-CLOCK direction rotation
        val transferMatM = mat[
                Math.cos(rad), Math.sin(rad) end
                        0 - Math.sin(rad), Math.cos(rad)
        ]
        val ori = Toolkit.nodePosition(this)
        val oriX = ori[0]
        val oriY = ori[1]
        val transferMatB = mat[
                (1 - Math.cos(rad)) * oriX - oriY * Math.sin(rad),
                (1 - Math.cos(rad)) * oriY + oriX * Math.sin(rad)
        ]
        val transferred = (transferMatM * mat[x,y].T).removeNegativeZeros() + transferMatB.T
        return Pair(transferred[0,0],transferred[1,0])
    }

    private fun Matrix<Double>.removeNegativeZeros(): Matrix<Double> {
        return this.map { it -> if (it == -0.0) Math.abs(it) else it }
    }
    fun getRotation(): Double{
        return rotation
    }

    fun getOppositeConnectionCenterCoordinate(port: Port): Pair<Double,Double>{
        val pos = Toolkit.nodePosition(this)
        return when(port){
            Port.UP -> centeredRotate(rotation, pos[0], (pos[1] + portLength * 2 + edgeLength))
            Port.DOWN -> centeredRotate(rotation, pos[0], (pos[1] - portLength  * 2 - edgeLength))
            Port.RIGHT -> centeredRotate(rotation, (pos[0] + portLength * 2 + edgeLength), pos[1])
            Port.LEFT -> centeredRotate(rotation,(pos[0] - portLength * 2 - edgeLength), pos[1])
        }
    }

    fun getOppositeConnectionCenterNode(port: Port): GridNode?{
        val interactNodeThisSide = getInteractPortNode(port) //self interact node
        val neighbors = interactNodeThisSide.getNeighborNodeIterator<Node>().asSequence()
        //this - interactNodeThisSide - interactNodeAnotherSide - another
        for(neighbor in neighbors){
            if (neighbor != this){
                val anotherNeighbors = neighbor.getNeighborNodeIterator<Node>().asSequence()
                for (neighborAnother in anotherNeighbors){
                    if (neighborAnother != interactNodeThisSide && neighborAnother is GridNode) return neighborAnother
                }
            }
        }
        return null
    }

    fun getInteractPortNode(port: Port): Node = when(port){
        Port.DOWN -> downPort
        Port.RIGHT -> rightPort
        Port.UP -> upPort
        Port.LEFT -> leftPort
    }

    fun getInteractEdge(port: Port): Edge = when(port){
        Port.DOWN -> graph.getEdge(this.id + " | d")
        Port.RIGHT -> graph.getEdge(this.id + " | r")
        Port.UP -> graph.getEdge(this.id + " | u")
        Port.LEFT -> graph.getEdge(this.id + " | l")
    }

    fun updateRotation(degree: Double){
        rotation += degree
        rotation %= 360
        val pos = Toolkit.nodePosition(this)
        val rotatedUp = centeredRotate(rotation,pos[0], pos[1] + portLength)
        upPort.setAttribute("xy",rotatedUp.first,rotatedUp.second)
        val rotatedDown = centeredRotate(rotation,pos[0], pos[1] - portLength)
        downPort.setAttribute("xy",rotatedDown.first,rotatedDown.second)
        val rotatedRight = centeredRotate(rotation,pos[0] + portLength, pos[1])
        rightPort.setAttribute("xy",rotatedRight.first,rotatedRight.second)
        val rotatedLeft = centeredRotate(rotation,pos[0] - portLength, pos[1])
        leftPort.setAttribute("xy",rotatedLeft.first,rotatedLeft.second)
    }

    fun setPos(x: Double, y: Double){
        this.setAttribute("xy",x,y)
        val pos = Toolkit.nodePosition(this)
        val rotatedUp = centeredRotate(rotation,pos[0], pos[1] + portLength)
        upPort.setAttribute("xy",rotatedUp.first,rotatedUp.second)
        val rotatedDown = centeredRotate(rotation,pos[0], pos[1] - portLength)
        downPort.setAttribute("xy",rotatedDown.first,rotatedDown.second)
        val rotatedRight = centeredRotate(rotation,pos[0] + portLength, pos[1])
        rightPort.setAttribute("xy",rotatedRight.first,rotatedRight.second)
        val rotatedLeft = centeredRotate(rotation,pos[0] - portLength, pos[1])
        leftPort.setAttribute("xy",rotatedLeft.first,rotatedLeft.second)
    }



    fun bindNodeToPort(node: Node, port: Port) {
        val pos = Toolkit.nodePosition(this)
        when(port){
            Port.UP -> {
                upPort = node
                graph.removeEdge<Edge>(this.id + " | u")
                val rotated = centeredRotate(rotation,pos[0], pos[1] + portLength)
                upPort.setAttribute("xy",rotated.first,rotated.second)
                graph.addEdge<Edge>(this.id + " | u", this,upPort)
                upPort.addAttribute("ui.label","U")

            }

            Port.DOWN -> {
                downPort = node
                graph.removeEdge<Edge>(this.id + " | d")
                val rotated = centeredRotate(rotation,pos[0], pos[1] - portLength)
                downPort.setAttribute("xy",rotated.first,rotated.second)
                graph.addEdge<Edge>(this.id + " | d", this,downPort)
                downPort.addAttribute("ui.label","D")

            }
            Port.RIGHT -> {
                rightPort = node
                graph.removeEdge<Edge>(this.id + " | r")
                val rotated = centeredRotate(rotation,pos[0] + portLength, pos[1] )
                rightPort.setAttribute("xy",rotated.first,rotated.second)
                graph.addEdge<Edge>(this.id + " | r", this,rightPort)
                rightPort.addAttribute("ui.label","R")

            }
            Port.LEFT -> {
                leftPort = node
                graph.removeEdge<Edge>(this.id + " | l")
                val rotated = centeredRotate(rotation,pos[0] - portLength, pos[1] )
                leftPort.setAttribute("xy",rotated.first,rotated.second)
                graph.addEdge<Edge>(this.id + " | l", this,leftPort)
                leftPort.addAttribute("ui.label","L")
            }
        }

    }

}