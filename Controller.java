import java.util.Scanner;

public class Controller extends Thread{
	private String outputMode = "verbose";//"quiet";
	private String runMode = "test";//"normal";
	private String transferMode = "OCTET";
	public boolean quit = false;
	private String user;
	private String path = "";
	private String testSituation = "2";
	private String affectedOpcode = "1";
	private String packetNumber = "57";
	private String delayTime = "2000";

	public static TFTPClient client;

	//Initialized by class passed in, used to determine
	//what options user has access to
	public Controller(TFTPClient client){
		this.user = "Client";
	}
	
	public Controller(TFTPSim sim){
		this.user = "Sim";
	}

	public Controller(TFTPServer server){
		this.user = "Server";
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
		}
		if(user.equals("Sim")){
			System.out.println("Test Situation:\t" + testSituation);
			System.out.println("Affected Packet Opcode:\t" + affectedOpcode);
			System.out.println("Packet Number:\t" + packetNumber);
			System.out.println("Delay Time:\t" + delayTime);
		}
		System.out.println("Path:\t" + path);
		System.out.print("\nTo set output mode type 'verbose' or 'quiet'");
		System.out.print("\nTo change path type 'path'");
		if(user.equals("Client")){
			System.out.print("\nTo set run mode type 'normal' or 'test'");
			System.out.print("\nTo set send a request type 'read' or 'write'");
		}
		if(user.equals("Sim")){
			System.out.print("\nTo set test situation type 'situation'");
			System.out.print("\nTo set affect packet and packet number type 'packet' or 'number'");
			System.out.print("\nTo set delay time type 'delay'");
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
        	path = scanner.nextLine();
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
				c.sendAndReceive("READ", filename, transferMode);
			}
			if(command.equals("write")){
				System.out.print("\nEnter a file name:");
				String filename = scanner.nextLine();
				System.out.println();
            	TFTPClient c = new TFTPClient();
            	c.sendAndReceive("WRITE", filename, transferMode);
			}
		}
		
		if(user.equals("Sim")){
			if(command.equals("situation")){
				System.out.print("\n0:normal 1:lose packet 2:delay packet 3:duplicate packet:");
				String situation = scanner.nextLine();
				System.out.println();
				if(situation.equals("0")||situation.equals("1")||situation.equals("2")||situation.equals("3")){
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
		}
		
        if(command.equals("quit")){
        	quit = true;
        	scanner.close();
        }
	}
	
	public String getPath() {
		return path;
	}
	public String getOutputMode() {
		return outputMode;
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
}
