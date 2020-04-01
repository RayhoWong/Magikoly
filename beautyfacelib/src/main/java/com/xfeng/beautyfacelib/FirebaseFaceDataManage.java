package com.xfeng.beautyfacelib;

import com.xfeng.beautyfacelib.bean.EyesInfo;


/**
 * Created by xfengimacgomo
 * data 2019-06-13 11:48
 * email xfengv@yeah.net
 * 人脸识别数据保持,仅作测试用
 */
public class FirebaseFaceDataManage {
    private volatile static FirebaseFaceDataManage firebaseFaceDataManage;
    EyesInfo mEyesInfo;
    private FirebaseFaceDataManage() {
    }

    public static FirebaseFaceDataManage getInstance() {
        if (firebaseFaceDataManage == null) {
            synchronized (FirebaseFaceDataManage.class) {
                if (firebaseFaceDataManage == null) {
                    firebaseFaceDataManage = new FirebaseFaceDataManage();
                }
            }
        }
        return firebaseFaceDataManage;
    }

    public void setEyesInfoData(EyesInfo eyesInfo){
        this.mEyesInfo=eyesInfo;
    }

    public EyesInfo getEyesInfoData(){
        return mEyesInfo;
    }
}
