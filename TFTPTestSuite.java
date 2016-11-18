import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TFTPTestSuite {

	@BeforeClass
	public static void setUp() {
		File outFile1 = new File("./Server/testWriteOne.txt");
		if (outFile1.exists()) {
			outFile1.delete();
		}
		File outFile2 = new File("./Server/testWriteLess.txt");
		if (outFile2.exists()) {
			outFile2.delete();
		}
		File outFile3 = new File("./Server/testWriteExact.txt");
		if (outFile3.exists()) {
			outFile3.delete();
		}
		File outFile4 = new File("./Server/testWriteMore.txt");
		if (outFile4.exists()) {
			outFile4.delete();
		}
		File outFile5 = new File("./Server/testWriteMulti.txt");
		if (outFile5.exists()) {
			outFile5.delete();
		}
		File inFile1 = new File("./Client/testReadOne.txt");
		if (inFile1.exists()) {
			inFile1.delete();
		}
		File inFile2 = new File("./Client/testReadLess.txt");
		if (inFile2.exists()) {
			inFile2.delete();
		}
		File inFile3 = new File("./Client/testReadExact.txt");
		if (inFile3.exists()) {
			inFile3.delete();
		}
		File inFile4 = new File("./Client/testReadMore.txt");
		if (inFile4.exists()) {
			inFile4.delete();
		}
		File inFile5 = new File("./Client/testReadMulti.txt");
		if (inFile5.exists()) {
			inFile5.delete();
		}
	}

	@Test
	public void TestReadNoSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "testReadOne.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}

	@Test
	public void TestReadNoSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "testReadLess.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}

	@Test
	public void TestReadNoSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "testReadExact.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestReadNoSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "testReadMore.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestReadNoSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "testReadMulti.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestWriteNoSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "testWriteOne.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestWriteNoSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "testWriteLess.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestWriteNoSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "testWriteExact.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestWriteNoSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "testWriteMore.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	
	@Test
	public void TestWriteNoSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "testWriteMulti.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
	}
	/*
	@Test
	public void TestReadSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadOne.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestReadSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadLess.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestReadSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadExact.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestReadSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadMore.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestReadSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadMulti.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestWriteSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteOne.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestWriteSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteLess.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestWriteSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteExact.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestWriteSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteMore.txt", client.controller.getTransferMode());
	}
	
	@Test
	public void TestWriteSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteMulti.txt", client.controller.getTransferMode());
	}
	*/
}
