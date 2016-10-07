

import java.io.*;
import java.io.File;

public class ReadWrite {	
	public static byte[][] readFileBytes(String filename) {
		byte[][] fileBytes;
		File file = new File(filename);
		int numSections = 0;
		numSections = (int)(file.length()/512)+1;
		fileBytes = new byte[numSections][512];
		try (FileInputStream fs = new FileInputStream(file)) {
			for (int i = 0; i < numSections-1; i++) {
				fs.read(fileBytes[i], 0, 512);
			}
			fs.read(fileBytes[numSections-1], 0, (int)(file.length()-((numSections-1)*512)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fileBytes;
	}
	public static File writeFilesBytes(String filename, byte[][] fileBytes) {
		File file = new File(filename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (FileOutputStream fs = new FileOutputStream(file)){
			for(int i = 0; i < fileBytes.length; i++) {
				System.out.println(i);
				fs.write(fileBytes[i], 0, fileBytes[i].length);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return file;
	}
}
