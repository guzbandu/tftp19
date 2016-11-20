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
	private final int MAX_RESEND = 10; //The total number of times we will resend before giving up TODO drop this once finished testing
	private boolean receive_success = false; //Used to track if our threads receive was successful
	protected int error_number = 0;
	protected int hostPort = -1;
	protected boolean hasHostPort;

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

	public void sendAndReceive(String request, String filename, String mode, String path, String outputMode, String runMode)
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

		//If user enters "normal" as the mode
		//user sends directly to port 69 on the server
		//otherwise it sends to the error simulator
		if (runMode.equals("normal")) 
			sendPort = 69;
		else
			sendPort = 23;

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
		try {
			sendPacket = new DatagramPacket(msg, len,
					InetAddress.getLocalHost(), sendPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//Output for sending packet
		if (outputMode.equals("verbose")){
			TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
		}

		//Sending initial request packet
		try {
			sendReceiveSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (outputMode.equals("verbose"))
			System.out.println("Client: Packet sent.\n");

		int i = 1;
		//Main loop
		while (!quit) {

			data = new byte[516];
			receivePacket = new DatagramPacket(data, data.length);

			if (outputMode.equals("verbose"))
				System.out.println("Client: Waiting for packet.");

			//Receiving packet
			receive_success=false; //Start loop not having received anything
			resend_count = 0;
			error_number = 0;
			while(!receive_success&&resend_count<MAX_RESEND) {
				if(request.equalsIgnoreCase("ERROR")&&resend_count>=1) break;
				Thread receiveConnection = new TFTPReceive(sendReceiveSocket, this);
					receiveConnection.start();
				try{
					receiveConnection.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(error_number==5) { //send a message to unknown TID and continue normal flow
					TFTPException e = new TFTPException(5,"Error Code #5: Unknown transfer ID");
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), receivePacket.getPort());
					System.out.println("Server: Sending packet:");
					if (outputMode.equals("verbose")){
						TFTPReadWrite.printPacket(unknownIDPacket, unknownIDPacket.getPort(), "send");
					}	
					try {
						sendReceiveSocket.send(unknownIDPacket);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (error_number==4) { //send an error message and terminate flow
					TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
					DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
							receivePacket.getAddress(), receivePacket.getPort());
					System.out.println("Server: Sending packet:");
					if (outputMode.equals("verbose")){
						TFTPReadWrite.printPacket(unknownIDPacket, unknownIDPacket.getPort(), "send");
					}
					try {
						sendReceiveSocket.send(unknownIDPacket);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
				if(error_number==0||error_number==5) {
					if(!receive_success) {
						//we did not receive a packet before timing out, re-send our packet
						//Sending request packet
						if(sendPacket.getData()[1]!=1) {
							try {
								sendReceiveSocket.send(sendPacket);
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(1);
							}
							if (outputMode.equals("verbose"))
								System.out.println("Client: Re-sending packet.\n");
						}
					}
					resend_count++;
				}
			}

			if(!receive_success) break;

			// Process the received datagram.
			len = receivePacket.getLength();
			System.out.println("Client: Packet received:");
			if (outputMode.equals("verbose")){
				TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
			}
			byte unsignedByte = (byte) ((data[2] << 8) + data[3]);
			int packetNo = (int) (unsignedByte & 0xff);
			System.out.println("Packet No.: " + packetNo + "\n");
			if(packetNo==0) { //The counter has rolled over
				packetNumber = 0;
				ackPacketNumber = 0;
			}

			//Checking for error packets
			if(5 == (int)((data[0] << 8) + data[1])) {
				request = "ERROR";
				System.out.print("Error from the server ");
				String message = new String(data);
				message = message.substring(4,message.length());
				System.out.println(message+"\n");
				quit = true;
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
					System.out.println("Data length: " + len);
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
				if(ackPacketNumber==packetNo ){
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
				if(request.equalsIgnoreCase("READ")||(request.equalsIgnoreCase("WRITE")&&ackPacketNumber==packetNo)||send) {
					int p; // Port we are sending to
					// Sim's sendSocket is 23, Server's is the Thread's
					if (runMode.equals("test")) p = sendPort;
					else p = receivePacket.getPort();

					//Sending packet
					try {
						sendPacket = new DatagramPacket(msg, len,
								InetAddress.getLocalHost(), p);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						System.exit(1);
					}
					//Output for sending packet
					if (outputMode.equals("verbose")){
						TFTPReadWrite.printPacket(sendPacket, sendPacket.getPort(), "send");
						System.out.println("Byte Packet No.: " + msg[2] + " " + msg[3]);
						// Form a String from the byte array, and print the string.
						String sending = new String(msg,0,len);
						System.out.println(sending);
					}
					unsignedByte = (byte) ((data[2] << 8) + data[3]);
					packetNo = (int) (unsignedByte & 0xff);
					System.out.println("Packet No.: " + packetNo);

					// Send the datagram packet to the server via the send/receive socket.

					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
					if (outputMode.equals("verbose"))
						System.out.println("Client: Packet sent.\n");

					// Construct a DatagramPacket for receiving packets up
					// to 100 bytes long (the length of the byte array).

					i++;

					ackPacketNumber++;
				} else if (request.equalsIgnoreCase("WRITE")&&ackPacketNumber!=packetNo&&!quit) {
					System.out.println("Ignoring duplicate ack.");
					System.out.println("");
					if(i >= fileHandler.getNumSections() )
						quit = true;
				}

				System.out.println();

				/* Sent final packet can break now */
				if(last_packet) break;

				/* Wait for final acknowledgement */
				if(quit&&request.equalsIgnoreCase("WRITE")&&ackPacketNumber==packetNo) {
					if (outputMode.equals("verbose"))
						System.out.println("Client: Waiting for packet.");

					//Receiving packet
					receive_success=false; //Start loop not having received anything
					resend_count = 0;
					error_number = 0;
					while(!receive_success&&resend_count<MAX_RESEND) {
						if(request.equalsIgnoreCase("ERROR")&&resend_count>=1) break;
						Thread receiveConnection = new TFTPReceive(sendReceiveSocket, this);
						receiveConnection.start();
						try{
							receiveConnection.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(error_number==5) { //send a message to unknown TID and continue normal flow
							TFTPException e = new TFTPException(5,"Error Code #5: Unknown transfer ID");
							DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
									receivePacket.getAddress(), receivePacket.getPort());
							System.out.println("Server: Sending packet:");
							if (outputMode.equals("verbose")){
								TFTPReadWrite.printPacket(unknownIDPacket, unknownIDPacket.getPort(), "send");
							}	
							try {
								sendReceiveSocket.send(unknownIDPacket);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} else if (error_number==4) { //send an error message and terminate flow
							TFTPException e = new TFTPException(4,"Error Code #4: Illegal TFTP operation");
							DatagramPacket unknownIDPacket = new DatagramPacket(e.getErrorBytes(), e.getErrorBytes().length,
									receivePacket.getAddress(), receivePacket.getPort());
							System.out.println("Server: Sending packet:");
							if (outputMode.equals("verbose")){
								TFTPReadWrite.printPacket(unknownIDPacket, unknownIDPacket.getPort(), "send");
							}
							try {
								sendReceiveSocket.send(unknownIDPacket);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							break;
						}
						if(error_number==0||error_number==5) {
							if(!receive_success) {
								//we did not receive a packet before timing out, re-send our packet
								//Sending request packet
								try {
									sendReceiveSocket.send(sendPacket);
								} catch (IOException e) {
									e.printStackTrace();
									System.exit(1);
								}
								if (outputMode.equals("verbose"))
									System.out.println("Client: Re-sending packet.\n");
							}
							resend_count++;
						}
					}

					if(!receive_success) break;


					// Process the received datagram.
					len = receivePacket.getLength();
					System.out.println("Client: Packet received:");
					if (outputMode.equals("verbose")){
						TFTPReadWrite.printPacket(receivePacket, receivePacket.getPort(), "receive");
					}
					//Make the byte count in the range from 0 to 255 instead of -128 to 127
					unsignedByte = (byte) ((data[2] << 8) + data[3]);
					packetNo = (int) (unsignedByte & 0xff);
					System.out.println("Packet No.: " + packetNo + "\n");
					if(packetNo==0) { //The counter has rolled over
						packetNumber = 0;
						ackPacketNumber = 0;
					}

					if(request.equalsIgnoreCase("WRITE")) {
						if(ackPacketNumber==packetNo) {
							ackPacketNumber++;
						} else {
							System.out.println("Duplicate ack ignored.");
							System.out.println("");
							quit=false;
						}
					}

				}
			}
		}

		System.out.println("File Transfer Complete");
		System.out.println();

		if(request.equalsIgnoreCase("READ")) {
			try {
				fileHandler.closeOutFile();
			} catch (TFTPException e) {
				System.out.println("Error closing file " + path + filename + "\n" );
			} 
		} else if (request.equalsIgnoreCase("WRITE")) {
			try {
				fileHandler.closeInFile();
			} catch (TFTPException e) {
				System.out.println("Error closing file " + path + filename + "\n" );
			}
		}

	}

	public static void main(String args[]){
		TFTPClient client = new TFTPClient();
		client.controller.start();
	}
}
