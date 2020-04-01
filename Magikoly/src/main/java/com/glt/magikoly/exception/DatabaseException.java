package com.glt.magikoly.exception;

/**
 * 
 * @author yangguanxiang
 *
 */
public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = 1396155837630180169L;

	public DatabaseException(Throwable t) {
		super(t);
	}
}
