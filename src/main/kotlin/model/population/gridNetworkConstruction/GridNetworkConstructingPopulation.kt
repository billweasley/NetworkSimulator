package model.population.gridNetworkConstruction

import model.population.LinkedPopulation
import model.scheduler.RandomScheduler
import model.scheduler.Scheduler
import model.shared.LocallyCoordinatedModelNode
import model.shared.Port
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MarkedSet<E>(val groupID: Int): HashSet<E>(){
    companion object {
        fun <E> of(elements: Array<E>,groupID: Int): MarkedSet<E>{
            val res = MarkedSet<E>(groupID)
            res.addAll(elements)
            return res
        }
        fun <E> of(elements: Set<E>,groupID: Int): MarkedSet<E>{
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
                                        private val interactFunction: (Pair<LocallyCoordinatedModelNode,Port>, Pair<LocallyCoordinatedModelNode, Port>) -> Triple<Boolean,Pair<String,String>,Boolean>,
                                        private val symbols: Set<String>,
                                        private val initialStates: Map<String, Int>)
                                       : LinkedPopulation {

    @Volatile var increasedIDAllocator = 0
    override val nodes: List<LocallyCoordinatedModelNode> = LocallyCoordinatedModelNode.createMultipleNodes(symbols,initialStates)
    override val statisticsMap = symbols.map { it -> Pair(it, if (initialStates.containsKey(it)) initialStates[it]!! else 0) }.toMap(ConcurrentHashMap())
    var groupOfNodes: ConcurrentHashMap<Int, MarkedSet<LocallyCoordinatedModelNode>> = ConcurrentHashMap()

    companion object {
        val random = Random()
        fun getRandomNumber(exclusiveBoundary: Int) = random.nextInt(exclusiveBoundary)
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
    private fun canActiveConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean{
        return nodeA != nodeB && LocallyCoordinatedModelNode.canActive(nodeA,portA,nodeB,portB)
    }
    private fun activeConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port){
        if (!canActiveConnection(nodeA,portA,nodeB,portB)) return
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
        if (nodeA.belongtoSet == null) throw IllegalArgumentException("nodeA belongs to no set")
        if (nodeB.belongtoSet == null) throw IllegalArgumentException("nodeB belongs to no set")
        if (nodeA.belongtoSet != nodeB.belongtoSet){
            val smallerID = Math.min(nodeA.belongtoSet!!,nodeB.belongtoSet!!)
            val largerID = Math.max(nodeA.belongtoSet!!,nodeB.belongtoSet!!)
            groupOfNodes[smallerID]?.addAll(groupOfNodes[largerID]!!.toList())
            groupOfNodes.remove(largerID)
        }
    }
    private fun canDeactiveConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port): Boolean{
        return nodeA != nodeB && LocallyCoordinatedModelNode.canInactive(nodeA,portA,nodeB,portB)
    }

    private fun deactiveConnection(nodeA: LocallyCoordinatedModelNode, portA: Port, nodeB: LocallyCoordinatedModelNode, portB: Port){
        if (!canDeactiveConnection(nodeA,portA,nodeB,portB)) return
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


        val connectedNodeOfA = MarkedSet.of(dfs(nodeA, mutableSetOf()),nodeA.belongtoSet!!)
        val oriConnectedNodeOfA = groupOfNodes[nodeA.belongtoSet!!]
        if (!connectedNodeOfA.contains(nodeB)){
            val connectedNodeOfB = MarkedSet<LocallyCoordinatedModelNode>(getIncreasedID())
            oriConnectedNodeOfA!!.forEach {
                if (!connectedNodeOfA.contains(it)){
                    connectedNodeOfB.add(it)
                }
            }
            groupOfNodes[oriConnectedNodeOfA.groupID] = connectedNodeOfA
            groupOfNodes[connectedNodeOfB.groupID] = connectedNodeOfB
        }
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

    override fun interact(): Triple<Pair<Boolean,Boolean>, Pair<LocallyCoordinatedModelNode, Port>,  Pair<LocallyCoordinatedModelNode, Port>> {
        val selected = scheduler.select(this)
        val nodeA = selected.first as LocallyCoordinatedModelNode
        val nodeB = selected.second as LocallyCoordinatedModelNode
        val portA = Port.values()[getRandomNumber(Port.values().size)]
        val portB = Port.values()[getRandomNumber(Port.values().size)]

        /*val firstATest = String(nodeA.state.currentState.toCharArray())
        val firstBTest = String(nodeB.state.currentState.toCharArray())
        val firstATestBl = nodeB.belongtoSet
        val firstATestAl = nodeA.belongtoSet*/

        val result = interactFunction.invoke(Pair(nodeA,portA),Pair(nodeB, portB))
        /*if (result.first){
            println("From ${firstATest} $portA ${firstBTest} $portB")
            println("Before To (Interacted: ${result.first})  ${nodeA.state.currentState} $portA ${nodeB.state.currentState} Connection ${result.second}")
        }*/
        var hasInteracted = result.first
        var shouldBeActivated = result.third
        var afterState = result.second
        if (hasInteracted) {
            if (shouldBeActivated){
                if(!nodeA.hasConnectionWith(portA,nodeB)){
                    if(canActiveConnection(nodeA,portA,nodeB,portB)){
                        activeConnection(nodeA,portA,nodeB,portB)
                        statisticsMap[nodeA.state.currentState] = statisticsMap[nodeA.state.currentState]!! - 1
                        statisticsMap[nodeB.state.currentState] = statisticsMap[nodeB.state.currentState]!! - 1
                        nodeA.state.currentState = afterState.first
                        nodeB.state.currentState = afterState.second
                        statisticsMap[nodeA.state.currentState] = statisticsMap[nodeA.state.currentState]!! + 1
                        statisticsMap[nodeB.state.currentState] = statisticsMap[nodeB.state.currentState]!! + 1
                    }else{
                       // System.err.println("Interaction of connection REJECTED. A belongs to ${firstATestAl} with state ${firstATest}; B belongs to ${firstATestBl} with state ${firstBTest}")
                        hasInteracted = false
                    }
                }
            }else{
                if(nodeA.hasConnectionWith(portA,nodeB)){
                    if (canDeactiveConnection(nodeA,portA,nodeB,portB)){
                        deactiveConnection(nodeA,portA,nodeB,portB)
                        statisticsMap[nodeA.state.currentState] = statisticsMap[nodeA.state.currentState]!! - 1
                        statisticsMap[nodeB.state.currentState] = statisticsMap[nodeB.state.currentState]!! - 1
                        nodeA.state.currentState = afterState.first
                        nodeB.state.currentState = afterState.second
                        statisticsMap[nodeA.state.currentState] = statisticsMap[nodeA.state.currentState]!! + 1
                        statisticsMap[nodeB.state.currentState] = statisticsMap[nodeB.state.currentState]!! + 1
                    }else{
                        //System.err.println("Interaction of disconnection REJECTED")
                        hasInteracted = false
                    }
                }
            }
        }
        //if (result.first) println("(Interacted: ${hasInteracted}) To ${nodeA.state.currentState} $portA ${nodeB.state.currentState} Connection ${shouldBeActivated}")
        return Triple(Pair(hasInteracted,shouldBeActivated),Pair(nodeA,portA),Pair(nodeB,portB))
    }

}
