package org.apache.spark.mllib.feature

import scala.math.Ordered
import scala.collection.mutable.PriorityQueue

// Represents an object capable of searching starting from an initial state
abstract class StateSearcher(evaluator: SearchStateEvaluator) {
  def search(initalState:SearchState) : SearchState
}

// Represents an object capable of evaluating a given state
abstract class SearchStateEvaluator {
  def evaluate(state: SearchState): EvaluatedSearchState
}

// Represents a state of a search
abstract class SearchState {
  def expand: IndexedSeq[SearchState]
}

// Represents a SearchState and its merit
class EvaluatedSearchState(val state: SearchState, val merit: Double) 
  extends Ordered[EvaluatedSearchState] {
  def compare(that: EvaluatedSearchState) = {
    if(this.merit - that.merit > 0.0) 1 
    else if (this.merit == that.merit) 0
    else -1
  }
}


// Implements an object capable of doing a greedy best first search
class BestFirstSearcher(
  evaluator: SearchStateEvaluator,
  maxFails: Int) extends StateSearcher(evaluator) {

  def search(initState:SearchState): SearchState = {
    val evaluatedInitState = 
      new EvaluatedSearchState(initState, Double.NegativeInfinity)

    doSearch(
      PriorityQueue.empty[EvaluatedSearchState] += evaluatedInitState,
      evaluatedInitState, 0)
  }

  // TODO? This is a non-purely functional definition!!
  // The received queue must always have at least one element
  private def doSearch(
    queue: PriorityQueue[EvaluatedSearchState],
    bestState: EvaluatedSearchState,
    nFails: Int): SearchState = {

    
    // Remove head and expand it
    val head = queue.dequeue()
    // A collection of evaluated search states
    val newStates = head.state.expand.map(evaluator.evaluate)
    // DEBUG
    println("REMOVING HEAD:" + head.merit)
    println("EXPANDED STATES: ")
    newStates.foreach(s => println(s.merit))
    // Add new states to queue (in priority order)
    queue ++= newStates
    // If queue is empty it means that no new states
    // could be generated from head
    if (queue.isEmpty) {
      bestState.state
    } else {
      val bestNewState = queue.head
      if (bestNewState.merit > bestState.merit) {
        // DEBUG
        println("NEW BEST STATE: \n" + bestNewState.merit)
        doSearch(queue, bestNewState, 0)
      } else if (nFails < this.maxFails) {
        // DEBUG
        println("FAIL++: " + (nFails + 1).toString)
        doSearch(queue, bestState, nFails + 1)
      } else {
        bestState.state
      }
    }
  }
}