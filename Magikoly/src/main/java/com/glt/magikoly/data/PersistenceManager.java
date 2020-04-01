package com.glt.magikoly.data;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.*;
import android.os.Process;

import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.exception.DatabaseCorruptException;
import com.glt.magikoly.exception.DatabaseException;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * <br>
 * 类描述: 持久化管理者 <br>
 * 功能详细描述: 负责桌面持久化管理，包括异步和同步持久化机制，支持事务嵌套
 * 
 * @author yangguanxiang
 * @date [2012-12-14]
 */
@SuppressLint("NewApi")
public class PersistenceManager {
	private static final boolean ASYNC_ENABLE = true;

	public static final String AUTHORITIE = FaceAppState.getContext().getPackageName() + ".contentprovider";

	private static final String DATA_PATH_CALL = "call";

	public static final Uri URI_CALL = Uri.parse("content://" + AUTHORITIE + "/" + DATA_PATH_CALL);

	public static final String URI_PREFIX = "content://" + AUTHORITIE + "/";

	public static final String METHOD_EXEC = "exec";

	public static final String METHOD_UPDATE_OR_INSERT = "updateOrInsert";

	private static HashMap<String, DatabaseCreator> sCreatorMap;
	private static HashMap<String, PersistenceManager> sInstancePool = new HashMap<String, PersistenceManager>();
	private SQLiteOpenHelper mDbHelper;
	private String mDbName;
	private HandlerThread mDbThread;
	private Handler mDbHandler;
	// 可以执行多表关联查询
	private SQLiteQueryBuilder mSqlQB = null;
	private ConcurrentHashMap<Integer, TransactionCtrl> mTransactionMap = new ConcurrentHashMap<Integer, TransactionCtrl>();
	private HashSet<PersistenceJob> mJobSet = new HashSet<PersistenceJob>();
	private boolean mIsMultiProcess;
	private ContentResolver mResolver;
	private HashSet<Integer> mSyncTidSet = new HashSet<Integer>();
	public interface DatabaseCreator {
		Object create(String dbName);
	}
	private PersistenceManager(String dbName, SQLiteOpenHelper helper) {
		mDbName = dbName;
		mDbHelper = helper;
		mDbThread = new HandlerThread("Thread-db-" + mDbName);
		mDbThread.start();
		mDbHandler = new Handler(mDbThread.getLooper());
		mSqlQB = new SQLiteQueryBuilder();
	}

	private PersistenceManager(String dbName, ContentResolver resolver) {
		mDbName = dbName;
		mIsMultiProcess = true;
		mResolver = resolver;
		mDbThread = new HandlerThread("Thread-db-" + mDbName);
		mDbThread.start();
		mDbHandler = new Handler(mDbThread.getLooper());
	}

	public static void initDatabaseCreator(HashMap<String, DatabaseCreator> map) {
		sCreatorMap = map;
	}

	public synchronized static PersistenceManager getInstance(String dbName) {
		if (!sInstancePool.containsKey(dbName)) {
			if (sCreatorMap.containsKey(dbName)) {
				Object v = sCreatorMap.get(dbName).create(dbName);
				PersistenceManager persistenceManager = null;
				if (v instanceof ContentResolver) {
					persistenceManager = new PersistenceManager(dbName, (ContentResolver) v);
				} else if (v instanceof SQLiteOpenHelper) {
					persistenceManager = new PersistenceManager(dbName, (SQLiteOpenHelper) v);
				}
				if (persistenceManager != null) {
					sInstancePool.put(dbName, persistenceManager);
				} else {
					throw new IllegalArgumentException("no instance of dbName: " + dbName);
				}
			} else {
				throw new IllegalArgumentException("no instance of dbName: " + dbName);
			}
		}
		return sInstancePool.get(dbName);
	}

	/**
	 * 获取当前数据库名
	 * 
	 * @return
	 */
	public String getDBName() {
		return mDbName;
	}

