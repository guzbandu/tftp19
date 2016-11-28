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
			sendReceiveSocket.receive(parent.receivePacket);
			parent.set_receive_success(true);
			
			//Get Host Port
			if (!parent.hasHostPort){
				parent.hasHostPort = true;
				parent.hostPort = parent.receivePacket.getPort();
			}
			//checking for error code #5
			if (parent.hostPort != parent.receivePacket.getPort()){
				parent.error_number=5;
				parent.set_receive_success(false);
			}
			//checking for error code #4
			if (checkIllegalTFTP(parent.receivePacket)){
				parent.set_receive_success(false);
				parent.error_number=4;
			}
		} catch (SocketTimeoutException e) {
			if(parent.controller.quit) {
				sendReceiveSocket.close();
				System.exit(0);
			} else {
				parent.set_receive_success(false);
			}
		} catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean checkIllegalTFTP(DatagramPacket packet){
		boolean illegalTFTP = false;
		byte[]data = packet.getData();
		int len = packet.getLength();
		boolean ack = false;

		if (data[0]!=0) illegalTFTP = true; // bad
		else if (data[1]>5) illegalTFTP = true; //illegal opcode
		else if (data[1]<1) illegalTFTP = true;
		
		if(data[1]==4) ack = true; //it is an ack request ensure it is only 4 characters in length
		
		if(ack && len>4) illegalTFTP = true; //extra characters at the end of the ack

		if(len>516) illegalTFTP = true; // longer than 516 bytes

		return illegalTFTP;
	}
}

