package presentation.ui

import javafx.application.Application
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
import org.graphstream.ui.layout.Layouts
import org.graphstream.ui.view.Viewer
import presentation.generator.SimulationGenerator
import presentation.generator.concrete.GridNetworkGenerator
import presentation.generator.concrete.PopulationProtocolGenerator
import presentation.generator.concrete.ShapeConstructorGenerator
import scheduler.RandomScheduler
import scheduler.Scheduler
import shared.*
import tornadofx.*
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.SwingUtilities
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties

typealias Conf = ConfigurationData

fun main(args: Array<String>) {
    Application.launch(SimulatorApp::class.java, *args)
}
class ConfigurationData{
    companion object {
        @Volatile var clazzOfPopulation: KClass<out Population>? = PopulationProtocol::class
        @Volatile var isFastForward = false
        @Volatile var numOfFastForwardStep = 0
        @Volatile var maximumSelectionTimes: Long = 1000000
        @Volatile var schedulerClazz: KClass<out Scheduler>? = RandomScheduler::class
        @Volatile var currentPopulationProtocolFunction : KFunction<Boolean>? = getInteractionMethodAndName(clazzOfPopulation)?.values?.elementAt(0) as KFunction<Boolean>
        @Volatile var currentShapeConstructionProtocolFunction : KFunction<Pair<Boolean, Boolean>>? = null
        @Volatile var currentGridNetworkFunction:KFunction<Triple<Boolean, Pair<String,String>,Boolean>>? = null
        val initialState = mutableMapOf<String,Int>()
        @Synchronized fun canBuild(): Boolean{
            val captureOfClazz = clazzOfPopulation
            val captureOfScheduler = schedulerClazz
            val captureOfMaximumSelectionTime = maximumSelectionTimes
            val captureOfIsFastForward = isFastForward
            val captureOfNumForFastForwardStep = numOfFastForwardStep
            val captureOfPopulationProtocolFunction : KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction : KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction:KFunction<Triple<Boolean, Pair<String,String>,Boolean>>? = currentGridNetworkFunction
            if (captureOfClazz == null){
                println("ClazzOfPopulation is null.")
                return false
            }
            if(initialState.values.sum()<=0){
                println("No nodes existing.")
                return false
            }
            var res = true
            when(captureOfClazz){
                PopulationProtocol::class -> res = captureOfPopulationProtocolFunction != null
                ShapeConstructingPopulation::class -> res = captureOfShapeConstructionProtocolFunction != null
                GridNetworkConstructingPopulation::class -> res = captureOfGridNetworkFunction != null
            }
            return res
        }
        @Synchronized fun build(): SimulationGenerator {
            print("ReBuild")
            val captureOfClazz = clazzOfPopulation
            val captureOfScheduler = schedulerClazz
            val captureOfMaximumSelectionTime = maximumSelectionTimes
            val captureOfIsFastForward = isFastForward
            val captureOfNumForFastForwardStep = numOfFastForwardStep
            val clazzOfFunction = when(captureOfClazz){
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }
            val captureOfPopulationProtocolFunction : KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction : KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction:KFunction<Triple<Boolean, Pair<String,String>,Boolean>>? = currentGridNetworkFunction
            if (captureOfScheduler == null){
                throw IllegalArgumentException("Cannot determine the class of population")
            }
            var flagOfUnset = false
            when(captureOfClazz){
                PopulationProtocol::class -> if (captureOfPopulationProtocolFunction == null) flagOfUnset = true
                ShapeConstructingPopulation::class -> if (captureOfShapeConstructionProtocolFunction == null) flagOfUnset = true
                GridNetworkConstructingPopulation::class -> if (captureOfGridNetworkFunction == null) flagOfUnset = true
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }
            if (flagOfUnset) throw IllegalArgumentException("Function is not selected properly")

            val population = when(captureOfClazz){
                PopulationProtocol::class -> {
                    val symbols = getSymbolInternal(clazzOfFunction,
                            captureOfPopulationProtocolFunction as KFunction<*>)
                    symbols.forEach({it -> println(it)})
                    PopulationProtocol(captureOfScheduler.createInstance(),
                            {a,b-> captureOfPopulationProtocolFunction.call(a,b)},symbols,initialState)
                }
                ShapeConstructingPopulation::class ->{
                    @Suppress("UNCHECKED_CAST")
                    val symbols = getSymbolInternal(clazzOfFunction,
                            captureOfShapeConstructionProtocolFunction as KFunction<*>)

                    ShapeConstructingPopulation(captureOfScheduler.createInstance(),
                            {a,b,list -> captureOfShapeConstructionProtocolFunction.call(a,b,list)},symbols,initialState
                    )
                }
                GridNetworkConstructingPopulation::class ->{
                    @Suppress("UNCHECKED_CAST")
                    val symbols = getSymbolInternal(clazzOfFunction,captureOfGridNetworkFunction as KFunction<*>)

                    GridNetworkConstructingPopulation(captureOfScheduler.createInstance(),
                            {a,b -> captureOfGridNetworkFunction.call(a,b)},symbols,initialState
                    )
                }
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }
            println("Constructing Population with num of nodes ${population.nodes.size}")
            return when(population){
                is PopulationProtocol ->
                    PopulationProtocolGenerator(population ,captureOfMaximumSelectionTime,captureOfIsFastForward,captureOfNumForFastForwardStep)
                is ShapeConstructingPopulation ->
                    ShapeConstructorGenerator(population,captureOfMaximumSelectionTime,captureOfIsFastForward,captureOfNumForFastForwardStep)
                is GridNetworkConstructingPopulation->
                    GridNetworkGenerator(population,captureOfMaximumSelectionTime, captureOfIsFastForward,captureOfNumForFastForwardStep)
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            }

        }
        private fun getInitialStateMapTemplateInternal(clazz:  KClass<out InteractionFunctions>, function: KFunction<*>): Map<String, Int>{
            //val receiver = mutableMapOf<String,Int>()
            @Suppress("UNCHECKED_CAST")
            println("Map name ${getInitialStateMapNameFromInteractionFuncName(
                    function.name
            )}")
            val res = clazz.companionObject?.declaredMemberProperties?.
                    filter { it ->
                        println("Map name key ${it.name}")
                        it.name == getInitialStateMapNameFromInteractionFuncName(
                              function.name
                        )
                    }?.get(0) as KProperty1<MutableMap<String, Int>,Map<String, Int>>

            return res.getter.call(this)
        }
        fun getInitialStateMapTemplate(): Map<String, Int>{
            val captureOfClazz = clazzOfPopulation
            val captureOfPopulationProtocolFunction : KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction : KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction:KFunction<Triple<Boolean, Pair<String,String>,Boolean>>? = currentGridNetworkFunction
            return getInitialStateMapTemplateInternal(when(captureOfClazz){
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            },when(captureOfClazz){
                PopulationProtocol::class -> captureOfPopulationProtocolFunction as KFunction<*>
                ShapeConstructingPopulation::class -> captureOfShapeConstructionProtocolFunction  as KFunction<*>
                GridNetworkConstructingPopulation::class -> captureOfGridNetworkFunction as KFunction<*>
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            })
        }

