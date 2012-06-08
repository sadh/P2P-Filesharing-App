package com.mezbah;

import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

public class P2PFileReplicationMessegeImpl implements Message {

	private static final long serialVersionUID = 8453450527157450643L;
	private NodeHandle to,from;
	private String user,messege;
	
	public P2PFileReplicationMessegeImpl(NodeHandle to,NodeHandle from,String user,String messege){
		this.to = to;
		this.from = from;
		this.user = user;
		this.messege = messege;
	}
	
	@Override
	public int getPriority() {
		return LOW_PRIORITY;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public NodeHandle getTo() {
		return to;
	}

	public NodeHandle getFrom() {
		return from;
	}

	public void setTo(NodeHandle to) {
		this.to = to;
	}

	public void setFrom(NodeHandle from) {
		this.from = from;
	}

	public String getMessege() {
		return messege;
	}

	public void setMessege(String messege) {
		this.messege = messege;
	}

}
