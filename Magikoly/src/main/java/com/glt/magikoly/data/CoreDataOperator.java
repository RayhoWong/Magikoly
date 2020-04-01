package com.glt.magikoly.data;

import android.content.Context;

/**
 * GO桌面核心数据层Operator，对应androidheart.db这个数据库
 * @author yangguanxiang
 *
 */
public class CoreDataOperator implements IDataOperator {

	protected PersistenceManager mManager;

	protected Context mContext;

	public CoreDataOperator(Context context) {
		mContext = context;
		mManager = PersistenceManager.getInstance(DatabaseNames.DB_FACE);
	}


	@Override
	public void beginTransaction() {
		mManager.beginTransaction();
	}

	@Override
	public void setTransactionSuccessfully() {
		mManager.setTransactionSuccessful();
	}

	@Override
	public void endTransaction() {
		mManager.endTransaction();
	}

	@Override
	public void beginTransaction(String dbName) {

	}

	@Override
	public void setTransactionSuccessfully(String dbName) {

	}

	@Override
	public void endTransaction(String dbName) {

	}


	@Override
	public void setSynchronizeInThread(boolean synchronize) {
		mManager.setSynchronizeInThread(synchronize);
	}


	@Override
	public void endTransaction(PersistenceManager.IAsyncPersistenceCallback callback) {
		mManager.endTransaction(callback);
	}


	@Override
	public void endTransaction(String dbName, PersistenceManager.IAsyncPersistenceCallback callback) {
	}
	
	public boolean isTableEmpty(String tableName) {
		return mManager.isTableEmpty(tableName);
	}
}
