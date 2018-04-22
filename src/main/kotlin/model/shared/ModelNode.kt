package model.shared

open class ModelNode protected constructor(open var state: State, val index: Int) {

    companion object {

        private fun createNode(symbols: Set<String>, initialState: String, index: Int): ModelNode {
            if (symbols.isEmpty() || !symbols.contains(initialState))
                throw IllegalArgumentException()
            return ModelNode(State.createState(symbols, initialState), index)
        }

        fun createMultipleNodes(symbols: Set<String>,
                                initialStates: Map<String, Int>): MutableList<ModelNode> {

            if (symbols.isEmpty() || !symbols.containsAll(initialStates.keys))
                throw IllegalArgumentException(
                        if(symbols.isEmpty())
                            "Symbol cannot be empty"
                        else "Symbols does not contains "
                                + symbols
                                .filter { it -> !initialStates.containsKey(it) }
                                .reduce { acc, s -> acc.plus(";$s") }
                )

            val result = ArrayList<ModelNode>()
            var index = 0
            initialStates.entries.forEach({ entry ->
                repeat(
                        entry.value,
                        { _ -> result.add(createNode(symbols, entry.key, index++)) }
                )
            })
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelNode) return false
        if (index != other.index) return false
        return true
    }

    override fun hashCode(): Int {
        return 31 * index
    }
}