package utils

import model.shared.ModelNode
import model.shared.Port
import model.shared.State

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
open class InteractionFunctions {

    companion object {

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
        fun sumModelFourFunc(initializer: ModelNode, receiver: ModelNode): Boolean {
            var isChanged = false
            println("From ${initializer.state.currentState} ${receiver.state.currentState}")

            val transferred = when{
                Pair(initializer.state,receiver.state) match Pair("[0123]","[0123]") ->{
                    println("Match first")
                    Pair((initializer.state + receiver.state)%4, "N" and ((initializer.state + receiver.state)%4))

                }
                Pair(initializer.state, receiver.state) match Pair("[0123]","N[0123]") -> {
                    println("Match second")
                    Pair(initializer.state,"N" and initializer.state)
                }

                else -> null
            }

            if (transferred!=null){
                println("To ${transferred.first.currentState} ${transferred.second.currentState}")
                initializer.state = transferred.first
                receiver.state = transferred.second
                isChanged = true
            }

            return isChanged
        }

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
        fun cycleCoverFunc(initializer: ModelNode, receiver: ModelNode,
                           adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean>{
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
        fun globalStarFunc(initializer: ModelNode, receiver: ModelNode,
                           adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean>{
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