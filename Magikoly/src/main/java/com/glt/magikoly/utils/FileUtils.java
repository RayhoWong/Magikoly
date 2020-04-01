package com.glt.magikoly.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class FileUtils {

	private static final int BUFFER_LENGTH = 2097152;

	public static void copy(String src, String des, FilenameFilter filter) {
		File file1 = new File(src);
		File[] fs = file1.listFiles(filter);
		File file2 = new File(des);
		if (!file2.exists()) {
			file2.mkdirs();
		}
		if (fs != null) {
			for (File f : fs) {
				if (f.isFile()) {
					String destName = f.getName();
					if (destName.endsWith(".9.jpg")) {
						destName = destName.replace(".9.jpg", ".9.png");
					}
					fileCopy(f.getPath(), des + File.separator + destName); // 调用文件拷贝的方法
				} else if (f.isDirectory()) {
					copy(f.getPath(), des + File.separator + f.getName(), filter);
				}
			}
		}
	}

	public static void copyFolder(String srcStr, String decStr) {
		File srcFile = new File(srcStr);
		File destFile = new File(decStr);
		if (!srcFile.exists()) {
			srcFile.mkdirs();
		}
		if (!destFile.exists()) {
			destFile.mkdirs();
		}
		File[] files = srcFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				copyFolder(file.getAbsolutePath(), decStr + "/" + file.getName());
			} else {
				copyFile(file.getAbsolutePath(), decStr + "/" + file.getName());
			}
		}
	}

	public static void copyFile(String srcStr, String decStr) {
		// 前提
		File srcFile = new File(srcStr);
		if (!srcFile.exists()) {
			return;
		}
		File decFile = new File(decStr);
		if (!decFile.exists()) {
			File parent = decFile.getParentFile();
			parent.mkdirs();
		} else {
			decFile.delete();
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(srcFile);
			output = new FileOutputStream(decFile);
			byte[] data = new byte[4 * 1024]; // 4k
			while (true) {
				int len = input.read(data);
				if (len <= 0) {
					break;
				}
				output.write(data, 0, len);
			}
			Logcat.d("xiaowu_copy", "srcStr: " + srcStr +  " decStr:" + decStr);
		} catch (Exception e) {
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
			if (null != output) {
				try {
					output.flush();
					output.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	public static void fileCopy(String f1, String f2) {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			File destFile = new File(f2);
			if (!destFile.exists()) {
				destFile.createNewFile();
			}
			in = new FileInputStream(f1);
			out = new FileOutputStream(destFile);
			byte[] buffer = new byte[BUFFER_LENGTH];
			while (true) {
				int ins = in.read(buffer);
				if (ins == -1) {
					out.flush();
					break;
				} else {
					out.write(buffer, 0, ins);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String readString(File file, String charsetName) {
		byte[] buffer = readFile(file);
		if (buffer != null) {
			try {
				return new String(buffer, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static byte[] readFile(File file) {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			// size 为字串的长度 ，这里一次性读完
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void writeString(File file, String content) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void appendString(File file, String content) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.append(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean deleteFile(File file) {
		if (file.isDirectory()) {
			String[] children = file.list();
			if (children != null && children.length > 0) {
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteFile(new File(file, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		return file.delete();
	}

	public static void renameFile(String orignalPath, String destPath) {
		File origFile = new File(orignalPath);
		File renameFile = new File(destPath);
		origFile.renameTo(renameFile);
	}

	public static boolean isExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}

	public static boolean isValidFile(String origPath, String filePrefix) {
		if (TextUtils.isEmpty(origPath)) {
			return false;
		}
		File file = new File(origPath);
		return !file.getName().startsWith(filePrefix);
	}



	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {

				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				long idLong = 0;
				try {
					idLong = Long.valueOf(id);
				} catch (Exception e) {
					if ("content".equalsIgnoreCase(uri.getScheme())) {
						return getDataColumn(context, uri, null, null);
					}
				}
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), idLong);

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}


	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
}
