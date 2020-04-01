package com.glt.magikoly.pref;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.application.IApplication;

import java.util.HashSet;

/**
 * 
 * @author guoyiqing
 * 
 */
@SuppressLint("CommitPrefEdits")
public class PrivatePreference {

	private static final byte[] MUTEX = new byte[0];
	public static final String SP_NAME = "face_mystery_pref";
	private static PrivatePreference sInstance;
	private SinglePreference mPrefImpl;
	private HashSet<SinglePreference> mCommitEdits;
	private volatile boolean mUpgradeFinished;
	private Context mContext;

	private PrivatePreference(Context context) {
		mContext = context.getApplicationContext();
		mCommitEdits = new HashSet<>();
		initSharedPreferences();
	}

	private void initSharedPreferences() {
		synchronized (MUTEX) {
			mPrefImpl = new SinglePreference(mContext, SP_NAME);
		}
	}

	/**
	 * 子进程禁止使用,请使用旧的,原因:Mode_Private,主进程数据和子进程互不相知
	 * @param context
	 * @return
	 */
	public static synchronized PrivatePreference getPreference(Context context) {
		if (sInstance == null) {
			sInstance = new PrivatePreference(context);
		}
		return sInstance;
	}

	public boolean getBoolean(String key, boolean defValue) {
		return mPrefImpl.getSp().getBoolean(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return mPrefImpl.getSp().getFloat(key, defValue);
	}

	public int getInt(String key, int defValue) {
		return mPrefImpl.getSp().getInt(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return mPrefImpl.getSp().getLong(key, defValue);
	}

	public String getString(String key, String defValue) {
		return mPrefImpl.getSp().getString(key, defValue);
	}

	public void putBoolean(String key, boolean b) {
		Editor editor = mPrefImpl.getEditor();
		editor.putBoolean(key, b);
		putSafely(mPrefImpl);
	}

	public void putInt(String key, int i) {
		Editor editor = mPrefImpl.getEditor();
		editor.putInt(key, i);
		putSafely(mPrefImpl);
	}

	public void putFloat(String key, float f) {
		Editor editor = mPrefImpl.getEditor();
		editor.putFloat(key, f);
		putSafely(mPrefImpl);
	}

	public void putLong(String key, long l) {
		Editor editor = mPrefImpl.getEditor();
		editor.putLong(key, l);
		putSafely(mPrefImpl);
	}

	public void putString(String key, String s) {
		Editor editor = mPrefImpl.getEditor();
		editor.putString(key, s);
		putSafely(mPrefImpl);
	}

	public void remove(String key) {
		Editor editor = mPrefImpl.getEditor();
		editor.remove(key);
		putSafely(mPrefImpl);
	}

	public boolean contains(String key) {
		return mPrefImpl.getSp().contains(key);
	}

	public void commit() {
		synchronized (MUTEX) {
			for (SinglePreference pref : mCommitEdits) {
				pref.getEditor().apply();
			}
			mCommitEdits.clear();
		}
	}

	public void commitSync() {
		synchronized (MUTEX) {
			for (SinglePreference pref : mCommitEdits) {
				pref.getEditor().commit();
			}
			mCommitEdits.clear();
		}
	}

	private void putSafely(SinglePreference singlePreference) {
		synchronized (MUTEX) {
			mCommitEdits.add(singlePreference);
		}
	}

	
	/**
	 * 
	 * @author guoyiqing
	 * 
	 */
	class SinglePreference {

		private FaceSharedPreferences mSp;
		private Editor mEditor;
		private SharedPreferences mSpImpl;
		private Editor mEditorImpl;
		private String mName;


		public SinglePreference(Context context, String name) {
			mName = name;
			mSp = FaceSharedPreferences.getSharedPreferences(context, name, Context.MODE_PRIVATE);
			mEditor = mSp.edit();

			mSpImpl = context.getSharedPreferences(name, Context.MODE_PRIVATE);
			mEditorImpl = mSpImpl.edit();
		}

		public SharedPreferences getSp() {
			Application app = FaceAppState.getApplication();
			if (app instanceof IApplication) {
				if (((IApplication) app).isMainProcess()) {
					return mSpImpl;
				}
			}
			return mSp;
		}

		public Editor getEditor() {
			Application app = FaceAppState.getApplication();
			if (app instanceof IApplication) {
				if (((IApplication) app).isMainProcess()) {
					return mEditorImpl;
				}
			}
			return mEditor;
		}

		public String getName() {
			return mName;
		}

		@Override
		public int hashCode() {
			return mName.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof SinglePreference) {
				return mName.equals(((SinglePreference) o).getName());
			}
			return false;
		}

	}

}
