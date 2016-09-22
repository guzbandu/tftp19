/* 
 * TFTPClientConnection.java
 * This class is the client connection side of a multi-threaded TFTP 
 * server based on UDP/IP. It receives a data or ack packet from the
 * server listener and sends it on a newly created socket to the port
 * provided in the packet. The socket is closed after the packet is sent.
*/

import java.io.IOException;
import java.net.*;

public class TFTPClientConnection extends Thread {

	// UDP datagram packet and socket used to send 
	private DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	
	public TFTPClientConnection(String name, DatagramPacket packet) {
		super(name); // Name the thread
		sendPacket = packet;
		
		// Construct a datagram socket and bind it to any available port
        // on the local host machine. This socket will be used to
        // send a UDP Datagram packet.
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		// Send the datagram packet to the server via the send socket.
		try {
            sendSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
         System.out.println();

         // We're finished with this socket, so close it.
         sendSocket.close();
	}
}
