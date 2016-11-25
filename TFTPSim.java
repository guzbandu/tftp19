// TFTPSim.java
// This class is the beginnings of an error simulator for a simple TFTP server 
// based on UDP/IP. The simulator receives a read or write packet from a client and
// passes it on to the server.  Upon receiving a response, it passes it on to the 
// client.
// One socket (23) is used to receive from the client, and another to send/receive
// from the server.  A new socket is used for each communication back to the client.   

import java.io.*;
import java.net.*; 

public class TFTPSim{

	// UDP datagram packets and sockets used to send / receive
	public static final int SERVER_PORT=2069;
	public static final int CLIENT_PORT=2023;
	private DatagramPacket sendPacket;
	protected DatagramPacket receivePacket;
	protected DatagramSocket receiveSocket;
	private DatagramSocket sendSocket;
	private DatagramSocket sendReceiveSocket;
	private int opCode;
	private int packetCount;
	public static Controller controller;
	protected boolean receive_success = false;
	private boolean networkErrorDone = false;
	private boolean initialConnection = true;
	private int resend_count = 0; //Used to track the number of times we try to resend a packet
	private static final int MAX_RESEND = 10; //The total number of times we will resend before giving up 
	private int clientPort=0, serverPort=SERVER_PORT;

	public TFTPSim()
	{
		try {
			// Construct a datagram socket and bind it to port 23
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets from clients.
			receiveSocket = new DatagramSocket(CLIENT_PORT);
			receiveSocket.setSoTimeout(20);
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets from the server.
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(20);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		// Send the datagram packet to the client via a new socket.
		try {
			// Construct a new datagram socket and bind it to any port
			// on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}

	}

	public void relayPacket()
	{
		byte[] data;

		//int clientPort=0, len, serverPort=69;
		int len;


		packetCount = 0; //We start by dealing with the request packet and the next packet is the "first" packet

		for(;;) { // loop forever

			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			//Receiving packet from client
			receive_success=false; //Start loop not having received anything
			resend_count = 0;
			while(!receive_success&&resend_count<MAX_RESEND) {
				Thread receiveConnection = new TFTPSimReceive(receiveSocket,this);
				receiveConnection.start();
				try{
					receiveConnection.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resend_count++;
			}
			if(receive_success) {
				len = receivePacket.getLength();
				clientPort = receivePacket.getPort();

				System.out.println("Simulator: Client packet received:");
				if (controller.getOutputMode().equals("verbose")){
					TFTPReadWrite.printPacket(receivePacket, clientPort, "receive");
					System.out.println("Client port: "+receivePacket.getPort());

					// Form a String from the byte array, and print the string.
					String received = new String(data,0,len);
					System.out.println(received);
				}

				if(receivePacket.getData()[1] == 1) {
					packetCount = 1; //Data comes back right away
					networkErrorDone = false;
					serverPort = SERVER_PORT;
					opCode = 1;
					initialConnection = true;
				} else if (receivePacket.getData()[1]==2) {
					packetCount = 0; //There is a 0 ack
					networkErrorDone = false;
					serverPort = SERVER_PORT;
					opCode = 2;
					initialConnection = true;
				} else {
					initialConnection = false;
				}

				sendPacket = new DatagramPacket(data, len,
						receivePacket.getAddress(), serverPort);

				System.out.println("Simulator: sending client packet.");
				if (controller.getOutputMode().equals("verbose")){
					TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
				}

				// Send the datagram packet to the server via the send/receive socket potentially starting the error scenario
				errorSimSend(sendReceiveSocket,sendPacket,data);

				if (controller.getOutputMode().equals("verbose")){
					System.out.println("Simulator: packet sent using port " + sendReceiveSocket.getLocalPort());
					System.out.println();
					System.out.println("Packet count "+packetCount);
				}
				
			}

			if(opCode==1&&!initialConnection) packetCount++;


			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			//Receive packet from server
			receive_success=false; //Start loop not having received anything
			resend_count = 0;
			while(!receive_success&&resend_count<MAX_RESEND) {
				Thread receiveConnection = new TFTPSimReceive(sendReceiveSocket,this);
				receiveConnection.start();
				try{
					receiveConnection.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resend_count++;
			}
			if(receive_success) {

				serverPort = receivePacket.getPort();

				System.out.println("Simulator: Server packet received:");
				if (controller.getOutputMode().equals("verbose")){
					TFTPReadWrite.printPacket(receivePacket, serverPort, "receive");
				}

				sendPacket = new DatagramPacket(data, receivePacket.getLength(),
						receivePacket.getAddress(), clientPort);
				len = sendPacket.getLength();
				System.out.println( "Simulator: Sending server packet:");
				if (controller.getOutputMode().equals("verbose")){
					TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
				}

				//System.out.println("sendSocket:"+sendSocket);
				errorSimSend(sendSocket,sendPacket,data);

				if (controller.getOutputMode().equals("verbose")){
					System.out.println("Simulator: packet sent using port " + sendSocket.getLocalPort());
					System.out.println();
					System.out.println("Packet count "+packetCount);

				}

				if(opCode==2)packetCount++;
			}

		} // close 'if' that checks if the sim should start back at the top
	} // end of loop

	// }

	private void errorSimSend(DatagramSocket socket, DatagramPacket packet, byte[] d){
		if(controller.getTestSituation()==1 && packet.getData()[1]==controller.getAffectedOpcode() 
				&& packetCount == controller.getPacketNumber() && !networkErrorDone){
			networkErrorDone = true;
			System.out.println("Losing packet.");
			System.out.println();
			return; //lose packet
		}else if(controller.getTestSituation()==2 && packet.getData()[1]==controller.getAffectedOpcode()
				&& packetCount == controller.getPacketNumber() && !networkErrorDone){
			networkErrorDone = true;
			System.out.println("Delaying packet.");
			System.out.println();
			//delay packet
			try {
				Thread.sleep(controller.getDelayTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}   
		}else if(controller.getTestSituation()==3 && packet.getData()[1]==controller.getAffectedOpcode()
				&& packetCount == controller.getPacketNumber() && !networkErrorDone){
			networkErrorDone = true;
			System.out.println("Duplicating packet.");
			System.out.println();
			//duplicate packet. Send packet, sleep, then send again.
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} 
			try {
				Thread.sleep(controller.getDelayTime());
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} 
		}else if(controller.getTestSituation()==4 && packet.getData()[1]==controller.getAffectedOpcode()
				&& packetCount == controller.getPacketNumber() && !networkErrorDone) {

			int nullIndex = 0;
			for (int a = 2; a < packet.getLength(); ++a) {
				if (packet.getData()[a] == 0) {
					nullIndex = a;
					break;
				}
			}

			networkErrorDone = true;
			byte[] illegalData = null;
			int len = packet.getLength();

			if(controller.getIllegalOperation()==1) {
				// invalid TFTP opcode
				len = packet.getLength();
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, len);
				illegalData[1] = 7;
			}
			else if(controller.getIllegalOperation()==2) {
				// invalid mode
				len = packet.getLength()+1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, nullIndex+1);
				illegalData[nullIndex+1] = (byte) '*';
				System.arraycopy(packet.getData(), nullIndex+1, illegalData, nullIndex+2, packet.getLength()-1-nullIndex);
			}
			else if(controller.getIllegalOperation()==3) {
				// invalid filename
				len = packet.getLength()+1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, nullIndex);
				illegalData[nullIndex] = (byte) '*';
				System.arraycopy(packet.getData(), nullIndex, illegalData, nullIndex+1, packet.getLength()-nullIndex);
			}
			else if(controller.getIllegalOperation()==4) {
				// invalid block number
				len = packet.getLength();
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, len);
				illegalData[3] = (byte) (illegalData[3] + controller.getByteChange());
			}
			else if(controller.getIllegalOperation()==5) {
				// no null separation between filename and mode
				len = packet.getLength()-1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, nullIndex);
				System.arraycopy(packet.getData(), nullIndex+1, illegalData, nullIndex, packet.getLength()-1-nullIndex);
			}
			else if(controller.getIllegalOperation()==6) {
				// extra null separation
				len = packet.getLength()+1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, nullIndex+1);
				illegalData[nullIndex+1] = 0;
				System.arraycopy(packet.getData(), nullIndex+1, illegalData, nullIndex+2, packet.getLength()-1-nullIndex);
			}
			else if(controller.getIllegalOperation()==7) {
				// no null termination
				len = packet.getLength()-1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, packet.getLength()-1);
			}
			else if(controller.getIllegalOperation()==8) {
				// extra null termination
				len = packet.getLength()+1;
				illegalData = new byte[len];
				System.arraycopy(packet.getData(), 0, illegalData, 0, packet.getLength());
				illegalData[packet.getLength()] = 0;
			}

			//if normal leave packet as is
			if (controller.getIllegalOperation()!=0) {
				packet = new DatagramPacket(illegalData, len, packet.getAddress(), packet.getPort()); 
			}
			
			System.out.println("Illegal operation packet.");
			System.out.println();
			//send packet
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

		}else if(controller.getTestSituation()==5 && packet.getData()[1]==controller.getAffectedOpcode()
				&& packetCount == controller.getPacketNumber() && !networkErrorDone) {

			networkErrorDone = true;

			System.out.println("Unknown TID packet.");
			System.out.println();
			//send packet using unknownTID
			DatagramSocket unknownTID = null;
			try {
				unknownTID = new DatagramSocket();
				unknownTID.send(packet);
				unknownTID.disconnect();
				unknownTID.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			} 

			//send packet using normal TID
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}


		}else{
			//send packet normally
			try {
				socket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static void main( String args[] ){
		TFTPSim sim = new TFTPSim();
		controller = new Controller(sim);
		controller.start();
		sim.relayPacket();
	}
}
