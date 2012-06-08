package com.mezbah;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;

import javax.swing.JTextArea;

import rice.environment.Environment;
import rice.p2p.commonapi.IdFactory;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class P2PReplicatorNode {

	private P2PFileTransferApplicationImpl app_file;
	private P2PFileReplicatorScribeImpl app_scribe;
	private P2PFileReplicationMessegeApplicationImpl app_msg;
	private PastryNode node;
	private String user;
	private JTextArea log;

	public P2PReplicatorNode(int bindport, InetSocketAddress bootaddress,
			final Environment env, String user,JTextArea log) throws Exception {
		// Generate the NodeIds Randomly
		NodeIdFactory nidFactory = new RandomNodeIdFactory(env);
		// construct the PastryNodeFactory, this is how we use
		// rice.pastry.socket
		PastryNodeFactory factory;
		this.user = user;
		this.log = log;
		factory = new SocketPastryNodeFactory(nidFactory, bindport, env);
		IdFactory idf = new PastryIdFactory(env);
		Object bootHandle = null;
		node = factory.newNode();
		// construct a new file transfer application
		app_file = new P2PFileTransferApplicationImpl(node, factory,log);
		// construct a new scribe application
		File f = new File("storeage\\original.txt");
		app_msg = new P2PFileReplicationMessegeApplicationImpl(node, user, log, app_file);
		app_scribe = new P2PFileReplicatorScribeImpl(node, app_msg,log,new Date(f.lastModified()));
		bootHandle = bootaddress;
		node.boot(bootHandle);
		synchronized (node) {
			while (!node.isReady() && !node.joinFailed()) {
				// delay so we don't busy-wait
				node.wait(500);
				// abort if can't join
				if (node.joinFailed()) {
					throw new IOException(
							"Could not join the FreePastry ring.  Reason:"
									+ node.joinFailedReason());
				}
			}
		}
		app_scribe.subscribe(this.user);
		this.log.append("Subscribing to the file replicator network..\n");
	}

	public void publishUpdate(Date update_Date) {
		app_scribe.setLastUpdated(update_Date);
		app_scribe.publishLastUpdate(user);
		log.append("Announcing last update time\n");
	}

}
