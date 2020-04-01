package com.glt.magikoly.exception;

/**
 * 
 * @author yangguanxiang
 *
 */
public class DatabaseCorruptException extends RuntimeException {

	private static final long serialVersionUID = -8490178851301231237L;

	public DatabaseCorruptException(Throwable t) {
		super(t);
	}
	
	public DatabaseCorruptException() {
	}
}
