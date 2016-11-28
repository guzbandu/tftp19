// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class TFTPClient {

	public DatagramPacket receivePacket;
	private DatagramPacket sendPacket;
	private DatagramSocket sendReceiveSocket;
	public Controller controller;
	private int resend_count = 0; //Used to track the number of times we try to resend a packet
	private final int MAX_RESEND = 10; //The total number of times we will resend before giving up TODO change this once finished testing
	private boolean receive_success = false; //Used to track if our threads receive was successful
	protected int error_number = 0;
	protected boolean err_msg_sent;
	protected int hostPort = -1;
	protected boolean hasHostPort;
	private int oldPacketNo;

	public synchronized void set_receive_success(boolean success) {
		receive_success = success;
	}

	public TFTPClient()
	{
		controller = new Controller(this);
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {   // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

<<<<<<< HEAD
	public void sendAndReceive(String request, String filename, String mode, String path, String outputMode, String runMode, InetAddress serverIp)
=======
	public void sendAndReceive(String request, String filename, String mode, String path, String outputMode, String runMode, InetAddress serverip)
>>>>>>> c95ef36ebfb23c817bb38438140bc012867dabd9
	{
		byte[] msg = new byte[100], // message we send
				fn, // filename as an array of bytes
				md, // mode as an array of bytes
				data; // reply as array of bytes
		int j, len, sendPort;
		boolean quit = false; //Used for exit condition
		boolean last_packet = false; //Used to ensure final ack is sent
		int packetNumber = 1;
		int ackPacketNumber = 0; //the initial request returns a 00 ack
		boolean send = false; //Used if error on this side and we need to send the final error message
		hasHostPort = false;
		int finalPacketCount = 0;
	    
		//If user enters "normal" as the mode
		//user sends directly to port 69 on the server
		//otherwise it sends to the error simulator
		if (runMode.equals("normal")) 
			sendPort = 69;
		else
			sendPort = 23;
		
		//01 for READ and 02 for WRITE
		msg[0] = 0;
		if(request.equalsIgnoreCase("READ"))
			msg[1]=1;
		if(request.equalsIgnoreCase("WRITE")) 
			msg[1]=2;

		//Create class instance handling file I/O
		//If client is writing, it's reading from a file, if
		//client is reading, it's writing to a file
		TFTPReadWrite fileHandler;
		if(request.equalsIgnoreCase("READ")) {
			try{
				fileHandler = new TFTPReadWrite(filename, "WRITE", path, "Client");
			}catch(TFTPException e){
				System.out.println("The specified file " + path + filename + " already exists.");
				return;
			}
		} else {
			try{
				fileHandler = new TFTPReadWrite(filename, "READ", path, "Client");
			}catch(TFTPException e){
				System.out.println("The specified file " + path + filename + " was not found.\n");
				return;
			}
		}

		// convert to bytes
		fn = filename.getBytes(); 

		// and copy into the msg
		System.arraycopy(fn,0,msg,2,fn.length);
		// format is: source array, source index, dest array,
		// dest index, # array elements to copy
		// i.e. copy fn from 0 to fn.length to msg, starting at
		// index 2

		// now add a 0 byte
		msg[fn.length+2] = 0;

		// now add "octet" (or "netascii")
		md = mode.getBytes();

		// and copy into the msg
		System.arraycopy(md,0,msg,fn.length+3,md.length);

		len = fn.length+md.length+4; // length of the message
		// length of filename + length of mode + opcode (2) + two 0s (2)
		// second 0 to be added next:

		// end with another 0 byte 
		msg[len-1] = 0;

		// Construct a datagram packet that is to be sent to a specified port
		// on a specified host.
		// The arguments are:
		//  msg - the message contained in the packet (the byte array)
		//  the length we care about - k+1        
		//  InetAddress.getLocalHost() - the Internet address of the
		//     destination host
		//     In this example, we want the destination to be the same as
		//     the source (i.e., we want to run the client and server on the
		//     same computer). InetAddress.getLocalHost() returns the Internet
		//     address of the local host.
		//  69 - the destination port number on the destination host.
		//Sending packet
		
<<<<<<< HEAD
		sendPacket = new DatagramPacket(msg, len, serverIp, sendPort);
=======
		sendPacket = new DatagramPacket(msg, len, serverip, sendPort);
>>>>>>> c95ef36ebfb23c817bb38438140bc012867dabd9

		//Output for sending packet
		if (outputMode.equals("verbose")){
			System.out.println("Client: Sending packet:");
			TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
		}

		//Sending initial request packet
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (outputMode.equals("verbose")) {
			System.out.println("Client: packet sent using port " + sendReceiveSocket.getLocalPort());
			System.out.println();
		}		

		int i = 1;
		//Main loop
		while (!quit) {

			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			if (outputMode.equals("verbose"))
				System.out.println("Client: Waiting for packet.");

			//Receiving packet
			receivePacketFromServer(request, outputMode, runMode, sendPort);

			if(!receive_success) {
				request = "ERROR";
				break;
			}

			// Process the received datagram.
			len = receivePacket.getLength();
			System.out.println("Client: Packet received:");
			if (outputMode.equals("verbose")){
				TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
			}
			
			byte unsignedByteTens = (byte) (receivePacket.getData()[2]);
			byte unsignedByteOnes = (byte) (receivePacket.getData()[3]);
			int packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
			System.out.println("Packet No.: " + packetNo);
			if(outputMode!="verbose") {
				System.out.println(); //Add a blank line separator if in quiet mode
			}
			if(packetNo==0&&oldPacketNo>packetNo) { //The counter has rolled over
				packetNumber = 0; 
				ackPacketNumber = 0; 
			}
			oldPacketNo = packetNo;

			//Checking for error packets
			if(5 == (int)((receivePacket.getData()[0] << 8) + receivePacket.getData()[1])) {
				if(!(5 == (int)((receivePacket.getData()[2] << 8) + receivePacket.getData()[3]))){ //Unless it is an unknown TID error quit
					request = "ERROR";
					System.out.print("Error from the server:");
					String message = new String(data);
					message = message.substring(4,message.length());
					System.out.println(message+"\n");
					quit = true;
				}
			}

			//If not an error packet then check for illegal packet numbers
			if(request.equalsIgnoreCase("WRITE")) {
				if (packetNo>ackPacketNumber||packetNo<=(ackPacketNumber-3)) { //Check for illegal packet number
					TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
					int p; // Port we are sending to
					// Sim's sendSocket is 23, Server's is the Thread's
					if (runMode.equals("test")) p = sendPort;
					else p = receivePacket.getPort();
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), p);
					sendErrorMessage(unknownIDPacket);
					request = "ERROR";
					break;
				} 
			} else if (request.equalsIgnoreCase("READ")) {
				if (packetNo>packetNumber||packetNo<=(packetNumber-3)) { //Check for illegal packet number
					int p; // Port we are sending to
					// Sim's sendSocket is 23, Server's is the Thread's
					if (runMode.equals("test")) p = sendPort;
					else p = receivePacket.getPort();
					TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), p);
					sendErrorMessage(unknownIDPacket);
					request = "ERROR";
					break;
				}
			}

			if (packetNumber==packetNo) {
				if (request.equalsIgnoreCase("READ")){
					try {
						fileHandler.writeFilesBytes(Arrays.copyOfRange(data, 4, len));
					} catch (TFTPException e) {
						request = "ERROR";
						msg = new byte[516];
						byte[] error = e.getErrorBytes();
						System.arraycopy(error, 0, msg, 0, error.length);
						last_packet=true; //We send this error message then we quit
						send=true;
						System.out.println("The disk ran out of space while reading was in progress.");
					}
					if (outputMode.equals("verbose")){
						System.out.println("Data length: " + len);
						System.out.println();
					}
					if (len < 516)
						last_packet=true;
				}
				packetNumber++;
			} else {
				if(request.equalsIgnoreCase("READ")) {
					System.out.println("Duplicate packet ignored.");
					System.out.println();
				}
			}

			//Doesn't reset quit condition if error packet sent
			if(!request.equalsIgnoreCase("ERROR")) quit = false;

			//Preparing next packet
			if(request.equalsIgnoreCase("WRITE")) {
				if (i < fileHandler.getNumSections()) {
					finalPacketCount = 0;
				}
				if (ackPacketNumber==packetNo && i>=fileHandler.getNumSections())	{
					finalPacketCount++;
				}
				if(ackPacketNumber==packetNo&&finalPacketCount<=1){
					int length = 512;
					if (i == fileHandler.getNumSections())
						length = fileHandler.getFileLength() - ((fileHandler.getNumSections()-1) * 512);
					msg = new byte[length+4];
					msg[0] = 0;
					msg[1] = 3;
					msg[2] = (byte) ((i >> 8)& 0xff);
					msg[3] = (byte) (i & 0xff);
					try {
						System.arraycopy(fileHandler.readFileBytes(length), 0, msg, 4, length);
					} catch (TFTPException e) { //Error
						System.out.println("Unable to access either the parent directory or file " + path + filename + "\n" );
						request = "ERROR";
						msg = new byte[516];
						byte[] error = e.getErrorBytes();
						System.arraycopy(error, 0, msg, 0, error.length);
						last_packet=true; //We send this error message then we quit
						send=true;
						//Make this the final packet to the server and send an error.
					}
					len = length+4;
					if(i >= fileHandler.getNumSections() ) {
						quit = true;
					}
				}
			} else if(request.equalsIgnoreCase("READ")) {
				msg = new byte[4];
				msg[0] = 0;
				msg[1] = 4;
				msg[2] = data[2];
				msg[3] = data[3];
				len = 4;
			}

			if(!request.equalsIgnoreCase("ERROR")||send) {
				if(request.equalsIgnoreCase("READ")||(request.equalsIgnoreCase("WRITE")&&ackPacketNumber==packetNo&&finalPacketCount<=1)||send) {
					int p; // Port we are sending to
					// Sim's sendSocket is 23, Server's is the Thread's
					if (runMode.equals("test")) p = sendPort;
					else p = receivePacket.getPort();

					//Sending packet
				
<<<<<<< HEAD
					sendPacket = new DatagramPacket(msg, len, serverIp, p);
=======
					sendPacket = new DatagramPacket(msg, len, serverip, p);
>>>>>>> c95ef36ebfb23c817bb38438140bc012867dabd9
						
					//Output for sending packet
					System.out.println("Client: Sending packet:");
					if (outputMode.equals("verbose")){						
						TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
					}
					unsignedByteTens = (byte) (sendPacket.getData()[2]);
					unsignedByteOnes = (byte) (sendPacket.getData()[3]);
					packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
					System.out.println("Packet No.: " + packetNo);

					// Send the datagram packet to the server via the send/receive socket.

					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					if (outputMode.equals("verbose")) {
						System.out.println("Client: packet sent using port " + sendReceiveSocket.getLocalPort());
						System.out.println();
					}		


					// Construct a DatagramPacket for receiving packets up
					// to 100 bytes long (the length of the byte array).

					i++;

					ackPacketNumber++;
				} else if (request.equalsIgnoreCase("WRITE")&&ackPacketNumber!=packetNo) {
					System.out.println("Ignoring duplicate ack.");
					System.out.println();
					if(i >= fileHandler.getNumSections() && finalPacketCount>=1)
						quit = true;
				} else if (request.equalsIgnoreCase("WRITE")&&ackPacketNumber==packetNo&&finalPacketCount>=1) {
					break; //we have received the final ack packet and we may quit
				}

				System.out.println();

				/* Sent final packet can break now */
				if(last_packet) break;

				/* Wait for final acknowledgement */
				if(quit&&request.equalsIgnoreCase("WRITE")) {
					if (outputMode.equals("verbose"))
						System.out.println("Client: Waiting for packet.");

					//Receiving packet
					receivePacketFromServer(request, outputMode, runMode, sendPort);
					
					if(!receive_success) {
						request = "ERROR";
						break;
					}

					// Process the received datagram.
					len = receivePacket.getLength();
					System.out.println("Client: Packet received:");
					if (outputMode.equals("verbose")){
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
							request = "ERROR";
							System.out.print("Error from the server:");
							String message = new String(data);
							message = message.substring(4,message.length());
							System.out.println(message+"\n");
							quit = true;
						}
					}
					
					if(request.equalsIgnoreCase("WRITE")) {
						if(ackPacketNumber==packetNo) {
							ackPacketNumber++;
						} else if (packetNo>ackPacketNumber||packetNo<=(ackPacketNumber-3)) { //Double check for Illegal packet number
							TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
							int p; // Port we are sending to
							// Sim's sendSocket is 23, Server's is the Thread's
							if (runMode.equals("test")) p = sendPort;
							else p = receivePacket.getPort();
							DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
									receivePacket.getAddress(), p);
							sendErrorMessage(unknownIDPacket);
							request = "ERROR";
							break;
						} else {
							System.out.println("Duplicate ack ignored.");
							System.out.println("");
							quit=false;
						}
					}

				}
			}
		}
		
		if(request.equals("ERROR")) {
			System.out.println("Error during file transfer");
		} else {
			System.out.println("File Transfer Complete");
		}
		System.out.println();

		//Closing Files
		try {
			fileHandler.closeOutFile();
		} catch (TFTPException e) {
			System.out.println("Error closing file " + path + filename + "\n" );
		} 
		try {
			fileHandler.closeInFile();
		} catch (TFTPException e) {
			System.out.println("Error closing file " + path + filename + "\n" );
		}

	}
	
	private void receivePacketFromServer(String request, String outputMode, String runMode, int sendPort) {
		receive_success=false; //Start loop not having received anything
		resend_count = 0;
		error_number = 0;
		err_msg_sent = false;
		while(!receive_success&&resend_count<MAX_RESEND) {
			if(request.equalsIgnoreCase("ERROR")&&resend_count>=1) break;
			Thread receiveConnection = new TFTPReceive(sendReceiveSocket, this);
				receiveConnection.start();
			try{
				receiveConnection.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(error_number==5&&!err_msg_sent) { //send a message to unknown TID and continue normal flow but only send the error message once
				err_msg_sent = true;
				System.out.println("Client: Unknown TID received:");
				if (outputMode.equals("verbose")){
					TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
				}
				TFTPException e = new TFTPException(5,"Error Code #5: Unknown transfer ID");
				// In the case of an invalid TID we do not want to force it back to the proper Sim port
				int p = receivePacket.getPort();
				DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), p);
				sendErrorMessage(unknownIDPacket);
			} else if (error_number==4) { //send an error message and terminate flow
				TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
				int p; // Port we are sending to
				// Sim's sendSocket is 23, Server's is the Thread's
				if (runMode.equals("test")) p = sendPort;
				else p = receivePacket.getPort();
				DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
						receivePacket.getAddress(), p);
				sendErrorMessage(unknownIDPacket);
				break;
			}
			if(error_number==0||error_number==5) {
				if(!receive_success) {
					//we did not receive a packet before timing out, re-send our packet
					//Sending request packet
					if(sendPacket.getData()[1]!=1&&sendPacket.getData()[1]!=2) { //Do not re-send an initial request for read or write
						try {
							if(outputMode.equals("verbose"))
								TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
							sendReceiveSocket.send(sendPacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}
						if (outputMode.equals("verbose")) {
							System.out.println("Client: re-sendind packet using port " + sendReceiveSocket.getLocalPort());
							System.out.println();
						}
					} else {
						if(outputMode.equals("verbose")) {
							System.out.println("Client: Did not receive packet. Receive Timed Out.");
							System.out.println();
						}
					}
				}
				resend_count++;
			}
		}
	}
	
	private void sendErrorMessage(DatagramPacket errorPacket) {
		System.out.println("Client: Sending packet:");
		if (controller.getOutputMode().equals("verbose")){
			TFTPReadWrite.printPacket(errorPacket, errorPacket.getPort(), "send");
		}
		byte unsignedByteTens = (byte) (errorPacket.getData()[2]);
		byte unsignedByteOnes = (byte) (errorPacket.getData()[3]);
		int packetNo = (int) (unsignedByteOnes & 0xff) + 256*(int)(unsignedByteTens & 0xff);
		System.out.println("Packet No.: " + packetNo);
		// Send the datagram packet to the client via the send socket.
		try {
			sendReceiveSocket.send(errorPacket);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (controller.getOutputMode().equals("verbose")){
			System.out.println("Client: packet sent using port " + sendReceiveSocket.getLocalPort());
			System.out.println();
		}		
	}

	public static void main(String args[]){
		TFTPClient client = new TFTPClient();
		client.controller.start();
	}
}
