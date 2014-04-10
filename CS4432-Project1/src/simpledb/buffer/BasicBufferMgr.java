package simpledb.buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import simpledb.file.*;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
   private Buffer[] bufferpool;
   private int numAvailable;
   // CS4432-Project1
   private HashMap<Integer, Integer> poolMap; // hashmap to store info about pages in the buffer pool
   private int clockPosition; // int position into the array the Clock-Replacement algorithm will run along
   private int rpolicy; // Stores the selected replacement policy chosen when the db is started
   private List<Integer> emptyFrames; // stores an index of empty frames
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs, int rpolicy) {
	  this.rpolicy = rpolicy; /** CS4432-Project1 **/ //Dictates what replacement policy to use. 
      bufferpool = new Buffer[numbuffs];
      numAvailable = numbuffs;
      poolMap = new HashMap<Integer, Integer>();
      clockPosition = 0; // CS4432-Project1 - Initialize the position in the array that will be looked at first when evicting
      emptyFrames = new ArrayList<Integer>(); // CS4432-Project1 - Initialize instance of empty frame index
      for (int i=0; i<numbuffs; i++){
    	 Buffer newBuff = new Buffer();
    	 newBuff.setPosition(i);
         bufferpool[i] = newBuff;
         emptyFrames.add(i); // add all frames to index of empty frames
      }
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
         buff.flush();
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         System.out.println(blk);
         System.out.println(this.toString());
         System.out.println("\n");
         buff.assignToBlock(blk);
         poolMap.put(buff.getBlock().hashCode(), buff.getPosition()); // Put the block of the buffer into a hashmap for efficient checking later
      }
      if (!buff.isPinned())
         numAvailable--;
      buff.pin();
      /** CS4432-Project1 **/
      // Setting the new accessed time of the  buffer
      // Also setting the second chance ref
      long accessed = System.nanoTime();
      buff.setAccessed(accessed);
      buff.setRef(true);
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      buff.pin();
      // Setting the first accessed time of the  buffer
      // I mean it is new, considering this maybe should be in the buffer constructor
      long accessed = System.nanoTime();
      buff.setAccessed(accessed);
      buff.setRef(true);
      System.out.println("Putting in: " + buff.getBlock().hashCode());
      poolMap.put(buff.getBlock().hashCode(), buff.getPosition()); // Put the block of the buffer into a hashmap for efficient checking later
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      if (!buff.isPinned())
         numAvailable++;
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   // CS4432-Project1
   // Given a block ID, checks whether this ID exists
   // in the buffer pool by checking the key against the hashmap
   private Buffer findExistingBuffer(Block blk) {
	   /*
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;*/
	  System.out.println("Searching for: " + blk);
	  Integer returnBuff = poolMap.get(blk.hashCode());
	  if (returnBuff == null){
		  return null;
	  }
	  else {
		  System.out.println("FOUND MATCH: " + blk);
		  return bufferpool[returnBuff];
	  }
   }
   

   private Buffer chooseUnpinnedBuffer() {
	  if (rpolicy == 2) {
		  // CS4432-Project1 - efficient finding of empty frames
		  if (emptyFrames.isEmpty() == false){
			  Buffer buff = bufferpool[emptyFrames.get(0)]; // get first empty frame in the list
			  emptyFrames.remove(0); // remove frame from list of empty frames
			  return buff; 
		  }
		  // No empty frames, proceed to LRUPolicy
		  return LRUPolicy();
	  }
	  else if (rpolicy == 3) {
		  // CS4432-Project1 - efficient finding of empty frames
		  if (emptyFrames.isEmpty() == false){
			  Buffer buff = bufferpool[emptyFrames.get(0)]; // get first empty frame in the list
			  emptyFrames.remove(0); // remove frame from list of empty frames
			  return buff; 
		  }
		  // No empty frames proceed to ClockPolicy
		  return ClockPolicy();
	  } 
	  else {
		  // CS4432-Project1 - efficient finding of empty frames
		  if (emptyFrames.isEmpty() == false){
			  Buffer buff = bufferpool[emptyFrames.get(0)]; // get first empty frame in the list
			  emptyFrames.remove(0); // remove frame from list of empty frames
			  return buff; 
		  }
		  // No empty frames, do the default
			for (Buffer buff : bufferpool)
				if (!buff.isPinned()) {
					poolMap.remove(buff.getBlock().hashCode());
					return buff;
				}
			return null;
	  }
   }
   
   /** CS4432-Project1 **/
   // LRU Policy
   private Buffer LRUPolicy() {
	   long leastRecent = Long.MAX_VALUE;
	   int candidatePosition = 0;
	   int actualPosition = 0;
	   for (Buffer buff : bufferpool) {
		   if (!buff.isPinned() && buff.getAccessed() < leastRecent) {
			   leastRecent = buff.getAccessed();
			   candidatePosition = actualPosition;
		   }
		   actualPosition++;
	   }
	   poolMap.remove(bufferpool[candidatePosition].getBlock().hashCode());
	   return bufferpool[candidatePosition];
   }
   
   /** CS4432-Project1 **/
   // Assumption: If the buffer is empty, ClockPolicy isn't called and the clock pointer
   // Will not move until actual eviction is needed, will start at 0th position until then.
   private Buffer ClockPolicy() {
	   Buffer candidateBuff = bufferpool[clockPosition];
	   // Is this buffer pinned (being or going to be used)?
	   // Advance clock position and don't do anything to it
	   if (candidateBuff.isPinned()) {
		   moveClockPosition();
		   return ClockPolicy();
	   }
	   // Is this buffer unpinned and hasn't gotten a second chance?
	   // Change the ref bit to false so it will be dropped next time
	   else if (!candidateBuff.isPinned() && candidateBuff.getRef()) {
		   bufferpool[clockPosition].setRef(false);
		   moveClockPosition();
		   return ClockPolicy();
	   }
	   // This buffer is not pinned and has been given a second chance
	   // Evict this page
	   else if (!candidateBuff.isPinned() && !candidateBuff.getRef()) {
		   moveClockPosition();
		   poolMap.remove(candidateBuff.getBlock().hashCode());
		   return candidateBuff;
	   }
	   // If somehow everything fails return null
	   return null;
   }
   
   /** CS4432-Project1 **/
   // Helper method to advance metaphorical clock pointer
   private void moveClockPosition() {
	   if (clockPosition == bufferpool.length - 1) {
		   clockPosition = 0;
	   }
	   else {
		   clockPosition++;
	   }
   }
   
   /** CS4432-Project1 **/
   @Override
   public String toString() {
	   StringBuilder bufferDisplay = new StringBuilder();
	   for (int i=0; i < bufferpool.length; i++) {
		   bufferDisplay.append(bufferpool[i]);
		   bufferDisplay.append("\n");
	   }
	   return "Buffer Contents: " + bufferDisplay + "HashMap Contents" + poolMap + "| Empty Frames Contents: " + emptyFrames + "| ClockPosition: " + clockPosition;
   }

}