	//	private ProgressDialog mDialog;
	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		synchronized (mJobSet) {
			long maxWaitTime = 2000;
			long startWaitTime = System.currentTimeMillis();
			while (hasWaitJobs()) {
				//				if (showDialog) {
				//					if (mDialog == null) {
				//						GoLauncher golauncher = GoLauncherActivityProxy.getActivity();
				//						if (golauncher != null) {
				//							mDialog = new ProgressDialog(golauncher);
				//							mDialog.setMessage("正在退出GO桌面，请稍后...");
				//						}
				//					}
				//					if (!mDialog.isShowing()) {
				//						Logcat.i("Test", "show dialog");
				//						mDialog.show();
				//					}
				//				}
				try {
					mJobSet.wait(maxWaitTime);
					long endWaitTime = System.currentTimeMillis();
					if (endWaitTime - startWaitTime >= maxWaitTime) {
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (hasWaitJobs()) {
				mDbHandler.removeCallbacksAndMessages(null);
				mJobSet.clear();
			}
		}
		if (mDbHelper != null) {
			try {
				mDbHelper.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 是否还有持久化任务没有完成
	 * 
	 * @return
	 */
	private boolean hasWaitJobs() {
		synchronized (mJobSet) {
			return !mJobSet.isEmpty();
		}
	}

	/**
	 * 关闭数据库
	 * 
	 * @param dbName
	 */
	public static void closeDB(String dbName) {
		PersistenceManager manager = sInstancePool.get(dbName);
		if (manager != null) {
			manager.closeDB();
		}
	}

	/**
	 * 关闭所有数据库
	 */
	public static void closeAllDB() {
		Collection<PersistenceManager> values = sInstancePool.values();
		for (PersistenceManager manager : values) {
			manager.closeDB();
		}
	}

	public Cursor rawQuery(String sql, String[] selectionArgs) {
		if (mIsMultiProcess) {
			throw new UnsupportedOperationException("content provider is not support rawQuery");
		} else {
			Cursor cur = null;
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			if (db != null) {
				try {
					cur = db.rawQuery(sql, selectionArgs);
				} catch (Throwable e) {
					e.printStackTrace();
					throw new DatabaseException(e);
				}
			}
			return cur;
		}
	}
	
	/**
	 * <br>
	 * 功能简述: 单表查询 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param tableName
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @return
	 */
	public Cursor query(String tableName, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
		if (mIsMultiProcess) {
			return mResolver.query(convertToUri(tableName), projection, selection, selectionArgs,
					sortOrder);
		} else {
			return query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
		}
	}
	/**
	 * <br>
	 * 功能简述: 单表查询 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param tableName
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param sortOrder
	 * @return
	 * @throws DatabaseException
	 */
	public Cursor query(String tableName, String[] projection, String selection,
                        String[] selectionArgs, String groupBy, String having, String sortOrder) {
		if (mIsMultiProcess) {
			return mResolver.query(convertToUri(tableName), projection, selection, selectionArgs,
					sortOrder);
		} else {
			return query(tableName, projection, selection, selectionArgs, groupBy, having,
					sortOrder, false);
		}
	}

	/**
	 * 
	 * @param tableName
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param sortOrder
	 * @param ignoreCheck 一般情况下异步机制不允许在事务中进行查询操作，如果处于事务中会抛出IllegalStateException。该值默认为false，应进行事务检查；true时，忽略检查，用于兼容旧代码
	 * @return
	 */
	public Cursor query(String tableName, String[] projection, String selection,
                        String[] selectionArgs, String groupBy, String having, String sortOrder,
                        boolean ignoreCheck) {
		if (mIsMultiProcess) {
			return mResolver.query(convertToUri(tableName), projection, selection, selectionArgs,
					sortOrder);
		} else {
			if (!ignoreCheck) {
				checkQueryInTransaction(new String[] { tableName });
			}
			Cursor cur = null;
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				if (db != null) {
					cur = db.query(tableName, projection, selection, selectionArgs, groupBy,
							having, sortOrder);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new DatabaseException(e);
			}
			return cur;
		}
	}

	public Cursor queryCrossTables(String tableNames, String[] projection, String selection,
                                   String[] selectionArgs, String sortOrder) {
		if (mIsMultiProcess) {
			throw new UnsupportedOperationException(
					"content provider is not support queryCrossTables");
		} else {
			return queryCrossTables(tableNames, projection, selection, selectionArgs, sortOrder,
					false);
		}
	}

	/**
	 * 
	 * @param tableNames
	 * @param projection
	 * @param selection
	 * @param selectionArgs
	 * @param sortOrder
	 * @param ignoreCheck 一般情况下异步机制不允许在事务中进行查询操作，如果处于事务中会抛出IllegalStateException。该值默认为false，应进行事务检查；true时，忽略检查，用于兼容旧代码
	 * @return
	 */
	public Cursor queryCrossTables(String tableNames, String[] projection, String selection,
                                   String[] selectionArgs, String sortOrder, boolean ignoreCheck) {
		if (mIsMultiProcess) {
			throw new UnsupportedOperationException(
					"content provider is not support queryCrossTables");
		} else {
			if (!ignoreCheck) {
				checkQueryInTransaction(tableNames.split(","));
			}
			Cursor result = null;
			synchronized (mSqlQB) {
				mSqlQB.setTables(tableNames);
				try {
					SQLiteDatabase db = mDbHelper.getReadableDatabase();
					result = mSqlQB.query(db, projection, selection, selectionArgs, null, null,
							sortOrder);
				} catch (Throwable e) {
					e.printStackTrace();
					throw new DatabaseException(e);
				}
			}
			return result;
		}
	}

	private void checkQueryInTransaction(String[] tableNames) {
		int tid = Process.myTid();
		if (mTransactionMap.containsKey(tid)) {
			TransactionCtrl transaction = mTransactionMap.get(tid);
			if (!transaction.mStack.isEmpty()) {
				for (TransactionBean bean : transaction.mStack) {
					checkTransaction(bean, tableNames, tid);
				}
			}
		}
	}

	private void checkTransaction(TransactionBean bean, String[] tableNames, int tid) {
		for (Object action : bean.mActionList) {
			if (action instanceof PersistenceOperation) {
				PersistenceOperation operation = (PersistenceOperation) action;
				for (String name : tableNames) {
					if (name.trim().equals(operation.mTableName)) {
						throw new IllegalStateException("Can't support query table " + name.trim()
								+ " in transaction with tid " + tid);
					}
				}
			} else if (action instanceof TransactionBean) {
				checkTransaction((TransactionBean) action, tableNames, tid);
			}
		}
	}

	/**
	 * 插入数据
	 * 
	 * @param tableName
	 * @param values
	 */
	public void insert(final String tableName, final ContentValues values) {
		insert(tableName, values, null);
	}

	/**
	 * 插入数据
	 * 
	 * @param tableName
	 * @param values
	 * @param callback
	 */
	public void insert(final String tableName, final ContentValues values,
                       final IAsyncPersistenceCallback callback) {
		if (values != null) {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				if (mTransactionMap.containsKey(tid)) {
					mTransactionMap.get(tid).addOperation(
							PersistenceOperation.newInsertOperation(tableName, values));
				} else {
					postJob(new PersistenceJob() {

						@Override
						protected void doPersistence() {
							boolean success = true;
							DatabaseException exception = null;
							try {
								doInsert(tableName, values);
							} catch (DatabaseException e) {
								success = false;
								exception = e;
							}
							if (callback != null) {
								callback.callback(success, exception);
							} else {
								if (exception != null) {
									throw exception;
								}
							}
						}
					});
				}
			} else {
				doInsert(tableName, values);
			}
		}
	}

	private void doInsert(final String tableName, final ContentValues values) {
		if (mIsMultiProcess) {
			mResolver.insert(convertToUri(tableName), values);
		} else {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null && !db.isReadOnly()) {
					db.insert(tableName, null, values);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * 更新数据
	 * 
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 */
	public void update(final String tableName, final ContentValues values, final String selection,
                       final String[] selectionArgs) {
		update(tableName, values, selection, selectionArgs, null);
	}

	/**
	 * 更新数据
	 * 
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @param callback
	 */
	public void update(final String tableName, final ContentValues values, final String selection,
                       final String[] selectionArgs, final IAsyncPersistenceCallback callback) {
		if (values != null) {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				if (mTransactionMap.containsKey(tid)) {
					mTransactionMap.get(tid).addOperation(
							PersistenceOperation.newUpdateOperation(tableName, values, selection,
									selectionArgs));
				} else {
					postJob(new PersistenceJob() {

						@Override
						public void doPersistence() {
							boolean success = true;
							DatabaseException exception = null;
							try {
								doUpdate(tableName, values, selection, selectionArgs);
							} catch (DatabaseException e) {
								success = false;
								exception = e;
							}
							if (callback != null) {
								callback.callback(success, exception);
							} else {
								if (exception != null) {
									throw exception;
								}
							}
						}
					});
				}
			} else {
				doUpdate(tableName, values, selection, selectionArgs);
			}
		}
	}

	private void doUpdate(final String tableName, final ContentValues values,
                          final String selection, final String[] selectionArgs) {
		if (mIsMultiProcess) {
			mResolver.update(convertToUri(tableName), values, selection, selectionArgs);
		} else {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null && !db.isReadOnly()) {
					db.update(tableName, values, selection, selectionArgs);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * 更新数据或插入数据
	 * 
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 */
	public void updateOrInsert(final String tableName, final ContentValues values,
                               final String selection, final String[] selectionArgs) {
		updateOrInsert(tableName, values, selection, selectionArgs, null);

	}

	/**
	 * 更新数据或插入数据
	 * 
	 * @param tableName
	 * @param values
	 * @param selection
	 * @param selectionArgs
	 * @param callback
	 */
	public void updateOrInsert(final String tableName, final ContentValues values,
                               final String selection, final String[] selectionArgs,
                               final IAsyncPersistenceCallback callback) {
		if (values != null) {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				if (mTransactionMap.containsKey(tid)) {
					mTransactionMap.get(tid).addOperation(
							PersistenceOperation.newUpdateOrInsertOperation(tableName, values,
									selection, selectionArgs));
				} else {
					postJob(new PersistenceJob() {

						@Override
						public void doPersistence() {
							boolean success = true;
							DatabaseException exception = null;
							try {
								doUpdateOrInsert(tableName, values, selection, selectionArgs);
							} catch (DatabaseException e) {
								success = false;
								exception = e;
							}
							if (callback != null) {
								callback.callback(success, exception);
							} else {
								if (exception != null) {
									throw exception;
								}
							}
						}
					});
				}
			} else {
				doUpdateOrInsert(tableName, values, selection, selectionArgs);
			}
		}
	}

	private void doUpdateOrInsert(final String tableName, final ContentValues values,
                                  final String selection, final String[] selectionArgs) {
		if (mIsMultiProcess) {
			mResolver.update(convertToUri(tableName, METHOD_UPDATE_OR_INSERT),
					values, selection, selectionArgs);
		} else {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null && !db.isReadOnly()) {
					int rows = db.update(tableName, values, selection, selectionArgs);
					if (rows <= 0) {
						db.insert(tableName, null, values);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * 删除数据
	 * 
	 * @param tableName
	 * @param selection
	 * @param selectionArgs
	 */
	public void delete(final String tableName, final String selection, final String[] selectionArgs) {
		delete(tableName, selection, selectionArgs, null);
	}

	/**
	 * 删除数据
	 * 
	 * @param tableName
	 * @param selection
	 * @param selectionArgs
	 * @param callback
	 */
	public void delete(final String tableName, final String selection,
                       final String[] selectionArgs, final IAsyncPersistenceCallback callback) {
		if (isAsyncEnable()) {
			int tid = Process.myTid();
			if (mTransactionMap.containsKey(tid)) {
				mTransactionMap.get(tid).addOperation(
						PersistenceOperation
								.newDeleteOperation(tableName, selection, selectionArgs));
			} else {
				postJob(new PersistenceJob() {

					@Override
					public void doPersistence() {
						boolean success = true;
						DatabaseException exception = null;
						try {
							doDelete(tableName, selection, selectionArgs);
						} catch (DatabaseException e) {
							success = false;
							exception = e;
						}
						if (callback != null) {
							callback.callback(success, exception);
						} else {
							if (exception != null) {
								throw exception;
							}
						}
					}
				});
			}
		} else {
			doDelete(tableName, selection, selectionArgs);
		}
	}

	private void doDelete(final String tableName, final String selection,
                          final String[] selectionArgs) {
		if (mIsMultiProcess) {
			mResolver.delete(convertToUri(tableName), selection, selectionArgs);
		} else {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null && !db.isReadOnly()) {
					db.delete(tableName, selection, selectionArgs);
				}
			} catch (Throwable e) {
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * 执行相应的SQL语句
	 * 
	 * @param sql
	 */
	public void exec(final String sql) {
		exec(sql, null);
	}

	/**
	 * 执行相应的SQL语句
	 * 
	 * @param sql
	 * @param callback
	 */
	public void exec(final String sql, final IAsyncPersistenceCallback callback) {
		if (isAsyncEnable()) {
			int tid = Process.myTid();
			if (mTransactionMap.containsKey(tid)) {
				mTransactionMap.get(tid).addOperation(PersistenceOperation.newExecOperation(sql));
			} else {
				postJob(new PersistenceJob() {

					@Override
					public void doPersistence() {
						boolean success = true;
						DatabaseException exception = null;
						try {
							doExec(sql);
						} catch (DatabaseException e) {
							success = false;
							exception = e;
						}
						if (callback != null) {
							callback.callback(success, exception);
						} else {
							if (exception != null) {
								throw exception;
							}
						}
					}
				});
			}
		} else {
			doExec(sql);
		}
	}

	private void doExec(final String sql) {
		if (mIsMultiProcess) {
			call(METHOD_EXEC, sql);
			//			throw new UnsupportedOperationException("content provider is not support execSQL");
		} else {
			try {
				SQLiteDatabase db = mDbHelper.getWritableDatabase();
				if (db != null && !db.isReadOnly()) {
					db.execSQL(sql);
				}
			} catch (Throwable e) {
				throw new DatabaseException(e);
			}
		}
	}

	/**
	 * 开启事务
	 */
	public void beginTransaction() {
		if (mIsMultiProcess) {
			//			call(GoContentProvider.METHOD_BEGIN_TRANSACTION, null);
			//			throw new UnsupportedOperationException("content provider is not support transaction");
		} else {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				//				Logcat.i("Test", "beginTransaction pid: " + Process.myPid() + " tid: " + tid);
				if (!mTransactionMap.containsKey(tid)) {
					TransactionCtrl transaction = new TransactionCtrl();
					TransactionBean bean = new TransactionBean();
					transaction.pushTransactionBean(bean);
					mTransactionMap.put(tid, transaction);
				} else {
					TransactionCtrl transaction = mTransactionMap.get(tid);
					TransactionBean bean = new TransactionBean();
					transaction.pushTransactionBean(bean);
				}
			} else {
				try {
					SQLiteDatabase db = mDbHelper.getWritableDatabase();
					if (db != null) {
						db.beginTransaction();
					}
				} catch (Throwable e) {
					throw new DatabaseException(e);
				}
			}
		}
	}

	/**
	 * 结束事务
	 */
	public void endTransaction() {
		endTransaction(null);
	}

	/**
	 * 结束事务
	 * 
	 * @param callback
	 */
	public void endTransaction(final IAsyncPersistenceCallback callback) {
		if (mIsMultiProcess) {
			//由于每次调用ContentProvider的call方法时都处于不同的线程中执行，故无法使用事务
			//			call(GoContentProvider.METHOD_END_TRANSACTION, null);
			//			throw new UnsupportedOperationException("content provider is not support transaction");
		} else {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				//				Logcat.i("Test", "endTransaction pid: " + Process.myPid() + " tid: " + tid);
				if (mTransactionMap.containsKey(tid)) {
					final TransactionCtrl transaction = mTransactionMap.get(tid);
					final TransactionBean bean = transaction.popTransactionBean();
					bean.mCallback = callback;
					if (transaction.isAllTransactionsEnd()) {
						mTransactionMap.remove(tid);
						if (bean.mSuccess) {
							postJob(new PersistenceJob() {

								@Override
								public void doPersistence() {
									SQLiteDatabase db = mDbHelper.getWritableDatabase();
									doTransaction(db, bean);
								}
							});
						} else {
							if (callback != null) {
								if (bean.mActionList.isEmpty()) {
									callback.callback(true, null);
								} else {
									callback.callback(false, null);
								}
							}
						}
					}
				} else {
					throw new IllegalStateException("No transaction in current thread");
				}
			} else {
				boolean success = true;
				DatabaseException exception = null;
				try {
					SQLiteDatabase db = mDbHelper.getWritableDatabase();
					if (db.inTransaction()) {
						db.endTransaction();
					}
				} catch (Exception e) {
					success = false;
					if (exception == null) {
						exception = new DatabaseException(e);
					}
				} catch (Throwable t) {
					exception = new DatabaseException(t);
				}
				if (callback != null) {
					callback.callback(success, exception);
				} else {
					if (exception != null) {
						throw exception;
					}
				}
			}
		}
	}

	private void doTransaction(SQLiteDatabase db, final TransactionBean bean) {
		boolean success = true;
		DatabaseException exception = null;
		try {
			db.beginTransaction();
			//			Logcat.i("Test", bean + " beginTransaction");
			for (Object action : bean.mActionList) {
				if (action instanceof PersistenceOperation) {
					((PersistenceOperation) action).execute(db);
				} else if (action instanceof TransactionBean) {
					doTransaction(db, (TransactionBean) action);
				}
			}
			db.setTransactionSuccessful();
			//			Logcat.i("Test", bean + " setTransactionSuccessful");
		} catch (Exception e) {
			success = false;
			exception = new DatabaseException(e);
		} catch (Throwable t) {
			exception = new DatabaseException(t);
		} finally {
			try {
				db.endTransaction();
			} catch (Exception e) {
				success = false;
				if (exception == null) {
					exception = new DatabaseException(e);
				}
			}
			if (bean.mCallback != null) {
				bean.mCallback.callback(success, exception);
			} else {
				if (exception != null) {
					throw exception;
				}
			}
			//			Logcat.i("Test", bean + " endTransaction");
		}
	}

	/**
	 * 设置事务成功
	 */
	public void setTransactionSuccessful() {
		if (mIsMultiProcess) {
			//			call(GoContentProvider.METHOD_TRANSACTION_SUCCESS, null);
			//			throw new UnsupportedOperationException("content provider is not support transaction");
		} else {
			if (isAsyncEnable()) {
				int tid = Process.myTid();
				//				Logcat.i("Test", "setTransactionSuccessful pid: " + Process.myPid() + " tid: " + tid);
				if (mTransactionMap.containsKey(tid)) {
					TransactionCtrl transaction = mTransactionMap.get(tid);
					TransactionBean bean = transaction.getCurrentTransactionBean();
					if (!bean.mActionList.isEmpty()) {
						bean.mSuccess = true;
					}
				} else {
					throw new IllegalStateException("No transaction in current thread");
				}
			} else {
				try {
					SQLiteDatabase db = mDbHelper.getWritableDatabase();
					if (db != null) {
						db.setTransactionSuccessful();
					}
				} catch (Exception e) {
					throw new DatabaseException(e);
				} catch (Throwable t) {
					throw new DatabaseException(t);
				}
			}
		}
	}

	private void call(String method, String param) {
		if (Build.VERSION.SDK_INT >= 11) {
			mResolver.call(URI_CALL, method, param, null);
		} else { // 3.0以下不支持call方法，借用delete调用call
			mResolver.delete(URI_CALL, method, new String[] { param });
		}
	}

	/**
	 * 表是否存在
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean isTableExist(String tableName) {
		boolean result = false;
		if (mDbHelper != null) {
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			if (db != null) {
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
			}
		}
		return result;
	}

	/**
	 * 表是否为空
	 * @param tableName
	 * @return
	 */
	public boolean isTableEmpty(String tableName) {
		boolean result = true;
		if (mDbHelper != null) {
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			if (db != null) {
				Cursor cursor = null;
				try {
					cursor = db.query(tableName, new String[] { "count(*)" }, null, null, null,
							null, null);
					if (cursor != null) {
						if (cursor.moveToNext()) {
							int count = cursor.getInt(0);
							result = count == 0;
						}
					}
				} catch (Exception e) {
					throw new DatabaseException(e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}
		return result;
	}

	/**
	 * 把一个持久化过程post到DB异步线程上跑，常用于整个事务过程放到异步线程中执行
	 * 
	 */
	public void postJob(PersistenceJob job) {
		synchronized (mJobSet) {
			mJobSet.add(job);
			mDbHandler.post(job);
		}
	}

	private Uri convertToUri(final String tableName) {
		return Uri.parse(URI_PREFIX + tableName);
	}

	private Uri convertToUri(final String tableName, String suffix) {
		return Uri.parse(URI_PREFIX + tableName + "/" + suffix);
	}


	/**
	 * 设置数据库操作为同步执行。默认为false
	 * 执行setSynchronizeInThread(true)后，该线程下所有数据写入操作均同步执行，直到执行setSynchronizeInThread(false)
	 * 注意：执行完同步操作后，切记执行setSynchronizeInThread(false)。
	 * @param synchronize
	 */
	public void setSynchronizeInThread(boolean synchronize) {
		synchronized (mSyncTidSet) {
			if (synchronize) {
				mSyncTidSet.add(Process.myTid());
			} else {
				mSyncTidSet.remove(Process.myTid());
			}
		}
	}

	private boolean isAsyncEnable() {
		synchronized (mSyncTidSet) {
			return ASYNC_ENABLE && !mIsMultiProcess && !mSyncTidSet.contains(Process.myTid());
		}
	}

	public static boolean isDBExist(Context context, String dbName) {
		String dbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName()
				+ "/databases/";
		String dbFilePath = dbPath + dbName;
		File dbFile = new File(dbFilePath);
		return dbFile.exists();
	}
	
	public static boolean isDBCanAccess(Context context, String dbName) {
		String dbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName()
				+ "/databases/";
		String dbFilePath = dbPath + dbName;
		File dbFile = new File(dbFilePath);
		return dbFile.canRead() && dbFile.canWrite();
	}
	
	public static void checkIsDBValid(Context context, String dbName) {
		boolean canDBAccess = PersistenceManager.isDBCanAccess(context,
				dbName);
		if (!canDBAccess) {
			throw new DatabaseCorruptException();
		}
	}
	
	public static boolean dropDB(Context context, String dbName) {
		String dbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName()
				+ "/databases/";
		String dbFilePath = dbPath + dbName;
		File dbFile = new File(dbFilePath);
		return dbFile.delete();
	}

	public static int getDBVersion(Context context, String dbName) {
		if (sInstancePool.containsKey(dbName)) {
			PersistenceManager manager = sInstancePool.get(dbName);
			return manager.getDBVersion();
		}
		
		int versionCode = 0;
		SQLiteDatabase db = null;
		String dbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName()
				+ "/databases/";
		String dbFilePath = dbPath + dbName;
		try {
			db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
			if (db != null) {
				versionCode = db.getVersion();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return versionCode;
	}

	public int getDBVersion() {
		if (mDbHelper != null) {
			try {
				SQLiteDatabase db = mDbHelper.getReadableDatabase();
				if (db != null) {
					return db.getVersion();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
//	/**
//	 * 创建数据库
//	 *
//	 * @param context
//	 * @param dbName
//	 * @return
//	 */
//	public static void createDatabase(Context context, String dbName) {
//		getInstance(context, dbName);
//	}

	/**
	 * 
	 * <br>
	 * 类描述: 异步持久化回调接口 <br>
	 * 功能详细描述: 每次异步操作执行后都会回调该接口的callback方法
	 * 
	 * @author yangguanxiang
	 * @date [2012-12-14]
	 */
	public interface IAsyncPersistenceCallback {
		void callback(boolean success, DatabaseException exception);
	}

	/**
	 * 
	 * @author yangguanxiang
	 * 
	 */
	private static class PersistenceOperation {

		public static final int INSERT = 0;
		public static final int DELETE = 1;
		public static final int UPDATE = 2;
		public static final int EXEC = 3;
		public static final int UPDATE_OR_INSERT = 4;

		private int mType = -1;
		private String mTableName;
		private ContentValues mValues;
		private String mSelection;
		private String[] mSelectionArgs;
		private String mSql;

		private PersistenceOperation(int type) {
			mType = type;
		}

		public static PersistenceOperation newInsertOperation(String tableName, ContentValues values) {
			PersistenceOperation operation = new PersistenceOperation(INSERT);
			operation.mTableName = tableName;
			operation.mValues = values;
			return operation;
		}

		public static PersistenceOperation newDeleteOperation(String tableName, String selection,
                                                              String[] selectionArgs) {
			PersistenceOperation operation = new PersistenceOperation(DELETE);
			operation.mTableName = tableName;
			operation.mSelection = selection;
			operation.mSelectionArgs = selectionArgs;
			return operation;
		}

		public static PersistenceOperation newUpdateOperation(String tableName,
                                                              ContentValues values, String selection, String[] selectionArgs) {
			PersistenceOperation operation = new PersistenceOperation(UPDATE);
			operation.mTableName = tableName;
			operation.mValues = values;
			operation.mSelection = selection;
			operation.mSelectionArgs = selectionArgs;
			return operation;
		}

		public static PersistenceOperation newExecOperation(String sql) {
			PersistenceOperation operation = new PersistenceOperation(EXEC);
			operation.mSql = sql;
			return operation;
		}

		public static PersistenceOperation newUpdateOrInsertOperation(String tableName,
                                                                      ContentValues values, String selection, String[] selectionArgs) {
			PersistenceOperation operation = new PersistenceOperation(UPDATE_OR_INSERT);
			operation.mTableName = tableName;
			operation.mValues = values;
			operation.mSelection = selection;
			operation.mSelectionArgs = selectionArgs;
			return operation;
		}

		public void execute(SQLiteDatabase db) {
			switch (mType) {
				case INSERT :
					db.insert(mTableName, null, mValues);
					//					Logcat.i("Test", "insert " + mTableName);
					break;
				case DELETE :
					db.delete(mTableName, mSelection, mSelectionArgs);
					//					Logcat.i("Test", "delete " + mTableName);
					break;
				case UPDATE :
					db.update(mTableName, mValues, mSelection, mSelectionArgs);
					//					Logcat.i("Test", "update " + mTableName);
					break;
				case EXEC :
					db.execSQL(mSql);
					//					Logcat.i("Test", "execSQL " + mSql);
					break;
				case UPDATE_OR_INSERT :
					int rows = db.update(mTableName, mValues, mSelection, mSelectionArgs);
					if (rows <= 0) {
						db.insert(mTableName, null, mValues);
					}
					break;
			}
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 * 
	 */
	private class TransactionCtrl {
		private Stack<TransactionBean> mStack = new Stack<TransactionBean>();

		private void addOperation(PersistenceOperation operation) {
			getCurrentTransactionBean().add(operation);
		}

		private void pushTransactionBean(TransactionBean bean) {
			TransactionBean curBean = getCurrentTransactionBean();
			if (curBean != null) {
				curBean.add(bean);
			}
			mStack.push(bean);
		}

		private TransactionBean popTransactionBean() {
			if (mStack.isEmpty()) {
				return null;
			}
			return mStack.pop();
		}

		private TransactionBean getCurrentTransactionBean() {
			if (mStack.isEmpty()) {
				return null;
			}
			return mStack.peek();
		}

		private boolean isAllTransactionsEnd() {
			return mStack.isEmpty();
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 * 
	 */
	private class TransactionBean {
		private boolean mSuccess;
		private ArrayList<Object> mActionList = new ArrayList<Object>();
		private IAsyncPersistenceCallback mCallback;

		private void add(PersistenceOperation operation) {
			mActionList.add(operation);
		}

		private void add(TransactionBean bean) {
			mActionList.add(bean);
		}
	}

	/**
	 * 
	 * @author yangguanxiang
	 * 
	 */
	private abstract class PersistenceJob implements Runnable {

		@Override
		final public void run() {
			try {
				doPersistence();
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				synchronized (mJobSet) {
					mJobSet.remove(this);
					//					Logcat.i("Test", "PersistenceJob notifyAll");
					mJobSet.notifyAll();
				}
			}
		}

		protected abstract void doPersistence();
	}
}
