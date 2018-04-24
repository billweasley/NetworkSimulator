package presentation.ui

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.embed.swing.SwingNode
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TableView
import javafx.scene.text.Font
import model.population.Population
import model.population.gridNetworkConstruction.GridNetworkConstructingPopulation
import model.population.populationProtocols.PopulationProtocol
import model.population.shapeConstruction.ShapeConstructingPopulation
import model.scheduler.RandomScheduler
import model.scheduler.Scheduler
import model.shared.ModelNode
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import presentation.generator.SimulationGenerator
import presentation.generator.concrete.GridNetworkGenerator
import presentation.generator.concrete.PopulationProtocolGenerator
import presentation.generator.concrete.ShapeConstructorGenerator
import shared.*
import tornadofx.*
import java.awt.Color
import java.awt.Dimension
import java.util.concurrent.ConcurrentHashMap
import javax.swing.BorderFactory
import javax.swing.SwingUtilities
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

typealias Conf = ConfigurationData

/*fun main(args: Array<String>) {
    Application.launch(SimulatorApp::class.java, *args)
}*/

class ConfigurationData {
    companion object {
        @Volatile
        var clazzOfPopulation: KClass<out Population>? = PopulationProtocol::class
        @Volatile
        var isFastForward = false
        @Volatile
        var numOfFastForwardStep:Long = 0
        @Volatile
        var maximumSelectionTimes: Long = 1000000
        @Volatile
        var schedulerClazz: KClass<out Scheduler>? = RandomScheduler::class
        @Volatile
        var currentPopulationProtocolFunction: KFunction<Boolean>? = getInteractionMethodAndName(clazzOfPopulation)?.values?.elementAt(0) as KFunction<Boolean>
        @Volatile
        var currentShapeConstructionProtocolFunction: KFunction<Pair<Boolean, Boolean>>? = null
        @Volatile
        var currentGridNetworkFunction: KFunction<Triple<Boolean, Pair<String, String>, Boolean>>? = null
        val initialState = ConcurrentHashMap<String,Int>()
        @Synchronized
        fun canBuild(): Boolean {
            val captureOfClazz = clazzOfPopulation
            val captureOfScheduler = schedulerClazz
            val captureOfMaximumSelectionTime = maximumSelectionTimes
            val captureOfIsFastForward = isFastForward
            val captureOfNumForFastForwardStep = numOfFastForwardStep
            val captureOfPopulationProtocolFunction: KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction: KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction: KFunction<Triple<Boolean, Pair<String, String>, Boolean>>? = currentGridNetworkFunction
            if (captureOfClazz == null) {
                println("ClazzOfPopulation is null.")
                return false
            }
            if (initialState.values.sum() <= 0) {
                println("No nodes existing.")
                return false
            }
            var res = true
            when (captureOfClazz) {
                PopulationProtocol::class -> res = captureOfPopulationProtocolFunction != null
                ShapeConstructingPopulation::class -> res = captureOfShapeConstructionProtocolFunction != null
                GridNetworkConstructingPopulation::class -> res = captureOfGridNetworkFunction != null
            }
            return res
        }

        @Synchronized
        fun build(): SimulationGenerator {
            println("ReBuild")

            val captureOfClazz = clazzOfPopulation
            val captureOfScheduler = schedulerClazz
            val captureOfMaximumSelectionTime = maximumSelectionTimes
            val captureOfIsFastForward = isFastForward
            val captureOfNumForFastForwardStep = numOfFastForwardStep
            val clazzOfFunction = when (captureOfClazz) {
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }
            val captureOfPopulationProtocolFunction: KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction: KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction: KFunction<Triple<Boolean, Pair<String, String>, Boolean>>? = currentGridNetworkFunction
            if (captureOfScheduler == null) {
                throw IllegalArgumentException("Cannot determine the class of population")
            }
            var flagOfUnset = false
            when (captureOfClazz) {
                PopulationProtocol::class -> if (captureOfPopulationProtocolFunction == null) flagOfUnset = true
                ShapeConstructingPopulation::class -> if (captureOfShapeConstructionProtocolFunction == null) flagOfUnset = true
                GridNetworkConstructingPopulation::class -> if (captureOfGridNetworkFunction == null) flagOfUnset = true
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }
            if (flagOfUnset) throw IllegalArgumentException("Function is not selected properly")

            val population = when (captureOfClazz) {
                PopulationProtocol::class -> {
                    val symbols = getSymbolInternal(clazzOfFunction,
                            captureOfPopulationProtocolFunction as KFunction<*>)
                    symbols.forEach({ it -> println(it) })
                    PopulationProtocol(captureOfScheduler.createInstance(),
                            { a: ModelNode, b: ModelNode
                                ->
                                captureOfPopulationProtocolFunction.call(clazzOfFunction.companionObjectInstance, a, b)
                            },
                            symbols,
                            initialState.toMap()
                    )
                }
                ShapeConstructingPopulation::class -> {
                    @Suppress("UNCHECKED_CAST")
                    val symbols = getSymbolInternal(clazzOfFunction,
                            captureOfShapeConstructionProtocolFunction as KFunction<*>)

                    ShapeConstructingPopulation(captureOfScheduler.createInstance(),
                            { a, b, list ->
                                captureOfShapeConstructionProtocolFunction.call(clazzOfFunction.companionObjectInstance, a, b, list)
                            },
                            symbols,
                            initialState.toMap()
                    )

                }
                GridNetworkConstructingPopulation::class -> {
                    @Suppress("UNCHECKED_CAST")
                    val symbols = getSymbolInternal(clazzOfFunction, captureOfGridNetworkFunction as KFunction<*>)

                    GridNetworkConstructingPopulation(captureOfScheduler.createInstance(),
                            { a, b ->
                                captureOfGridNetworkFunction.call(clazzOfFunction.companionObjectInstance, a, b)
                            },
                            symbols,
                            initialState.toMap()
                    )
                }
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }

            return when (population) {
                is PopulationProtocol ->
                    PopulationProtocolGenerator(population, captureOfMaximumSelectionTime,captureOfIsFastForward, captureOfNumForFastForwardStep,convertFunctionNameToDisplayName(captureOfPopulationProtocolFunction!!.name))
                is ShapeConstructingPopulation ->
                    ShapeConstructorGenerator(population, captureOfMaximumSelectionTime, captureOfIsFastForward, captureOfNumForFastForwardStep,convertFunctionNameToDisplayName(captureOfShapeConstructionProtocolFunction!!.name))
                is GridNetworkConstructingPopulation ->
                    GridNetworkGenerator(population, captureOfMaximumSelectionTime, captureOfIsFastForward, captureOfNumForFastForwardStep, convertFunctionNameToDisplayName(captureOfGridNetworkFunction!!.name))
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }

        }

        private fun getInitialStateMapTemplateInternal(clazz: KClass<out InteractionFunctions>, function: KFunction<*>): Map<String, Int> {
            @Suppress("UNCHECKED_CAST")
            println("Map name ${getInitialStateMapNameFromInteractionFuncName(
                    function.name
            )}")
            @Suppress("UNCHECKED_CAST")
            val res = clazz.companionObject?.declaredMemberProperties?.filter { it ->
                println("Map name key ${it.name}")
                it.name == getInitialStateMapNameFromInteractionFuncName(
                        function.name
                )
            }?.get(0) as KProperty1<MutableMap<String, Int>, Map<String, Int>>

            return res.getter.call(this)
        }

