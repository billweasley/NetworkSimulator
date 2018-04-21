package shared

import model.shared.LocallyCoordinatedModelNode
import model.shared.ModelNode
import model.shared.Port
import model.shared.State
const val NUM_NOT_SPECIFIED = -1
fun isThePattern(str: String, pattern: String): Boolean = pattern.toRegex().matches(str)

fun isThePatternPair(pair: Pair<String, String>, patternPair: Pair<String, String>): Boolean {
    return isThePattern(pair.first, patternPair.first) && isThePattern(pair.second, patternPair.second)
}
infix fun Triple<Pair<State, Port>, Pair<State, Port>, Boolean>.match(patternTriple: Triple<Pair<String, Port>, Pair<String, Port>, Boolean>): Boolean = isTheTriple(this, patternTriple)

fun isTheTriple(triple: Triple<Pair<State, Port>, Pair<State, Port>, Boolean>, patternTriple: Triple<Pair<String, Port>, Pair<String, Port>, Boolean>): Boolean {
    return isThePatternPair(Pair(triple.first.first.currentState, triple.second.first.currentState),
            Pair(patternTriple.first.first, patternTriple.second.first))
            && triple.first.second == patternTriple.first.second
            && triple.second.second == patternTriple.second.second
            && triple.third == patternTriple.third
}
infix fun Pair<State, State>.match(patternTriple: Pair<String, String>): Boolean = isThePair(this, patternTriple)

fun isThePair(pair: Pair<State, State>, patternPair: Pair<String, String>): Boolean{
    return isThePattern(pair.first.currentState,patternPair.first)
            && isThePattern(pair.second.currentState, patternPair.second)
}

infix fun String.and (another: State): State{
    val newSet = mutableSetOf<String>()
    newSet.addAll(another.symbols)
    newSet.add("${this}${another.currentState}")
    return State.createState(newSet,"${this}${another.currentState}")
}

interface InteractionFunctions

open class GridNetworkConstructingFunctions: InteractionFunctions{
    companion object {
        @JvmField val squareGridNetworkInitialState = mapOf(Pair("q0", NUM_NOT_SPECIFIED), Pair("Lu", 1))
        @JvmField val squareGridNetworkSymbol = setOf("Lu", "q0", "q1", "Lr", "Ld", "Ll", "Lu")
        fun squareGridNetworkFunc(firstPair: Pair<LocallyCoordinatedModelNode, Port>, secondPair: Pair<LocallyCoordinatedModelNode, Port>):Triple<Boolean, Pair<String,String>,Boolean>{
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

            return if (transferredState == null) Triple(false, Pair("",""),isConnected) else{
                Triple(true, Pair(transferredState.first,transferredState.second), transferredState.third)
            }

        }
    }
}
open class ShapeConstructionFunctions: InteractionFunctions {

    companion object {
        @JvmField val simpleGlobalLineInitialState = mapOf(Pair("q0", NUM_NOT_SPECIFIED))
        @JvmField val simpleGlobalLineSymbol = setOf("q0", "q1", "l", "w", "q2")
        fun simpleGlobalLineFunc(initializer: ModelNode, receiver: ModelNode,
                                 adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean> {
            var isChanged = false
            val initialCurrentState = initializer.state.currentState
            val receiverCurrentState = receiver.state.currentState
            var linkState = adjacencyList[initializer]?.contains(receiver)!!

            when (listOf(initialCurrentState, receiverCurrentState, linkState)) {
                listOf("q0", "q0", false) -> {
                    initializer.state.currentState = "q1"
                    receiver.state.currentState = "l"

                    linkState = true
                    isChanged = true
                }
                listOf("l", "q0", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "l"

                    linkState = true
                    isChanged = true
                }
                listOf("l", "l", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "w"

                    linkState = true
                    isChanged = true
                }
                listOf("w", "q2", true) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "w"

                    linkState = true
                    isChanged = true
                }
                listOf("w", "q1", true) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "l"

                    linkState = true
                    isChanged = true
                }
            }
            return Pair(isChanged, linkState)
        }

