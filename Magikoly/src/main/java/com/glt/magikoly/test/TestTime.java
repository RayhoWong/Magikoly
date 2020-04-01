package com.glt.magikoly.test;

/**
 * AB:测试的有效时间段
 * @author wushuangshuang
 *
 */
public enum TestTime {
	/**
	 * 时间格式：2015-10-16 00:01:00
	 */
//////////////////////新用户时间表////////////////////////////////////////
	STARTTIME("2019-06-04 00:00:00"),
	FISTTIME("2019-07-04 23:59:59"),
	SECONDTIME("2019-07-04 23:59:00"),
	ENDTIME("2019-07-04 23:59:00"),

////////////////////老用户时间表/////////\///////////////////////////////
	OLDSTARTTIME("2017-01-10 23:59:00"),
	OLDFISTTIME("2017-01-26 23:59:00"),
	OLDSECONDTIME("2018-04-13 00:00:00"),
	OLDENDTIME("2018-04-24 23:59:00");
	
	
	
	
/////////////////////////////////////////////////////////////////////构造器和有效值/////////////////////////////////////////////////////////////////
	private String mTimeValue;
	
	TestTime(String time) {
		this.mTimeValue = time;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTimeValue() {
		return mTimeValue;
	}
}
