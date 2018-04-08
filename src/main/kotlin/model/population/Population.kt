package model.population

import model.shared.ModelNode

interface Population {
    fun interact(): Triple<Any, Any, Any>
    val nodes: List<ModelNode>
}
