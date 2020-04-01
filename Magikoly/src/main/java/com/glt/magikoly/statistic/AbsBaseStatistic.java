package com.glt.magikoly.statistic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.cs.statistic.OnInsertDBListener;
import com.cs.statistic.StatisticsManager;
import com.cs.statistic.beans.OptionBean;

import java.util.HashMap;
import java.util.Map;

/**
 * GoLauncher统计基类
 * 
 * @author luozhiping
 * @date [2014-10-23]
 * 
 */
public abstract class AbsBaseStatistic {
	// LOG_TAG
	public static final String LOG_TAG = "AbsBaseStatistic";
	// 操作结果---操作成功
	public final static int OPERATE_SUCCESS = 1;
	// 操作结果---操作失败
	public final static int OPERATE_FAIL = 0;
	// 拼装上传参数分隔符
	protected static final String STATISTICS_DATA_SEPARATE_STRING = "||";
	// 保存统计数据的分割符
	protected static final String STATISTICS_DATA_SEPARATE_ITEM = "#";

	// 下载与安装成功的最大间隔时间(半小时)--暂时调整为24小时
    protected static final long DOWNLOAD_TO_INSTALL_MAX_INTERVAL_TIME = 24 * 60 * 60 * 1000;
	// SharedPreferences
    protected static SharedPreferences sSharedPreferences;
    // 保存激活应用信息Key前缀
    private static final String ACTIVATE_PREFIX_KEY = "ACTIVATE#";
    // 保存预安装应用信息Key前缀
    private static final String INSTALL_PREFIX_KEY = "INSTALL#";
    
    // 缓存预安装/卸载包信息key值
    public static final int FUN_ID = 0; //统计功能ID
    public static final int SENDER = FUN_ID + 1; //统计对象
    public static final int PACKAGENAME = SENDER + 1; // 应用包名
    public static final int OPTION_CODE = PACKAGENAME + 1; // 安装统计操作代码
    public static final int ENTRANCE = OPTION_CODE + 1; // 入口值
    public static final int TAB_CATEGORY = ENTRANCE + 1; // Tab分类
    public static final int POSITION = TAB_CATEGORY + 1; // 位置
    public static final int ASSOCIATED_OBJ = POSITION + 1; // 关联对象
    public static final int ADVERT_ID = ASSOCIATED_OBJ + 1; // 广告ID
    public static final int REMARK = ADVERT_ID + 1; // 备注信息
    public static final int DOWNLOAD_TIME = REMARK + 1; // 开始下载/卸载时间
    public static final int CALL_URL = DOWNLOAD_TIME + 1; //回调地址
    
	// 存放应用激活信息Map
    protected static Map<Integer, String> sMPreActiveMap = null;
    // 存放应用安装信息Map
    protected static Map<Integer, String> sMPreInstallMap = null;
    
	/**
	 * 最基本的上传接口
	 * 
	 * @param context
	 * @param logSequence
	 *            协议号
	 * @param funId
	 *            功能id
	 * @param data
	 *            不包括基础数据(在SDK中做了处理)的统计数据，详见各统计wiki页
	 */
	public static void uploadStatisticData(Context context, int logSequence, int funId, StringBuffer data) {
		// 上传统计数据
		StatisticsManager.getInstance(context).uploadStaticData(logSequence, funId, data.toString());
	}

	/**
	 * 带 ‘插入DB监听’ 的基本上传接口
	 * 
	 * @param insertDBListener
	 *            数据库操作监听
	 */
	public static void uploadStatisticDataForListener(Context context, int logId, int funId, StringBuffer data,
                                                      OnInsertDBListener insertDBListener) {
		StatisticsManager.getInstance(context).uploadStaticData(logId, funId, data.toString(), insertDBListener);
	}