        @JvmField val cycleCoverInitialState = mapOf(Pair("q0", NUM_NOT_SPECIFIED))
        @JvmField val cycleCoverSymbol = setOf("q0", "q1", "q2")
        fun cycleCoverFunc(initializer: ModelNode, receiver: ModelNode,
                           adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean> {
            var isChanged = false
            val initialCurrentState = initializer.state.currentState
            val receiverCurrentState = receiver.state.currentState
            var linkState = adjacencyList[initializer]?.contains(receiver)!!
            when (listOf(initialCurrentState, receiverCurrentState, linkState)) {
                listOf("q0", "q0", false) -> {
                    initializer.state.currentState = "q1"
                    receiver.state.currentState = "q1"

                    linkState = true
                    isChanged = true
                }
                listOf("q1", "q0", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "q1"

                    linkState = true
                    isChanged = true
                }
                listOf("q1", "q1", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "q2"

                    linkState = true
                    isChanged = true
                }
            }
            return Pair(isChanged, linkState)
        }

        @JvmField val globalStarInitialState = mapOf(Pair("c", NUM_NOT_SPECIFIED))
        @JvmField val globalStarSymbol = setOf("c", "p")
        fun globalStarFunc(initializer: ModelNode, receiver: ModelNode,
                           adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean> {
            var isChanged = false
            val initialCurrentState = initializer.state.currentState
            val receiverCurrentState = receiver.state.currentState
            var linkState = adjacencyList[initializer]?.contains(receiver)!!

            when (listOf(initialCurrentState, receiverCurrentState, linkState)) {
                listOf("c", "c", false) -> {
                    initializer.state.currentState = "c"
                    receiver.state.currentState = "p"

                    linkState = true
                    isChanged = true
                }
                listOf("p", "p", true) -> {
                    initializer.state.currentState = "p"
                    receiver.state.currentState = "p"

                    linkState = false
                    isChanged = true
                }
                listOf("c", "p", false) -> {
                    initializer.state.currentState = "c"
                    receiver.state.currentState = "p"

                    linkState = true
                    isChanged = true
                }
            }
            return Pair(isChanged, linkState)
        }
    }
}

open class PopulationProtocolFunctions: InteractionFunctions{
    companion object {

        @JvmField val dancingProtocolInitialState = mapOf(Pair("L", NUM_NOT_SPECIFIED),Pair("F", NUM_NOT_SPECIFIED))
        @JvmField val dancingProtocolSymbol = setOf("L", "F", "0", "1")
        fun dancingProtocolFunc(initializer: ModelNode, receiver: ModelNode): Boolean {
            var isChanged = false
            val initialCurrentState = initializer.state.currentState
            val receiverCurrentState = receiver.state.currentState

            when (Pair(initialCurrentState, receiverCurrentState)) {
                Pair("L", "F") -> {
                    initializer.state.currentState = "0"
                    receiver.state.currentState = "0"
                    isChanged = true
                }
                Pair("L", "0") -> {
                    initializer.state.currentState = "L"
                    receiver.state.currentState = "1"
                    isChanged = true
                }
                Pair("F", "1") -> {
                    initializer.state.currentState = "F"
                    receiver.state.currentState = "0"
                    isChanged = true
                }
                Pair("0", "1") -> {
                    initializer.state.currentState = "0"
                    receiver.state.currentState = "0"
                    isChanged = true
                }
            }
            return isChanged
        }

        @JvmField val sumModeFourInitialState = mapOf(Pair("0", NUM_NOT_SPECIFIED),Pair("1", NUM_NOT_SPECIFIED)
                ,Pair("2", NUM_NOT_SPECIFIED),Pair("3", NUM_NOT_SPECIFIED))
        @JvmField val sumModeFourSymbol = setOf("0", "1", "2", "3","N1","N2","N0","N3")
        fun sumModeFourFunc(initializer: ModelNode, receiver: ModelNode): Boolean {
            var isChanged = false
            val transferred = when{
                Pair(initializer.state,receiver.state) match Pair("[0123]","[0123]") ->{
                    Pair((initializer.state + receiver.state)%4, "N" and ((initializer.state + receiver.state)%4))

                }
                Pair(initializer.state, receiver.state) match Pair("[0123]","N[0123]") -> {
                    Pair(initializer.state,"N" and initializer.state)
                }

                else -> null
            }

            if (transferred!=null){
                initializer.state = transferred.first
                receiver.state = transferred.second
                isChanged = true
            }

            return isChanged
        }
    }
}
