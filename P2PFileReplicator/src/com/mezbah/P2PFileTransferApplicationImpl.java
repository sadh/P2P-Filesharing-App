package com.mezbah;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.swing.JTextArea;

import org.mpisws.p2p.filetransfer.BBReceipt;
import org.mpisws.p2p.filetransfer.FileReceipt;
import org.mpisws.p2p.filetransfer.FileTransfer;
import org.mpisws.p2p.filetransfer.FileTransferCallback;
import org.mpisws.p2p.filetransfer.FileTransferImpl;
import org.mpisws.p2p.filetransfer.FileTransferListener;
import org.mpisws.p2p.filetransfer.Receipt;
import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;
import rice.p2p.commonapi.appsocket.AppSocket;
import rice.p2p.commonapi.appsocket.AppSocketReceiver;
import rice.p2p.util.rawserialization.SimpleInputBuffer;
import rice.p2p.util.rawserialization.SimpleOutputBuffer;
import rice.pastry.PastryNodeFactory;

public class P2PFileTransferApplicationImpl implements Application {
	
	private Endpoint endpoint;
	private FileTransfer receiver,sender;
	private Node node;
	private Environment env;
	private JTextArea log;
	
	public P2PFileTransferApplicationImpl(Node node, final PastryNodeFactory factory,JTextArea logging){
		this.endpoint  = node.buildEndpoint(this, "filetransfer_instance");
		this.node  = node;
		this.log  = logging;
		env = node.getEnvironment();
		endpoint.accept(new AppSocketReceiver() {
			@Override
			public void receiveSocket(AppSocket socket) throws IOException {
				receiver = new FileTransferImpl(socket, new FileTransferCallback() {
					
					@Override
					public void receiveException(Exception ioe) {
						log.append("Error in file transfer : "+ ioe+"\n");
					}
					
					@Override
					public void messageReceived(ByteBuffer bb) {
						log.append("Messege recieved "+ bb+"\n");
						
					}
					
					@Override
					public void fileReceived(File f, ByteBuffer metadata) {
						String original_file_name = "";
						try {
							original_file_name = new SimpleInputBuffer(metadata).readUTF();
							File replica = new File("storage\\replicated.txt");
							log.append("Moving" +f+ " to "+replica+" original: "+original_file_name+"\n");
							f.renameTo(replica);
						} catch (IOException e) {
							log.append("Error deserializing file: "+ e+"\n");
						}
						
					}
				}, env);
				
				receiver.addListener(new P2PFileTransferListener());
				endpoint.accept(this);
				
			}
			
			@Override
			public void receiveSelectResult(AppSocket socket, boolean canRead,
					boolean canWrite) throws IOException {
				throw new RuntimeException("Thes Method souldent be called");
				
			}
			
			@Override
			public void receiveException(AppSocket socket, Exception e) {
				e.printStackTrace();
				
			}
		});
		
		endpoint.register();
	}
	private class P2PFileTransferListener implements FileTransferListener{

		@Override
		public void fileTransferred(FileReceipt receipt, long bytesTransferred,
				long total, boolean incoming) {
			String s;
			if(incoming){
				s= "downloaded";
			}else {
				s= "uploaded";
			}
			double percent = 100.0 * bytesTransferred/total;
			log.append(s+" : "+percent +"% of "+receipt+"\n");
			
		}

		@Override
		public void msgTransferred(BBReceipt receipt, int bytesTransferred,
				int total, boolean incoming) {
			String s;
			if(incoming){
				s= "downloaded";
			}else {
				s= "uploaded";
			}
			double percent = 100.0 * bytesTransferred/total;
			log.append(s+" : "+percent +"% of "+receipt+"\n");			
		}

		@Override
		public void transferCancelled(Receipt receipt, boolean incoming) {
			String s;
			if(incoming){
				s= "download";
			}else {
				s= "upload";
			}
			log.append(" canceled "+s+" of "+receipt+"\n");			
		}

		@Override
		public void transferFailed(Receipt receipt, boolean incoming) {
			String s;
			if(incoming){
				s= "download";
			}else {
				s= "upload";
			}
			log.append(" failed to  "+s+" of "+receipt+"\n");
			
		}
		
	}
	
	public void sendMessegeDirect(NodeHandle nh){
		log.append("Opening Application socket to "+nh+"\n");
		endpoint.connect(nh, new AppSocketReceiver() {
			
			@Override
			public void receiveSocket(AppSocket socket) throws IOException {
				sender = new FileTransferImpl(socket, null , env);
				sender.addListener(new P2PFileTransferListener());
				ByteBuffer send_ini = ByteBuffer.allocate(5);
				send_ini.put((byte)1);
				send_ini.put((byte)2);
				send_ini.put((byte)3);
				send_ini.put((byte)4);
				send_ini.put((byte)5);
				send_ini.flip();
				log.append("Sending initial messege :  " + send_ini);
				sender.sendMsg(send_ini, (byte)1, null);
				
				final File f = new File("storage\\original.txt");
				if(!f.exists()){
					log.append("File "+f+ "does not exist !"+"\n");
					System.exit(-1);
				}
				SimpleOutputBuffer sob = new SimpleOutputBuffer();
				sob.writeUTF(f.getName());
				sender.sendFile(f, sob.getByteBuffer(), (byte)2 , new Continuation<FileReceipt, Exception>() {

					@Override
					public void receiveResult(FileReceipt result) {
						log.append("Sending complete..."+ result+"\n");
												
					}

					@Override
					public void receiveException(Exception exception) {
						log.append("Error sending ..."+f+ " "+ exception+"\n");
												
					}
				});
			}
			
			@Override
			public void receiveSelectResult(AppSocket socket, boolean canRead,
					boolean canWrite) throws IOException {
				throw new RuntimeException("Thes Method souldent be called");
				
			}
			
			@Override
			public void receiveException(AppSocket socket, Exception e) {
				e.printStackTrace();
				
			}
		}, 30000);
		
	}
	
	@Override
	public boolean forward(RouteMessage message) {
		return true;
	}

	@Override
	public void deliver(Id id, Message message) {
		log.append("Received: "+message+"\n");
	}

	@Override
	public void update(NodeHandle handle, boolean joined) {
		log.append("New node joined :"+handle+" : "+joined+"\n");

	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public Node getNode() {
		return node;
	}
	
	public String toString(){
		return endpoint.getId().toString();
		
	}

}
