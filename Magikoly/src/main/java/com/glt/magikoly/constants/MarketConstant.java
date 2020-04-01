package com.glt.magikoly.constants;

/**
 * 
 * @author yangguanxiang
 *
 */
public interface MarketConstant {
	String PACKAGE = "com.android.vending";

	// 用包名搜索market上的软件
	String BY_PKGNAME = "market://search?q=pname:";

	// 进入软件详细页面
	String APP_DETAIL = "market://details?id=";

	// 浏览器版本的电子市场详情地址
	String BROWSER_APP_DETAIL = "https://play.google.com/store/apps/details?id=";
}
