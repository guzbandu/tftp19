

//import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Scanner;

//import org.junit.Test;

public class TFTPReadWriteTest {

//	@Test
	public void testReaderByteLength() {
		Scanner sc = new Scanner(System.in);
		String path = sc.nextLine();
		String newPath = path.substring(path.lastIndexOf("\\|/"));
		System.out.println(newPath);
	}


}
