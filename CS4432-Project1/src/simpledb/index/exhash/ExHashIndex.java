package simpledb.index.exhash;


import java.util.HashMap;
import java.util.Map.Entry;
import simpledb.tx.Transaction;
import simpledb.record.*;
import simpledb.query.*;
import simpledb.index.Index;

/**
 */
public class ExHashIndex implements Index {
	public static int NUM_BUCKETS = 4;
	public static int MAX_BUCKET_SIZE = 8;
	public static int globalDepth = 2; // Start global depth at 2
	private String idxname;
	private Schema sch;
	private Transaction tx;
	private static HashMap<Integer, Bucket> buckets = new HashMap<Integer, Bucket>();;
	private Constant searchkey = null;
	private TableScan ts = null;

	/**
	 * Opens an extensible hash index for the specified index.
	 * @param idxname the name of the index
	 * @param sch the schema of the index records
	 * @param tx the calling transaction
	 */
	public ExHashIndex(String idxname, Schema sch, Transaction tx) {
		this.idxname = idxname;
		this.sch = sch;
		this.tx = tx;
	}

	/**
	 * Positions the index before the first index record
	 * having the specified search key.
	 * The method hashes the search key to determine the bucket,
	 * and then opens a table scan on the file
	 * corresponding to the bucket.
	 * The table scan for the previous bucket (if any) is closed.
	 * @see simpledb.index.Index#beforeFirst(simpledb.query.Constant)
	 */
	public void beforeFirst(Constant searchkey) {
		close();
		// Initialize 4 buckets
		if (!(buckets.containsKey(0))){
			System.out.println("EH: INITIALIZING BUCKETS");
			Bucket bA = new Bucket(0, 2); buckets.put(0, bA);
		}
		if (!(buckets.containsKey(1))){
			Bucket bB = new Bucket(1, 2); buckets.put(1, bB);		
		}
		if (!(buckets.containsKey(2))){
			Bucket bC = new Bucket(2, 2); buckets.put(2, bC);
		}
		if (!(buckets.containsKey(3))){
			Bucket bD = new Bucket(3, 2); buckets.put(3, bD);
		}

		this.searchkey = searchkey;
		int bitMask = genBitmask(2); // Create a bitmask, using 2 by default. The mask will be updated as need be
		int bucket = searchkey.hashCode() & bitMask; // Calculate the bucket it needs to go to

		System.out.println("EH: SEARCHKEY = " + searchkey + ", BUCKET ID = " + bucket);
		System.out.println("EH: BUCKET " + bucket + " TOTAL = " + buckets.get(bucket).getContents().size());

		hashToBucket(bucket, bitMask);
	}

	/**
	 * Hashes a value into its appropriate bucket, splitting if need be
	 */
	public void hashToBucket(int bucket, int bitMask){
		// Update bitmask according to local depth
		bitMask = genBitmask(buckets.get(bucket).getLocalDepth());
		
		// Check if the bucket is full. If not, add to the bucket. If it is, split the bucket.
		if (buckets.get(bucket).getContents().size() == MAX_BUCKET_SIZE){
			System.out.println("EH: SPLITTING BUCKET " + buckets.get(bucket).getBucketNum());
			// Increment local depth
			buckets.get(bucket).incLocalDepth();
			System.out.println("EH: INCREMENTING LOCAL DEPTH OF BUCKET " + buckets.get(bucket).getBucketNum() + " TO " + buckets.get(bucket).getLocalDepth());
			// Update global depth if necessary
			if (buckets.get(bucket).getLocalDepth() >= globalDepth){
				globalDepth = buckets.get(bucket).getLocalDepth();
				System.out.println("EH: GLOBAL DEPTH UPDATED TO " + globalDepth);
			}
			
			// Update the buckets
			int nextValue = searchkey.hashCode();
			updateBuckets(buckets.get(bucket), nextValue, bitMask); // Update the contents of the old and newly expanded bucket
		}
		else {
			System.out.println("EH: ADDING " + searchkey.hashCode() + " TO BUCKET " + buckets.get(bucket).getBucketNum());			
			buckets.get(bucket).addToContents(searchkey.hashCode()); // "Add" a value to the bucket
			String tblname = idxname + bucket;
			TableInfo ti = new TableInfo(tblname, sch);
			ts = new TableScan(ti, tx);
		}
		//System.out.println(toString());
	}

	/**
	 * Generate a bitmask given a local depth
	 * @param depth
	 * @return bitmask to use
	 */
	public int genBitmask(int depth){
		int i;
		int bitMask = 0;
		// Shift the value over and add 1.
		// When completed, bitMask will consist of a total
		// of "depth" 1 bits (ex. 5 => 11111, 2 => 11)
		for (i = 0; i < depth; i++){
			bitMask = (bitMask << 1) + 1;
		}
		System.out.println("EH: UPDATING BITMASK TO " + i);
		return bitMask;
	}

