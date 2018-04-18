package presentation.ui

import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.text.Font
import model.population.gridNetworkConstruction.GridNetworkConstructingPopulation
import model.shared.LocallyCoordinatedModelNode
import model.shared.Port
import org.graphstream.stream.Source
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import presentation.generator.SimulationGenerator
import presentation.generator.concrete.GridNetworkGenerator
import presentation.generator.concrete.PopulationProtocolGenerator
import presentation.generator.concrete.ShapeConstructorGenerator
import tornadofx.*
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.SwingUtilities
import kotlin.reflect.KClass


fun main(args: Array<String>) {
    Application.launch(SimulatorApp::class.java, *args)
}

class SimulatorApp : App(SimulatorAppView::class)

object TextFieldUpdateEvent: FXEvent(EventBus.RunOn.ApplicationThread)
object TerminateEvent: FXEvent(EventBus.RunOn.ApplicationThread)

class SimulatorAppView : View() {

    /*private val generator = PopulationProtocolGenerator(
            DancingProtocol(
                    initialStates = mapOf(Pair("L", 6), Pair("F", 9)), scheduler = RandomScheduler()
            ),
            1000000,
            false,
            300
    )*/

   /* private val generator = ShapeConstructorGenerator(
             population = ShapeConstructingPopulation(
             scheduler = RandomScheduler(), interactFunction = { nodeA, nodeB, map ->
                 InteractionFunctions.globalStarFunc(nodeA, nodeB, map)
             }, symbols = setOf("c", "p"), initialStates = mapOf(Pair("c", 12))
     ),maxTimes = 100000)*/


    val generator = GridNetworkGenerator(GridNetworkConstructingPopulation(interactFunction = { firstPair: Pair<LocallyCoordinatedModelNode, Port>, secondPair: Pair<LocallyCoordinatedModelNode, Port> ->
        val firstModelNode = firstPair.first
        val secondModelNode = secondPair.first
        val isConnected = firstModelNode.getPort(firstPair.second) == secondModelNode
        val givenState =
                Triple(
                        Pair(firstModelNode.state.currentState, firstPair.second),
                        Pair(secondModelNode.state.currentState, secondPair.second),
                        isConnected
                )
        val transferredState =
                when(givenState) {
                    Triple(Pair("Ll", Port.LEFT), Pair("q1", Port.RIGHT), false)
                    -> Triple("Ld", "q1", true)
                    Triple(Pair("Lu", Port.UP), Pair("q0", Port.DOWN), false)
                    -> Triple("q1", "Lr", true)
                    Triple(Pair("Lr", Port.RIGHT), Pair("q0", Port.LEFT), false)
                    -> Triple("q1", "Ld", true)
                    Triple(Pair("Ld", Port.DOWN), Pair("q0", Port.UP), false)
                    -> Triple("q1", "Ll", true)
                    Triple(Pair("Ll", Port.LEFT), Pair("q0", Port.RIGHT), false)
                    -> Triple("q1", "Lu", true)
                    Triple(Pair("Lu", Port.UP), Pair("q1", Port.DOWN), false)
                    -> Triple("Ll", "q1", true)
                    Triple(Pair("Lr", Port.RIGHT), Pair("q1", Port.LEFT), false)
                    -> Triple("Lu", "q1", true)
                    Triple(Pair("Ld", Port.DOWN), Pair("q1", Port.UP), false)
                    -> Triple("Lr", "q1", true)
                    else -> null
                }

        if (transferredState == null) Triple(false, Pair("",""),isConnected) else{
            Triple(true, Pair(transferredState.first,transferredState.second), transferredState.third)
        }

    }, symbols = setOf("Lu", "q0", "q1", "Lr", "Ld", "Ll", "Lu"),
            initialStates = mapOf(Pair("q0", 25), Pair("Lu", 1))
    ))


    private var shouldStop = false
    @Volatile private var effectiveInteractionCount = 0
    @Synchronized private fun runProcess(generator: SimulationGenerator) {
        tornadofx.runAsync {
            while (!shouldStop && !generator.shouldTerminate()) {
                generator.nextEvents()
                effectiveInteractionCount++
                fire(TextFieldUpdateEvent)
            }
        }.ui {
            if (generator.shouldTerminate()) {
                println("Terminating in UI...")
                fire(TerminateEvent)
            }
        }
    }

