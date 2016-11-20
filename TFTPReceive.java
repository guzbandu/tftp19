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
			TFTPClient.set_receive_success(true);
			//Get Host Port
			if (!TFTPClient.hasHostPort){
				TFTPClient.hasHostPort = true;
				TFTPClient.hostPort = TFTPClient.receivePacket.getPort();
			}
			//checking for error code #5
			if (TFTPClient.hostPort != TFTPClient.receivePacket.getPort()){
				throw new TFTPException(5,"Error Code #5: Unknown transfer ID");
			}
			//checking for error code #4
			if (checkIllegalTFTP(TFTPClient.receivePacket)){
				TFTPClient.set_receive_success(false);
				throw new TFTPException(4,"Error Code #4: Illegal TFTP operation");
			}
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
			if (j==len) illegalTFTP = true;// didn't find a 0 byte
			if (j==2) illegalTFTP = true;// filename is 0 bytes long
		}

		if(!illegalTFTP && readWrite) { // check for mode
			// search for next all 0 byte
			for(k=j+1;k<len;k++) { 
				if (data[k] == 0) break;
			}
			//if (k==len) {illegalTFTP = true; System.out.println("FFFFFFFF");}// didn't find a 0 byte
			if (k==j+1) illegalTFTP = true;// mode is 0 bytes long
		}

		if(readWrite && k!=len-1) illegalTFTP = true; // other stuff at end of packet

		if(len>516) illegalTFTP = true; // longer than 516 bytes

		return illegalTFTP;
	}
}

