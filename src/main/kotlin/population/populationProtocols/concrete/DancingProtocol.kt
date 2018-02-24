package population.populationProtocols.concrete

import population.populationProtocols.PopulationProtocol
import scheduler.Scheduler
import utils.InteractionFunctions

class DancingProtocol(numOfNodes: Int, initialStates: Map<String, Int>, scheduler: Scheduler) : PopulationProtocol(
        numOfNodes = numOfNodes,
        initialStates = initialStates,
        scheduler = scheduler,
        symbols = setOf("L", "F", "0", "1"),
        interactFunction =
        { initializer, receiver -> InteractionFunctions.dancingProtocolFunc(initializer, receiver) }
)