	/**
	 * 带选项的上传接口
	 * 
	 * @param logId
	 *            协议号
	 * @param funId
	 *            功能号 buffer 拼装好的数据，格式见wiki,
	 *            http://wiki.3g.net.cn/pages/viewpage.action?pageId=11273639
	 * @param options
	 *            类型OptionBean<br>
	 *            可选选项： OPTION_INDEX_IMMEDIATELY_CARE_SWITCH
	 *            为是否立即上传的标识符(在开关限制下立即上传)，传入true or false;
	 *            {@link #getImmediatelyBean()}<br>
	 *            OPTION_INDEX_IMMEDIATELY_ANYWAY
	 *            为是否立即上传的标识符(不管开关，直接、立即上传)，传入true or false;
	 *            {@link #getImmediatelyAnyWayBean()}<br>
	 *            OPTION_INDEX_POSITION 为位置信息；类型为字符串，如"105.23,155.88";
	 *            {@link #getPositionBean(String)}<br>
	 *            OPTION_INDEX_ABTEST 为ABTest值；类型为字符串,如"A"
	 *            {@link #getABTestBean(String)} <br>
	 *            insertDBListener 插入DB的监听<br>
	 */
	public static void uploadStatisticDataForOption(Context context, int logId, int funId, StringBuffer data,
                                                    OnInsertDBListener insertDBListener, OptionBean... options) {
		StatisticsManager.getInstance(context).uploadStaticDataForOptions(logId, funId, data.toString(),
				insertDBListener, options);
	}

	/**
	 * 生成统计选项bean
	 * 
	 * @return 立即上传，受开关控制,也就是在有开启了开关的情况下才立即上传
	 */
	public static OptionBean getImmediatelyBean() {
		return new OptionBean(OptionBean.OPTION_INDEX_IMMEDIATELY_CARE_SWITCH, true);
	}

	/**
	 * 生成统计选项bean
	 * 
	 * @return 立即上传，不受开关控制，也就是不管开关有没有开启，都立即上传
	 */
	public static OptionBean getImmediatelyAnyWayBean() {
		return new OptionBean(OptionBean.OPTION_INDEX_IMMEDIATELY_ANYWAY, true);
	}

	/**
	 * 生成统计选项bean
	 * 
	 * @param position
	 *            位置信息
	 * @return 带位置信息的统计
	 */
	public static OptionBean getPositionBean(String position) {
		return new OptionBean(OptionBean.OPTION_INDEX_POSITION, position);
	}

	/**
	 * 生成统计选项bean
	 * 
	 * @param aOrB
	 *            ABTest信息,输入String "A" or "B"<br>
	 * @return 带ABTest的统计选项
	 */
	public static OptionBean getABTestBean(String aOrB) {
		return new OptionBean(OptionBean.OPTION_INDEX_ABTEST, aOrB);
	}

	//===================================================安装相关统计(start)==============================================
    
    /**
     * <br>功能简述: 保存安装信息
     * @param context
     * @param packageName
     * @param seqId
     * @param installStatisticData
     */
    public static void saveReadyInstallList(Context context,
                                            String packageName, String seqId, String installStatisticData) {
        // 保存数据
        getSharedPreferences(context);
        if (sSharedPreferences != null) {
            Editor editor = sSharedPreferences.edit();
            // 获取成功后删除该应用预安装信息.
            editor.putString(INSTALL_PREFIX_KEY + seqId + packageName,
                    installStatisticData);
            editor.commit();
        }
    }
    
