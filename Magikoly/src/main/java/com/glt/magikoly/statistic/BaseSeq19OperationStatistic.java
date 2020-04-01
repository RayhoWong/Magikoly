package com.glt.magikoly.statistic;

import com.cs.statistic.StatisticsManager;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.ProductionInfo;
import com.glt.magikoly.test.ABTest;
import com.glt.magikoly.version.VersionController;

/**
 * @author kingyang
 */
public class BaseSeq19OperationStatistic {

    public static boolean sHasUploadNew;

    public static void uploadBasicInfo() {
        boolean isNew = false;
        if (VersionController.isFirstRun() && !sHasUploadNew) {
            isNew = true;
            sHasUploadNew = true;
        }
        StatisticsManager.getInstance(FaceAppState.getContext())
                .upLoadBasicInfoStaticData(String.valueOf(ProductionInfo.STATISTIC_19_CID), FaceEnv.sChannelId, false, false,
                        ABTest.getInstance().getUser(), isNew, null);
    }
}
