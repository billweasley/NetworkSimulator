package model.population

interface LinkedPopulation : Population {
    fun numOfEdge(): Int
    fun numOfNode(): Int
}