package utils

open class Node protected constructor(open val state: State, private val index: Int) {

    companion object {

        private fun createNode(symbols: Set<String>, initialState: String, index: Int): Node {
            if (symbols.isEmpty() || !symbols.contains(initialState))
                throw IllegalArgumentException()
            return Node(State.createState(symbols, initialState), index)
        }

        fun createMultipleNodes(symbols: Set<String>,
                                initialStates: Map<String, Int>, numOfNodes: Int): MutableList<Node> {

            if (symbols.isEmpty()
                    || !symbols.containsAll(initialStates.keys)
                    || numOfNodes != initialStates.values.sum())
                throw IllegalArgumentException()

            val result = ArrayList<Node>()
            var index = 0
            initialStates.entries.forEach({ entry ->
                repeat(
                        entry.value,
                        { _ -> result.add(Node.createNode(symbols, entry.key, index++)) }
                )
            })
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        if (index != other.index) return false
        return true
    }

    override fun hashCode(): Int {
        return 31 * index
    }
}