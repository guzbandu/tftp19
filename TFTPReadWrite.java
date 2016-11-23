import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class TFTPReadWrite {
	private FileInputStream inStream;
	private FileOutputStream outStream;
	private File file;
	private int numSections;
	private int fileLength;
	
	/*
	 * Arguments:
	 * 	-String filename: file to be opened
	 * 	-String mode: specifies type of stream to be opened
	 * 	  on the file (read or write)
	 * 	-String path: path that file is found in
	 * 	-String userType: type of user using the object (client or server, not currently
	 *    being used, for debug)
	 */
	public TFTPReadWrite(String filename, String mode, String path, String userType) throws TFTPException {
		if (path != "") {
			String[] pathArr = filename.split("\\\\|/");
			filename = pathArr[pathArr.length - 1];
		}
		file = new File(path + filename);
		//Opening file and streams, throws exceptions on errors using error packet class
		if (mode.equals("WRITE")) {
			try {
				if(file.exists()){
					throw new TFTPException(6,"Error Code #6: File Already Exists");
				}
				file.createNewFile();
			} catch (IOException e) {
				throw new TFTPException(4,"Error Code #4: Illegal TFTP operation");
			}
			try {
				outStream = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				throw new TFTPException(2,"Error Code #2: Access Violation");
			}
		} else {
			try {
				if(!file.exists()){
					throw new TFTPException(1,"Error Code #1: File not found");
				}
				inStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new TFTPException(2,"Error Code #2: Access Violation");
			}
		}
		
		//Variables for reading
		fileLength = (int)file.length();
		numSections = (int)(file.length()/512)+1;
	}
	
	//Reads length bytes from file
	public byte[] readFileBytes(int length) throws TFTPException {
		byte[] fileBytes = new byte[512];
		try  {
			inStream.read(fileBytes, 0, length);
		} catch (IOException e) {
			throw new TFTPException(2,"Error Code #2: Access Violation");
		}
		
		return fileBytes;
	}
	
	//Writes bytes into file
	public void writeFilesBytes(byte[] fileBytes) throws TFTPException {
		try {
			outStream.write(fileBytes, 0, fileBytes.length);
		} catch (IOException e){
			throw new TFTPException(3,"Error Code #3: Disk Full");
		}
	}
	
	public void closeInFile() {
		if (inStream != null){
			try {
				inStream.close();
			} catch (IOException e) {
				throw new TFTPException(2,"Error Code #2: Access Violation");
			}
		}
	}

	public void closeOutFile() {
		if (outStream != null){
			try {
				outStream.close();
			} catch (IOException e) {
				throw new TFTPException(2,"Error Code #2: Access Violation");
			}
		}
	}

	
	public File getFile() { return file; }
	
	public int getNumSections() { return numSections; }
	
	public int getFileLength() { return fileLength; }

	/*
	 * Process the packet: print its contents
	 * Input: the packet itself, the port, and if it's send or receive
	 * Output: prints out packet address, port, length and bytes
	 */
	public static void printPacket (DatagramPacket packet, int port, String type) {
		if (type.equals("send")){
			System.out.println("To host: " + packet.getAddress());
			System.out.println("Destination host port: " + port);
		} else {
			System.out.println("From host: " + packet.getAddress());
			System.out.println("Host port: " + port);
		}
		int len = packet.getLength();
		System.out.println("Length: " + len);
		System.out.println("Containing: ");
		for (int j=0;j<len;j++) {
			System.out.print(packet.getData()[j] + " | ");
		}
		System.out.println();
	}
}