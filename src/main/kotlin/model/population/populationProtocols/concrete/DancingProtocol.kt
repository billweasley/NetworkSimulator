package model.population.populationProtocols.concrete

import model.population.populationProtocols.PopulationProtocol
import scheduler.Scheduler
import utils.InteractionFunctions

class DancingProtocol(initialStates: Map<String, Int>, scheduler: Scheduler) : PopulationProtocol(
        scheduler = scheduler,
        interactFunction =
        { initializer, receiver -> InteractionFunctions.dancingProtocolFunc(initializer, receiver) },
        symbols = setOf("L", "F", "0", "1"),
        initialStates = initialStates
)