        fun getInitialStateMapTemplate(): Map<String, Int> {
            val captureOfClazz = clazzOfPopulation
            val captureOfPopulationProtocolFunction: KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction: KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction: KFunction<Triple<Boolean, Pair<String, String>, Boolean>>? = currentGridNetworkFunction
            return getInitialStateMapTemplateInternal(when (captureOfClazz) {
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }, when (captureOfClazz) {
                PopulationProtocol::class -> captureOfPopulationProtocolFunction as KFunction<*>
                ShapeConstructingPopulation::class -> captureOfShapeConstructionProtocolFunction as KFunction<*>
                GridNetworkConstructingPopulation::class -> captureOfGridNetworkFunction as KFunction<*>
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            })
        }

        private fun getSymbolInternal(clazz: KClass<out InteractionFunctions>, function: KFunction<*>): Set<String> {
            @Suppress("UNCHECKED_CAST")
            val res = clazz.companionObject?.declaredMemberProperties?.filter { it ->
                it.name == getSymbolNameFromInteractionFuncName(function.name)
            }?.get(0) as KProperty1<MutableSet<String>, Set<String>>
            return res.getter.call(this)
        }

        fun getSymbol(): Set<String> {
            val captureOfClazz = clazzOfPopulation
            val captureOfPopulationProtocolFunction: KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction: KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction: KFunction<Triple<Boolean, Pair<String, String>, Boolean>>? = currentGridNetworkFunction
            return getSymbolInternal(when (captureOfClazz) {
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }, when (captureOfClazz) {
                PopulationProtocol::class -> captureOfPopulationProtocolFunction as KFunction<*>
                ShapeConstructingPopulation::class -> captureOfShapeConstructionProtocolFunction as KFunction<*>
                GridNetworkConstructingPopulation::class -> captureOfGridNetworkFunction as KFunction<*>
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            })
        }

