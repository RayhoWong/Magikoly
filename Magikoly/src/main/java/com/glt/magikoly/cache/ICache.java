/*
 * 文 件 名:  ICacheTask.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liguoliang
 * 修改时间:  2012-9-25
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.glt.magikoly.cache;

import java.util.List;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liguoliang
 * @date  [2012-9-25]
 */
public interface ICache {

	void saveCache(String key, byte[] cacheData);

	void saveCacheAsync(String key, byte[] cacheData, com.glt.magikoly.cache.ICacheListener listener);

	byte[] loadCache(String key);

	void loadCacheAsync(String key, com.glt.magikoly.cache.ICacheListener listener);

	void clearCache(String key);

	void clearCache(List<String> keyList);

	boolean isCacheExist(String key);
}
