package model.shared

import koma.end
import koma.extensions.get
import koma.extensions.map
import koma.mat
import koma.matrix.Matrix
import koma.pow
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.roundToInt

enum class Port(val degree: Int) {
    UP(0), RIGHT(90), DOWN(180), LEFT(270)
}

class Coordinate private constructor(val x: Int, val y: Int) {
    companion object {
        fun of(x: Int, y: Int): Coordinate {
            return Coordinate(x = x, y = y)
        }
    }

    fun coordinateShiftInCopy(oriX: Int,oriY: Int,direction: Port): Coordinate {
        return when (direction) {
            Port.UP -> of(oriX, oriY + 1)
            Port.DOWN -> of(oriX, oriY - 1)
            Port.LEFT -> of(oriX - 1, oriY)
            Port.RIGHT -> of(oriX + 1, oriY)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Coordinate) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

class LocallyCoordinatedModelNode(x: Int = 0,
                                  y: Int = 0,
                                  state: State, index: Int,var rotationDegree: Double = 0.0) : ModelNode(state = state, index = index) {
    var up: LocallyCoordinatedModelNode? = null
    var down: LocallyCoordinatedModelNode? = null
    var left: LocallyCoordinatedModelNode? = null
    var right: LocallyCoordinatedModelNode? = null
    var coordinate = Coordinate.of(x, y)
    var belongtoSet: Int? = null
    fun getPort(port: Port): LocallyCoordinatedModelNode? = when(port){
        Port.UP -> this.up
        Port.DOWN -> this.down
        Port.LEFT -> this.left
        Port.RIGHT -> this.right
    }
    fun hasConnectionWith(thisPort: Port, node: LocallyCoordinatedModelNode): Boolean{
        return this.getPort(thisPort) == node
    }

    companion object {
        val random = Random()
        private fun createNode(symbols: Set<String>, initialState: String, index: Int): LocallyCoordinatedModelNode {
            if (symbols.isEmpty() || !symbols.contains(initialState))
                throw IllegalArgumentException()
            return LocallyCoordinatedModelNode(state = State.createState(symbols, initialState),rotationDegree = random.nextInt(360).toDouble(),index = index)
        }

        fun createMultipleNodes(symbols: Set<String>,
                                initialStates: Map<String, Int>): MutableList<LocallyCoordinatedModelNode> {
            if (symbols.isEmpty() || !symbols.containsAll(initialStates.keys))
                throw IllegalArgumentException()
            val result = ArrayList<LocallyCoordinatedModelNode>()
            var index = 0
            initialStates.entries.forEach({ entry ->
                repeat(
                        entry.value,
                        { _ -> result.add(createNode(symbols, entry.key, index++)) }
                )
            })
            return result
        }

        private fun relativeDistance(nodeA: LocallyCoordinatedModelNode,nodeB: LocallyCoordinatedModelNode): Double{
            return ((nodeA.coordinate.x - nodeB.coordinate.x).pow(2) + (nodeA.coordinate.y - nodeB.coordinate.y).pow(2)).pow(0.5)
        }

        private fun hasConnectionWith(nodeA: LocallyCoordinatedModelNode,nodeB: LocallyCoordinatedModelNode): Boolean{
            if (nodeA.left == nodeB || nodeA.right == nodeB || nodeA.down == nodeB || nodeA.up == nodeB ) return true
            return false
        }

        fun canActive(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean {
            if (nodeA == nodeB) return false
            val toInteractA = when (portA) {
                Port.UP -> nodeA.up
                Port.DOWN -> nodeA.down
                Port.LEFT -> nodeA.left
                Port.RIGHT -> nodeA.right
            }
            val toInteractB = when (portB) {
                Port.UP -> nodeB.up
                Port.DOWN -> nodeB.down
                Port.LEFT -> nodeB.left
                Port.RIGHT -> nodeB.right
            }
            if (toInteractA != null || toInteractB != null) return false
            if (hasConnectionWith(nodeA,nodeB)) return false
            if (nodeA.belongtoSet!= null && nodeA.belongtoSet == nodeB.belongtoSet){
                if (Math.abs(portA.degree - nodeA.rotationDegree - (portB.degree - nodeB.rotationDegree)).toInt() % 360 != 180){
                    System.err.println("REJECTED BECAUSE DEGREE DIFFERENCE IS NOT 180, the portA has degree ${portA.degree},the portB has degree ${portB.degree}")
                    return false
                }
                nodeA.coordinate = Coordinate.of(0, 0)
                populateCoordinate(nodeA)
                if(relativeDistance(nodeA,nodeB) != 1.0){
                    System.err.println("REJECTED BECAUSE DISTANCE IS NOT 1, the nodeA has coordinate (${nodeA.coordinate.x}, ${nodeA.coordinate.y}) ,the nodeB has coordinate (${nodeB.coordinate.x}, ${nodeB.coordinate.y})")
                    return false
                }else{
                    System.err.println("Accept distance because the nodeA has coordinate (${nodeA.coordinate.x}, ${nodeA.coordinate.y}) ,the nodeB has degree (${nodeB.coordinate.x}, ${nodeB.coordinate.y})")

                }
                return true
            }else{
                nodeA.coordinate = Coordinate.of(0, 0)
                nodeB.coordinate = nodeA.coordinate.coordinateShiftInCopy(0,0,portA)
                val populationACoordinates = populateAndRotateNodes(nodeA, 0.0)
                        .map { node -> Pair(node.coordinate,node)}.toMap()
                nodeA.coordinate = Coordinate.of(0, 0)
                nodeB.coordinate = nodeA.coordinate.coordinateShiftInCopy(0,0,portA)
                val populationBCoordinates =
                        populateAndRotateNodes(nodeB, 180 - Math.abs(portA.degree - portB.degree).toDouble())
                                .map { node -> Pair(node.coordinate,node)}.toMap()
                for (key in populationACoordinates.keys){
                    if(populationBCoordinates.containsKey(key) && populationBCoordinates[key] != populationACoordinates[key]){
                        return false
                    }
                }
                return true
            }
//       0
//  270     90
//      180

        }

        fun canInactive(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean {
            if (nodeA == nodeB) return false
            if (nodeA.belongtoSet != nodeB.belongtoSet) return false
            val toSeparateA = when (portA) {
                Port.UP -> nodeA.up
                Port.DOWN -> nodeA.down
                Port.LEFT -> nodeA.left
                Port.RIGHT -> nodeA.right
            }
            val toSeparateB = when (portB) {
                Port.UP -> nodeB.up
                Port.DOWN -> nodeB.down
                Port.LEFT -> nodeB.left
                Port.RIGHT -> nodeB.right
            }
            if (toSeparateA != nodeB) return false
            if (toSeparateB != nodeA) return false
            return true
        }

        private fun populateAndRotateNodes(origin: LocallyCoordinatedModelNode, degree: Double): Set<LocallyCoordinatedModelNode> {
            return rotateAllNodes(origin, degree, populateCoordinate(origin))
        }
        private fun populateCoordinate(origin: LocallyCoordinatedModelNode):
                Set<LocallyCoordinatedModelNode> {
            val queue = LinkedList<LocallyCoordinatedModelNode>()
            queue.offer(origin)
            val hasVisitedIndexSet = HashSet<LocallyCoordinatedModelNode>()
            while (queue.isNotEmpty()) {
                val current = queue.pop()
                if (current.left != null && !hasVisitedIndexSet.contains(current.left!!)) {
                    current.left!!.coordinate = current.coordinate.coordinateShiftInCopy(current.coordinate.x,current.coordinate.y,Port.LEFT)
                    queue.offer(current.left)
                }
                if (current.right != null && !hasVisitedIndexSet.contains(current.right!!)) {
                    current.right!!.coordinate = current.coordinate.coordinateShiftInCopy(current.coordinate.x,current.coordinate.y,Port.RIGHT)
                    queue.offer(current.right)
                }
                if (current.up != null && !hasVisitedIndexSet.contains(current.up!!)) {
                    current.up!!.coordinate = current.coordinate.coordinateShiftInCopy(current.coordinate.x,current.coordinate.y,Port.UP)
                    queue.offer(current.up)
                }
                if (current.down != null && !hasVisitedIndexSet.contains(current.down!!)) {
                    current.down!!.coordinate = current.coordinate.coordinateShiftInCopy(current.coordinate.x,current.coordinate.y,Port.DOWN)
                    queue.offer(current.down)
                }
                hasVisitedIndexSet.add(current)
            }
            return hasVisitedIndexSet
        }

        private fun rotateAllNodes(origin: LocallyCoordinatedModelNode, degree: Double,
                                   nodes: Set<LocallyCoordinatedModelNode>): Set<LocallyCoordinatedModelNode> {
            origin.rotationDegree += degree
            origin.rotationDegree %= 360
            val rad = degree * Math.PI / 180
            // Ref: https://www.zhihu.com/question/52027040
            // IN-CLOCK direction rotation
            val transferMatM = mat[
                    Math.cos(rad), Math.sin(rad) end
                            0 - Math.sin(rad), Math.cos(rad)
            ]
            val transferMatB = mat[
                    (1 - Math.cos(rad)) * origin.coordinate.x - origin.coordinate.y * Math.sin(rad),
                    (1 - Math.cos(rad)) * origin.coordinate.y + origin.coordinate.x * Math.sin(rad)]
            for (theNode in nodes) {
                val transformed =
                        transferMatM * (mat[theNode.coordinate.x, theNode.coordinate.y].T).removeNegativeZeros() + transferMatB.T
                theNode.coordinate = Coordinate.of(transformed[0, 0].roundToInt(), transformed[1, 0].roundToInt())
            }

            return nodes
        }


        private fun Matrix<Double>.removeNegativeZeros(): Matrix<Double> {
            return this.map { it -> if (it == -0.0) Math.abs(it) else it }
        }

    }
}