        private fun getInitialStateMapNameFromInteractionFuncName(nameOfMethod: String): String = nameOfMethod.removeSuffix("Func").plus("InitialState")
        private fun getSymbolNameFromInteractionFuncName(nameOfMethod: String): String = nameOfMethod.removeSuffix("Func").plus("Symbol")
        private fun convertFunctionNameToDisplayName(str: String): String {
            val words = str.split("(?=[A-Z])".toRegex()).toMutableList()
            if (words.isNotEmpty()) {
                val oriWord = words[0]
                println(oriWord)
                val newWord = oriWord.replaceFirst(oriWord[0], oriWord[0].toUpperCase())
                println(newWord)
                words[0] = newWord
            }
            return words.reduce { acc, s -> acc.plus(" $s") }.removeSuffix(" Func")
        }


        fun getInteractionMethodAndName(clazz: KClass<out Population>?): Map<String, KFunction<*>>? {
            return when (clazz) {
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> null
            }?.companionObject
                    ?.declaredFunctions
                    ?.map { function -> Pair(convertFunctionNameToDisplayName(function.name), function) }
                    ?.toMap()
        }

    }
}

class SimulatorApp : App(SimulatorAppView::class)

object TextFieldUpdateEvent : FXEvent(EventBus.RunOn.ApplicationThread)
object TerminateEvent : FXEvent(EventBus.RunOn.ApplicationThread)
object HasStoppedNoTerminateEvent : FXEvent(EventBus.RunOn.ApplicationThread)
object ModelSelectionChangeEvent : FXEvent(EventBus.RunOn.ApplicationThread)
object FunctionSelectionChangeEvent : FXEvent(EventBus.RunOn.ApplicationThread)

class SimulatorAppView : View() {

    @Volatile
    private var generator = Conf.build()

    @Volatile
    private var shouldStop = false

    @Synchronized
    private fun runProcess(generator: SimulationGenerator) {
        tornadofx.runAsync {
            while (!shouldStop && !generator.shouldTerminate()) {
                if (!shouldStop) {
                    generator.nextEvents()
                    fire(TextFieldUpdateEvent)
                }
            }
        }.ui {
            if (generator.shouldTerminate()) {
                println("Terminating in UI...")
                fire(TerminateEvent)
            }else{
                fire(HasStoppedNoTerminateEvent)
            }
        }
    }

