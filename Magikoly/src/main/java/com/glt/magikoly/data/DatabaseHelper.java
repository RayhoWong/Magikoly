package com.glt.magikoly.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import com.glt.magikoly.data.table.FaceAnimalTable;
import com.glt.magikoly.data.table.ImageInfoTable;
import com.glt.magikoly.data.table.SubscribeTable;
import com.glt.magikoly.utils.Logcat;

import java.lang.reflect.Method;

/**
 * 数据库具体操作
 *
 * @author HuYong
 * @version 1.0
 */
@SuppressLint("NewApi")
public class DatabaseHelper extends SQLiteOpenHelper {

	private final static int DB_VERSION_ONE = 1;

	private final static int DB_VERSION_MAX = 3;

	// 可以执行多表关联查询
	SQLiteQueryBuilder mSqlQB = null;

	private final Context mContext;

	private boolean mUpdateResult = true; // 更新数据库结果，默认是成功的。

	/**
	 * version2.16加入标记位： 标记当前一次启动程序是刚刚升级完数据库，因为有些操作具体执行需要区分是不是第一次升级,默认-1
	 */
	// public static int sIsLastUpdateThisRun = -1;

	public DatabaseHelper(Context context, String dataBaseName) {
		super(context, dataBaseName, null, DB_VERSION_MAX);
		mContext = context;
		// just for test
		mSqlQB = new SQLiteQueryBuilder();
		SQLiteDatabase db = null;
		// 首先尝试用writeable模式来获取db，若取不到，则再用readable模式来取，若仍未取到，则沿用原有逻辑。
		try {
			db = getWritableDatabase();
		} catch (Exception e) {
			db = getReadableDatabase();
		}

		if (!mUpdateResult) {
			// 更新失败，则删除数据库，再行创建。
			if (db != null) {
				db.close();
			}
			mContext.deleteDatabase(DatabaseNames.DB_FACE);
			getWritableDatabase();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mUpdateResult = true;
		db.beginTransaction();
		try {
			db.execSQL(ImageInfoTable.CREATE_SQL);
			db.execSQL(SubscribeTable.CREATE_SQL);
			db.execSQL(FaceAnimalTable.CREATE_SQL);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * 检查指定的表是否存在
	 *
	 * @author huyong
	 * @param tableName
	 * @return
	 */
	private boolean isExistTable(final SQLiteDatabase db, String tableName) {
		boolean result = false;
		Cursor cursor = null;
		String where = "type='table' and name='" + tableName + "'";
		try {
			cursor = db.query("sqlite_master", null, where, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return result;
	}

	/**
	 * 检查表中是否存在该字段
	 *
	 * @author huyong
	 * @param db
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	private boolean isExistColumnInTable(SQLiteDatabase db, String tableName, String columnName) {
		boolean result = false;
		Cursor cursor = null;
		try {
			// 查询列数
			String columns[] = { columnName };
			cursor = db.query(tableName, columns, null, null, null, null, null);
			if (cursor != null && cursor.getColumnIndex(columnName) >= 0) {
				result = true;
			}
		} catch (Exception e) {
			// e.printStackTrace();
			Logcat.i("DatabaseHelper", "isExistColumnInTable has exception");
			result = false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return result;
	}

	/**
	 * 新添加字段到表中
	 *
	 * @author huyong
	 * @param db
	 * @param tableName
	 *            : 修改表名
	 * @param columnName
	 *            ：新增字段名
	 * @param columnType
	 *            ：新增字段类型
	 * @param defaultValue
	 *            ：新增字段默认值。为null，则不提供默认值
	 */
	private void addColumnToTable(SQLiteDatabase db, String tableName, String columnName,
			String columnType, String defaultValue) {
		if (!isExistColumnInTable(db, tableName, columnName)) {
			db.beginTransaction();
			try {
				// 增加字段
				String updateSql = "ALTER TABLE " + tableName + " ADD " + columnName + " "
						+ columnType;
				db.execSQL(updateSql);

				// 提供默认值
				if (defaultValue != null) {
					if (columnType.equals(SqlGenerator.TEXT)) {
						// 如果是字符串类型，则需加单引号
						defaultValue = "'" + defaultValue + "'";
					}

					updateSql = "update " + tableName + " set " + columnName + " = " + defaultValue;
					db.execSQL(updateSql);
				}

				db.setTransactionSuccessful();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.endTransaction();
			}
		}
	}

	// 只针对安卓3.0系统以上
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 默认支持向下兼容。（oldVersion = 2, newVersion = 1）
		// 后期在做版本降级处理时，在此可根据需要做相应处理
		Logcat.i("DatabaseHelper", "onDowngrade oldVersion=" + oldVersion + ", newVersion="
				+ newVersion);
		return;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		doUpgrade(db, oldVersion, newVersion);
	}

	private void doUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logcat.i("Test", "onUpgrade");
		if (oldVersion < DB_VERSION_ONE || oldVersion > newVersion || newVersion > DB_VERSION_MAX) {
			return;
		}
		Logcat.i("Test", "doUpgrade");
		RuntimeException exception = null;
		for (int i = oldVersion; i < newVersion; i++) {
			int preVersion = i;
			int curVersion = i + 1;
			String methodName = "onUpgradeDB" + preVersion + "To" + curVersion;
			try {
				Method method = getClass().getMethod(methodName, SQLiteDatabase.class);
				mUpdateResult = (Boolean) method.invoke(this, db);
			} catch (NoSuchMethodException e) {
				exception = new RuntimeException(e);
			} catch (Throwable t) {
				exception = new RuntimeException(t);
			}
			if (!mUpdateResult || exception != null) {
				if (exception != null) {
					throw exception;
				} else {
					String detailMessage = "update database has exception in " + methodName;
					throw new RuntimeException(detailMessage);
				}
			}
		}
	}

	public static int getVersion() {
		return DB_VERSION_MAX;
	}

	public boolean onUpgradeDB1To2(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			db.execSQL(ImageInfoTable.CREATE_SQL);
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return result;
	}

	public boolean onUpgradeDB2To3(SQLiteDatabase db) {
		boolean result = false;
		db.beginTransaction();
		try {
			db.execSQL(FaceAnimalTable.CREATE_SQL);
			db.setTransactionSuccessful();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		return result;
	}
}