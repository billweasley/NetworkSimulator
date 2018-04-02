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
import model.population.populationProtocols.concrete.DancingProtocol
import model.shared.Port
import org.graphstream.stream.Source
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import presentation.generator.PopulationProtocolGenerator
import presentation.generator.ShapeConstructorGenerator
import presentation.generator.SimulationGenerator
import scheduler.RandomScheduler
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



class SimulatorAppView : View() {

    private val generator = PopulationProtocolGenerator(
            DancingProtocol(
                    initialStates = mapOf(Pair("L", 6), Pair("F", 9)), scheduler = RandomScheduler()
            ),
            1000000,
            false,
            300
    )

    /* private val generator = ShapeConstructorGenerator(
             population = ShapeConstructingPopulation(
             scheduler = RandomScheduler(), interactFunction = { nodeA, nodeB, map ->
                 InteractionFunctions.globalStarFunc(nodeA, nodeB, map)
             }, symbols = setOf("c", "p"), initialStates = mapOf(Pair("c", 10))
     ),maxTimes = 100000)*/

    private var shouldStop = false

    private fun runProcess(generator: SimulationGenerator) {
        tornadofx.runAsync {
            while (!shouldStop && !generator.shouldTerminate()) {
                generator.nextEvents()
            }
        }.ui {
            if (generator.shouldTerminate()) {
                println("Terminating in UI...")
                resetBtnStates()
            }
        }
    }

    private val swingNode = SwingNode()
    private val startBtn = button("Start")
    private val stopBtn = button("Stop") {
        isDisable = true
    }

    private fun resetBtnStates() {
        shouldStop = false
        startBtn.isDisable = false
        stopBtn.isDisable = true
    }
    private fun resetGraphStates() {
        generator.restart()
        generator.begin()
    }

    private fun createAndSetSwingContent(swingNode: SwingNode) {

        SwingUtilities.invokeLater {
            val viewer = Viewer(
                    generator.graph,
                    Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD
            )
            primaryStage.setOnCloseRequest { viewer.close() }

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
                createAndSetSwingContent(swingNode)
                //prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.50))
            }
            borderpane {
                left {
                    this += startBtn
                    startBtn.action {
                        startBtn.isDisable = true
                        stopBtn.isDisable = false
                        shouldStop = false
                        if (generator.shouldTerminate()) {
                            resetGraphStates()
                        }
                        runProcess(generator)
                    }
                }
                center {
                    this += stopBtn
                    stopBtn.action {
                        startBtn.isDisable = false
                        stopBtn.isDisable = true
                        shouldStop = true
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

class RulesInput(abefore: String = "", aafter: String = "", bbefore: String = "", bafter: String = "",
                 linkBefore: Boolean? = false, linkAfter: Boolean? = false,
                 aport: Port? = null, bport: Port? = null) {
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


