package population

import utils.Node

interface Population {
    fun interact(): Boolean
    val nodes: List<Node>
}
