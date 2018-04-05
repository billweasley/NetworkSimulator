package model.population.gridNetworkConstruction

import model.population.LinkedPopulation
import model.shared.LocallyCoordinatedModelNode
import model.shared.ModelNode
import model.shared.Port
import scheduler.RandomScheduler
import scheduler.Scheduler
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MarkedSet<E>(val groupID: Int): HashSet<E>(){
    companion object {
        fun <E> of(elements: Array<E>,groupID: Int): MarkedSet<E>{
            val res = MarkedSet<E>(groupID)
            res.addAll(elements)
            return res
        }
        fun <E> of(element: E,groupID: Int): MarkedSet<E>{
            val res = MarkedSet<E>(groupID)
            res.add(element)
            return res
        }
    }

    override fun add(element: E): Boolean {
        if (element is LocallyCoordinatedModelNode){
            element.belongtoSet = groupID
        }
        return super.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        for (element in elements){
            if (element is LocallyCoordinatedModelNode){
                element.belongtoSet = groupID
            }
        }
        return super.addAll(elements)
    }

}

class GridNetworkConstructingPopulation(private val scheduler: Scheduler = RandomScheduler(),
                                        private val interactFunction: (Pair<ModelNode,Port>, Pair<ModelNode, Port>) -> Pair<Boolean, Boolean>,
                                        private val symbols: Set<String>,
                                        private val initialStates: Map<String, Int>)
                                       : LinkedPopulation {

    @Volatile var increasedIDAllocator = 0
    override val nodes: List<LocallyCoordinatedModelNode> = LocallyCoordinatedModelNode.createMultipleNodes(symbols,initialStates)
    val groupOfNodes: ConcurrentHashMap<Int, MarkedSet<LocallyCoordinatedModelNode>> = ConcurrentHashMap()
    companion object {
        val random = Random()
        fun getRandomNumber(excusiveBoundery: Int) = random.nextInt()
    }
    fun getIncreasedID(): Int{
        synchronized(this,{
            increasedIDAllocator++
            return increasedIDAllocator
        })
    }


    init {

        nodes.forEach {it ->
            val id = getIncreasedID()
            groupOfNodes[id] = MarkedSet.of(it,id)
        }
    }


    private var numOfActiveEdges = 0

    constructor(another: GridNetworkConstructingPopulation) :
            this(scheduler = another.scheduler,
                    interactFunction = another.interactFunction,
                    symbols = another.symbols,
                    initialStates = another.initialStates
            )

    private fun activeConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean{
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
        val smallerID = Math.min(nodeA.belongtoSet,nodeB.belongtoSet)
        val largerID = Math.max(nodeA.belongtoSet,nodeB.belongtoSet)
        groupOfNodes[smallerID]?.addAll(groupOfNodes[largerID]!!.toList())
        groupOfNodes.remove(largerID)
        return true
    }

    private fun deactiveConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean{
        if (nodeA == nodeB) return false
        if (!LocallyCoordinatedModelNode.canInactive(nodeA,portA,nodeB,portB)) return false
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


        val connectedNodeOfA = dfs(nodeA, mutableSetOf())
        val oriConnectedNodeOfA = groupOfNodes[nodeA.belongtoSet]
        if (!connectedNodeOfA.contains(nodeB)){
            val connectedNodeOfB = MarkedSet<LocallyCoordinatedModelNode>(getIncreasedID())
            oriConnectedNodeOfA!!.forEach {
                if (!connectedNodeOfA.contains(it)){
                    connectedNodeOfB.add(it)
                }
            }
            groupOfNodes[connectedNodeOfB.groupID] = connectedNodeOfB
        }
        return true
    }

    fun dfs(current: LocallyCoordinatedModelNode?, res: MutableSet<LocallyCoordinatedModelNode>): Set<LocallyCoordinatedModelNode>{
        if (current == null) return setOf()
        res.add(current)
        if (current.up!=null && !res.contains(current.up!!)) res.addAll(dfs(current.up, res))
        if (current.down!=null && !res.contains(current.down!!)) res.addAll(dfs(current.down, res))
        if (current.left!=null && !res.contains(current.left!!)) res.addAll(dfs(current.left, res))
        if (current.right!=null && !res.contains(current.right!!)) res.addAll(dfs(current.right, res))
        return res
    }



    override fun numOfEdge(): Int {
        return numOfActiveEdges
    }

    override fun numOfNode(): Int {
        return nodes.size
    }

    override fun interact(): Triple<Boolean, Pair<LocallyCoordinatedModelNode, Port>,  Pair<LocallyCoordinatedModelNode, Port>> {
        val selected = scheduler.select(this)
        val nodeA = selected.first as LocallyCoordinatedModelNode
        val nodeB = selected.second as LocallyCoordinatedModelNode
        val portA = Port.values()[getRandomNumber(Port.values().size)]
        val portB = Port.values()[getRandomNumber(Port.values().size)]


        val result = interactFunction.invoke(Pair(nodeA,portA),Pair(nodeB, portB))

        val hasInteracted = result.first
        val shouldBeActivated = result.second
        if (hasInteracted) {
            if (shouldBeActivated) {
                activeConnection(nodeA,portA,nodeB,portB)
            } else {
                deactiveConnection(nodeA,portA,nodeB,portB)
            }
        }
        return Triple(hasInteracted,Pair(nodeA,portA),Pair(nodeB,portB))
    }

}
