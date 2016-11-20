import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

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
		client.sendAndReceive("READ", "./Server/testReadOne.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File outFile = new File("./Client/testReadOne.txt");
		File inFile = new File("./Server/testReadOne.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}

	@Test
	public void TestReadNoSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "./Server/testReadLess.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File outFile = new File("./Client/testReadLess.txt");
		File inFile = new File("./Server/testReadLess.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}

	@Test
	public void TestReadNoSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "./Server/testReadExact.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File outFile = new File("./Client/testReadExact.txt");
		File inFile = new File("./Server/testReadExact.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadNoSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "./Server/testReadMore.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File outFile = new File("./Client/testReadMore.txt");
		File inFile = new File("./Server/testReadMore.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadNoSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("READ", "./Server/testReadMulti.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File outFile = new File("./Client/testReadMulti.txt");
		File inFile = new File("./Server/testReadMulti.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteNoSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "./Client/testWriteOne.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File inFile = new File("./Server/testWriteOne.txt");
		File outFile = new File("./Client/testWriteOne.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteNoSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "./Client/testWriteLess.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File inFile = new File("./Server/testWriteLess.txt");
		File outFile = new File("./Client/testWriteLess.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteNoSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "./Client/testWriteExact.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File inFile = new File("./Server/testWriteExact.txt");
		File outFile = new File("./Client/testWriteExact.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteNoSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "./Client/testWriteMore.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File inFile = new File("./Server/testWriteMore.txt");
		File outFile = new File("./Client/testWriteMore.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteNoSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.sendAndReceive("WRITE", "./Client/testWriteMulti.txt", client.controller.getTransferMode(), client.controller.getPath(), client.controller.getOutputMode(), client.controller.getRunMode());
		File inFile = new File("./Server/testWriteMulti.txt");
		File outFile = new File("./Client/testWriteMulti.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	/*
	@Test
	public void TestReadSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadOne.txt", client.controller.getTransferMode());
		File outFile = new File("./Client/testReadOne.txt");
		File inFile = new File("./Server/testReadOne.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadLess.txt", client.controller.getTransferMode());
		File outFile = new File("./Client/testReadLess.txt");
		File inFile = new File("./Server/testReadLess.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadExact.txt", client.controller.getTransferMode());
		File outFile = new File("./Client/testReadExact.txt");
		File inFile = new File("./Server/testReadExact.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadMore.txt", client.controller.getTransferMode());
		File outFile = new File("./Client/testReadMore.txt");
		File inFile = new File("./Server/testReadMore.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestReadSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("READ", "./Server/testReadMulti.txt", client.controller.getTransferMode());
		File outFile = new File("./Client/testReadMulti.txt");
		File inFile = new File("./Server/testReadMulti.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteSimOnePacket() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteOne.txt", client.controller.getTransferMode());
		File inFile = new File("./Server/testWriteOne.txt");
		File outFile = new File("./Client/testWriteOne.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteSimLess() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteLess.txt", client.controller.getTransferMode());
		File inFile = new File("./Server/testWriteLess.txt");
		File outFile = new File("./Client/testWriteLess.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteSimExact() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteExact.txt", client.controller.getTransferMode());
		File inFile = new File("./Server/testWriteExact.txt");
		File outFile = new File("./Client/testWriteExact.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteSimMore() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteMore.txt", client.controller.getTransferMode());
		File inFile = new File("./Server/testWriteMore.txt");
		File outFile = new File("./Client/testWriteMore.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	
	@Test
	public void TestWriteSimMultiple() {
		TFTPClient client = new TFTPClient();
		client.controller.setPath("./Client/");
		client.controller.setRunMode("test");
		client.sendAndReceive("WRITE", "./Client/testWriteMulti.txt", client.controller.getTransferMode());
		File inFile = new File("./Server/testWriteMulti.txt");
		File outFile = new File("./Client/testWriteMulti.txt");
		byte[] outFileB = new byte[0];
		byte[] inFileB = new byte[0];
		try {
			outFileB = Files.readAllBytes(outFile.toPath());
			inFileB = Files.readAllBytes(inFile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(outFile.exists() && Arrays.equals(inFileB, outFileB));
	}
	*/
}
