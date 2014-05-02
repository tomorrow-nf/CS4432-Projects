package simpledb.index.exhash;


import java.util.HashMap;
import simpledb.tx.Transaction;
import simpledb.record.*;
import simpledb.query.*;
import simpledb.index.Index;

/**
 * A static hash implementation of the Index interface.
 * A fixed number of buckets is allocated (currently, 100),
 * and each bucket is implemented as a file of index records.
 * @author Edward Sciore
 */
public class ExHashIndex implements Index {
	public static int NUM_BUCKETS = 100;
	private String idxname;
	private Schema sch;
	private Transaction tx;
	private Constant searchkey = null;
	private TableScan ts = null;
	
	private int globalDepth = 2; // Start globalDepth at 2...
	private int bitMask = 3; //... and start bitMask at 3 appropriately
	private HashMap<Integer, Bucket> buckets;

	/**
	 * Opens a hash index for the specified index.
	 * @param idxname the name of the index
	 * @param sch the schema of the index records
	 * @param tx the calling transaction
	 */
	public ExHashIndex(String idxname, Schema sch, Transaction tx) {
		this.idxname = idxname;
		this.sch = sch;
		this.tx = tx;
		
		buckets = new HashMap<Integer, Bucket>();
		// Initialize 4 buckets
		Bucket bA = new Bucket(0, 2); buckets.put(0, bA);
		Bucket bB = new Bucket(1, 2); buckets.put(1, bB);
		Bucket bC = new Bucket(2, 2); buckets.put(2, bC);
		Bucket bD = new Bucket(3, 2); buckets.put(3, bD);
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
		this.searchkey = searchkey;
		int bucket = searchkey.hashCode() & bitMask; // Calculate the bucket it needs to go to
		// Check if the bucket is full. If not, add to the bucket. If it is, split the bucket.
		if (buckets.get(bucket).getTotal() == 4){
			// Increment depths
			globalDepth++;
			buckets.get(bucket).incLocalDepth();
			// Update the buckets
			bitMask = (bitMask << 1) + 1; // Update the bit mask to account for increased depth
			int newBucket = searchkey.hashCode(); // Add a new bucket
			updateBuckets(buckets.get(bucket), newBucket); // Update the contents of the old and newly expanded bucket
		}
		else {
			buckets.get(bucket).addToContents(searchkey.hashCode()); // "Add" a value to the bucket
			String tblname = idxname + bucket;
			TableInfo ti = new TableInfo(tblname, sch);
			ts = new TableScan(ti, tx);
		}
	}
	
	/**
	 * Updates the contents of each bucket when a new bucket is added
	 */
	public void updateBuckets(Bucket bucket, int newBucketID){
		int i, newHash;
		int newBucketHashed = newBucketID & bitMask;
		// Create the new bucket. Updated values and the new value will go into
		// either the old bucket or this new bucket. We do not need to explicitly
		// tell anything to go into this new bucket, just check the masked hashes
		Bucket newBucket = new Bucket(buckets.size() + 1, globalDepth);
		
		// Increment through the contents of the old bucket, reapplying the hash
		for (i = 0; i < bucket.getTotal(); i++){
			newHash = bucket.getContents().get(i) & bitMask; // Apply the new bitmask
			bucket.getContents().remove(i); // Remove this value from the contents
			
			// Add the value to its appropriate bucket
			buckets.get(newHash).addToContents(bucket.getContents().get(i)); 
			String tblname = idxname + newHash;
			TableInfo ti = new TableInfo(tblname, sch);
			ts = new TableScan(ti, tx);
		}
		// Add the new value to its appropriate bucket
		buckets.get(newBucketHashed).addToContents(newBucketHashed); 
		String tblname2 = idxname + newBucketHashed;
		TableInfo ti2 = new TableInfo(tblname2, sch);
		ts = new TableScan(ti2, tx);
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
}