	/**
	 * Updates the contents of each bucket when a new bucket is added
	 */
	public void updateBuckets(Bucket oldBucket, int newValue, int bitMask){
		int i, newHash, newSearchkey;
		// Update the bitmask
		bitMask = genBitmask(oldBucket.getLocalDepth()); // Update the bit mask to account for increased depth

		int newValueHashed = newValue & bitMask;

		// Increment through the contents of the old bucket, reapplying the hash
		for (i = 0; i < oldBucket.getContents().size(); i++){

			newSearchkey = oldBucket.getContents().get(i); // Store the value at this position
			newHash = oldBucket.getContents().get(i) & bitMask; // Apply the new bitmask
			oldBucket.getContents().remove(i); // Remove this value from the contents

			// Create the new bucket. Updated values and the new value will go into
			// either the old bucket or this new bucket. We do not need to explicitly
			// tell anything to go into this new bucket, just check the masked hashes
			if (buckets.get(newValueHashed) == null){
				Bucket newBucket = new Bucket(newValueHashed, oldBucket.getLocalDepth());
				buckets.put(newValueHashed, newBucket);
				System.out.println("EH: MAKING A NEW BUCKET: " + newValueHashed);
				NUM_BUCKETS++;
			}
			if (buckets.get(newHash) == null){
				Bucket newBucket = new Bucket(newHash, globalDepth);
				buckets.put(newHash, newBucket);
				System.out.println("EH: MAKING A NEW BUCKET: " + newHash);
				NUM_BUCKETS++;
			}

			// Add the value to its appropriate bucket
			buckets.get(newHash).addToContents(newSearchkey); 
			System.out.println("EH: MOVING " + newSearchkey + " TO BUCKET " + buckets.get(newHash).getBucketNum());
			//System.out.println(toString());
			String tblname = idxname + newHash;
			TableInfo ti = new TableInfo(tblname, sch);
			ts = new TableScan(ti, tx);
		}
		// Add the new value to its appropriate bucket
		System.out.println("EH: (NEW) ADDING " + newValue + " TO BUCKET " + buckets.get(newValueHashed).getBucketNum());
		hashToBucket(newValueHashed, bitMask);
	}

	/**
	 * Moves to the next record having the search key.
	 * The method loops through the table scan for the bucket,
	 * looking for a matching record, and returning false
	 * if there are no more such records.
	 * @see simpledb.index.Index#next()
	 */
	public boolean next() {
		while (ts.next())
			if (ts.getVal("dataval").equals(searchkey))
				return true;
		return false;
	}

	/**
	 * Retrieves the dataRID from the current record
	 * in the table scan for the bucket.
	 * @see simpledb.index.Index#getDataRid()
	 */
	public RID getDataRid() {
		int blknum = ts.getInt("block");
		int id = ts.getInt("id");
		return new RID(blknum, id);
	}

	/**
	 * Inserts a new record into the table scan for the bucket.
	 * @see simpledb.index.Index#insert(simpledb.query.Constant, simpledb.record.RID)
	 */
	public void insert(Constant val, RID rid) {
		beforeFirst(val);
		ts.insert();
		ts.setInt("block", rid.blockNumber());
		ts.setInt("id", rid.id());
		ts.setVal("dataval", val);
	}

	/**
	 * Deletes the specified record from the table scan for
	 * the bucket.  The method starts at the beginning of the
	 * scan, and loops through the records until the
	 * specified record is found.
	 * @see simpledb.index.Index#delete(simpledb.query.Constant, simpledb.record.RID)
	 */
	public void delete(Constant val, RID rid) {
		beforeFirst(val);
		while(next())
			if (getDataRid().equals(rid)) {
				ts.delete();
				return;
			}
	}

	/**
	 * Closes the index by closing the current table scan.
	 * @see simpledb.index.Index#close()
	 */
	public void close() {
		if (ts != null)
			ts.close();
	}

	/**
	 * Returns the cost of searching an index file having the
	 * specified number of blocks.
	 * The method assumes that all buckets are about the
	 * same size, and so the cost is simply the size of
	 * the bucket.
	 * @param numblocks the number of blocks of index records
	 * @param rpb the number of records per block (not used here)
	 * @return the cost of traversing the index
	 */
	public static int searchCost(int numblocks, int rpb){
		return numblocks / ExHashIndex.NUM_BUCKETS;
	}

	/**
	 * Prints out buckets and their contents
	 */
	public String toString(){
		String str = "\n********************\nBUCKETS (GlobalDepth = " + globalDepth + "):\n********************\n";
		for (Entry<Integer, Bucket> entry : buckets.entrySet()) {
			  Integer key = entry.getKey();
			  Bucket value = entry.getValue();
				str = (str + key + ": LocalDepth = " + value.getLocalDepth() + "\n-----------\n" + value.printContents() + "\n============\n");
			}
		str = (str + "\n********************\n");
		return str;
	}
}

