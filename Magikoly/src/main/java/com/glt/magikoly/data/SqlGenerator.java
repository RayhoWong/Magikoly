package com.glt.magikoly.data;


/**
 * 
 * @author yangguanxiang
 *
 */
public class SqlGenerator {

	public final static String TEXT = "text";
	public final static String NUMERIC = "numeric";
	public final static String INTEGER = "integer";
	public final static String TIMESTAMP = "timestamp";
	public final static String BLOB = "blob";

	private static long sId = System.currentTimeMillis();
	
	public static CreateBuffer create(String tableName) {
		return new CreateBuffer(tableName);
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	private interface ISqlBuffer {
		String generate();
	}

	/**
	 * 
	 * @author yangguanxiang
	 *
	 */
	public static class CreateBuffer implements ISqlBuffer {

		private final static String CREATE_PREFIX = "create table if not exists ";
		private StringBuilder mBuilder = new StringBuilder();

		public CreateBuffer(String tableName) {
			super();
			mBuilder.append(CREATE_PREFIX).append(tableName).append(" (");
		}
		public CreateBuffer addColumn(String name, String type) {
			mBuilder.append(", ").append(name).append(" ").append(type);
			return this;
		}

		public CreateBuffer addColumn(String name, String type, boolean isPrimaryKey,
                                      boolean isAutoIncrement, boolean isNotNull, boolean isUnique) {
			addColumn(name, type);
			if (isPrimaryKey) {
				mBuilder.append(" primary key");
			}
			if (isAutoIncrement) {
				mBuilder.append(" autoincrement");
			}
			if (isNotNull) {
				mBuilder.append(" not null");
			}
			if (isUnique) {
				mBuilder.append(" unique");
			}
			return this;
		}

		@Override
		public String generate() {
			String replaceStr = "(, ";
			int start = mBuilder.indexOf(replaceStr, CREATE_PREFIX.length());
			int end = start + replaceStr.length();
			mBuilder = mBuilder.append(")").replace(start, end, "(");
			return mBuilder.toString();
		}
	}

	public synchronized static long generateId() {
		return sId++;
	}
}
