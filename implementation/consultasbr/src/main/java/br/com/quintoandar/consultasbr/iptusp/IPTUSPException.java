package br.com.quintoandar.consultasbr.iptusp;

public class IPTUSPException extends Exception {

	private byte[] file;
	private String fileType= "text/plain";
	
	public IPTUSPException(Throwable ex){
		super(ex);
	}
	
	public IPTUSPException(Throwable ex, byte[] file, String fileType){
		this(ex);
		
		this.file = file;
		this.fileType = fileType;
	}
 
	public IPTUSPException(String message, byte[] bytes, String fileType) {
		super(message);
		
		this.file = bytes;
		this.fileType = fileType;
	}
	
	public IPTUSPException(String message){
		super(message);
	}

	public byte[] getFile() {
		return file;
	}

	public void setFile(byte[] file) {
		this.file = file;
	}

	public String getFileType() {
		return this.fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

}
