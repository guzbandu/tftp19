import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class TFTPSimReceive extends Thread {

	private DatagramSocket sendReceiveSocket;
	private TFTPSim parent;

	public TFTPSimReceive(DatagramSocket sendReceiveSocket, TFTPSim parent) {
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
			parent.receive_success = false;
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}
