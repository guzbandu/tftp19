
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
    
    public byte[] setErrorBytes(int errnum){
		byte[] error = new byte[516]; // message we send
		byte[] errmsg = new byte[512];
		int len;
	         
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
	       if(errnum == 6){
	    	   error[3]= 6;
	    	   errmsg = "Error Code #6: File Already Exists".getBytes();
	       }

	       // and copy into the error
	       System.arraycopy(errmsg,0,error,4,errmsg.length);

	       return error;
	}
}