        private fun getSymbolInternal(clazz:  KClass<out InteractionFunctions>,function: KFunction<*>): Set<String>{
            println("fun name: ${function.name}")
           // val receiver = mutableSetOf<String>()
            @Suppress("UNCHECKED_CAST")
             val res = clazz.companionObject?.declaredMemberProperties?.
                    filter { it ->
                        it.name == getSymbolNameFromInteractionFuncName(function.name)
                    }?.
                    get(0) as KProperty1<MutableSet<String>,Set<String>>
             println(res.name)
             return  res.
                     getter.call(this)
                     //call(clazz::companionObjectInstance)
        }
        fun getSymbol(): Set<String>{
            val captureOfClazz = clazzOfPopulation
            val captureOfPopulationProtocolFunction : KFunction<Boolean>? = currentPopulationProtocolFunction
            val captureOfShapeConstructionProtocolFunction : KFunction<Pair<Boolean, Boolean>>? = currentShapeConstructionProtocolFunction
            val captureOfGridNetworkFunction:KFunction<Triple<Boolean, Pair<String,String>,Boolean>>? = currentGridNetworkFunction
            return getSymbolInternal(when(captureOfClazz){
                PopulationProtocol::class -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            },when(captureOfClazz){
                PopulationProtocol::class -> captureOfPopulationProtocolFunction as KFunction<*>
                ShapeConstructingPopulation::class -> captureOfShapeConstructionProtocolFunction  as KFunction<*>
                GridNetworkConstructingPopulation::class -> captureOfGridNetworkFunction as KFunction<*>
                else -> throw UnsupportedOperationException("Sorry. Not support yet")
            })
        }

