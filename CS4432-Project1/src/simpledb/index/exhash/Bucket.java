package simpledb.index.exhash;

import java.util.ArrayList;

public class Bucket {

	private int bucketNum;
	private int localDepth;
	private ArrayList<Integer> contents = new ArrayList<Integer>();
	
	public Bucket(int bucketNum, int globalDepth) {
		this.bucketNum = bucketNum;
		this.localDepth = globalDepth;
	}
	
	public void incLocalDepth(){
		localDepth++;
	}

	public void addToContents(Integer value){
		contents.add(value);
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

	public ArrayList<Integer> getContents() {
		return contents;
	}

	public void setContents(ArrayList<Integer> contents) {
		this.contents = contents;
	}
}