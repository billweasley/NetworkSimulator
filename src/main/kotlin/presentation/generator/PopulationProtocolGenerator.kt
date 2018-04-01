package presentation.generator

import model.population.populationProtocols.PopulationProtocol
import model.population.populationProtocols.concrete.DancingProtocol
import org.graphstream.algorithm.Toolkit
import org.graphstream.graph.Graph
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.SourceBase
import org.graphstream.ui.view.Viewer
import scheduler.RandomScheduler
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.pow



fun main(args: Array<String>) {
    val protocol = DancingProtocol(
            initialStates = mapOf(Pair("L", 9), Pair("F", 6)), scheduler = RandomScheduler()
    )

    val populationProtocolGenerator = PopulationProtocolGenerator(
            protocol,
            1000000,
            true,
            300,
            "Test"
    )

    populationProtocolGenerator.display()
}


class PopulationProtocolGenerator(var population: PopulationProtocol,
                                  val maxTimes: Long = -1,
                                  val fastRes: Boolean= false,
                                  val preExecutedSteps: Int = 0,
                                  nameOfPopulation: String = "",
                                  val graph: Graph = SingleGraph(nameOfPopulation),
                                  private val styleSheet: String =
                                          "node {fill-color: black; text-size: 30px;}" +
                                          "node.marked {fill-color: red; }" +
                                          "edge.marked {fill-color: red;}"): SourceBase(),SimulationGenerator{
    @Volatile override var countOfSelectWithoutInteraction = 0
    override val terminateTheshold = 1000
    override val requireLayoutAlgorithm = false
    private var count = 0
    private val random = Random()
    init {
        if(fastRes && preExecutedSteps < 0)
            throw IllegalArgumentException("The fast forwarding requires a non-negative value.")
        graph.addAttribute("ui.stylesheet", styleSheet)
        graph.addAttribute("layout.stabilization-limit",0.01)
        graph.addAttribute("layout.force",0.0)
        graph.addAttribute("layout.weight",0.0)
        addSink(graph)
    }
    override fun restart() {
        count = 0
        countOfSelectWithoutInteraction = 0
        val ori = population
        population = PopulationProtocol(population)
        graph.clear()
        graph.addAttribute("ui.stylesheet", styleSheet)
        graph.addAttribute("layout.stabilization-limit",0.01)
        graph.addAttribute("layout.force",0.0)
        graph.addAttribute("layout.weight",0.0)
    }
    fun display(){
        val frame = JFrame("Network Simulator")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val panel = JPanel(GridLayout())
        panel.preferredSize = Dimension(640,480)
        panel.border =BorderFactory.createLineBorder(Color.BLUE,5)

        val viewer = Viewer(graph,Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
        val viewPanel = viewer.addDefaultView(false)

        panel.add(viewPanel)
        frame.add(panel)
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        this.begin()
        while (count < maxTimes){
            this.nextEvents()
        }
        this.end()
    }


    override fun end() {}
    override fun begin() {
        if (fastRes){
            for(i in 0..preExecutedSteps){
                population.interact()
            }
        }
        val positions = HashSet<String>()
        for(node in population.nodes ){
            graph.addNode<Node>(node.index.toString())
            graph.getNode<Node>(node.index.toString())?.addAttribute("ui.label",node.state.currentState)
            var x = random.nextInt(population.size())
            var y = random.nextInt(population.size())
            while (positions.contains("$x | $y")){
                x = random.nextInt(population.size())
                y = random.nextInt(population.size())
            }
            positions.add("$x | $y")
            graph.getNode<Node>(node.index.toString())?.addAttribute("xy",x,y)

        }


    }

    @Synchronized
    override fun nextEvents(): Boolean {
        val (isInteracted, firstNode, secondNode) = population.interact()
        if (isInteracted){
            count ++
            countOfSelectWithoutInteraction = 0
            val firstNodeRep = graph.getNode<Node>(firstNode.index) as Node
            val secondNodeRep = graph.getNode<Node>(secondNode.index) as Node
            val firstOriLabel = firstNodeRep.getAttribute<String>("ui.label")
            val secondOriLabel = secondNodeRep.getAttribute<String>("ui.label")

            firstNodeRep.setAttribute("ui.class","marked")
            secondNodeRep.setAttribute("ui.class","marked" )
            firstNodeRep.setAttribute("ui.label",firstOriLabel+ "->" + firstNode.state.currentState)
            secondNodeRep.setAttribute("ui.label",secondOriLabel + "->"+ secondNode.state.currentState)
            val (oriFirstXInitial,oriFirstYInitial) =
                    Pair(Toolkit.nodePosition(firstNodeRep)[0],Toolkit.nodePosition(firstNodeRep)[1])
            val (oriSecondXInitial,oriSecondYInitial) =
                    Pair(Toolkit.nodePosition(secondNodeRep)[0], Toolkit.nodePosition(secondNodeRep)[1])

            val (interactXPos,interactYPos) =
                    Pair((oriFirstXInitial + oriSecondXInitial)/2, (oriFirstYInitial + oriSecondYInitial)/2 )

            val oriDistance =
                    ((oriFirstXInitial- oriSecondXInitial).pow(2) + (oriFirstYInitial- oriSecondYInitial).pow(2)).pow(0.5) / 2
            var  distance = oriDistance
            while (distance > 0.01){
                if (distance < 0.1 && distance > 0.05){
                    sleepWith(1)
                }
                val (oriFirstX,oriFirstY,_)= Toolkit.nodePosition(firstNodeRep)
                val (newPosXForFirst, newPosYForFirst) =
                        calculateUnitMove(Pair(oriFirstX,oriFirstY),Pair(interactXPos,interactYPos),0.01)

                val (oriSecondX,oriSecondY,_)= Toolkit.nodePosition(secondNodeRep)
                val (newPosXForSecond, newPosYForSecond) =
                        calculateUnitMove(Pair(oriSecondX,oriSecondY),Pair(interactXPos,interactYPos),0.01)
                if(!newPosXForFirst.isNaN() && !newPosYForFirst.isNaN()){
                    firstNodeRep.setAttribute("xy",newPosXForFirst,newPosYForFirst)
                }else{
                    break
                }
                if(!newPosXForSecond.isNaN() && !newPosYForSecond.isNaN()){
                    secondNodeRep.setAttribute("xy",newPosXForSecond,newPosYForSecond)
                }else{
                    break
                }
                distance =  ((Toolkit.nodePosition(firstNodeRep)[0]- Toolkit.nodePosition(secondNodeRep)[0]).pow(2)
                        + (Toolkit.nodePosition(firstNodeRep)[1]- Toolkit.nodePosition(secondNodeRep)[1]).pow(2)).pow(0.5) / 2

            }

            if (distance > 0){
                firstNodeRep.setAttribute("xy",interactXPos,interactYPos)
                secondNodeRep.setAttribute("xy",interactXPos,interactYPos)
                sleepWith(50)
            }

            distance =  ((Toolkit.nodePosition(firstNodeRep)[0]- oriFirstXInitial).pow(2)
                    + (Toolkit.nodePosition(firstNodeRep)[1]- oriFirstYInitial).pow(2)).pow(0.5) / 2
            while (distance > 0.01){
                val (oriFirstX,oriFirstY,_)= Toolkit.nodePosition(firstNodeRep)
                val (newPosXForFirst, newPosYForFirst) =
                        calculateUnitMove(Pair(oriFirstX,oriFirstY),Pair(oriFirstXInitial,oriFirstYInitial),0.01)
                if(!newPosXForFirst.isNaN() && !newPosYForFirst.isNaN()){
                    firstNodeRep.setAttribute("xy",newPosXForFirst,newPosYForFirst)
                }else{
                    break
                }
                val (oriSecondX,oriSecondY,_)= Toolkit.nodePosition(secondNodeRep)
                val (newPosXForSecond, newPosYForSecond) =
                        calculateUnitMove(Pair(oriSecondX,oriSecondY),Pair(oriSecondXInitial,oriSecondYInitial),0.01)
                if(!newPosXForSecond.isNaN() && !newPosYForSecond.isNaN()){
                    secondNodeRep.setAttribute("xy",newPosXForSecond,newPosYForSecond)
                }else{
                    break
                }
                distance =  ((Toolkit.nodePosition(firstNodeRep)[0]- oriFirstXInitial).pow(2)
                        + (Toolkit.nodePosition(firstNodeRep)[1]- oriFirstYInitial).pow(2)).pow(0.5) / 2

            }
            if (distance > 0){
                firstNodeRep.setAttribute("xy",oriFirstXInitial,oriFirstYInitial)
                secondNodeRep.setAttribute("xy",oriSecondXInitial,oriSecondYInitial)
            }

            sleepWith(500)
            graph.getNode<Node>(firstNode.index).setAttribute("ui.label",firstNode.state.currentState)
            graph.getNode<Node>(secondNode.index).setAttribute("ui.label",secondNode.state.currentState)
            graph.getNode<Node>(firstNode.index).removeAttribute("ui.class" )
            graph.getNode<Node>(secondNode.index).removeAttribute("ui.class" )

            sleepWith(500)
        }else{
            countOfSelectWithoutInteraction ++
        }

        return isInteracted
    }

    @Synchronized
    override fun shouldTerminate(): Boolean{
        return countOfSelectWithoutInteraction > this.terminateTheshold
    }

    private fun calculateUnitMove(origin: Pair<Double, Double>, destination: Pair<Double, Double>, unit: Double)
            :  Pair<Double, Double>{
        val k = (destination.second - origin.second) / (destination.first - origin.first)
        val b = origin.second - origin.first * k
        val distance = ((origin.first- destination.first).pow(2) + (origin.second- origin.second).pow(2)).pow(0.5)

        // ① (y-y0)^2+(x-x0)^2=L^2
        // ② y=kx+b;
        // => (k^2+1)x^2+2[(b-y0)k-x0]x+[(b-y0)^2+x0^2-L^2]=0
        //aka. Ax^2 + Bx + C = 0
        // where x,y is coordinate to be calculated, x0, y0 is original point
        val A = k.pow(2) + 1
        val B = 2 * ((b - origin.second) * k - origin.first)
        val C = (b - origin.second).pow(2) + origin.first.pow(2) - (distance * unit).pow(2)
        val x1 = (-B + (B.pow(2) - 4 * A * C).pow(0.5))/(2 * A)
        val x2 = (-B - (B.pow(2) - 4 * A * C).pow(0.5))/(2 * A)
        val maxBoundary =  if (origin.first > destination.first) origin.first else destination.first
        val minBoundary =  if (origin.first < destination.first) origin.first else destination.first
        var x = 0.0
        if (x1 in minBoundary..maxBoundary) {
            x = x1
        }else if(x2 in minBoundary..maxBoundary) {
            x = x2
        }
        return Pair(x, x * k + b)

    }

    private fun sleepWith(millisecond: Long){
        try {
            Thread.sleep(millisecond)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}