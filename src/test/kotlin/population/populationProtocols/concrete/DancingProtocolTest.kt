package population.populationProtocols.concrete

import org.junit.After
import org.junit.Before
import org.junit.Test
import scheduler.RandomScheduler

class DancingProtocolTest {

    val dancingProtocolInstance = DancingProtocol(
            20, mapOf(Pair("L", 10), Pair("F", 10)), RandomScheduler()
    )

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {
    }

    @Test
    fun test() {
        var i = 100000000
        while (i-- > 0) {
            dancingProtocolInstance.interact()
        }
        println(dancingProtocolInstance.nodes.map { node -> node.state.currentState })
    }


}