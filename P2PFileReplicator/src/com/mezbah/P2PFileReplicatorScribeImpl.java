package com.mezbah;

import java.util.Collection;
import java.util.Date;

import javax.swing.JTextArea;

import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.scribe.Scribe;
import rice.p2p.scribe.ScribeContent;
import rice.p2p.scribe.ScribeImpl;
import rice.p2p.scribe.ScribeMultiClient;
import rice.p2p.scribe.Topic;
import rice.pastry.commonapi.PastryIdFactory;

public class P2PFileReplicatorScribeImpl implements ScribeMultiClient,
		Application {

	private int seqNum = 0;
	private Scribe scribe;
	private Topic topic;
	private Endpoint endpoint;
	private NodeHandle updater;
	private Date lastUpdated;
	private JTextArea log;
	private P2PFileReplicationMessegeApplicationImpl transfer_msg;

	/**
	 * The constructor for this scribe client. It will construct the
	 * ScribeApplication.
	 * 
	 * @param node
	 *            the replicator node
	 */
	public P2PFileReplicatorScribeImpl(Node node,
			P2PFileReplicationMessegeApplicationImpl transfer_msg,
			JTextArea logging, Date lasUpdated) {
		this.endpoint = node.buildEndpoint(this, "filereplicator");
		this.updater = node.getLocalNodeHandle();
		this.log = logging;
		this.transfer_msg = transfer_msg;
		this.lastUpdated = lasUpdated;

		// construct Scribe
		scribe = new ScribeImpl(node, "last update announcement");

		// construct the topic
		topic = new Topic(new PastryIdFactory(node.getEnvironment()),
				"letest file update");
		// now we can receive messages
		endpoint.register();

	}

	public void subscribe(String user) {
		scribe.subscribe(topic, this,
				new P2PFileReplicatorContentImpl(endpoint.getLocalNodeHandle(),
						user, false), endpoint.getLocalNodeHandle());
	}

	public void publishLastUpdate(String user) {
		sendMulticast(user);
	}

	public void sendMulticast(String user) {
		P2PFileReplicatorContentImpl myMessage = new P2PFileReplicatorContentImpl(
				endpoint.getLocalNodeHandle(), lastUpdated, "original.txt",
				user, true);
		scribe.publish(topic, myMessage);
		seqNum++;
	}

	/**
	 * Sends an anycast message.
	 */
	public void sendAnycast(String user) {
		P2PFileReplicatorContentImpl myMessage = new P2PFileReplicatorContentImpl(
				endpoint.getLocalNodeHandle(), new Date(), "original.txt",
				user, true);
		scribe.anycast(topic, myMessage);
		seqNum++;
	}

	@Override
	public boolean forward(RouteMessage message) {
		return true;
	}

	@Override
	public void deliver(Id id, Message message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(NodeHandle handle, boolean joined) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean anycast(Topic topic, ScribeContent content) {
		boolean returnValue = scribe.getEnvironment().getRandomSource()
				.nextInt(3) == 0;
		return returnValue;
	}

	@Override
	public void deliver(Topic topic, ScribeContent content) {
		P2PFileReplicatorContentImpl update = (P2PFileReplicatorContentImpl) content;
		final NodeHandle latest_updater = update.getFrom();
		final Date latestUpdate_time = update.getLastUpdate();
		if (update.isUpdateAnnounce()) {
			if (latestUpdate_time.after(lastUpdated)) {
				Thread start_replicate_task = new Thread(new Runnable() {

					@Override
					public void run() {
						transfer_msg
								.routeFileTransferRequestDirect(latest_updater);
					}
				});
				log.append("Last update from: " + update.getUserName()
						+ "\n --file :" + update.getFileName() + "\n updated: "
						+ latestUpdate_time + "\n");
				start_replicate_task.start();
			} else {
				log.append("Last update from: " + update.getUserName()
						+ "\n --file :" + update.getFileName() + "\n updated: "
						+ latestUpdate_time + "\n");
				log.append("Update announcment is older then" + lastUpdated
						+ "\n");
			}
		}
		if (((P2PFileReplicatorContentImpl) content).getFrom() == null) {
			new Exception("Delivered from null").printStackTrace();
		}

	}

	@Override
	public void childAdded(Topic topic, NodeHandle child) {
		// TODO Auto-generated method stub

	}

	@Override
	public void childRemoved(Topic topic, NodeHandle child) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeFailed(Topic topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeFailed(Collection<Topic> topics) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeSuccess(Collection<Topic> topics) {
		log.append("Subscribe successfull..\n");

	}

	public boolean isRoot() {
		return scribe.isRoot(topic);
	}

	public NodeHandle getParent() {
		return ((ScribeImpl) scribe).getParent(topic);
	}

	public Collection<NodeHandle> getChildren() {
		return scribe.getChildrenOfTopic(topic);
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public NodeHandle getUpdater() {
		return updater;
	}

	public Date getLastUpdate() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
