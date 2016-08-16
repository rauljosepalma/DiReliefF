package sparkfs

// class BinaryHeap[T: reflect.ClassTag](capacity: Int)(implicit ord: math.Ordering[T]) extends java.io.Serializable {
class BinaryHeap[T](capacity: Int)
  (implicit ord: math.Ordering[T], tag: reflect.ClassTag[T]) 
  extends java.io.Serializable {

  // TODO Init array
  var data: Array[T] = new Array[T](capacity+1)
  var size = 0
  
  /**
   * Adds a value to the min-heap. Silently ignores elements with
   * priority less than or equal to min. Maintains its capacity by removing
   * min when a better element is received
   */
  def +=(value: T) = {

    if(size == capacity && ord.compare(value, min) > 0) {
      removeMin
    }
    
    if(size < capacity){
      // place element into heap at bottom
      size+=1
      data(size) = value 

      bubbleUp
    }
  }
  
  /**
   * Returns (but does not remove) the minimum element in the heap.
   */
  def min: T = {
    require(!isEmpty, "Error trying to get element from empty heap")
    data(1)
  }

  /**
   * Removes and returns the minimum element in the heap.
   */
  def removeMin: T = {
    // what do want return?
    val result = min
    
    // get rid of the last leaf/decrement
    data(1) = data(size)
    // data(size) = null
    size -= 1
    
    bubbleDown
    
    result
  }

  def toArray: Array[T] = {
    data.slice(1, size + 1)
  }

  def merge(heap: BinaryHeap[T]): Unit = {
    val data = heap.toArray
    data.foreach{ d => this += d }
  }
  
  /**
   * Performs the "bubble down" operation to place the element that is at the 
   * root of the heap in its correct place so that the heap maintains the 
   * min-heap order property.
   */
  private def bubbleDown: Unit = {
    var index = 1   
    // bubble down
    while (hasLeftChild(index)) {
        // which of my children is smaller?
      var smallerChild = leftIndex(index)
      
      // bubble with the smaller child, if I have a smaller child
      if (hasRightChild(index) && 
            ord.compare(
              data(leftIndex(index)), 
              data(rightIndex(index))) > 0) {
          
        smallerChild = rightIndex(index)
      } 
      
      if (ord.compare(data(index), data(smallerChild)) > 0) {
      // if (data(index) > data(smallerChild)) {
        swap(index, smallerChild)
      } else {
        // otherwise, get outta here!
        return
      }
      
      // make sure to update loop counter/index of where last el is put
      index = smallerChild
    }        
  }
  
  /**
   * Performs the "bubble up" operation to place a newly inserted element 
   * (i.e. the element that is at the size index) in its correct place so 
   * that the heap maintains the min-heap order property.
   */
  private def bubbleUp: Unit = {
    var index = size
    
    while(hasParent(index) && 
            ord.compare(parent(index), data(index)) > 0) {
      // parent/child are out of order swap them
      swap(index, parentIndex(index))
      index = parentIndex(index)
    }
  }
  
  private def isEmpty = (size == 0)
  
  private def hasParent(i: Int) = i > 1
  
  private def leftIndex(i: Int) =  i * 2
  
  private def rightIndex(i: Int) =  i * 2 + 1
  
  private def hasLeftChild(i: Int) = (leftIndex(i) <= size)
  
  private def hasRightChild(i: Int) = (rightIndex(i) <= size)
  
  private def parent(i: Int) = data(parentIndex(i))
  
  private def parentIndex(i: Int) = i / 2
  
  private def swap(index1: Int, index2: Int): Unit = {
    val tmp = data(index1)
    data(index1) = data(index2)
    data(index2) = tmp        
  }
}