        private fun getInitialStateMapNameFromInteractionFuncName(nameOfMethod: String): String = nameOfMethod.removeSuffix("Func").plus("InitialState")
        private fun getSymbolNameFromInteractionFuncName(nameOfMethod: String):String = nameOfMethod.removeSuffix("Func").plus("Symbol")
        fun getInteractionMethodAndName(clazz: KClass<out Population>?):Map<String,KFunction<*>>?{
            return when(clazz){
                PopulationProtocol::class  -> PopulationProtocolFunctions::class
                ShapeConstructingPopulation::class -> ShapeConstructionFunctions::class
                GridNetworkConstructingPopulation::class -> GridNetworkConstructingFunctions::class
                else -> null
            }?.companionObject
                    ?.declaredFunctions
                    ?.map{ function -> Pair(convertFunctionNameToDisplayName(function.name),function) }
                    ?.toMap()
        }

        private fun convertFunctionNameToDisplayName(str: String): String{
            val words = str.split("(?=[A-Z])".toRegex()).toMutableList()
            if(words.isNotEmpty()){
                val oriWord = words[0]
                println(oriWord)
                val newWord = oriWord.replaceFirst(oriWord[0],oriWord[0].toUpperCase())
                println(newWord)
                words[0] = newWord
            }
            return words.reduce { acc, s ->  acc.plus(" $s")}.removeSuffix(" Func")
        }
    }
}
class SimulatorApp : App(SimulatorAppView::class)

object TextFieldUpdateEvent: FXEvent(EventBus.RunOn.ApplicationThread)
object TerminateEvent: FXEvent(EventBus.RunOn.ApplicationThread)
object ModelSelectionChangeEvent: FXEvent(EventBus.RunOn.ApplicationThread)
object FunctionSelectionChangeEvent: FXEvent(EventBus.RunOn.ApplicationThread)
object ModelModifiedEvent: FXEvent(EventBus.RunOn.ApplicationThread)
class SimulatorAppView : View() {

    /*private val generator = PopulationProtocolGenerator(
            DancingProtocol(
                    initialStates = mapOf(Pair("L", 6), Pair("F", 9)), scheduler = RandomScheduler()
            ),
            1000000,
            false,
            300
    )

   private val generator = ShapeConstructorGenerator(
             population = ShapeConstructingPopulation(
             scheduler = RandomScheduler(), interactFunction = { nodeA, nodeB, map ->
                 ShapeConstructionFunctions.globalStarFunc(nodeA, nodeB, map)
             }, symbols = setOf("c", "p"), initialStates = mapOf(Pair("c", 12))
     ),maxTimes = 100000)


    val generator = GridNetworkGenerator(GridNetworkConstructingPopulation(interactFunction =
    {
        firstPair, secondPair-> GridNetworkConstructingFunctions.squareGridNetworkFunc(firstPair,secondPair)


    }, symbols = setOf("Lu", "q0", "q1", "Lr", "Ld", "Ll", "Lu"),
            initialStates = mapOf(Pair("q0", 3), Pair("Lu", 1))
    ))*/

