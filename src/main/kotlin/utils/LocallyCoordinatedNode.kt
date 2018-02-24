package utils

import koma.end
import koma.extensions.get
import koma.extensions.map
import koma.mat
import koma.matrix.Matrix
import kotlin.math.roundToInt


data class LocallyCoordinatedNode(val x: Int,
                                  val y: Int,
                                  override val state: State, private val index: Int) : Node(state = state, index = index) {
    // IN-CLOCK direction rotation
    fun getLocallyRotatedNode(degree: Int): LocallyCoordinatedNode {
        val rad = degree * Math.PI / 180
        val transferMat = mat[
                Math.cos(rad), Math.sin(rad) end
                        0 - Math.sin(rad), Math.cos(rad)
                ]
        val transferred = (transferMat * mat[this.x, this.y].T).removeNegativeZeros()
        return LocallyCoordinatedNode(transferred[0, 0].roundToInt(), transferred[1, 0].roundToInt(), state, index)
    }

    private fun Matrix<Double>.removeNegativeZeros(): Matrix<Double> {
        return this.map { it -> if (it == -0.0) Math.abs(it) else it }
    }
}