    /**
     * 保存预安装信息
     * 
     * @param context
     * @param sender 统计对象Id
     * @param packageName 应用包名
     * @param optionCode 统计操作码
     * @param aId 广告ID
     * @param remark 备注信息
     * @param seqId 协议号
     */
    public static void saveReadyInstallList(Context context, String optionCode, String sender, String packageName,
                                            String aId, String remark, String seqId) {
        saveReadyInstallList(context, "", sender, packageName, optionCode, "", "", "", "", aId, remark, seqId, "");
    }
    /**
     * 保存预安装信息
     * 
     * @param context
     * @param funId 统计功能ID
     * @param sender 统计对象Id
     * @param packageName 应用包名
     * @param optionCode 统计操作码
     * @param entrance 入口
     * @param tabCategory Tab分类
     * @param position 位置
     * @param associatedObj 关联对象
     * @param aId 广告ID
     * @param remark 备注信息
     * @param seqId 协议号
     * @param callUrl 回调地址
     */
    public static void saveReadyInstallList(Context context, String funId, String sender, String packageName, String optionCode, String entrance, String tabCategory,
                                            String position, String associatedObj, String aId, String remark, String seqId, String callUrl) {
        if (TextUtils.isEmpty(optionCode) || TextUtils.isEmpty(packageName)) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        //安装统计操作代码
        sb.append(optionCode).append(STATISTICS_DATA_SEPARATE_ITEM);
        //统计对象
        sb.append(sender).append(STATISTICS_DATA_SEPARATE_ITEM);
        //应用包名
        sb.append(packageName).append(STATISTICS_DATA_SEPARATE_ITEM);
        //广告ID
        sb.append(aId).append(STATISTICS_DATA_SEPARATE_ITEM);
        //备注信息
        sb.append(remark).append(STATISTICS_DATA_SEPARATE_ITEM);
        // 进入下载页的时间
        sb.append(System.currentTimeMillis()).append(STATISTICS_DATA_SEPARATE_ITEM);
        //统计功能ID
        sb.append(funId).append(STATISTICS_DATA_SEPARATE_ITEM);
        //入口值
        sb.append(entrance).append(STATISTICS_DATA_SEPARATE_ITEM);
        //Tab分类
        sb.append(tabCategory).append(STATISTICS_DATA_SEPARATE_ITEM);
        //位置
        sb.append(position).append(STATISTICS_DATA_SEPARATE_ITEM);
        //关联对象
        sb.append(associatedObj).append(STATISTICS_DATA_SEPARATE_ITEM);
        //回调地址
        sb.append(callUrl);
        // 保存数据
        getSharedPreferences(context);
        if (sSharedPreferences != null) {
            Editor editor = sSharedPreferences.edit();
            // 获取成功后删除该应用预安装信息.
            editor.putString(INSTALL_PREFIX_KEY + seqId + packageName, sb.toString());
            editor.commit();
        }
    }

    /**
     * 获取安装信息
     * 
     * @param context
     * @param packageName
     *            应用包名
     * @param seqId
     *            协议号
     * @return
     */
    public static String[] getInstallData(Context context, String packageName, String seqId) {
        String[] datas = null;
        // 保存数据
        getSharedPreferences(context);
        if (sSharedPreferences != null) {
            String strData = sSharedPreferences.getString(INSTALL_PREFIX_KEY + seqId + packageName, "");
            if (!TextUtils.isEmpty(strData)) {
                datas = strData.split(STATISTICS_DATA_SEPARATE_ITEM);
                Editor editor = sSharedPreferences.edit();
                // 获取成功后删除该应用预安装信息.
                editor.putString(INSTALL_PREFIX_KEY + seqId + packageName, "");
                editor.commit();
            }

        }
        return datas;
    }

