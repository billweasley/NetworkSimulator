package model.interactFunctions

import model.population.populationProtocols.PopulationProtocol
import model.shared.ModelNode
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import presentation.generator.concrete.PopulationProtocolGenerator
import model.scheduler.RandomScheduler
import java.util.regex.Pattern
import javax.script.ScriptEngineManager

typealias S = Pair<String,String>
typealias T = Triple<String,String,Boolean>


fun main(arr: Array<String>) {

    val testRule1 = "(L , F) -> (0, 0)"
    val testRule2 = "(L , 0) -> (L, 1)"
    val testRule3 = "(F , 1) -> (F, 0)"
    val testRule4 = "(0 , 1) -> (0, 0)"

    val styleSheet = "node {" +
            "	fill-color: black;" +
            "}" +
            "node.marked {" +
            "	fill-color: red;" +
            "}" +
            "edge.marked {" +
            "	fill-color: red;" +
            "}"

    val rules =
    SimpleFunctionDSL
            .producePopulationProtocolMultipleRules(arrayOf(testRule1,testRule2,testRule3,testRule4))



    val populationProtocolGenerator = PopulationProtocolGenerator(
            PopulationProtocol(
                    initialStates = mapOf(Pair("L", 15), Pair("F", 5)),
                    scheduler = RandomScheduler(),
                    symbols = setOf("L", "F", "0", "1"),
                    interactFunction =
                    SimpleFunctionDSL.getPopulationProtocolFunction(rules)
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
        fun producePopulationProtocolMultipleRules(rawRules: Array<String>):List<Pair<S,S>>{
            return rawRules.map{ rule -> producePopulationProtocolRules(rule) }
        }
        private fun producePopulationProtocolRules(rawRule: String): Pair<S, S> {
            val p =
                    Pattern.compile("\\s*\\(\\s*?(\\w+)\\s*?,\\s*?(\\w+)\\s*?\\)\\s*->\\s*\\(\\s*?(\\w+)\\s*?,\\s*?(\\w+)\\s*?\\)\\s*?")
            val m = p.matcher(rawRule)
            val res = mutableListOf<String>()
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

            val x = {i: Long,j: Long -> (i + j).toString()}
            val y = x.invoke(1,2)
            var toExecutedCodeInitialPart = """
            import model.shared.ModelNode
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
                if (rule.second.first.startsWith("!")||rule.second.second.startsWith(prefix = "!")){
                    if(rule.second.first.startsWith("!")){
                        rule.second.first.removePrefix("!")
                        if (rule.second.first.contains(rule.first.first)
                                && rule.second.first.contains(rule.first.second)){
                            val internalFun = "{${rule.first.first}: Long,${rule.first.second}: Long -> (${rule.second.first}).toString()}"
                            """
                                Pair("${rule.first.first}","${rule.first.second}") -> {
                                    initializer.state.currentState =
                                    receiver.state.currentState = "${internalFun}.invoke"
                                    isChanged = true
                                }
                            """
                            TODO("Calculate during the process")

                        }else if (rule.second.first.contains(rule.first.first)){

                        }else if(rule.second.first.contains(rule.first.second)){

                        }else{

                        }

                    }else{}
                    if(rule.second.second.startsWith("!")){
                        rule.second.second.removePrefix("!")
                        if (rule.second.second.contains(rule.first.first)
                                && rule.second.second.contains(rule.first.second)){

                        }else if (rule.second.second.contains(rule.first.first)){

                        }else if(rule.second.second.contains(rule.first.second)){

                        }else{

                        }

                    }else{}

                }else{
                    """
                                Pair("${rule.first.first}","${rule.first.second}") -> {
                                    initializer.state.currentState = "${rule.second.first}"
                                    receiver.state.currentState = "${rule.second.second}"
                                    isChanged = true
                                }
                """
                }
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
            @Suppress("UNCHECKED_CAST")
            return engine.eval(toExecutedCodeInitialPart + toExecutedCodeLastPart)
                    as (initializer: X, receiver: X) -> Boolean
        }

    }
}






