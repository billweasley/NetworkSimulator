package model.population

import utils.ModelNode

interface Population {
    fun interact(): Triple<Boolean, ModelNode, ModelNode>
    val nodes: List<ModelNode>
}
