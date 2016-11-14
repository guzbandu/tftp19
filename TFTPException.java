public class TFTPException extends RuntimeException{
	private int errCode;
	private byte[] errBytes;

	public TFTPException(int errCode, String message) {
		super(message);
		this.errCode = errCode;
		errBytes = setErrorBytes(errCode);
	}

	public int getErrorCode(){
		return errCode;
	}

	public byte[] getErrorBytes(){
		return errBytes;
	}

	//Creates data to put in error packet
	public byte[] setErrorBytes(int errnum){
		byte[] error = new byte[516]; //Message to send
		byte[] errmsg = new byte[512];

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
		if(errnum == 4){
			error[3]= 4;
			errmsg = "Error Code #4: Illegal TFTP operation".getBytes();
		}
		if(errnum == 5){
			error[3]= 5;
			errmsg = "Error Code #5: Unknown transfer ID".getBytes();
		}
		if(errnum == 6){
			error[3]= 6;
			errmsg = "Error Code #6: File Already Exists".getBytes();
		}

		//Copy into the byte array to send in packet
		System.arraycopy(errmsg,0,error,4,errmsg.length);

		return error;
	}
}