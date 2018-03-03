package population.gridNetworkConstruction

import population.LinkedPopulation
import scheduler.Scheduler
import utils.LocallyCoordinatedNode

class GridNetworkConstructingPopulation(scheduler: Scheduler, nodes: List<LocallyCoordinatedNode>) : LinkedPopulation {
    override val nodes: List<LocallyCoordinatedNode> = nodes

    lateinit var groupOfNodes: List<Set<LocallyCoordinatedNode>>


    override fun numOfEdge(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun numOfNode(): Int {
        return nodes.size
    }

    override fun interact(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
