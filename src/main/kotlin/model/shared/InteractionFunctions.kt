package utils

import model.shared.ModelNode

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