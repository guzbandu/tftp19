
public class ServerStartTest {
	public static void main( String args[] ) {
		TFTPServer server = new TFTPServer();
		server.controller.setPath("./Server/");
		server.receiveAndSend();
	}
}
