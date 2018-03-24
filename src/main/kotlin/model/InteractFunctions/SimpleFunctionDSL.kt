package model.InteractFunctions

import model.InteractFunctions.SimpleFunctionDSL.Companion.producePopulationProtocolRule
import model.population.populationProtocols.PopulationProtocol
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import presentation.populationProtocol.PopulationProtocolGenerator
import scheduler.RandomScheduler
import utils.ModelNode
import java.util.*
import java.util.regex.Pattern
import javax.script.ScriptEngineManager

typealias S = Pair<String,String>
typealias T = Triple<String,String,Boolean>


fun main(arr: Array<String>) {
    val testRule1 = "(L , F) -> (0, 0)"
    val testRule2 = "(L , 0) -> (L, 1)"
    val testRule3 = "(F , 1) -> (F, 0)"
    val testRule4 = "(0 , 1) -> (0, 0)"

    val res1 = producePopulationProtocolRule(testRule1)
    val res2 = producePopulationProtocolRule(testRule2)
    val res3 = producePopulationProtocolRule(testRule3)
    val res4 = producePopulationProtocolRule(testRule4)


    val styleSheet = "node {" +
            "	fill-color: black;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}" +
            "edge.marked {" +
            "	fill-color: red;" +
            "}"

    val populationProtocolGenerator = PopulationProtocolGenerator(
            PopulationProtocol(
                    initialStates = mapOf(Pair("L",15),Pair("F",5)),
                    scheduler = RandomScheduler(),
                    symbols = setOf("L","F","0","1"),
                    interactFunction =
                    SimpleFunctionDSL.getPopulationProtocolFunction<ModelNode>(listOf(res1,res2,res3,res4))
            ),
            1000000,
            true,
            80,
            "Test",
            styleSheet = styleSheet
    )

    populationProtocolGenerator.display()
}

class SimpleFunctionDSL {
    companion object {

        fun producePopulationProtocolRule(rawRule: String): Pair<S, S> {
            val p =
                    Pattern.compile("\\s*\\(\\s*?(\\w*+)\\s*?,\\s*?(\\w*+)\\s*?\\)\\s*->\\s*\\(\\s*?(\\w*+)\\s*?,\\s*?(\\w*+)\\s*?\\)\\s*?")
            val m = p.matcher(rawRule)
            val res = LinkedList<String>()
            while (m.find()) {
                for (i in 1..m.groupCount()) {
                    res.add(m.group(i))
                }
            }
            if (res.size != 4) throw IllegalArgumentException("Given input is invalid!")
            return Pair(S(res[0], res[1]), S(res[2], res[3]))
        }

        fun <X : ModelNode> getPopulationProtocolFunction(rules: List<Pair<S, S>>): (initializer: X, receiver: X) -> Boolean {

            setIdeaIoUseFallback()
            val engine = ScriptEngineManager().getEngineByExtension("kts")

            var toExecutedCodeInitialPart = """
            import utils.ModelNode
            typealias S = Pair<String,String>
                var res = getPopulationProtocolFunction0<ModelNode>()
                fun <X: ModelNode> getPopulationProtocolFunction0():(initializer: X,receiver: X) -> Boolean{
                    var isChanged = false
                    return {
                        initializer, receiver -> run{
                            isChanged = false
                            val initialCurrentState = initializer.state.currentState
                            val receiverCurrentState = receiver.state.currentState
                            when (Pair(initialCurrentState, receiverCurrentState)) {
            """
            rules.stream().map { rule ->
                """
                                Pair("${rule.first.first}","${rule.first.second}") -> {
                                    initializer.state.currentState = "${rule.second.first}"
                                    receiver.state.currentState = "${rule.second.second}"
                                    isChanged = true
                                }
                """
            }.forEach { ruleCode -> toExecutedCodeInitialPart += ruleCode}
            val toExecutedCodeLastPart = """
                             }
                        }
                        isChanged
                    }
                }
                res
            """
            print(toExecutedCodeInitialPart + toExecutedCodeLastPart)

            return engine.eval(toExecutedCodeInitialPart + toExecutedCodeLastPart)
                    as (initializer: X, receiver: X) -> Boolean
        }

    }
}





