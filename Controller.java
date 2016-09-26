import java.io.IOException;
import java.util.Scanner;

public class Controller extends Thread{
	private String outputMode = "quiet";
	private String runMode = "normal";
	private String transferMode = "OCTET";
	public static boolean quit = false;
	private String user;

	public static TFTPClient client;

	
	public Controller(TFTPClient client){
		this.user = "Client";
		this.client = client;
	}
	
	public Controller(TFTPSim sim){
		this.user = "Sim";
	}

	public Controller(TFTPServer server){
		this.user = "Server";
	}
	
	public synchronized void run(){
		while(!quit){
			getInput();
		}
		/*
		 * Put way to shutdown the Server and Client here.
		 */
	}
	
	public void getInput(){
		Scanner scanner = new Scanner(System.in);
		System.out.println("Output Mode:\t" + outputMode);
		if(user.equals("Client")){
			System.out.println("Run Mode:\t" + runMode);
		}
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
				client.sendAndReceive("READ", filename, transferMode);
			}
			if(command.equals("write")){
				System.out.print("\nEnter a file name:");
				String filename = scanner.nextLine();
				System.out.println();
            	TFTPClient c = new TFTPClient();
            	client.sendAndReceive("WRITE", filename, transferMode);
			}
		}
        if(command.equals("quit")){
        	quit = true;
        }
	}
	
	public String getOutputMode() {
		return outputMode;
	}
	public String getRunMode() {
		return runMode;
	}
	public static void main( String args[] ) throws Exception{
	}
}
