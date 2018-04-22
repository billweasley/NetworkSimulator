package model.population.populationProtocols.concrete

import model.population.populationProtocols.PopulationProtocol
import model.scheduler.Scheduler
import shared.PopulationProtocolFunctions

class DancingProtocol(initialStates: Map<String, Int>, scheduler: Scheduler) : PopulationProtocol(
        scheduler = scheduler,
        interactFunction =
        { initializer, receiver -> PopulationProtocolFunctions.dancingProtocolFunc(initializer, receiver) },
        symbols = setOf("L", "F", "0", "1"),
        initialStates = initialStates
)

