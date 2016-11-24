import java.io.IOException;
import java.net.DatagramPacket;
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
			//checking for error code #5
			if (parent.hostPort != parent.receivePacket.getPort()){
				parent.error_number=5;
				parent.receive_success=false;
			}
			//checking for error code #4
			if (checkIllegalTFTP(parent.receivePacket)){
				parent.receive_success = false;
				parent.error_number=4;
			}

		} catch (SocketTimeoutException e) {
			if(parent.controller.quit==true) {
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

	private boolean checkIllegalTFTP(DatagramPacket packet){
		boolean illegalTFTP = false;
		int j=0, k=0;
		byte[]data = packet.getData();
		int len = packet.getLength();
		boolean readWrite = false;

		if (data[0]!=0) illegalTFTP = true; // bad
		else if (data[1]>5) illegalTFTP = true; //illegal opcode
		else if (data[1]<1) illegalTFTP = true; 

		if(data[1]==1||data[1]==2) readWrite = true;//it is a read or write request check for filename and mode

		if (!illegalTFTP && readWrite) { // check for filename
			// search for next all 0 byte
			for(j=2;j<len;j++) {
				if (data[j] == 0) break;
			}
			if (j==len) illegalTFTP = true; // didn't find a 0 byte
			if (j==2) illegalTFTP = true; // filename is 0 bytes long
		}

		if(!illegalTFTP && readWrite) { // check for mode
			// search for next all 0 byte
			for(k=j+1;k<len;k++) { 
				if (data[k] == 0) break;
			}
			if (k==len) illegalTFTP = true; // didn't find a 0 byte
			if (k==j+1) illegalTFTP = true; // mode is 0 bytes long
			if(!illegalTFTP) { //check if it is a valid mode
				String mode = new String(data,j+1,k-j-1);
				if(!(mode.equalsIgnoreCase("netascii")
						||mode.equalsIgnoreCase("octet")
						||mode.equalsIgnoreCase("mail"))) illegalTFTP = true;
			}
		}

		if(readWrite && k!=len-1) illegalTFTP = true; // other stuff at end of packet 
		
		if(len>516) illegalTFTP = true; // longer than 516 bytes

		return illegalTFTP;
	}


}
