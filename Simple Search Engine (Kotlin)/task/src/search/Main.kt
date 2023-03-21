package search

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = SearchEngine(args).menu()



data class Person(
    val firstName: String,
    val lastName: String?,
    val email: String? ) {

    fun contains(item: String) =  (firstName + lastName + email).lowercase().contains(item)

    override fun toString(): String =  listOf(firstName, lastName ?: "", email ?: "").joinToString (" ").trim()

}



class SearchEngine(args: Array<String>) {

    private val people = mutableListOf<Person?>()
    private var peoplesMap = mapOf <String, List<Int>>()
    private val file = File(args[1])

    init {
        createDatabase()
        createInvertedIndex()
    }


    private fun createDatabase() {
        file.readLines().forEach { line ->
                val lineList = line.split(" ")
                people.add(Person(lineList[0], lineList.getOrNull(1), lineList.getOrNull(2)))
        }
    }

    private fun createInvertedIndex(){

        peoplesMap = people.withIndex()
                    .flatMap { indexedPerson -> indexedPerson.value.toString()
                        .split(" ").map{ word -> IndexedValue(indexedPerson.index, word.lowercase())}}
                        .groupBy ({it.value}, {it.index} )
    }

    fun menu() {
        while(true){
            println("=== Menu ===" +
                    "\n1. Find a person" +
                    "\n2. Print all people" +
                    "\n0. Exit")
            when( readln().toInt()){
                    1 -> defineMethod()
                    2 -> printPeople()
                    0 -> println("\nBye!").also { exitProcess(0) }
                    else -> println("Incorrect option! Try again.")
            }
        }
    }

    private fun defineMethod(){
        println("Select a matching strategy: ALL, ANY, NONE")
        search(readln())

    }

    private fun printPeople() = println("\n=== List of people ===").also { people.forEach{ personInfo -> println(personInfo) } }

    private fun search(command: String){
        println("\nEnter a name or email to search all matching people.")
        var items = mutableSetOf<Int>()
        when(command){

            "ALL" -> readln().lowercase().split(" ").forEach { peoplesMap[it]?.let { list -> items =
                if(items.isEmpty())
                    list.toMutableSet()
                else
                    items.intersect(list.toMutableSet()).toMutableSet() } }

            "ANY" -> readln().lowercase().split(" ").forEach { peoplesMap[it]?.let { list -> items.addAll(list)} }

            "NONE" -> {
                val itemsToExclude = mutableSetOf<Int>()
                readln().lowercase().split(" ").forEach { peoplesMap[it]?.let { list -> itemsToExclude.addAll(list)} }
                peoplesMap.forEach { (key, indices) -> if( !indices.any{it in itemsToExclude } ) items.addAll(indices)}
            }
        }

        if(items.isEmpty())
            println("No matching people found.")
        else{
            println("${items.size} persons found:")
            for(item in items){
                println( people[item] )
            }
        }

    }
}
