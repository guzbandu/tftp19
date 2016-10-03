//import static org.junit.Assert.*;
import java.util.Arrays;

//import org.junit.Test;

public class TFTPReadWriteTest {

//	@Test
	public void testReaderByteLength() {
		byte[][] bytes = ReadWrite.readFileBytes(".\\test.txt");
		ReadWrite.writeFilesBytes(".\\testt.txt", bytes);
	}

}