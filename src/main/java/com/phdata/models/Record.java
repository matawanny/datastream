package com.phdata.models;

import java.util.Date;

public class Record {
	private String ipAddress;
	private Date timestamp;

	public Record(String ipAddress, Date timestamp) {
		this.ipAddress = ipAddress;
		this.timestamp = timestamp;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}