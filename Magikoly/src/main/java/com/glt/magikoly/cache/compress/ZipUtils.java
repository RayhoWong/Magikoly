package com.glt.magikoly.cache.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liguoliang
 * @date  [2012-10-12]
 */
public class ZipUtils {
	private static final int sLENGTH = 256;
	public static byte[] gzip(byte[] bs) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
		GZIPOutputStream gzout = null;
		try {
			gzout = new GZIPOutputStream(bout);
			gzout.write(bs);
			gzout.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (gzout != null) {
				try {
					gzout.close();
				} catch (Exception ex) {
				}
			}
		}
		return bout.toByteArray();

	}

	public static byte[] ungzip(byte[] bs) throws Exception {
		GZIPInputStream gzin = null;
		ByteArrayInputStream bin = null;
		try {
			bin = new ByteArrayInputStream(bs);
			gzin = new GZIPInputStream(bin);
			return toByteArray(gzin);
		} catch (Exception e) {
			throw e;
		} finally {
			if (bin != null) {
				bin.close();
			}
			if (gzin != null) {
				gzin.close();
			}
		}
	}
	
	public static String unzip(InputStream inStream) {
		try {
			byte[] old_bytes = toByteArray(inStream);
			// 统计下载速度 old_bytes/time2
			byte[] new_bytes = ungzip(old_bytes);
			return new String(new_bytes, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}


	public static byte[] toByteArray(InputStream input) throws Exception {
		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			copy(input, output);
			return output.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public static int copy(InputStream input, OutputStream output) throws Exception {
		try {
			byte[] buffer = new byte[1024 * 4];
			int count = 0;
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
			return count;
		} catch (Exception e) {
			throw e;
		}
	}

	public static byte[] zip(byte[] bs) throws Exception {

		ByteArrayOutputStream o = null;
		try {
			o = new ByteArrayOutputStream();
			Deflater compresser = new Deflater();
			compresser.setInput(bs);
			compresser.finish();
			byte[] output = new byte[1024];
			while (!compresser.finished()) {
				int got = compresser.deflate(output);
				o.write(output, 0, got);
			}
			o.flush();
			return o.toByteArray();
		} catch (Exception ex) {
			throw ex;

		} finally {
			if (o != null) {
				try {
					o.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}

	}

	public static byte[] unzip(byte[] bs) throws Exception {
		ByteArrayOutputStream o = null;
		try {
			o = new ByteArrayOutputStream();
			Inflater decompresser = new Inflater();
			decompresser.setInput(bs);
			byte[] result = new byte[1024];
			while (!decompresser.finished()) {
				int resultLength = decompresser.inflate(result);
				o.write(result, 0, resultLength);
			}
			decompresser.end();
			o.flush();
			return o.toByteArray();
		} catch (Exception ex) {
			throw ex;

		} finally {
			if (o != null) {
				try {
					o.close();
				} catch (IOException e) {
					throw e;
				}
			}
		}
	}
	/**
	 * 压缩字符串
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		String result = out.toString();
		out.close();
		return result;
	}
	/**
	 * 
	 * @param str
	 * @param encode 编码方式，如utf-8、ISO-8859-1、GBK
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str, String encode) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		String result = out.toString(encode);
		out.close();
		return result;
	}
	/**
	 * 
	 * @param str
	 * @param encode 编码方式，如utf-8、ISO-8859-1、GBK
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str, String stringToBytesEncode, String bytesToStringEncode) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes(stringToBytesEncode));
		gzip.close();
		String result = out.toString(bytesToStringEncode);
		out.close();
		return result;
	}
	/**
	 * 解压缩字符串
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String unCompress(String str) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[sLENGTH];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		String result = out.toString();
		out.close();
		return result;
	}
	/**
	 * 
	 * @param str
	 * @param unCompressEncode 解码的编码方式
	 * @return
	 * @throws IOException
	 */
	public static String unCompress(String str, String unCompressEncode) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes(unCompressEncode));
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[sLENGTH];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		String result = out.toString();
		out.close();
		return result;
	}
	/**
	 * 
	 * @param str
	 * @param unCompressEncode 解码的编码方式
	 * @param stringEncode 返回字符串的编码方式
	 * @return
	 * @throws IOException
	 */
	public static String unCompress(String str, String unCompressEncode, String stringEncode) throws IOException {
		if (str == null) {
			return str = "";
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes(unCompressEncode));
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[sLENGTH];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		String result = out.toString(stringEncode);
		try {
			out.close();
			gunzip.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
