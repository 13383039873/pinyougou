package entity;

import java.io.Serializable;

public class Result implements Serializable {
	
	private boolean success;//新增是否成功true或false
	private String message;//新增是否成功的提示
	 
	public Result(boolean success, String message) {
		super();
		this.success = success;
		this.message = message;
	}
	

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	 

}
