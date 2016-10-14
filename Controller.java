import java.util.Scanner;

public class Controller extends Thread{
	private String outputMode = "quiet";
	private String runMode = "normal";
	private String transferMode = "OCTET";
	public boolean quit = false;
	private String user;
	private String path = "";

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
		System.out.println("Path:\t" + path);
		System.out.print("\nTo set output mode type 'verbose' or 'quiet'");
		if(user.equals("Client")){
			System.out.print("\nTo set run mode type 'normal' or 'test'");
			System.out.print("\nTo set send a request type 'read' or 'write'");
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
}
