import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Controller extends Thread{
	private String outputMode = "verbose";//"verbose";
	private String runMode = "test";//"test";
	private String transferMode = "OCTET";
	public boolean quit = false;
	private String user;
	private String path = "";
	private String testSituation = "1";
	private String affectedOpcode = "1";
	private String packetNumber = "1";
	private String delayTime = "4000";
	private String illegalOperation = "1";
	//For illegal TFTP operation
	private String byteChange = "5";
	private InetAddress serverIP;

	public static TFTPClient client;

	//Initialized by class passed in, used to determine
	//what options user has access to
	public Controller(TFTPClient client){
		this.user = "Client";
		try {
			serverIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public Controller(TFTPSim sim){
		this.user = "Sim";
		try {
			serverIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public Controller(TFTPServer server){
		this.user = "Server";
		try {
			serverIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	//Main loop
	public synchronized void run(){
		while(!quit){
			getInput();
		}
	}
	
	/*
	 * Prompts and code for user input, certain options
	 * can only be selected by a client user
	 */
	public void getInput(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Output Mode:\t" + outputMode);
		if(user.equals("Client")){
			System.out.println("Run Mode:\t" + runMode);
			System.out.println("Server IP:\t" + serverIP.toString());
		}
		if(user.equals("Sim")){
			System.out.println("Test Situation:\t" + testSituation);
			System.out.println("Affected Packet Opcode:\t" + affectedOpcode);
			System.out.println("Packet Number:\t" + packetNumber);
			System.out.println("Delay Time:\t" + delayTime);
			System.out.println("Illegal TFTP Operation:\t" + illegalOperation);
			System.out.println("Byte Increase:\t" + byteChange);
			System.out.println("Server IP:\t" + serverIP.toString());
		}
		System.out.println("Path:\t" + path);
		System.out.print("\nTo set output mode type 'verbose' or 'quiet'");
		System.out.print("\nTo change path type 'path'");
		if(user.equals("Client")){
			System.out.print("\nTo set run mode type 'normal' or 'test'");
			System.out.print("\nTo set send a request type 'read' or 'write'");
			System.out.print("\nTo set the server's IP type 'serverip'");
		}
		if(user.equals("Server")){
			System.out.print("\nTo get the server's host name type 'getip'");
		}
		if(user.equals("Sim")){
			System.out.print("\nTo set test situation type 'situation'");
			System.out.print("\nTo set affect packet and packet number type 'packet' or 'number'");
			System.out.print("\n   A read starts at number 1");
			System.out.print("\n   A write starts at number 0");
			System.out.print("\nTo set delay time type 'delay'");
			System.out.print("\nTo set illegal TFTP operation type 'operation'");
			System.out.print("\nTo set byte increase for illegal TFTP operation type 'byte'");
			System.out.print("\nTo set the server's IP type 'serverip'");
		}
		System.out.print("\nTo set quit type 'quit'");
		System.out.println();
		
		System.out.print("\nEnter a command:");
        String command = scanner.nextLine();
        
        if(command.equals("verbose")){
        	outputMode = "verbose";
        }
        if(command.equals("quiet")){
        	outputMode = "quiet";
        }
        if(command.equals("path")) {
        	System.out.println("\nEnter a file path (Replace \\ with \\\\ or /):");
        	this.path = scanner.nextLine();
        	System.out.println();
        }
        //Client input
		if(user.equals("Client")){
			if(command.equals("normal")){
				runMode = "normal";
			}
			if(command.equals("test")){
				runMode = "test";
			}
			if(command.equals("read")){
				System.out.print("\nEnter a file name:");
				String filename = scanner.nextLine();
				System.out.println();
				TFTPClient c = new TFTPClient();
				c.sendAndReceive("READ", filename, transferMode, path, outputMode, runMode, serverIP);
			}
			if(command.equals("write")){
				System.out.print("\nEnter a file name:");
				String filename = scanner.nextLine();
				System.out.println();
            	TFTPClient c = new TFTPClient();
            	c.sendAndReceive("WRITE", filename, transferMode, path, outputMode, runMode, serverIP);
			}
			if(command.equals("serverip")){
				System.out.print("\nEnter a host name:");
				String hostname = scanner.nextLine();
				System.out.println();
				try {
					serverIP = InetAddress.getByName(hostname);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(user.equals("Server")){
			if(command.equals("getip")){
				try {
					System.out.print("\nHost name: " + InetAddress.getLocalHost().getHostName());
					System.out.println();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(user.equals("Sim")){
			if(command.equals("situation")){
				System.out.print("\n0:normal 1:lose packet 2:delay packet 3:duplicate packet 4:illegal TFTP operation 5:unknown TID:");
				String situation = scanner.nextLine();
				System.out.println();
				if(situation.equals("0")||situation.equals("1")||situation.equals("2")||situation.equals("3")||
						situation.equals("4")||situation.equals("5")){
					testSituation = situation;
				}
			}
			if(command.equals("packet")){
				System.out.print("\nEnter a packet opcode (RRQ=1, WRQ=2, DATA=3, ACK=4, ERROR=5:");
				String packet = scanner.nextLine();
				System.out.println();
				if(packet.equals("1")||packet.equals("2")||packet.equals("3")||packet.equals("4")||packet.equals("5")){
					affectedOpcode = packet;
				}
			}
			if(command.equals("number")){
				System.out.print("\nEnter a packet number:");
				String number = scanner.nextLine();
				System.out.println();
				packetNumber = number;
			}
			if(command.equals("delay")){
				System.out.print("\nEnter a delay time:");
				String time = scanner.nextLine();
				System.out.println();
				delayTime = time;
			}
			if(command.equals("operation")){
				System.out.print("\nEnter an illegal operation:"
						+ "\n  0:normal operation \n  1:invalid TFTP opcode \n  2:invalid mode "
						+ "\n  3:invalid filename \n  4:invalid block number \n  5:no null separation between"
						+ " filename and mode \n  6:extra null separation between filename and mode"
						+ "\n  7:no null termination \n  8:extra null termination\n");
				String operation = scanner.nextLine();
				System.out.println();
				if(operation.equals("0")||operation.equals("1")||operation.equals("2")||operation.equals("3")||operation.equals("4")||
						operation.equals("5")||operation.equals("6")||operation.equals("7")||operation.equals("8")){
					illegalOperation = operation;
				}
			}
			if(command.equals("byte")){
				System.out.print("\nEnter amount:");
				String time = scanner.nextLine();
				System.out.println();
				byteChange = time;
			}
			if(command.equals("serverip")){
				System.out.print("\nEnter a host name:");
				String hostname = scanner.nextLine();
				System.out.println();
				try {
					serverIP = InetAddress.getByName(hostname);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		
        if(command.equals("quit")){
        	quit = true;
        	scanner.close();
        }
	}
	
	public String getPath() {
		return this.path;
	}
	public String getOutputMode() {
		return this.outputMode;
	}
	
	public String getRunMode() {
		return runMode;
	}
	public int getTestSituation() {
		return Integer.parseInt(testSituation);
	}
	public int getAffectedOpcode() {
		return Integer.parseInt(affectedOpcode);
	}
	public int getPacketNumber() {
		return Integer.parseInt(packetNumber);
	}
	public int getDelayTime() {
		return Integer.parseInt(delayTime);
	}
	public int getIllegalOperation() {
		return Integer.parseInt(illegalOperation);
	}
	public int getByteChange() {
		return Integer.parseInt(byteChange);
	}
	public InetAddress getServerIP() {
		return serverIP;
	}
	
	public void setPath(String pth) {
		path = pth;
	}
		 
	public void setRunMode(String rn) {
		runMode = rn;
	}
	
	public String getTransferMode() {
		return transferMode;
	}
	
	public void setOutputMode(String mode) {
		outputMode = mode;
	}
}