    /**
     * 是否是预安装状态
     * 
     * @param context
     * @param pkgName
     *            应用包名
     * @param seqId
     *            协议号
     * @return
     */
    public static boolean isPreInstallState(Context context, String pkgName, String seqId) {
        boolean flag = false;
        String[] data = getInstallData(context, pkgName, seqId);
        if (null != data && data.length > 1) {
            flag = true;
            // 清除缓存数据
            if (sMPreInstallMap == null) {
                sMPreInstallMap = new HashMap<Integer, String>();
            }
            sMPreInstallMap.clear();
            //安装统计操作代码
            sMPreInstallMap.put(OPTION_CODE, data[0]);
            //统计对象
            sMPreInstallMap.put(SENDER, data[1]);
            //应用包名
            sMPreInstallMap.put(PACKAGENAME, data.length > 2 ? data[2] : "");
            //广告ID
            sMPreInstallMap.put(ADVERT_ID, data.length > 3 ? data[3] : "");
            //备注信息
            sMPreInstallMap.put(REMARK, data.length > 4 ? data[4] : "");
            // 进入下载页的时间
            sMPreInstallMap.put(DOWNLOAD_TIME, data.length > 5 ? data[5] : "0");
            //统计功能ID
            sMPreInstallMap.put(FUN_ID, data.length > 6 ? data[6] : "");
            //入口值
            sMPreInstallMap.put(ENTRANCE, data.length > 7 ? data[7] : "");
            //Tab分类
            sMPreInstallMap.put(TAB_CATEGORY, data.length > 8 ? data[8] : "");
            //位置
            sMPreInstallMap.put(POSITION, data.length > 9 ? data[9] : "");
            //关联对象
            sMPreInstallMap.put(ASSOCIATED_OBJ, data.length > 10 ? data[10] : "");
            //回调地址
            sMPreInstallMap.put(CALL_URL, data.length > 11 ? data[11] : "");
        }
        return flag;
    }
    
    //===================================================安装相关统计(end)==============================================
    
    //===================================================激活相关统计(start)==============================================
    
    /**
     * 保存预安装信息
     * 
     * @param context
     * @param funId 统计功能ID
     * @param sender 统计对象Id
     * @param packageName 应用包名
     * @param optionCode 统计操作码
     * @param aId 广告ID
     * @param position 位置
     * @param remark 备注信息
     * @param seqId 协议号
     */
    public static void saveReadyActivateList(Context context, int funId, String sender, String packageName, String optionCode, String aId, String position, String remark, String seqId) {
        saveReadyActivateList(context, funId, sender, packageName, optionCode, position, aId, remark, seqId, "");
    }
    /**
     * 保存预安装信息
     * 
     * @param context
     * @param funId 统计功能ID
     * @param sender 统计对象Id
     * @param packageName 应用包名
     * @param optionCode 统计操作码
     * @param aId 广告ID
     * @param position 位置
     * @param remark 备注信息
     * @param seqId 协议号
     * @param callUrl 回调地址
     */
    public static void saveReadyActivateList(Context context, int funId, String sender, String packageName, String optionCode, String aId, String position, String remark, String seqId, String callUrl) {
        saveReadyActivateList(context, funId, sender, packageName, optionCode, "", "", position, "", aId, remark, seqId, "");
    }
    /**
     * 保存预安装信息
     * 
     * @param context
     * @param funId 统计功能ID
     * @param sender 统计对象Id
     * @param packageName 应用包名
     * @param optionCode 统计操作码
     * @param entrance 入口
     * @param tabCategory Tab分类
     * @param position 位置
     * @param associatedObj 关联对象
     * @param aId 广告ID
     * @param remark 备注信息
     * @param seqId 协议号
     * @param callUrl 回调地址
     */
    public static void saveReadyActivateList(Context context, int funId, String sender, String packageName, String optionCode, String entrance, String tabCategory,
                                             String position, String associatedObj, String aId, String remark, String seqId, String callUrl) {
        if (TextUtils.isEmpty(optionCode) || TextUtils.isEmpty(packageName)) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        //统计功能ID
        sb.append(funId).append(STATISTICS_DATA_SEPARATE_ITEM);
        //统计对象
        sb.append(sender).append(STATISTICS_DATA_SEPARATE_ITEM);
        //应用包名
        sb.append(packageName).append(STATISTICS_DATA_SEPARATE_ITEM);
        //安装统计操作代码
        sb.append(optionCode).append(STATISTICS_DATA_SEPARATE_ITEM);
        //入口
        sb.append(entrance).append(STATISTICS_DATA_SEPARATE_ITEM);
        //Tab分类
        sb.append(tabCategory).append(STATISTICS_DATA_SEPARATE_ITEM);
        //位置
        sb.append(position).append(STATISTICS_DATA_SEPARATE_ITEM);
        //关联对象
        sb.append(associatedObj).append(STATISTICS_DATA_SEPARATE_ITEM);
        //广告ID
        sb.append(aId).append(STATISTICS_DATA_SEPARATE_ITEM);
        //备注信息
        sb.append(remark).append(STATISTICS_DATA_SEPARATE_ITEM);
        //进入下载页的时间
        sb.append(System.currentTimeMillis()).append(STATISTICS_DATA_SEPARATE_ITEM);
        //回调地址
        sb.append(callUrl);
        //保存数据
        getSharedPreferences(context);
        if (sSharedPreferences != null) {
            Editor editor = sSharedPreferences.edit();
            editor.putString(ACTIVATE_PREFIX_KEY + seqId + packageName, sb.toString());
            editor.commit();
        }
    }
    
