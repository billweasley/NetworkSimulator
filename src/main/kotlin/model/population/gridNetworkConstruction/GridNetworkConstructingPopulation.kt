package model.population.gridNetworkConstruction

import model.population.LinkedPopulation
import scheduler.Scheduler
import utils.LocallyCoordinatedModelNode
import utils.ModelNode
import utils.Port

class GridNetworkConstructingPopulation(scheduler: Scheduler, nodes: List<LocallyCoordinatedModelNode>) : LinkedPopulation {
    override val nodes: List<LocallyCoordinatedModelNode> = nodes

    private lateinit var groupOfNodes: List<Set<LocallyCoordinatedModelNode>>
    private var numOfActiveEdges = 0


    private fun activeConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB:LocallyCoordinatedModelNode, portB:Port): Boolean{
        if (nodeA == nodeB) return false
        if (!LocallyCoordinatedModelNode.canActive(nodeA,portA,nodeB,portB)) return false
        when (portA) {
            Port.UP -> nodeA.up = nodeB
            Port.DOWN -> nodeA.down = nodeB
            Port.LEFT -> nodeA.left = nodeB
            Port.RIGHT -> nodeA.right = nodeB
        }
        when (portB) {
            Port.UP -> nodeB.up = nodeA
            Port.DOWN -> nodeB.down = nodeA
            Port.LEFT -> nodeB.left = nodeA
            Port.RIGHT -> nodeB.right = nodeA
        }
        numOfActiveEdges++
        TODO("MERGE TWO GRAPH")
        return true
    }

    private fun deactiveConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB:LocallyCoordinatedModelNode, portB:Port): Boolean{
        if (nodeA == nodeB) return false
        if (!LocallyCoordinatedModelNode.canDeactive(nodeA,portA,nodeB,portB)) return false
        when (portA) {
            Port.UP -> nodeA.up = null
            Port.DOWN -> nodeA.down = null
            Port.LEFT -> nodeA.left = null
            Port.RIGHT -> nodeA.right = null
        }
        when (portB) {
            Port.UP -> nodeB.up = null
            Port.DOWN -> nodeB.down = null
            Port.LEFT -> nodeB.left = null
            Port.RIGHT -> nodeB.right = null
        }
        numOfActiveEdges--
        TODO("CHECK AND SPEATE TWO GRAPH")
        return true
    }


    override fun numOfEdge(): Int {
        return numOfActiveEdges
    }

    override fun numOfNode(): Int {
        return nodes.size
    }

    override fun interact(): Triple<Boolean, ModelNode, ModelNode> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
