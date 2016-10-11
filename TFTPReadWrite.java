import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TFTPReadWrite {
	private String filename;
	private FileInputStream inStream;
	private FileOutputStream outStream;
	private File file;
	private int numSections;
	private int fileLength;
	
	public TFTPReadWrite(String filename, String mode, String path, String userType) throws TFTPException {
		this.filename = filename;
		if (path != "") {
			String[] pathArr = filename.split("\\\\|/");
			filename = pathArr[pathArr.length - 1];
		}
		file = new File(path + filename);
		if (mode.equals("WRITE")) {
			try {
				if(file.exists()){
					throw new TFTPException(6,"Error Code #6: File Already Exists");
				}
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outStream = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				throw new TFTPException(2,"Error Code #2: Access Violation");
			}
		} else {
			try {
				inStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				if(userType.equals("Client")){
					throw new TFTPException(1,"Error Code #1: File not found");
				}
				if(userType.equals("Client Connection")){ 
					throw new TFTPException(2,"Error Code #2: Access Violation");
				}
			}
		}
		//Variables for reading
		fileLength = (int)file.length();
		numSections = (int)(file.length()/512)+1;
	}
	
	public byte[] readFileBytes(int length) throws TFTPException {
		byte[] fileBytes = new byte[512];
		try  {
			inStream.read(fileBytes, 0, length);
		} catch (IOException e) {
			throw new TFTPException(2,"Error Code #2: Access Violation");
		}
		
		return fileBytes;
	}
	
	public void writeFilesBytes(byte[] fileBytes) throws TFTPException {
		try {
			outStream.write(fileBytes, 0, fileBytes.length);
		} catch (IOException e){
			throw new TFTPException(3,"Error Code #3: Disk Full");
		}
	}
	
	public File getFile() { return file; }
	
	public int getNumSections() { return numSections; }
	
	public int getFileLength() { return fileLength; }
	
}