    /**
     * 获取安装信息
     * 
     * @param context
     * @param packageName
     *            应用包名
     * @param seqId
     *            协议号
     * @return
     */
    public static String[] getActivateData(Context context, String packageName, String seqId) {
        String[] datas = null;
        // 保存数据
        getSharedPreferences(context);
        if (sSharedPreferences != null) {
            String strData = sSharedPreferences.getString(ACTIVATE_PREFIX_KEY + seqId + packageName, "");
            if (!TextUtils.isEmpty(strData)) {
                datas = strData.split(STATISTICS_DATA_SEPARATE_ITEM);
                // 获取成功后删除该应用预安装信息.
                Editor editor = sSharedPreferences.edit();
                editor.putString(ACTIVATE_PREFIX_KEY + seqId + packageName, "");
                editor.commit();
            }

        }
        return datas;
    }

    /**
     * 是否是预安装状态
     * 
     * @param context
     * @param pkgName
     *            应用包名
     * @param seqId
     *            协议号
     * @return
     */
    public synchronized static boolean isPreActivateState(Context context, String pkgName, String seqId) {
        boolean flag = false;
        String[] data = getActivateData(context, pkgName, seqId);
        if (null != data && data.length > 1) {
            flag = true;
            // 清除缓存数据
            if (sMPreActiveMap == null) {
                sMPreActiveMap = new HashMap<Integer, String>();
            }
            sMPreActiveMap.clear();
            //统计功能ID
            sMPreActiveMap.put(FUN_ID, data[0]);
            //统计对象
            sMPreActiveMap.put(SENDER, data[1]);
            //应用包名
            sMPreActiveMap.put(PACKAGENAME, data.length > 2 ? data[2] : "");
            //安装统计操作代码
            sMPreActiveMap.put(OPTION_CODE, data.length > 3 ? data[3] : "");
            //入口值
            sMPreActiveMap.put(ENTRANCE, data.length > 4 ? data[4] : "");
            //Tab分类
            sMPreActiveMap.put(TAB_CATEGORY, data.length > 5 ? data[5] : "");
            //位置
            sMPreActiveMap.put(POSITION, data.length > 6 ? data[6] : "");
            //关联对象
            sMPreActiveMap.put(ASSOCIATED_OBJ, data.length > 7 ? data[7] : "");
            //广告ID
            sMPreActiveMap.put(ADVERT_ID, data.length > 8 ? data[8] : "");
            //备注信息
            sMPreActiveMap.put(REMARK, data.length > 9 ? data[9] : "");
            //开始下载时间
            sMPreActiveMap.put(DOWNLOAD_TIME, data.length > 10 ? data[10] : "0");
            //回调地址
            sMPreActiveMap.put(CALL_URL, data.length > 11 ? data[11] : "");
        }
        return flag;
    }
    
    //===================================================激活相关统计(End)==============================================
    /**
     * 获取PrivatePreference
     * 
     * @return
     */
    public synchronized static SharedPreferences getSharedPreferences(Context context) {
        if (sSharedPreferences == null) {
            // 获取PrivatePreference
            sSharedPreferences = context.getSharedPreferences("store_statistic_install_file", Context.MODE_MULTI_PROCESS);
        }
        return sSharedPreferences;
    }
}
