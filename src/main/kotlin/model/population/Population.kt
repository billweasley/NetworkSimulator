package model.population

import model.shared.ModelNode

interface Population {
    fun interact(): Triple<Boolean, Any, Any>
    val nodes: List<ModelNode>
}
