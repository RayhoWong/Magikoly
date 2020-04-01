package com.glt.magikoly.cache.impl;

/**
 * 简单的文件缓存实现
 * 与FileCacheImpl相比，少了加密和压缩
 * @author wangzhuobin
 *
 */
public class SimpleFileCacheImpl extends FileCacheImpl {

	public SimpleFileCacheImpl(String filePath) {
		super(filePath);
		mCompress = null;
		mEncrypt = null;
	}

}
