import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TFTPReadWrite {
	private String filename;
	private String outFilename;
	private FileInputStream inStream;
	private FileOutputStream outStream;
	private File inFile;
	private File outFile;
	private int numSections;
	private int fileLength;
	
	public TFTPReadWrite(String filenameIn, String mode, Object user, String userType) {
		//give client/clientconnection a error(byte[]) function.
		//when an error occurs do user.error(getErrorPacket(x))
		//In the error function it should send an error packet to the opposite of the user.
		//maybe also give client/clientconnection a boolean 'error',
		//that can be set to true so they know to stop waiting to receive more packets.
		filename = filenameIn;
		inFile = new File(filename);
		outFilename = "new." + filename;
		outFile = new File(outFilename);
		System.out.println("File length: "+inFile.length()+"\n");
		try {
			inStream = new FileInputStream(inFile);

		} catch (FileNotFoundException e) {
			if(userType.equals("Client")){
				System.out.println("ERROR Packet #1");
				System.out.println("Error from client");
				//Somehow get error packet to client
				//should be 0501|"Error Code #1: File not found"|0
				byte[] error = getErrorPacket(1);
			}
			if(userType.equals("Client Connection")){ 
				System.out.println("ERROR Packet #2");
				System.out.println("Error from client connection");
				//Somehow get error packet to client connection
				//should be 0502|"Error Code #2: Access Violation"|0;
				byte[] error = getErrorPacket(1);
			}
		}
		try{
			outStream = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			System.out.println("ERROR Packet #2");
			System.out.println("Error from client");
			//Somehow get error packet to client
			//should be 0502|"Error Code #2: Access Violation"|0;
			byte[] error = getErrorPacket(2);
			e.printStackTrace();
		}

		//Variables for reading
		fileLength = (int)inFile.length();
		numSections = (int)(inFile.length()/512)+1;
	}
	
	public byte[] readFileBytes(int length) {
		byte[] fileBytes = new byte[512];
		try  {
			inStream.read(fileBytes, 0, length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileBytes;
	}
	public void writeFilesBytes(byte[] fileBytes) {
		try {
			outStream.write(fileBytes, 0, fileBytes.length);
		} catch (IOException e){
			System.out.println("ERROR Packet #3");
			System.out.println("Error from client connection");
			//Somehow get error packet to clientconnection and send to client
			//should be 0503|"Error Code #3: Disk Full"|0;
			byte[] error = getErrorPacket(3);
			e.printStackTrace();
		}
	}
	
	public File getInFile() { return inFile; }
	public File getOutFile() { return outFile; }
	
	public int getNumSections() { return numSections; }
	
	public int getFileLength() { return fileLength; }
	
	public byte[] getErrorPacket(int errnum){
		byte[] error = new byte[516]; // message we send
		byte[] errmsg = new byte[516];
		int len;
	         
	       error[0] = 0;
	       error[1]= 5;
	       error[2]= 0;
	       if(errnum == 1){
	    	   error[3]= 1;
		       errmsg = "Error Code #1: File Not Found".getBytes(); 
	       }
	       if(errnum == 2){
	    	   error[3]= 2;
	    	   errmsg = "Error Code #2: Access Violation".getBytes();
	       }
	       if(errnum == 3){
	    	   error[3]= 3;
	    	   errmsg = "Error Code #3: Disk Full".getBytes();
	       }
	       if(errnum == 6){
	    	   error[3]= 6;
	    	   errmsg = "Error Code #6: File Already Exists".getBytes();
	       }

	       // and copy into the error
	       System.arraycopy(errmsg,0,error,4,errmsg.length);

	       len = errmsg.length+4; // length of the message
	       // length of error message + opcode (2) + error# (2)

	       // end with another 0 byte 
	       error[len-1] = 0;
	       return error;
	}
}