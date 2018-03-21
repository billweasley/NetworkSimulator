package utils

open class State private constructor(private val symbols: Set<String>, initialState: String) {

    var currentState = initialState
        set(value) {
            if (symbols.contains(value)) field = value
        }

    //Factory Design Pattern
    companion object {
        fun createState(symbols: Set<String>, initialState: String): State {
            if (symbols.isEmpty() || !symbols.contains(initialState))
                throw IllegalArgumentException()
            return State(symbols, initialState)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is State) {
            return this.currentState == other.currentState
        }
        return false
    }

    override fun hashCode(): Int {
        return 31 * currentState.hashCode()
    }


}