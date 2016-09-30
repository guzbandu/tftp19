import java.io.File;
import java.io.FileInputStream;
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
	
	public TFTPReadWrite(String filenameIn, String mode) {
		filename = filenameIn;
		inFile = new File(filename);
		outFilename = "new." + filename;
		outFile = new File(outFilename);
		System.out.println("File length: "+inFile.length()+"\n");
		try {
			inStream = new FileInputStream(inFile);
			outStream = new FileOutputStream(outFile);
		} catch (IOException e) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public File getInFile() { return inFile; }
	public File getOutFile() { return outFile; }
	
	public int getNumSections() { return numSections; }
	
	public int getFileLength() { return fileLength; }
}
