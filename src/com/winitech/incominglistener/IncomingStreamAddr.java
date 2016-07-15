package com.winitech.incominglistener;

public class IncomingStreamAddr {
	private boolean isPublish;
	private String vhostName;
	private String appName;
	private String appInstanceName;
	private String streamName;
	
	public IncomingStreamAddr(boolean isPublish, String vhostName, String appName, String appInstanceName, String streamName) {
		this.isPublish = isPublish;
		this.vhostName = vhostName;
		this.appName = appName;
		this.appInstanceName = appInstanceName;
		this.streamName = streamName;
	}
	
	public boolean isPublish() {
		return isPublish;
	}
	public void setPublish(boolean isPublish) {
		this.isPublish = isPublish;
	}
	public String getVhostName() {
		return vhostName;
	}
	public void setVhostName(String vhostName) {
		this.vhostName = vhostName;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getAppInstanceName() {
		return appInstanceName;
	}
	public void setAppInstanceName(String appInstanceName) {
		this.appInstanceName = appInstanceName;
	}
	public String getStreamName() {
		return streamName;
	}
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	
}
