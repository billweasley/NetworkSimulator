package utils

import koma.end
import koma.extensions.get
import koma.extensions.map
import koma.mat
import koma.matrix.Matrix
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

    fun coordinateShiftInCopy(direction: Port): Coordinate {
        return when (direction) {
            Port.UP -> Coordinate.of(x, y + 1)
            Port.DOWN -> Coordinate.of(x, y - 1)
            Port.LEFT -> Coordinate.of(x - 1, y)
            Port.RIGHT -> Coordinate.of(x + 1, y)
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

class LocallyCoordinatedModelNode(x: Int,
                                  y: Int,
                                  state: State, index: Int) : ModelNode(state = state, index = index) {
    var up: LocallyCoordinatedModelNode? = null
    var down: LocallyCoordinatedModelNode? = null
    var left: LocallyCoordinatedModelNode? = null
    var right: LocallyCoordinatedModelNode? = null
    var coordinate = Coordinate.of(x, y)

    companion object {
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

            nodeA.coordinate = Coordinate.of(0, 0)
            nodeB.coordinate = nodeA.coordinate.coordinateShiftInCopy(portA)

            val populationACoordinates = populateAndRotateNodes(nodeA, 0)
                    .map { node -> node.coordinate }.toSet()
            val populationBCoordinates =
                    populateAndRotateNodes(nodeB, 180 - Math.abs(portA.degree - portB.degree))
                            .map { node -> node.coordinate }.toSet()
            return populationACoordinates.intersect(populationBCoordinates).isEmpty()

//       0
//  270     90
//      180

        }

        fun canInactive(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean {
            if (nodeA == nodeB) return false
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

        private fun populateAndRotateNodes(origin: LocallyCoordinatedModelNode, degree: Int): Set<LocallyCoordinatedModelNode> {
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
                    current.left!!.coordinate = current.coordinate.coordinateShiftInCopy(Port.LEFT)
                    queue.offer(current.left)
                }
                if (current.right != null && !hasVisitedIndexSet.contains(current.right!!)) {
                    current.right!!.coordinate = current.coordinate.coordinateShiftInCopy(Port.RIGHT)
                    queue.offer(current.right)
                }
                if (current.up != null && !hasVisitedIndexSet.contains(current.up!!)) {
                    current.up!!.coordinate = current.coordinate.coordinateShiftInCopy(Port.UP)
                    queue.offer(current.up)
                }
                if (current.down != null && !hasVisitedIndexSet.contains(current.down!!)) {
                    current.down!!.coordinate = current.coordinate.coordinateShiftInCopy(Port.DOWN)
                    queue.offer(current.down)
                }
                hasVisitedIndexSet.add(current)
            }
            return hasVisitedIndexSet
        }

        private fun rotateAllNodes(origin: LocallyCoordinatedModelNode, degree: Int,
                                   nodes: Set<LocallyCoordinatedModelNode>): Set<LocallyCoordinatedModelNode> {
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

        // IN-CLOCK direction rotation
        private fun rotateOneNode(target: LocallyCoordinatedModelNode, degree: Int, centralCoordinate: LocallyCoordinatedModelNode) {
            val rad = degree * Math.PI / 180
            val transferMat = mat[
                    Math.cos(rad), Math.sin(rad) end
                            0 - Math.sin(rad), Math.cos(rad)
                    ]
            val centralCoordinateMat = mat[centralCoordinate.coordinate.x, centralCoordinate.coordinate.y]
            val originMat = mat[target.coordinate.x, target.coordinate]
            val transformed =
                    (transferMat * (originMat - centralCoordinateMat).T).removeNegativeZeros() + centralCoordinateMat.T
            target.coordinate = Coordinate.of(transformed[0, 0].roundToInt(), transformed[1, 0].roundToInt())
        }

        private fun Matrix<Double>.removeNegativeZeros(): Matrix<Double> {
            return this.map { it -> if (it == -0.0) Math.abs(it) else it }
        }
    }


}