    @Volatile private var generator = Conf.build()
    @Volatile private var shouldStop = false
    @Volatile private var effectiveInteractionCount = 0
    @Synchronized private fun runProcess(generator: SimulationGenerator) {
        tornadofx.runAsync {
            while (!shouldStop && !generator.shouldTerminate()) {
                if(!shouldStop) {
                    generator.nextEvents()
                    effectiveInteractionCount++
                    fire(TextFieldUpdateEvent)
                }
            }
        }.ui {
            if (generator.shouldTerminate()) {
                println("Terminating in UI...")
                fire(TerminateEvent)
            }
        }
    }
    private var isFirstRun = true
    private val swingNode = SwingNode()
    private var viewer = Viewer(generator.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
    @Synchronized private fun createAndSetSwingContent() {

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
    @Synchronized private fun resetSwingContent(){
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
    @Synchronized private fun resetGraphStates() {
        effectiveInteractionCount = 0
        generator.restart()
        generator.begin()
        resetSwingContent()
    }

    @Synchronized private fun resetGenerator(){
        effectiveInteractionCount = 0
        generator.begin()
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
        //Left Partition
        vbox(20) {
            vboxConstraints {
                prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.55))
            }
            pane {
                this += swingNode
                createAndSetSwingContent()
            }
            hbox(5, Pos.BASELINE_CENTER){
                val applyBtn = Button("Apply Settings")
                val startBtn = Button("Start").apply {
                    this.isDisable = true
                    subscribe<TerminateEvent>{
                        text = "Restart"
                        shouldStop = true
                    }
                    subscribe<ModelModifiedEvent>{
                        shouldStop = true
                        text = "Start"
                        runAsync{
                            resetGenerator()
                        }
                    }
                    action {
                        isFirstRun = false
                        isDisable = true
                        if (generator.shouldTerminate()) {
                            runAsync {
                                resetGraphStates()
                            }.onSucceeded = EventHandler {
                                text = "Pause"
                                shouldStop = false
                                runProcess(generator)
                                isDisable = false
                                applyBtn.isDisable = false
                            }
                        }else{
                            if(text == "Start"){
                                text = "Pause"
                                shouldStop = false
                                applyBtn.isDisable = true
                            }else if(text == "Pause"){
                                text = "Start"
                                shouldStop = true
                            }
                            if(!shouldStop) runProcess(generator)
                            isDisable = false
                        }
                    }
                }
                spacing = 10.0
                this += startBtn
                this += applyBtn.apply {
                    action {
                        if (startBtn.text == "Start"){
                            if(Conf.canBuild()){
                                generator = Conf.build()
                                fire(ModelModifiedEvent)
                                if(isFirstRun && startBtn.isDisabled) startBtn.isDisable = false
                            }else{
                                tooltip{
                                    text = "The setting is inValid. Please check again."
                                }
                            }
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
                    text = "Number of States distributed on nodes: " + generator.population.statisticsMap.map{ it -> " ${it.key}: ${it.value}; " }.reduce(String::plus).removeSuffix("; ")
                    subscribe<TextFieldUpdateEvent> {
                        text = "Number of Node in " + generator.population.statisticsMap.map{ its -> " ${its.key}: ${its.value} " }.reduce(String::plus).removeSuffix("; ")
                    }
                }

            }
        }
        //Right Partition
        vbox {
            prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.45))

            text("Model Type") {
                font = Font(20.0)
                vboxConstraints {
                    margin = Insets(20.0, 20.0, 0.0, 20.0)
                }
            }
            hbox(20){
                this += ComboBox<String>().apply{
                    hboxConstraints {
                        margin = Insets(20.0, 20.0, 20.0, 20.0)
                    }
                    items = FXCollections.observableArrayList(
                            "Basic Population Protocol",
                            "Network Constructor (Shape Constructor)",
                            "Terminated Grid Network Constructor"
                    )
                    selectionModel.select(0)
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
                        val functionMap =  Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty().toList()
                        when (Conf.clazzOfPopulation) {
                            PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[0].second
                                    as KFunction<Boolean>
                            ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[0].second
                                    as KFunction<Pair<Boolean, Boolean>>
                            GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[0].second
                                    as KFunction<Triple<Boolean, Pair<String,String>,Boolean>>
                            else -> null
                        }
                        fire(ModelSelectionChangeEvent)
                    }
                }

                val selected = SimpleBooleanProperty(false)

                vbox(5){
                    hbox(5){
                        text{
                            text = "Enable Pre-selections: "
                        }
                        checkbox{
                            Conf.isFastForward = this.isSelected
                            action {
                                Conf.isFastForward = this.isSelected
                                selected.set(this.isSelected)
                            }
                        }
                    }
                    hbox(5){
                        text{
                            text = "Pre-selection Times: "
                        }
                        val timesOfPreRunning = textfield ("0"){
                            prefWidth = 0.5 * this@hbox.width
                            this@hbox.widthProperty().onChange {
                                this.prefWidth =  0.5 * this@hbox.width
                            }
                            isDisable = !selected.value
                            promptText = "Times"
                            textProperty().addListener({_, _, newV ->
                                if (!newV.matches("[0]|([123456789][01234567890]*)".toRegex())|| !newV.isInt()|| newV.toInt() < 0) {
                                    text = newV.replace("[^\\d]".toRegex(),"")
                                    if(newV.isEmpty()) newV.plus("0")
                                    if (newV.isInt() && newV.toInt()<0) text = abs(newV.toInt()).toString()
                                }else{
                                    text = newV
                                    Conf.numOfFastForwardStep = text.toInt()
                                }

                            })
                        }
                        selected.onChange { timesOfPreRunning.isDisable = !selected.value }
                    }
                }
            }
            hbox(5, Pos.BASELINE_LEFT){
                text{
                    text = "Maximum times for selection: "
                }
                textfield (Conf.maximumSelectionTimes.toString()){
                    textProperty().addListener({_, _, newV ->
                        if (!newV.matches("[0]|([123456789][01234567890]*)".toRegex()) || !newV.isInt() || newV.toInt() < 0) {
                            text = newV.replace("[^\\d]".toRegex(),"")
                            if(newV.isEmpty()) newV.plus("0")
                            if (newV.isInt() && newV.toInt()<0) text = abs(newV.toInt()).toString()
                        }else{
                            text = newV
                            Conf.numOfFastForwardStep = text.toInt()
                        }

                    })
                }
            }
            text("Interaction Functions") {
                font = Font(20.0)
                vboxConstraints {
                    margin = Insets(0.0, 20.0, 0.0, 20.0)
                }
            }
            hbox(20){
                this += ComboBox<String>().apply {
                    hboxConstraints {
                        margin = Insets(20.0, 20.0, 20.0, 20.0)
                    }
                    var functionMap = Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty()
                    items = functionMap.keys.toList().observable()
                    selectionModel.select(0)
                    subscribe<ModelSelectionChangeEvent> {
                        functionMap =  Conf.getInteractionMethodAndName(Conf.clazzOfPopulation).orEmpty()
                        items = functionMap.keys.toList().observable()
                        selectionModel.select(0)
                        when (Conf.clazzOfPopulation) {
                            PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Boolean>
                            ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Pair<Boolean, Boolean>>
                            GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[items[selectionModel.selectedIndex]]
                                    as KFunction<Triple<Boolean, Pair<String,String>,Boolean>>
                            else -> null
                        }
                        fire(FunctionSelectionChangeEvent)
                    }
                    when (Conf.clazzOfPopulation) {
                        PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]] as KFunction<Boolean>
                        ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]]  as KFunction<Pair<Boolean, Boolean>>
                        GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction =   functionMap[items[selectionModel.selectedIndex]] as KFunction<Triple<Boolean, Pair<String,String>,Boolean>>
                        else -> null
                    }