    private val swingNode = SwingNode()
    private var viewer = Viewer(generator.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
    private fun createAndSetSwingContent() {

        SwingUtilities.invokeLater {
            primaryStage.setOnCloseRequest {
                    viewer.disableAutoLayout()
                    viewer.close()
            }

            val viewPanel = viewer.addDefaultView(false)
            if (generator.requireLayoutAlgorithm) {
                val layout = Layouts.newLayoutAlgorithm()
                viewer.enableAutoLayout(layout)
            }
            viewPanel.border = BorderFactory.createLineBorder(Color.BLUE, 5)
            viewPanel.preferredSize = Dimension(1000, 720)
            swingNode.content = viewPanel

        }

    }
    private fun resetSwingContent(){
        SwingUtilities.invokeLater {
            viewer.disableAutoLayout()
            viewer.close()
            swingNode.content = null
            primaryStage.setOnCloseRequest { }
            viewer = Viewer(
                    generator.graph,
                    Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD
            )
            primaryStage.setOnCloseRequest {
                viewer.disableAutoLayout()
                viewer.close()
            }
            val viewPanel = viewer.addDefaultView(false)

            if (generator.requireLayoutAlgorithm) {
                val layout = Layouts.newLayoutAlgorithm()
                viewer.enableAutoLayout(layout)
            }

            viewPanel.border = BorderFactory.createLineBorder(Color.BLUE, 5)
            viewPanel.preferredSize = Dimension(1000, 720)
            swingNode.content = viewPanel
        }
    }
    private fun resetGraphStates() {
        generator.restart()
        generator.begin()
        effectiveInteractionCount = 0
        resetSwingContent()
    }


    init {
        generator.begin()
        primaryStage.maxHeight = 750.0
        primaryStage.minWidth = 1280.0
        primaryStage.isResizable = false

    }

    override val root = hbox {
        title = "Network Simulator GUI Alpha"
        vbox(20) {
            vboxConstraints {
                prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.55))
            }
            pane {
                this += swingNode
                createAndSetSwingContent()
            }
            borderpane {
                center {
                    this += button("Start") {
                        subscribe<TerminateEvent>{
                            text = "Restart"
                            shouldStop = true
                        }
                        action {
                            if(text == "Start" || text == "Restart"){
                                text = "Pause"
                                shouldStop = false
                            }else if(text == "Pause"){
                                text = "Start"
                                shouldStop = true
                            }
                            if (generator.shouldTerminate()) {
                                resetGraphStates()
                                fire(TextFieldUpdateEvent)
                            }
                            runProcess(generator)
                        }
                    }

                }
            }
            vbox(10){
                text {
                    vboxConstraints {
                        margin = Insets(20.0, 20.0, 10.0, 20.0)
                    }
                    text = "Total number of Node: ${generator.population.nodes.size} \n"
                    text += "Effective interaction times:  ${generator.count} \n"
                    text += "Scheduler selection times: ${effectiveInteractionCount}\n"
                    subscribe<TextFieldUpdateEvent> {
                            text = "Total number of Node: ${generator.population.nodes.size} \n"
                            text += "Effective interaction times:  ${generator.count} \n"
                            text += "Scheduler selection times: ${effectiveInteractionCount}\n"
                  }
                }
                text{
                    vboxConstraints {
                        margin = Insets(0.0, 20.0, 20.0, 20.0)
                    }
                    text = "Number of States distributed on nodes: " + generator.population.statictisMap.map{ it -> " ${it.key}: ${it.value}; " }.reduce(String::plus).removeSuffix("; ")
                    subscribe<TextFieldUpdateEvent> {
                        text = "Number of Node in " + generator.population.statictisMap.map{ its -> " ${its.key}: ${its.value} " }.reduce(String::plus).removeSuffix("; ")
                    }
                }

            }
        }
        vbox {
            prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.45))

            text("Model Type") {
                font = Font(20.0)
                vboxConstraints {
                    margin = Insets(20.0, 20.0, 0.0, 20.0)
                }
            }
            val modelTypes = FXCollections.observableArrayList(
                    "Basic Population Protocol",
                    "Network Constructor (Shape Constructor)",
                    "Terminated Grid Network Constructor"
            )
            val typeBox = ComboBox<String>()
            typeBox.apply {
                hboxConstraints {
                    margin = Insets(20.0, 20.0, 20.0, 20.0)
                }
                items = modelTypes
                selectionModel.select(0)
            }
            hbox(20){
                this += typeBox
                val enablePreRun = SimpleBooleanProperty(false)
                vbox(5){
                    checkbox("Enable Pre-running") {
                        action { enablePreRun.set(isSelected) }
                    }
                    textfield ("0 "){
                        promptText = "Times"

                        textProperty().addListener({_, _, newV ->
                            if (!newV.matches("\\d*".toRegex())) {
                                text = newV.replace("[^\\d]".toRegex(),"")
                            }
                            if(newV.isEmpty()) text = "0"
                        })

                        isDisable = !enablePreRun.value
                        enablePreRun.addListener({_, _, newV -> isDisable = !newV })
                    }
                }
            }
            var clazzOfGenerator = when (typeBox.selectionModel.selectedIndex) {
                0 -> PopulationProtocolGenerator::class
                1 -> ShapeConstructorGenerator::class
                else -> null
            }
            val initialSymbolMap = mutableMapOf<String, Int>()
            val initialInputs = mutableListOf<InitialInputConfiguration>().observable()
            val initialStatesTableView = TableView(initialInputs)
            val rulesInputs = mutableListOf<RulesInput>().observable()
            repeat(100000, { _ -> rulesInputs.add(RulesInput()) })

            text("Defined Rules") {
                font = Font(20.0)
                vboxConstraints {
                    margin = Insets(0.0, 20.0, 0.0, 20.0)
                }
            }
            tableview(rulesInputs) {
                vboxConstraints {
                    margin = Insets(20.0, 20.0, 20.0, 20.0)
                }
                isCenterShape = true
                isEditable = true
                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

                column("A Before ", RulesInput::abefore).apply {
                    onEditCommit = EventHandler {
                        if (it.oldValue != it.newValue  && it.newValue.isNotBlank()) {
                            synchronized(initialInputs, {
                                if (initialSymbolMap.containsKey(it.oldValue)) {
                                    if (initialSymbolMap[it.oldValue] == 1) {
                                        initialSymbolMap.remove(it.oldValue)
                                        initialInputs.removeIf{initialInput -> initialInput.state == it.oldValue}
                                        initialStatesTableView.refresh()
                                    } else {
                                        initialSymbolMap[it.oldValue] =initialSymbolMap[it.oldValue]!! - 1
                                    }
                                }
                                if (initialSymbolMap.containsKey(it.newValue)) {
                                    initialSymbolMap[it.newValue] =initialSymbolMap[it.newValue]!! + 1
                                }else{
                                    initialSymbolMap[it.newValue] = 1
                                    initialInputs.add(InitialInputConfiguration(it.newValue))
                                    initialStatesTableView.refresh()
                                }
                            })
                        }else if(it.newValue.isBlank()){
                            if (initialSymbolMap.containsKey(it.oldValue)) {
                                if (initialSymbolMap[it.oldValue] == 1) {
                                    initialSymbolMap.remove(it.oldValue)
                                    initialInputs.removeIf{initialInput -> initialInput.state == it.oldValue}
                                    initialStatesTableView.refresh()
                                } else {
                                    initialSymbolMap[it.oldValue] =initialSymbolMap[it.oldValue]!! - 1
                                }
                            }
                        }
                        it.rowValue.abefore = it.newValue
                        initialSymbolMap.forEach({node -> println("${node.key} | ${node.value}")})
                        println("-----------------------------------------------------------------")
                        initialStatesTableView.refresh()
                    }
                    makeEditable()
                }
                column("B Before ", RulesInput::bbefore).apply{
                    makeEditable()
                    onEditCommit = EventHandler {
                        if (it.oldValue != it.newValue && it.newValue.isNotBlank()) {
                            synchronized(initialInputs, {
                                if (initialSymbolMap.containsKey(it.oldValue)) {
                                    if (initialSymbolMap[it.oldValue]!! == 1) {
                                        initialSymbolMap.remove(it.oldValue)
                                        initialInputs.removeIf{initialInput -> initialInput.state == it.oldValue}
                                        initialStatesTableView.refresh()
                                    } else {
                                        initialSymbolMap[it.oldValue] =initialSymbolMap[it.oldValue]!! - 1
                                    }
                                }
                                if (initialSymbolMap.containsKey(it.newValue)) {
                                    initialSymbolMap[it.newValue] =  initialSymbolMap[it.newValue]!!  + 1
                                }else{
                                    initialSymbolMap[it.newValue] = 1
                                    initialInputs.add(InitialInputConfiguration(it.newValue))
                                    initialStatesTableView.refresh()
                                }
                            })
                        }else if(it.newValue.isBlank()){
                            if (initialSymbolMap.containsKey(it.oldValue)) {
                                if (initialSymbolMap[it.oldValue]!! == 1) {
                                    initialSymbolMap.remove(it.oldValue)
                                    initialInputs.removeIf{initialInput -> initialInput.state == it.oldValue}
                                    initialStatesTableView.refresh()
                                } else {
                                    initialSymbolMap[it.oldValue] =initialSymbolMap[it.oldValue]!! - 1
                                }
                            }
                        }
                        it.rowValue.bbefore = it.newValue
                        initialSymbolMap.forEach({node -> println("${node.key} | ${node.value}")})
                        println("-----------------------------------------------------------------")

                        initialStatesTableView.refresh()
                    }
                }
                column("A After ", RulesInput::aafter).apply {
                    makeEditable()
                }
                column("B After ", RulesInput::bafter).apply{
                    makeEditable()
                }
                val portAcol = column("Port A", RulesInput::aport).apply {
                    isVisible = false
                    useComboBox(Port.values().toList().observable(), {})
                }
                val portBcol = column("Port B", RulesInput::bport).apply {
                    isVisible = false
                    useComboBox(Port.values().toList().observable(), {})
                }
                val linkBeforeCol = column("Link Before", RulesInput::linkBefore).apply {
                    makeEditable()
                    isVisible = false
                }
                val linkAfterCol = column("Link After", RulesInput::linkAfter).apply {
                    makeEditable()
                    isVisible = false
                }
                typeBox.selectionModel.selectedIndexProperty().addListener { _, oldValue, newValue ->
                    if (oldValue != newValue) {
                        items.clear()
                        initialStatesTableView.items.clear()
                        initialSymbolMap.clear()
                        repeat(100000, { _ -> rulesInputs.add(RulesInput()) })
                        clazzOfGenerator = when (typeBox.selectionModel.selectedIndex) {
                            0 -> PopulationProtocolGenerator::class
                            1 -> ShapeConstructorGenerator::class
                            else -> null
                        }
                        when (newValue.toInt()) {
                            0 -> {
                                portAcol.isVisible = false
                                portBcol.isVisible = false
                                linkBeforeCol.isVisible = false
                                linkAfterCol.isVisible = false
                            }
                            1 -> {
                                portAcol.isVisible = false
                                portBcol.isVisible = false
                                linkBeforeCol.isVisible = true
                                linkAfterCol.isVisible = true
                            }
                            2 -> {
                                portAcol.isVisible = true
                                portBcol.isVisible = true
                                linkBeforeCol.isVisible = true
                                linkAfterCol.isVisible = true
                            }
                        }
                    }
                }
            }

            text("Initial States") {
                font = Font(20.0)
                vboxConstraints {
                    margin = Insets(0.0, 20.0, 0.0, 20.0)
                }
            }
            this += initialStatesTableView.apply {
                vboxConstraints {
                    margin = Insets(20.0, 20.0, 20.0, 20.0)
                }
                initialStatesTableView.column("State", InitialInputConfiguration::state)
                initialStatesTableView.column("# Initial Config.", InitialInputConfiguration::count).apply{
                    makeEditable()
                    textProperty().addListener({_, _, newV ->
                        if (!newV.matches("\\d*".toRegex())) {
                            text = newV.replace("[^\\d]".toRegex(),"")
                        }
                    })
                }
                initialStatesTableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            }

        }
    }
}

