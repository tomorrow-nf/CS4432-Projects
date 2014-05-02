package simpledb.index.exhash;

import java.util.ArrayList;

public class Bucket {

	private int bucketNum;
	private int localDepth;
	private int total = 0;
	private ArrayList<Integer> contents;
	
	public Bucket(int bucketNum, int globalDepth) {
		this.bucketNum = bucketNum;
		this.localDepth = globalDepth;
		this.contents = new ArrayList<Integer>();
	}
	
	public void incLocalDepth(){
		localDepth++;
	}

	public void addToContents(Integer value){
		contents.add(value);
		total++;
	}
	
	// Getters and setters
	public int getBucketNum() {
		return bucketNum;
	}

	public void setBucketNum(int bucketNum) {
		this.bucketNum = bucketNum;
	}

	public int getLocalDepth() {
		return localDepth;
	}

	public void setLocalDepth(int localDepth) {
		this.localDepth = localDepth;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public ArrayList<Integer> getContents() {
		return contents;
	}

	public void setContents(ArrayList<Integer> contents) {
		this.contents = contents;
	}
}