                    selectionModel.selectedIndexProperty().onChange {
                        if(it >= 0){
                            when (Conf.clazzOfPopulation) {
                                PopulationProtocol::class -> Conf.currentPopulationProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Boolean>
                                ShapeConstructingPopulation::class -> Conf.currentShapeConstructionProtocolFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Pair<Boolean, Boolean>>
                                GridNetworkConstructingPopulation::class -> Conf.currentGridNetworkFunction = functionMap[items[selectionModel.selectedIndex]]
                                        as KFunction<Triple<Boolean, Pair<String,String>,Boolean>>
                                else -> null
                            }
                        }
                        fire(FunctionSelectionChangeEvent)
                    }

                }
            }

            val initialSymbolMap = mutableMapOf<String, Int>()
            val initialInputs = mutableListOf<InitialInputConfiguration>().observable()
            val initialStatesTableView = TableView(initialInputs)
            /* val rulesInputs = mutableListOf<RulesInput>().observable()
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
                        Conf.clazzOfPopulation = when (typeBox.selectionModel.selectedIndex) {
                            0 -> PopulationProtocolGenerator::class
                            1 -> ShapeConstructorGenerator::class
                            2 -> GridNetworkGenerator::class
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
            }*/
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
                Conf.getInitialStateMapTemplate().forEach { key, value ->
                    if(value == NUM_NOT_SPECIFIED){
                        initialInputs.add(InitialInputConfiguration(key))
                    }else{
                        Conf.initialState[key] = value
                    }
                }
                subscribe<FunctionSelectionChangeEvent> {
                    initialInputs.clear()
                    Conf.initialState.clear()
                    Conf.getInitialStateMapTemplate().forEach { key, value ->
                        if(value == NUM_NOT_SPECIFIED){
                            initialInputs.add(InitialInputConfiguration(key))
                        }else{
                            Conf.initialState[key] = value
                        }
                    }
                }
                initialStatesTableView.column("State", InitialInputConfiguration::state)
                initialStatesTableView.column("# Initial Config.", InitialInputConfiguration::count).apply{
                    makeEditable()
                    textProperty().addListener({_, _, newV ->
                        if (!newV.matches("\\d*".toRegex())) {
                            text = newV.replace("[^\\d]".toRegex(),"")
                        }
                    })
                    onEditCommit {
                        print("Commit")
                        val valueStr = newValue.toString()
                        val oldValueStr = oldValue.toString()
                        if(valueStr.isInt() && valueStr.toInt() >= 0 && oldValueStr.toInt() != valueStr.toInt()){
                            Conf.initialState[this.rowValue.state] = valueStr.toInt()
                        }
                        Conf.initialState.forEach({key,value -> println("${key}: ${value}")})
                    }
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

