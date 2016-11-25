/* 
 * TFTPClientConnection.java
 * This class is the client connection side of a multi-threaded TFTP 
 * server based on UDP/IP. It receives a data or ack packet from the
 * server listener and sends it on a newly created socket to the port
 * provided in the packet. The socket is closed after the packet is sent.
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class TFTPClientConnection extends Thread {

	// types of requests we can receive
	public static enum Request { READ, WRITE, ERROR};
	// responses for valid requests
	public static final byte[] readResp = {0, 3, 0, 1};
	public static final byte[] writeResp = {0, 4, 0, 0};

	// UDP datagram packet and socket used to send 
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket;
	public DatagramPacket receivePacket;
	public boolean receive_success = false; //Used to track if our receive thread was successful
	protected int error_number = 0;
	protected boolean err_msg_sent;
	private int resend_count = 0; //Used to track the number of times we try to resend a packet
	private static final int MAX_RESEND = 10; //The total number of times we will resend before giving up TODO change this once finished testing
	protected String outputMode;
	protected Controller controller;
	public int hostPort;
	public int expectedBlockNumber;
	private int oldPacketNo; 

	public TFTPClientConnection(String name, DatagramPacket packet, Controller controller, int hostPort) {
		super(name); // Name the thread
		receivePacket = packet;
		this.controller = controller;
		this.outputMode = controller.getOutputMode();
		this.hostPort = hostPort;

		// Construct a datagram socket and bind it to any available port
		// on the local host machine. This socket will be used to
		// send a UDP Datagram packet.
		try {
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(2000); 
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		byte[] data = receivePacket.getData();
		byte[] response = new byte[4];
		boolean quit = false;
		int packetNumber = 1;
		int ackPacketNumber = 1;
		int finalPacketCount = 0;

		Request req; // READ, WRITE or ERROR

		String path = controller.getPath();

		String filename = "";
		String mode;
		int len = receivePacket.getLength();
		int j=0, k=0;


		// If it's a read, send back DATA (03) block 1
		// If it's a write, send back ACK (04) block 0
		// Otherwise, ignore it
		if (data[0]!=0) req = Request.ERROR; // bad
		else if (data[1]==1) req = Request.READ; // could be read
		else if (data[1]==2) req = Request.WRITE; // could be write
		else req = Request.ERROR; // bad

		if (req!=Request.ERROR) { // check for filename
			// search for next all 0 byte
			for(j=2;j<len;j++) {
				if (data[j] == 0) break;
			}
			if (j==len) req=Request.ERROR; // didn't find a 0 byte
			if (j==2) req=Request.ERROR; // filename is 0 bytes long
			// otherwise, extract filename
			if(req!=Request.ERROR) {
				filename = new String(data,2,j-2);
				if(controller.getOutputMode().equals("verbose")) {
					System.out.println("filename: "+filename);
				}
			}
		}

		if(req!=Request.ERROR) { // check for mode
			// search for next all 0 byte
			for(k=j+1;k<len;k++) { 
				if (data[k] == 0) break;
			}
			if (k==len) req=Request.ERROR; // didn't find a 0 byte
			if (k==j+1) req=Request.ERROR; // mode is 0 bytes long
			mode = new String(data,j+1,k-j-1);
			if(req!=Request.ERROR) { //check if it is a valid mode
				if(mode.equalsIgnoreCase("netascii")
						||mode.equalsIgnoreCase("octet")
						||mode.equalsIgnoreCase("mail")) {
					if(controller.getOutputMode().equals("verbose")) {
						System.out.println("mode: "+mode);
						System.out.println();
					}
				} else {
					req=Request.ERROR;
				}
			}
		}

		if(k!=len-1) req=Request.ERROR; // other stuff at end of packet     

		// Send Error Packet for Error Code #4
		if (req==Request.ERROR) {
			TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
			sendPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
					receivePacket.getAddress(), receivePacket.getPort());
			quit = true;
		}

		//Create instance to handle file operations
		TFTPReadWrite fileHandler;
		if (req==Request.WRITE) {
			try{
				fileHandler = new TFTPReadWrite(filename, "WRITE", path, "Client Connection");
			}catch(TFTPException e){
				sendPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), receivePacket.getPort());
				req=Request.ERROR;
				fileHandler = null;
				quit = true;
			}
		} else if (req==Request.READ) {
			try{
				fileHandler = new TFTPReadWrite(filename, "READ", path, "Client Connection");
			}catch(TFTPException e){
				sendPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), receivePacket.getPort());
				req=Request.ERROR;
				fileHandler = null;
				quit = true;
			}
		} else { //Error Code #4
			fileHandler = null;
		}

		if (req==Request.READ) { // for Read it's 0301
			int length = 512;
			if (fileHandler.getFileLength() < 512) {
				length = fileHandler.getFileLength();
				quit = true;
			}
			response = new byte[length + 4];
			System.arraycopy(readResp, 0, response, 0, 4);
			System.arraycopy(fileHandler.readFileBytes(length), 0, response, 4, length);
		} else if (req==Request.WRITE) { // for Write it's 0400
			response = writeResp;
		} 

		// Construct a datagram packet that is to be sent to a specified port
		// on a specified host.
		// The arguments are:
		//  data - the packet data (a byte array). This is the response.
		//  receivePacket.getLength() - the length of the packet data.
		//     This is the length of the msg we just created.
		//  receivePacket.getAddress() - the Internet address of the
		//     destination host. Since we want to send a packet back to the
		//     client, we extract the address of the machine where the
		//     client is running from the datagram that was sent to us by
		//     the client.
		//  receivePacket.getPort() - the destination port number on the
		//     destination host where the client is running. The client
		//     sends and receives datagrams through the same socket/port,
		//     so we extract the port that the client used to send us the
		//     datagram, and use that as the destination port for the TFTP
		//     packet.
		if(!(req==Request.ERROR)) {
			sendPacket = new DatagramPacket(response, response.length,
					receivePacket.getAddress(), receivePacket.getPort());
		}
		//Output packet to send
		System.out.println("Server: Sending packet:");
		if (outputMode.equals("verbose")){
			if(sendPacket!=null) TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
		}
		//Sending first packet, iterator starts at packet to send next
		int i = 2;
		try {
			if(sendPacket!=null) sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		byte unsignedByteTens = (byte) (sendPacket.getData()[2]);
		byte unsignedByteOnes = (byte) (sendPacket.getData()[3]);
		int packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
		System.out.println("Packet No.: " + packetNo);
		if (outputMode.equals("quiet")) System.out.println();
		if (outputMode.equals("verbose")){
			System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
			System.out.println();
		}

		//Handle final ack
		if (req==Request.READ && quit) {
			if (controller.getOutputMode().equals("verbose")){
				System.out.println("Waiting for packet");
			}

			receivePacket = new DatagramPacket(data, data.length);

			//Receiving packet from client			
			receivePacketFromClient(req);

			if(!receive_success) {
				req = Request.ERROR;
				return;
			}

			// Process the received datagram.
			len = receivePacket.getLength();
			System.out.println("Server: Packet received:");
			if (controller.getOutputMode().equals("verbose")){
				TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
			}
			//Make the byte counts in the range from 0 to 255 instead of -128 to 127
			unsignedByteTens = (byte) (receivePacket.getData()[2]);
			unsignedByteOnes = (byte) (receivePacket.getData()[3]);
			packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
			System.out.println("Packet No.: " + packetNo + "\n");
			if(packetNo>ackPacketNumber||packetNo<=(ackPacketNumber-3)) { //Check for invalid packet number
				TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
				DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), receivePacket.getPort());
				sendErrorMessage(unknownIDPacket);
			}

		}

		//Main loop
		while (!quit) {

			data = new byte[516];
			if (controller.getOutputMode().equals("verbose")){
				System.out.println("Waiting for packet");
			}

			receivePacket = new DatagramPacket(data, data.length);
			
			//Receiving packet from client
			receivePacketFromClient(req);
			
			if(!receive_success) {
				req = Request.ERROR;
				break;
			}

			// Process the received datagram.
			len = receivePacket.getLength();
			System.out.println("Server: Packet received:");
			if (controller.getOutputMode().equals("verbose")){
				TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
			}
			//Make the byte count in the range from 0 to 255 instead of -128 to 127
			unsignedByteTens = (byte) (receivePacket.getData()[2]);
			unsignedByteOnes = (byte) (receivePacket.getData()[3]);
			packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
			System.out.println("Packet No.: " + packetNo + "\n");
			if(packetNo==0&&oldPacketNo>packetNo) { //The counter has rolled over
				packetNumber = 0; 
				ackPacketNumber = 0; 
			}
			oldPacketNo = packetNo;

			//Checking for error packets
			if(5 == (int)((receivePacket.getData()[0] << 8) + receivePacket.getData()[1])) {
				if(!(5 == (int)((receivePacket.getData()[2] << 8) + receivePacket.getData()[3]))){ //Unless it is an unknown TID error quit
					req = Request.ERROR;
					System.out.println("Error from the client:");
					String message = new String(data);
					message = message.substring(4,message.length());
					System.out.println(message+"\n");
					break; //Stop transfer error from client received
				}
			}
			
			//If it is not an error packet check for illegal packet numbers
			if(req == Request.READ) {
				if (packetNo>ackPacketNumber||packetNo<=(ackPacketNumber-3)) {
					TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), receivePacket.getPort());
					sendErrorMessage(unknownIDPacket);
					req = Request.ERROR;
					break;
				} 
			} else if (req == Request.WRITE) {
				if (packetNo>packetNumber||packetNo<=(packetNumber-3)) {
					TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), receivePacket.getPort());
					sendErrorMessage(unknownIDPacket);
					req = Request.ERROR;
					break;
				}
			}

			//Checking if received last packet
			if ((req == Request.WRITE) && len < 516) { 
				if(packetNumber==packetNo) quit = true;
			}
			//Creating packet to send, setting op code and data to send if client reading
			if(req == Request.READ) {
				if (i < fileHandler.getNumSections()) {
					finalPacketCount = 0;
				}
				if (ackPacketNumber==packetNo && i>=fileHandler.getNumSections())	{
					finalPacketCount++;
				}
				if(ackPacketNumber==packetNo&&finalPacketCount<=1) {
					int length = 512;
					if (i == fileHandler.getNumSections())
						length = fileHandler.getFileLength() - ((fileHandler.getNumSections()-1) * 512);
					response = new byte[length+4];
					response[0] = 0;
					response[1] = 3;
					response[2] = (byte) ((i >> 8)& 0xff);
					response[3] = (byte) (i & 0xff);
					try {
						System.arraycopy(fileHandler.readFileBytes(length), 0, response, 4, length);
					} catch(TFTPException e) {
						response = new byte[516];
						byte[] error = e.getErrorBytes();
						System.arraycopy(error, 0, response, 0, error.length);
						req=Request.ERROR;
						quit = true;
					}
					len = length+4;
					//if()
				} 
			} else if(req == Request.WRITE) {
				if(packetNumber==packetNo) {
					response = new byte[4];
					response[0] = 0;
					response[1] = 4;
					response[2] = data[2];
					response[3] = data[3];
					len = 4;
					try {
						fileHandler.writeFilesBytes(Arrays.copyOfRange(receivePacket.getData(), 4, receivePacket.getLength()));
					} catch(TFTPException e) {
						response = new byte[516];
						byte[] error = e.getErrorBytes();
						System.arraycopy(error, 0, response, 0, error.length);
						req=Request.ERROR;
						quit = true;
					}
					packetNumber++;
				} else {
					System.out.println("Duplicate packet ignored.");
					System.out.println("");
				}
			}

			if(req == Request.WRITE || (req== Request.READ && ackPacketNumber==packetNo&&finalPacketCount<=1)) {
				sendPacket = new DatagramPacket(response, response.length,
						receivePacket.getAddress(), receivePacket.getPort());

				//Output for sending packet
				System.out.println("Server: Sending packet:");
				if (outputMode.equals("verbose")){
					TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
				}
				unsignedByteTens = (byte) (sendPacket.getData()[2]);
				unsignedByteOnes = (byte) (sendPacket.getData()[3]);
				packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
				System.out.println("Packet No.: " + packetNo);
				if(outputMode.equals("quiet")){
					System.out.println();
				}
				
				// Send the datagram packet to the server via the send socket.
				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				if (outputMode.equals("verbose")){
					System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
					System.out.println();
				}

				if (req==Request.READ) {
					ackPacketNumber++;
				}

				if ((req == Request.READ) && i >= fileHandler.getNumSections())	{
					quit = true;
				}
				i++;

			} else if (req== Request.READ && ackPacketNumber!=packetNo) {
				System.out.println("Duplicate ack ignored.");
				System.out.println("");				
				if (i >= fileHandler.getNumSections()&& finalPacketCount>=1)	{
					quit = true;
				}
			} else if (req==Request.READ&&ackPacketNumber==packetNo&&finalPacketCount>=1) {
				break; //we have received the final ack packet and we may quit
			}

			if(quit&&req==Request.READ) {
				if (controller.getOutputMode().equals("verbose")){
				}

				receivePacket = new DatagramPacket(data, data.length);
				//Receiving packet from client
				receivePacketFromClient(req);

				if(!receive_success) {
					req = Request.ERROR;
					break;
				}

				// Process the received datagram.
				len = receivePacket.getLength();
				System.out.println("Server: Packet received:");
				if (controller.getOutputMode().equals("verbose")){
					TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
				}
				//Make the byte count in the range from 0 to 255 instead of -128 to 127
				unsignedByteTens = (byte) (receivePacket.getData()[2]);
				unsignedByteOnes = (byte) (receivePacket.getData()[3]);
				packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
				System.out.println("Packet No.: " + packetNo + "\n");
				if(packetNo==0&&oldPacketNo>packetNo) { //The counter has rolled over
					packetNumber = 0;
					ackPacketNumber = 0;
				}
				oldPacketNo = packetNo;

				//Checking for error packets
				if(5 == (int)((receivePacket.getData()[0] << 8) + receivePacket.getData()[1])) {
					if(!(5 == (int)((receivePacket.getData()[2] << 8) + receivePacket.getData()[3]))){ //Unless it is an unknown TID error quit
						req = Request.ERROR;
						System.out.println("Error from the client:");
						String message = new String(data);
						message = message.substring(4,message.length());
						System.out.println(message+"\n");
						break; //Stop transfer error from client received
					}
				}
				
				if(req==Request.READ) {
					if(ackPacketNumber==packetNo) {
						ackPacketNumber++;
					} else if (packetNo>ackPacketNumber||packetNo<=(ackPacketNumber-3)) {
						TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
						DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
								receivePacket.getAddress(), receivePacket.getPort());
						sendErrorMessage(unknownIDPacket);
						req = Request.ERROR;
						break;
					} else {
						System.out.println("Duplicate ack.");
						System.out.println("");
						quit=false;
					}
				}
			}
		}

		// We're finished with this socket, so close it.
		sendReceiveSocket.close();
		if(req == Request.ERROR) {
			System.out.println("Error during file transfer");
		} else {
			System.out.println("File Transfer Complete");
		}
		System.out.println();
		if(req == Request.READ) {
			try {
				fileHandler.closeInFile();
			} catch (TFTPException e) {
				System.out.println("Error closing file " + path + filename + "\n" );
			}
		} else if (req == Request.WRITE) {
			try {
				fileHandler.closeOutFile();
			} catch (TFTPException e) {
				System.out.println("Error closing file " + path + filename + "\n" );
			}
		}		
	}
	
	private void receivePacketFromClient(Request req) {
		receive_success=false; //Start loop not having received anything
		resend_count = 0;
		error_number = 0;
		err_msg_sent = false;
		while(!receive_success&&resend_count<MAX_RESEND) {
			if(req==Request.ERROR&&resend_count>=1) break;
			Thread receiveConnection = new TFTPServerReceive(sendReceiveSocket,this);
			try{
				receiveConnection.start();
			}catch(TFTPException e){
				req=Request.ERROR;
			}
			try{
				receiveConnection.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(error_number==5&&!err_msg_sent) { //send a message to unknown TID and continue normal flow. IMPORTANT only send the error message once
				err_msg_sent = true;
				System.out.println("Server: Unknown TID received:");
				if (outputMode.equals("verbose")){
					TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
				}				
				TFTPException e = new TFTPException(5,"Error Code #5: Unknown transfer ID");
				DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), receivePacket.getPort());
				sendErrorMessage(unknownIDPacket);
			} else if (error_number==4) { //send an error message and terminate flow
				TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
				DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), receivePacket.getPort());
				sendErrorMessage(unknownIDPacket);
				break;
			}
			if(error_number==0||error_number==5) {
				if(!receive_success) {
					//we did not receive a packet before timing out, re-send our packet
					//Sending request packet
					try {
						if(controller.getOutputMode().equals("verbose"))
							TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					if (controller.getOutputMode().equals("verbose")) {
						System.out.println("Client: Re-sending packet.\n");
						System.out.println();
					}
				}
				resend_count++;
			}
		}
	}
	
	private void sendErrorMessage(DatagramPacket errorPacket) {
		System.out.println("Server: Sending packet:");
		if (outputMode.equals("verbose")){
			TFTPReadWrite.printPacket(errorPacket, errorPacket.getPort(), "send");
		}
		byte unsignedByteTens = (byte) (errorPacket.getData()[2]);
		byte unsignedByteOnes = (byte) (errorPacket.getData()[3]);
		int packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
		System.out.println("Packet No.: " + packetNo);
		// Send the datagram packet to the client via the send socket.
		try {
			sendReceiveSocket.send(errorPacket);
		} catch (IOException ie) {
			ie.printStackTrace();
			System.exit(1);
		}
		if (outputMode.equals("verbose")){
			System.out.println("Server: packet sent using port " + sendReceiveSocket.getLocalPort());
			System.out.println();
		}
	}
}