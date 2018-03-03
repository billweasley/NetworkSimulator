package utils

class LocallyCoordinatedEdge(nodeA: LocallyCoordinatedNode, nodeB: LocallyCoordinatedNode) {
    init {
        if (nodeA == nodeB) throw IllegalArgumentException("Node cannot connect with itself.")
        val set = setOf(nodeA, nodeB)
    }
}