class InitialInputConfiguration(state: String, count: Int = 0) {
    var state by property(state)
    fun stateProperty() = getProperty(InitialInputConfiguration::state)
    var count by property(count)
    fun countProperty() = getProperty(InitialInputConfiguration::count)
}

class RulesInput(abefore: String = "", aafter: String = "", bbefore: String = "", bafter: String = "", linkBefore: Boolean? = false, linkAfter: Boolean? = false, aport: Port? = null, bport: Port? = null) {
    var abefore by property(abefore)
    fun abeforeProperty() = getProperty(RulesInput::abefore)
    var aafter by property(aafter)
    fun aafterProperty() = getProperty(RulesInput::aafter)
    var bbefore by property(bbefore)
    fun bbeforeProperty() = getProperty(RulesInput::bbefore)
    var bafter by property(bafter)
    fun bafterProperty() = getProperty(RulesInput::bafter)
    var linkBefore by property(linkBefore)
    fun linkBeforeProperty() = getProperty(RulesInput::linkBefore)
    var linkAfter by property(linkAfter)
    fun linkAfterProperty() = getProperty(RulesInput::linkAfter)
    var aport by property(aport)
    fun aportProperty() = getProperty(RulesInput::aport)
    var bport by property(bport)
    fun bportProperty() = getProperty(RulesInput::bport)

    fun isSelfASatisiableRule(clazz: KClass<out Source>?): Boolean {
        when (clazz) {
            PopulationProtocolGenerator::class -> {
                return isIllegalChar(abefore) && isIllegalChar(bbefore)
                        && isIllegalChar(aafter) && isIllegalChar(bafter)
            }
            ShapeConstructorGenerator::class -> {
                return isIllegalChar(abefore) && isIllegalChar(bbefore)
                        && isIllegalChar(aafter) && isIllegalChar(bafter)
            }

        }
        return false
    }

    private fun isIllegalChar(input: String?): Boolean {
        return input != null && input != ""
    }
    companion object {
        private fun getAllSymbols(rulesInputs: List<RulesInput>,clazz: KClass<out Source>?): Set<String>{
            val filtered = rulesInputs.filter { it -> it.isSelfASatisiableRule(clazz) }
            val res = HashSet<String>()
            filtered.forEach{ rule ->
                res.add(rule.aafter)
                res.add(rule.bafter)
                res.add(rule.bbefore)
                res.add(rule.abefore)
            }
            return res
        }
    }

}


