package com.glt.magikoly.cache.utils;

import com.cs.framework.util.FileUtils;
import com.glt.magikoly.cache.impl.BaseCacheImpl;
import com.glt.magikoly.cache.impl.SimpleFileCacheImpl;

import java.io.File;

public class RestoreUtil {
    public static BaseCacheImpl getCacheImpl(String oldPath, String newPath, String key) {
        File configDir = new File(newPath);
        File config = new File(configDir, key);
        if (!config.exists()) {
            if (!oldPath.endsWith(File.separator)) {
                oldPath += File.separator;
            }
            try {
                FileUtils.copyFile(oldPath + key, config.getAbsolutePath());
            }catch (Exception e) {
                //可能因为权限报错
                e.printStackTrace();
            }
        }
        return new SimpleFileCacheImpl(newPath);
    }
}
