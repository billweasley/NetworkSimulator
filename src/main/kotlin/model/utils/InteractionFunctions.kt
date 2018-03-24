package utils

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

        fun simpleGlobalLineFunc(initializer: ModelNode, receiver: ModelNode,
                                 adjacencyList: Map<ModelNode, HashSet<ModelNode>>): Pair<Boolean, Boolean> {
            var isChanged = false
            val initialCurrentState = initializer.state.currentState
            val receiverCurrentState = receiver.state.currentState
            var linkState = adjacencyList[initializer]?.contains(receiver)!!

            when (listOf<Any>(initialCurrentState, receiverCurrentState, linkState)) {
                listOf<Any>("q0", "q0", false) -> {
                    initializer.state.currentState = "q1"
                    receiver.state.currentState = "l"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("l", "q0", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "l"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("l", "l", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "w"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("w", "q2", true) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "w"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("w", "q1", true) -> {
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
            when (listOf<Any>(initialCurrentState, receiverCurrentState, linkState)) {
                listOf<Any>("q0", "q0", false) -> {
                    initializer.state.currentState = "q1"
                    receiver.state.currentState = "q1"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("q1", "q0", false) -> {
                    initializer.state.currentState = "q2"
                    receiver.state.currentState = "q1"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("q1", "q1", false) -> {
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

            when (listOf<Any>(initialCurrentState, receiverCurrentState, linkState)) {
                listOf<Any>("c", "c", false) -> {
                    initializer.state.currentState = "c"
                    receiver.state.currentState = "p"

                    linkState = true
                    isChanged = true
                }
                listOf<Any>("p", "p", true) -> {
                    initializer.state.currentState = "p"
                    receiver.state.currentState = "p"

                    linkState = false
                    isChanged = true
                }
                listOf<Any>("c", "p", false) -> {
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