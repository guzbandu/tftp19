import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class TFTPReceive extends Thread {
	
	private DatagramSocket sendReceiveSocket;
	private TFTPClient parent;

	public TFTPReceive(DatagramSocket sendReceiveSocket, TFTPClient parent) {
		this.sendReceiveSocket = sendReceiveSocket;
		this.parent = parent;
	}

	@Override
	public void run() {
	       try {
	           // Block until a datagram is received via sendReceiveSocket.
	           sendReceiveSocket.receive(TFTPClient.receivePacket);
	    	   System.out.println(TFTPClient.receivePacket.getPort());
	           TFTPClient.set_receive_success(true);
	       } catch (SocketTimeoutException e) {
	   			if(parent.controller.quit) {
	   				sendReceiveSocket.close();
	   				System.exit(0);
	   			} else {
	   				TFTPClient.set_receive_success(false);
	   			}
	        } catch(IOException e) {
	           e.printStackTrace();
	           System.exit(1);
	        }
	}
}

