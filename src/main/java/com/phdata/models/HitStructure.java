package com.phdata.models;

import java.util.Date;

public class HitStructure {
	private Date timestamp;
	private int count;

	public HitStructure(Date timestamp, int count) {
		this.timestamp = timestamp;
		this.count = count;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}