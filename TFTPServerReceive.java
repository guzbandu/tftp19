import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class TFTPServerReceive extends Thread {

	private DatagramSocket sendReceiveSocket;
	private TFTPClientConnection parent;

	public TFTPServerReceive(DatagramSocket sendReceiveSocket, TFTPClientConnection parent) {
		this.sendReceiveSocket = sendReceiveSocket;
		this.parent = parent;
	}

	@Override
	public void run() {
	       try {
	           // Block until a datagram is received via sendReceiveSocket.
	           sendReceiveSocket.receive(parent.receivePacket);
	           parent.receive_success = true;
	       } catch (SocketTimeoutException e) {
	   			if(TFTPClient.controller.quit) {
	   				sendReceiveSocket.close();
	   				System.exit(0);
	   			} else {
	   				parent.receive_success = false;
	   			}
	        } catch(IOException e) {
	           e.printStackTrace();
	           System.exit(1);
	        }
	}

	
}
