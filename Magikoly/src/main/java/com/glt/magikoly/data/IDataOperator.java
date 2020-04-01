package com.glt.magikoly.data;


/**
 * @author yangguanxiang
 */
public interface IDataOperator {
    void beginTransaction();

    void setTransactionSuccessfully();

    void endTransaction();

    void endTransaction(PersistenceManager.IAsyncPersistenceCallback callback);

    void beginTransaction(String dbName);

    void setTransactionSuccessfully(String dbName);

    void endTransaction(String dbName);

    void endTransaction(String dbName, PersistenceManager.IAsyncPersistenceCallback callback);

    void setSynchronizeInThread(boolean synchronize);
}
