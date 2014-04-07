package simpledb.buffer;

import java.util.HashMap;

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
	  this.rpolicy = rpolicy;
      bufferpool = new Buffer[numbuffs];
      numAvailable = numbuffs;
      clockPosition = 0; // CS4432-Project1 - Initialize the position in the array that will be looked at first when evicting
      for (int i=0; i<numbuffs; i++){
         bufferpool[i] = new Buffer();
         poolMap.put(bufferpool[i].getBlock().hashCode(), i); // Put the block of the buffer into a hashmap for efficient checking later
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
         buff.assignToBlock(blk);
      }
      if (!buff.isPinned())
         numAvailable--;
      buff.pin();
      // Setting the new accessed time of the  buffer
      // Also setting the second chance ref
      long accessed = System.currentTimeMillis();
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
      long accessed = System.currentTimeMillis();
      buff.setAccessed(accessed);
      buff.setRef(true);
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
   
   // CS4432-Project1
   // Given a block ID, checks whether this ID exists
   // in the buffer pool
   synchronized int findPage(Block bID){
	   if (poolMap.get(bID.hashCode()) == null){
		   return 0;
	   }
	   else return bID.number();
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
      for (Buffer buff : bufferpool) {
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
   }
   

   private Buffer chooseUnpinnedBuffer() {
	  if (rpolicy == 2) {
		  System.out.println("Using LRU");
		  return LRUPolicy();
	  }
	  else if (rpolicy == 3) {
		  System.out.println("Using Clock");
		  return ClockPolicy();
	  } 
	  else {
		  System.out.println("Using Default");
			for (Buffer buff : bufferpool)
				if (!buff.isPinned())
					return buff;
			return null;
	  }
   }
   
   // Both of these feel pretty inefficient right now...
   // Also considering moving them into separate classes when the new buffer pool hits
   
   // Pretty Naive LRU
   private Buffer LRUPolicy() {
	   long leastRecent = Long.MAX_VALUE;
	   Buffer candidateBuff = null;
	   for (Buffer buff : bufferpool) {
		   if (!buff.isPinned() && buff.getAccessed() < leastRecent) {
			   leastRecent = buff.getAccessed();
			   candidateBuff = buff;
		   }
	   }
	   return candidateBuff;
   }
   
   // Pretty Naive Clock-Replace, though I kinda want to opt in a linked list or something
   // Also checks for an empty buffer may need to be in order, it works without it but inefficient.
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
	   // This buffer is not pinned and has been given a second chane
	   // Evict this page
	   else if (!candidateBuff.isPinned() && !candidateBuff.getRef()) {
		   moveClockPosition();
		   return candidateBuff;
	   }
	   // If somehow everything fails return null
	   return null;
   }
   
   // Helper method to advance metaphorical clock pointer
   private void moveClockPosition() {
	   if (clockPosition == bufferpool.length - 1) {
		   clockPosition = 0;
	   }
	   else {
		   clockPosition++;
	   }
   }
}
