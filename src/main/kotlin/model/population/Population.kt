package model.population

import model.shared.ModelNode

interface Population {
    fun interact(): Triple<Boolean, ModelNode, ModelNode>
    val nodes: List<ModelNode>
}
