import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class TFTPReceive extends Thread {
	
	private DatagramSocket sendReceiveSocket;

	public TFTPReceive(DatagramSocket sendReceiveSocket) {
		this.sendReceiveSocket = sendReceiveSocket;
	}

	@Override
	public void run() {
	       try {
	           // Block until a datagram is received via sendReceiveSocket.
	           sendReceiveSocket.receive(TFTPClient.receivePacket);
	           TFTPClient.set_receive_success(true);
	       } catch (SocketTimeoutException e) {
	   			if(TFTPClient.controller.quit) {
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