    private var isFirstRun = true
    private val swingNode = SwingNode()
    private var viewer = Viewer(generator.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
    @Synchronized
    private fun createAndSetSwingContent() {

        SwingUtilities.invokeLater {
            primaryStage.setOnCloseRequest {
                shouldStop = true
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

    @Synchronized
    private fun resetSwingContent() {
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
                shouldStop = true
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

    @Synchronized
    private fun resetGraphStates() {
        generator.restart()
        resetSwingContent()
        generator.begin()
    }

    @Synchronized
    private fun resetGenerator() {
        resetSwingContent()
        generator.begin()
    }

    init {
        generator.begin()
        primaryStage.maxHeight = 750.0
        primaryStage.minWidth = 1280.0
        primaryStage.isResizable = false

    }
    val initialInputs = mutableListOf<InitialInputConfiguration>().observable()
    val initialStatesTableView = TableView(initialInputs)
    val applyBtn = Button("Apply Settings")
    val startBtn = Button("Start")
    override val root = hbox {
        title = "Network Simulator GUI Alpha"
        //UI Left Partition
        vbox(20) {
            vboxConstraints {
                prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.55))
            }
            this += swingNode
            createAndSetSwingContent()
            hbox(5, Pos.BASELINE_CENTER) {

                this += startBtn.apply {
                    this.isDisable = true
                    subscribe<TerminateEvent> {
                        text = "Restart"
                        shouldStop = true
                        applyBtn.isDisable = false
                    }
                    subscribe<HasStoppedNoTerminateEvent> {
                        text = "Continue"
                        applyBtn.isDisable = false
                    }
                    action {
                        if (isFirstRun) isFirstRun = false
                        isDisable = true
                        if (generator.shouldTerminate()) {
                            runAsync {
                                resetGraphStates()
                            }.onSucceeded = EventHandler {
                                text = "Pause"
                                shouldStop = false
                                applyBtn.isDisable = true
                                runProcess(generator)
                            }
                        } else {
                            if (text == "Start" || text == "Restart" || text == "Continue") {
                                text = "Pause"
                                shouldStop = false
                                applyBtn.isDisable = true
                            } else if (text == "Pause") {
                                text = "Continue"
                                shouldStop = true
                            }
                            if (!shouldStop) runProcess(generator)
                        }
                        isDisable = false
                    }
                }

            }
            hbox {
                //text {
                    vboxConstraints {
                        margin = Insets(0.0, 30.0, 0.0, 50.0)
                    }
                vbox{
                    text( "Running : - \n"){
                        subscribe<TextFieldUpdateEvent> {
                            text = "Running : ${generator.nameOfPopulation} \n"
                        }
                        tooltip("The name of loaded protocol. ")
                    }
                    text("Total number of Node : - \n"){
                        subscribe<TextFieldUpdateEvent> {
                           text = "Total number of Node : ${generator.population.nodes.size} \n"
                        }
                        tooltip("Total number of node in the loaded population.")
                    }
                    text("Effective selection times : - \n"){
                        subscribe<TextFieldUpdateEvent> {
                            text = "Effective selection times :  ${generator.countOfEffectiveSelect} \n"
                        }
                        tooltip("The counting of how many times \"Effective selection\" happens. " +
                                "\"Effective selection\" : a selection change at least one state for the population"
                        )
                    }
                    text("Scheduler selection times : - \n"){
                        subscribe<TextFieldUpdateEvent> {
                            text = "Scheduler selection times :  ${generator.countOfTotalSelect} \n"
                        }
                        tooltip("The counting of how many times that the scheduler took a selection")
                    }
                    text("Terminating Threshold for ineffective selection : - \n"){
                        subscribe<TextFieldUpdateEvent> {
                            text = "Terminating Threshold for ineffective selection : ${generator.terminateThreshold} \n"
                        }
                        tooltip(" The simulation process will " +
                                "be terminated after the countOfEffectiveSelect of consistent ineffective section larger than the threshold.")
                    }
                }

                    text {
                        hboxConstraints {
                            margin = Insets(0.0, 20.0, 30.0, 30.0)
                        }
                        text = "Number of States distributed on nodes: - "
                        subscribe<TextFieldUpdateEvent> {
                            text = "Number of Node in " + generator.population.statisticsMap.map { its -> "\n ${its.key} :  ${its.value} " }.reduce(String::plus).removeSuffix("; ")
                        }
                    }

            }

        }
        //UI Right Partition
        vbox {
            prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.45))
            hbox(20){
                this += applyBtn.apply {
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 20.0)
                    }
                    action {
                        isDisable = true

                        if (startBtn.text == "Start" || startBtn.text == "Restart"|| startBtn.text == "Continue") {
                            Conf.initialState.clear()
                            Conf.getInitialStateMapTemplate().forEach { it ->
                                if (it.value > NUM_NOT_SPECIFIED){
                                    Conf.initialState[it.key] = it.value
                                }
                            }
                            initialInputs.forEach{it ->
                                if (it.count.isInt() && it.count.toInt() > 0) {
                                    Conf.initialState[it.state] = it.count.toInt()
                                }
                            }
                            if (Conf.canBuild()) {
                                generator = Conf.build()

                                println("Max time "+generator.maxTimes)
                                println("Fast forward "+generator.preExecutedSteps)

                                resetGenerator()
                                fire(TextFieldUpdateEvent)
                                if (isFirstRun && startBtn.isDisabled) startBtn.isDisable = false
                                if (startBtn.text == "Restart"|| startBtn.text == "Continue") startBtn.text = "Start"
                            } else {
                                print("The setting is inValid. Please check again.")
                            }
                        }
                        isDisable = false
                    }
                }
                text("Model Type: ") {
                    font = Font(12.0)
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 0.0)
                    }
                }
                this += ComboBox<String>().apply {
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 5.0)
                    }
                    items = FXCollections.observableArrayList(
                            "Basic Population Protocol",
                            "Network Constructor (Shape Constructor)",
                            "Terminated Grid Network Constructor"
                    )
                    selectionModel.select(0)
                    tooltip(items[selectionModel.selectedIndex])
                    Conf.clazzOfPopulation = when (selectionModel.selectedIndex) {
                        0 -> PopulationProtocol::class
                        1 -> ShapeConstructingPopulation::class
                        2 -> GridNetworkConstructingPopulation::class
                        else -> null
                    }
                    selectionModel.selectedIndexProperty().onChange {
                        Conf.clazzOfPopulation = when (it) {
                            0 -> PopulationProtocol::class
                            1 -> ShapeConstructingPopulation::class
                            2 -> GridNetworkConstructingPopulation::class
                            else -> null
                        }
                        tooltip(items[selectionModel.selectedIndex])
                        val functionMap = Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty().toList()
                        @Suppress("UNCHECKED_CAST")
                        when (Conf.clazzOfPopulation) {
                            PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[0].second
                                    as KFunction<Boolean>
                            ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[0].second
                                    as KFunction<Pair<Boolean, Boolean>>
                            GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[0].second
                                    as KFunction<Triple<Boolean, Pair<String, String>, Boolean>>
                        }
                        fire(ModelSelectionChangeEvent)
                    }
                }
            }
            hbox(20) {



            /*}
            hbox(20) {*/
                text("Interaction Functions: ") {
                    font = Font(12.0)
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 20.0)
                    }
                }
                this += ComboBox<String>().apply {
                    hboxConstraints {
                        margin = Insets(20.0, 20.0, 0.0, 5.0)
                    }
                    var functionMap = Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty()
                    items = functionMap.keys.toList().observable()
                    selectionModel.select(0)
                    tooltip(items[selectionModel.selectedIndex])
                    subscribe<ModelSelectionChangeEvent> {
                        functionMap = Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty()
                        items = functionMap.keys.toList().observable()
                        selectionModel.select(0)
                        tooltip(items[selectionModel.selectedIndex])
                        @Suppress("UNCHECKED_CAST")
                        when (Conf.clazzOfPopulation) {
                            PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Boolean>
                            ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Pair<Boolean, Boolean>>
                            GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Triple<Boolean, Pair<String, String>, Boolean>>
                        }
                        fire(FunctionSelectionChangeEvent)
                    }
                    @Suppress("UNCHECKED_CAST")
                    when (Conf.clazzOfPopulation) {
                        PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]] as KFunction<Boolean>
                        ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]] as KFunction<Pair<Boolean, Boolean>>
                        GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[items[selectionModel.selectedIndex]] as KFunction<Triple<Boolean, Pair<String, String>, Boolean>>
                    }

                    selectionModel.selectedIndexProperty().onChange {
                        if (it >= 0) {
                            @Suppress("UNCHECKED_CAST")
                            when (Conf.clazzOfPopulation) {
                                PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Boolean>
                                ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Pair<Boolean, Boolean>>
                                GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Triple<Boolean, Pair<String, String>, Boolean>>
                            }
                            tooltip(items[selectionModel.selectedIndex])
                        }
                        fire(FunctionSelectionChangeEvent)
                    }

                }
            }
            val selected = SimpleBooleanProperty(false)
            hbox(20) {
                text("Enable Pre-selections: ") {
                    font = Font(12.0)
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 20.0)
                    }
                }
                checkbox {
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 0.0)
                    }
                    Conf.isFastForward = this.isSelected
                    action {
                        Conf.isFastForward = this.isSelected
                        selected.set(this.isSelected)
                    }
                }

                text("Pre-selection Times: "){
                    font = Font(12.0)
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 0.0)
                    }
                }
                val timesOfPreRunning = textfield("0") {
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 0.0)
                    }
                    prefWidth = 0.3 * this@hbox.width
                    this@hbox.widthProperty().onChange {
                        this.prefWidth = 0.3 * this@hbox.width
                    }
                    isDisable = !selected.value
                    textProperty().addListener({ _, _, newV ->
                        var value = newV
                        if (!value.matches("[0]|([123456789][0123456789]*)".toRegex()) || !value.isLong() || value.toLong() < 0) {
                            value = newV.replace("[^\\d]".toRegex(), "")
                            if (value.isEmpty()) value.plus("0")
                            if (value.isLong() && value.toLong() < 0) value = abs(value.toLong()).toString()
                        }
                        if(value.isLong() && value.toLong() >= 0){
                            Conf.numOfFastForwardStep = value.toLong()
                            text = value
                        }

                    })
                }
                selected.onChange { timesOfPreRunning.isDisable = !selected.value }

            }
            hbox(20) {
                text("Maximum times for interaction: ") {
                    font = Font(12.0)
                    hboxConstraints {
                        margin = Insets(20.0, 0.0, 0.0, 20.0)
                    }
                }
                textfield(Conf.maximumSelectionTimes.toString()) {
                    hboxConstraints {
                        margin = Insets(20.0, 20.0, 0.0, 0.0)
                    }
                    textProperty().addListener({ _, _, newV ->
                        var value = newV
                        if (!value.matches("[0]|([123456789][0123456789]*)".toRegex()) || !value.isLong() || value.toLong() < 0) {
                            value = newV.replace("[^\\d]".toRegex(), "")
                            if (value.isEmpty()) value.plus("0")
                            if (value.isLong() && value.toLong() < 0) value = abs(value.toLong()).toString()

                        }
                        if(value.isLong() && value.toLong() >= 0){
                            Conf.maximumSelectionTimes = value.toLong()
                            text = value
                        }

                    })
                }
            }

            text("Initial States: ") {
                font = Font(14.0)
                vboxConstraints {
                    margin = Insets(0.0, 20.0, 0.0, 20.0)
                }
            }
            this += initialStatesTableView.apply {
                vboxConstraints {
                    margin = Insets(20.0, 20.0, 20.0, 20.0)
                }
                Conf.getInitialStateMapTemplate().forEach { key, value ->
                    if (value == NUM_NOT_SPECIFIED) {
                        initialInputs.add(InitialInputConfiguration(key))
                    }
                }
                subscribe<FunctionSelectionChangeEvent> {
                    initialInputs.clear()
                    Conf.getInitialStateMapTemplate().forEach { key, value ->
                        if (value == NUM_NOT_SPECIFIED) {
                            initialInputs.add(InitialInputConfiguration(key))
                        }
                    }
                }
                initialStatesTableView.column("State", InitialInputConfiguration::state)
                initialStatesTableView.column("# Initial Config.", InitialInputConfiguration::count).apply {
                    makeEditable()
                    onEditCommit{
                        try{
                            var strV = newValue.toString()
                            if (!strV.matches("[1-9][0-9]*".toRegex()) || !strV.isInt() || strV.toInt() <= 0) {
                                strV = strV.replace("[^\\d]".toRegex(), "")
                                if (strV.isEmpty()) strV.plus("0")
                                if (strV.isInt() && strV.toInt() < 0) strV = abs(strV.toInt()).toString()
                            }
                            if(strV.matches("[0]|[1-9][0-9]*".toRegex()) && strV.isInt() && strV.toInt() >= 0){
                                this.rowValue.count = strV
                            }else{
                                this.rowValue.count = if(Conf.initialState.containsKey(this.rowValue.state) && Conf.initialState[this.rowValue.state]!! > NUM_NOT_SPECIFIED) Conf.initialState[this.rowValue.state].toString() else "0"
                            }
                        }catch (err: NumberFormatException){
                            this.rowValue.count = if(Conf.initialState.containsKey(this.rowValue.state) && Conf.initialState[this.rowValue.state]!! > NUM_NOT_SPECIFIED) Conf.initialState[this.rowValue.state].toString() else "0"
                        }
                        initialStatesTableView.refresh()
                    }
                }
                initialStatesTableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            }
        }
    }
}

class InitialInputConfiguration(state: String, count: String = "0") {
    var state by property(state)
    fun stateProperty() = getProperty(InitialInputConfiguration::state)
    var count by property(count)
    fun countProperty() = getProperty(InitialInputConfiguration::count)
}

