package com.glt.magikoly.function.resultreport.baby;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.glt.magikoly.bean.net.BabyResponseBean;
import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.bean.FaceRectangle;
import com.glt.magikoly.constants.ErrorCode;
import com.glt.magikoly.event.PredictionResultSaveEvent;
import com.glt.magikoly.function.FaceFunctionBean;
import com.glt.magikoly.function.FaceFunctionManager;
import com.glt.magikoly.function.ReportNames;
import com.glt.magikoly.mvp.AbsPresenter;
import com.glt.magikoly.net.RequestCallback;
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic;
import com.glt.magikoly.statistic.Statistic103Constant;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.BitmapUtils;
import com.glt.magikoly.utils.Logcat;
import com.glt.magikoly.view.RoundedImageView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import magikoly.magiccamera.R;

/**
 * @desc:
 * @auther:duwei
 * @date:2019/4/18
 */
public class BabyReportPresenter extends AbsPresenter<BabyReportView> {

    private long startTime = 0L;

    public void startAnalysis(final FaceFunctionBean left, final FaceFunctionBean right) {
        if (left == null || right == null) {
            return;
        }
        FaceFunctionBean mother = left.getFaceInfo().getGender().equals("F") ? left : right;
        FaceFunctionBean father = left.getFaceInfo().getGender().equals("M") ? left : right;
//        if(mother == father){
//
//        }
        startTime = System.currentTimeMillis();
        FaceFunctionManager.INSTANCE.babyPrediction(
                "F", left.faceInfo.getEthnicity(),
                mother.getImageInfo(),
                new FaceRectangle(mother.faceInfo.getLeft(),
                        mother.faceInfo.getTop(),
                        mother.faceInfo.getWidth(),
                        mother.faceInfo.getHeight()),
                father.getImageInfo(),
                new FaceRectangle(father.faceInfo.getLeft(),
                        father.faceInfo.getTop(),
                        father.faceInfo.getWidth(),
                        father.faceInfo.getHeight()),
                false, new RequestCallback<BabyResponseBean>() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Logcat.d("wdw-baby", "宝宝预测：返回错误");
                        if (getView() != null) {
                            getView().showErrorDialog(ErrorCode.NETWORK_ERROR);
                        }
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                                (System.currentTimeMillis() - startTime) / 1000f) + "",
                                Statistic103Constant.FUNCTION_ACHIEVE,
                                Statistic103Constant.ENTRANCE_BABY, "2", "");
                    }

                    @Override
                    public void onResponse(BabyResponseBean response) {
                        if (response.getBabyReport() != null) {
                            String babyUrl = response.getBabyReport().getBabyImageUrl();
                            if (!TextUtils.isEmpty(babyUrl) && getView() != null) {
                                getView().showBabyImage(babyUrl);
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                                        (System.currentTimeMillis() - startTime) / 1000f) + "",
                                        Statistic103Constant.FUNCTION_ACHIEVE,
                                        Statistic103Constant.ENTRANCE_BABY, "1", "");
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                                        Statistic103Constant.PARTNER_UPLOADED, "",
                                        right.getCategory());
                                return;
                            }
                        } else {
                            String errorMsg = response.getStatusResult().getStatusCode();
                            int errorCode = -1;
                            switch (errorMsg) {
                                case ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE_STR:
                                    errorCode = ErrorCode.THIRD_PART_PROVIDER_UNAVAILABLE;
                                    break;
                                case ErrorCode.TEMPLATE_NOT_FOUND_STR:
                                    errorCode = ErrorCode.TEMPLATE_NOT_FOUND;
                                    break;
                                default:
                                    break;
                            }
                            if (getView() != null) {
                                getView().showErrorDialog(errorCode);
                                BaseSeq103OperationStatistic.uploadSqe103StatisticData(Math.round(
                                        (System.currentTimeMillis() - startTime) / 1000f) + "",
                                        Statistic103Constant.FUNCTION_ACHIEVE,
                                        Statistic103Constant.ENTRANCE_BABY, "2", "");
                            }

                        }
                    }
                });
    }


    public void saveBabyReport(Bitmap father, Bitmap mother, Bitmap baby) {
        final Context context = FaceAppState.getContext();
        final ConstraintLayout posterView = (ConstraintLayout) LayoutInflater.from(context)
                .inflate(R.layout.face_poster_container_baby, null);

        RoundedImageView parentLeft = posterView.findViewById(R.id.poster_original_image_left);
        RoundedImageView parentRight = posterView.findViewById(R.id.poster_original_image_right);
        ImageView result = posterView.findViewById(R.id.poster_result_image);
//        posterView.setGPColor(getGPColor());
//        posterView.setPosterBackground(background);
//        posterView.setPosterTitle(posterTitle);
//        posterView.setPosterDesc(posterDesc);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        parentLeft.setImageBitmap(father);
        parentRight.setImageBitmap(mother);
//        posterView.setOriginalImage(BitmapUtils.getShadowBitmap(originalImage,
//                BitmapFactory.decodeResource(FaceAppState.getContext().getResources(), R.drawable.poster_circle_mask, options),
//                BitmapFactory.decodeResource(FaceAppState.getContext().getResources(), R.drawable.poster_circle_shadow, options)));

        result.setImageBitmap(BitmapUtils.getShadowBitmap(baby,
                BitmapFactory.decodeResource(FaceAppState.getContext().getResources(),
                        R.drawable.poster_square_mask, options),
                BitmapFactory.decodeResource(FaceAppState.getContext().getResources(),
                        R.drawable.poster_square, options), ImageView.ScaleType.CENTER_CROP));

        int widthSpec = View.MeasureSpec.makeMeasureSpec(
                FaceAppState.getContext().getResources().getDimensionPixelSize(R.dimen.face_report_poster_width),
                View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(
                FaceAppState.getContext().getResources().getDimensionPixelSize(R.dimen.face_report_poster_height),
                View.MeasureSpec.EXACTLY);

        posterView.measure(widthSpec, heightSpec);
        posterView.layout(0, 0, posterView.getMeasuredWidth(), posterView.getMeasuredHeight());
        FaceThreadExecutorProxy.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap poster = BitmapUtils.createBitmap(posterView, 1f);
                String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_PICTURES;
                final String path = basePath + File.separator + ReportNames.AGING_REPORT_PREFIX + System.currentTimeMillis() + ".jpg";
                final boolean success = BitmapUtils.saveBitmap(poster, path, Bitmap.CompressFormat.JPEG);
                FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
                        }
                        //保存成功，评分
                        EventBus.getDefault().post(new PredictionResultSaveEvent(PredictionResultSaveEvent.FROM_BABY, success, true));
                    }
                });
            }
        });


    }
}
