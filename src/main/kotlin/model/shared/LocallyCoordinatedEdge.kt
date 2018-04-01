package model.shared

class LocallyCoordinatedEdge(nodeA: LocallyCoordinatedModelNode, nodeB: LocallyCoordinatedModelNode) {
    init {
        if (nodeA == nodeB) throw IllegalArgumentException("ModelNode cannot connect with itself.")
        val set = setOf(nodeA, nodeB)
    }
}