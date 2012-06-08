package com.mezbah;

import java.util.Date;

import rice.p2p.commonapi.NodeHandle;
import rice.p2p.scribe.ScribeContent;

public class P2PFileReplicatorContentImpl implements ScribeContent {

	private static final long serialVersionUID = 7876818492588282895L;
	private NodeHandle from;
	private Date lastUpdate;
	private String userName;
	private String fileName;
	private boolean updateAnnounce;
	
	public P2PFileReplicatorContentImpl(NodeHandle from,String userName,boolean updateAnnounce){
		this.from = from;
		this.userName = userName;
		this.updateAnnounce = updateAnnounce;
	}
	
	public P2PFileReplicatorContentImpl(NodeHandle from, Date lastUpdate,String fileName,String userName,boolean updateAnnounce) {
		this.from = from;
		this.lastUpdate = lastUpdate;
		this.fileName = fileName;
		this.userName = userName;
		this.updateAnnounce = updateAnnounce;
	}

	public NodeHandle getFrom() {
		return from;
	}

	public String getUserName() {
		return userName;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}
	
	public String getFileName() {
		return fileName;
	}

	public boolean isUpdateAnnounce() {
		return updateAnnounce;
	}

}
