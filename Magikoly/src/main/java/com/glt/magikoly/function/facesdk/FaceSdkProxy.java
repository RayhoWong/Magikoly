package com.glt.magikoly.function.facesdk;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;

import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.amazons3.KeyCreator;
import com.glt.magikoly.bean.S3ImageInfo;
import com.glt.magikoly.bean.net.DetectResponseBean;
import com.glt.magikoly.constants.ErrorCode;
import com.glt.magikoly.exception.FirebaseVisionException;
import com.glt.magikoly.function.FaceFunctionBean;
import com.glt.magikoly.function.FaceFunctionManager;
import com.glt.magikoly.utils.BitmapUtils;
import com.glt.magikoly.utils.WindowController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.tencent.bugly.crashreport.CrashReport;
import com.xfeng.beautyfacelib.FirebaseFaceDataManage;
import com.xfeng.beautyfacelib.bean.EyesInfo;
import com.xfeng.beautyfacelib.view.GLImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.glt.magikoly.constants.ErrorCode.FACE_DETECT_ERROR;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_0;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_180;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_270;
import static com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata.ROTATION_90;

public class FaceSdkProxy {

    public final static String TAG = "FaceSdkProxy";

    private static FirebaseVisionFaceDetector sFastDetector = null;
    private static FirebaseVisionFaceDetector sAllDetector = null;

    static {
        FirebaseVisionFaceDetectorOptions fastOptions =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setMinFaceSize(0.1f).build();
        FirebaseVisionFaceDetectorOptions allOptions =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .setMinFaceSize(0.1f).build();
        try {
            sFastDetector = FirebaseVision.getInstance().getVisionFaceDetector(fastOptions);
            sAllDetector = FirebaseVision.getInstance().getVisionFaceDetector(allOptions);
        } catch (Throwable t) {
            t.printStackTrace();
            CrashReport.postCatchedException(t);
        }
    }

    public static void detectImage(final boolean fast, final boolean checkFaceBounds, Bitmap bitmap,
            int rotation, final OnSuccessListener<List<FirebaseVisionFace>> successListener,
            final OnFailureListener failureListener) {
        rotation = rotation % 360;
        int rotateValue = ROTATION_0;
        switch (rotation) {
            case 0:
                rotateValue = ROTATION_0;
                break;
            case 90:
                rotateValue = ROTATION_90;
                break;
            case 180:
                rotateValue = ROTATION_180;
                break;
            case 270:
                rotateValue = ROTATION_270;
                break;
        }
        bitmap = BitmapUtils.fixBitmap(bitmap);
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(bitmap.getWidth())
                .setHeight(bitmap.getHeight())
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(rotateValue)
                .build();
        FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(
                BitmapUtils.getNV21Data(bitmap.getWidth(), bitmap.getHeight(), bitmap), metadata);
        final Rect imageBounds = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        FirebaseVisionFaceDetector detector = sFastDetector;
        if (!fast) {
            detector = sAllDetector;
        }
        if (detector == null) {
            failureListener.onFailure(new FirebaseVisionException());
        } else {
            detector.detectInImage(image).addOnSuccessListener(
                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionFace> faces) {
                            if (checkFaceBounds) {
                                ArrayList<FirebaseVisionFace> invalidFaces = new ArrayList<>();
                                for (FirebaseVisionFace face : faces) {
                                    fixFaceRect(face);
                                    if (!isFaceInImage(face.getBoundingBox(), imageBounds)) {
                                        invalidFaces.add(face);
                                    }
                                }
                                faces.removeAll(invalidFaces);
                            }
                            if (successListener != null) {
                                successListener.onSuccess(faces);
                            }
                        }
                    })
                    .addOnFailureListener(failureListener);
        }
    }

    public static void detectForFaceCrop(final String originalPath, Bitmap originBitmap,
            @NonNull final OnDetectResult onDetectResult) {
        final Bitmap targetBitmap = BitmapUtils
                .scaleForDisplay(originBitmap, WindowController.getScreenHeight(),
                        WindowController.getScreenWidth());
        detectImage(true, true, targetBitmap, 0, new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                if (faces == null || faces.isEmpty()) {
                    onDetectResult.onDetectFail(originalPath, FACE_DETECT_ERROR);
                    return;
                }
                if (faces.size() > 1) {
                    onDetectResult
                            .onDetectMultiFaces(originalPath, targetBitmap, faces, onDetectResult);
                } else {
                    final Rect rectWithHeadNew =
                            SharedUtil.adjustRect(targetBitmap, faces.get(0).getBoundingBox());
                    final Bitmap thumbnail = BitmapUtils.clipBitmap(targetBitmap, rectWithHeadNew);
                    final String absolutePath =
                            FaceEnv.InternalPath.getCacheInnerFilePath(FaceAppState.getContext(),
                                    FaceEnv.InternalPath.PHOTO_CROP_DIR +
                                            System.currentTimeMillis() + "");
                    boolean saveBitmap = BitmapUtils
                            .saveBitmap(thumbnail, absolutePath, Bitmap.CompressFormat.JPEG);
                    if (saveBitmap) {
                        FaceFunctionManager.INSTANCE
                                .detectFace(KeyCreator.TYPE_FACE_DETECT, new File(absolutePath),
                                        rectWithHeadNew.width(),
                                        rectWithHeadNew.height(),
                                        new FaceFunctionManager.IFaceDetectListener() {
                                            @Override
                                            public void onDetectSuccess(
                                                    @NotNull S3ImageInfo imageInfo,
                                                    @NotNull DetectResponseBean detectBean) {
                                                onDetectResult.onDetectSuccess(originalPath,
                                                        new FaceFunctionBean(targetBitmap,
                                                                thumbnail, absolutePath,
                                                                rectWithHeadNew, imageInfo,
                                                                detectBean.getFaceInfos().get(0)));
                                            }

                                            @Override
                                            public void onDetectFail(String errorCode) {
                                                onDetectResult.onDetectFail(originalPath,
                                                        FaceFunctionManager.INSTANCE
                                                                .convertErrorString(errorCode));
                                            }
                                        });

                    } else {
                        onDetectResult.onDetectFail(originalPath,
                                ErrorCode.AMAZON_UPLOAD_FAIL_FILE_NO_EXISTS);
                    }
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onDetectResult.onDetectFail(originalPath, FACE_DETECT_ERROR);
            }
        });
    }

    /**
     * 获取瘦脸大眼所需要的数据
     *
     * @param bitmap
     * @param glImageView
     */
    public static void detectFaceEyeInfo(Bitmap bitmap, final GLImageView glImageView, final OnDetectFinish onDetectFinish) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        detectImage(false, false, bitmap, 0, new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> faces) {
                if (faces.isEmpty()) {
                    return;
                }

                EyesInfo eyesInfo = new EyesInfo();
                FirebaseVisionFace firebaseVisionFace = faces.get(0);

                FirebaseVisionFaceContour leftEyeContour = firebaseVisionFace.getContour(FirebaseVisionFaceContour.LEFT_EYE);
                if (leftEyeContour != null && leftEyeContour.getPoints().size() > 0) {
                    ArrayList<EyesInfo.Position> positions = new ArrayList<>();
                    for (int i = 0; i < leftEyeContour.getPoints().size(); i++) {
                        FirebaseVisionPoint firebaseVisionPoint = leftEyeContour.getPoints().get(i);
                        positions.add(new EyesInfo.Position(firebaseVisionPoint.getX(), firebaseVisionPoint.getY()));
                        eyesInfo.setLeftEyesPoint(positions);
                    }
                }

                FirebaseVisionFaceContour rightEyeContour = firebaseVisionFace.getContour(FirebaseVisionFaceContour.RIGHT_EYE);
                if (rightEyeContour != null && rightEyeContour.getPoints().size() > 0) {
                    ArrayList<EyesInfo.Position> positions = new ArrayList<>();
                    for (int i = 0; i < rightEyeContour.getPoints().size(); i++) {
                        FirebaseVisionPoint firebaseVisionPoint = rightEyeContour.getPoints().get(i);
                        positions.add(new EyesInfo.Position(firebaseVisionPoint.getX(), firebaseVisionPoint.getY()));
                        eyesInfo.setRightEyesPoint(positions);
                    }
                }

                FirebaseVisionFaceLandmark leftEyeLandmark =
                        firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                if (leftEyeLandmark != null) {
                    EyesInfo.Position leftPosition =
                            new EyesInfo.Position(leftEyeLandmark.getPosition().getX(),
                                    leftEyeLandmark.getPosition().getY());
                    eyesInfo.setLeftEyesPosition(leftPosition);
                }

                FirebaseVisionFaceLandmark rightEyeLandmark =
                        firebaseVisionFace.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                if (rightEyeLandmark != null) {
                    EyesInfo.Position rightPosition =
                            new EyesInfo.Position(rightEyeLandmark.getPosition().getX(),
                                    rightEyeLandmark.getPosition().getY());
                    eyesInfo.setRightEyesPosition(rightPosition);
                }

                //人脸
                FirebaseVisionFaceContour faceContour =
                        firebaseVisionFace.getContour(FirebaseVisionFaceContour.FACE);
                if (faceContour != null) {
                    if (faceContour.getPoints().size() > 0) {
                        FloatBuffer floatBuffer = FloatBuffer.allocate(14);
                        for (int i = 19; i < 26; i++) {
                            FirebaseVisionPoint firebaseVisionPoint =
                                    faceContour.getPoints().get(i);
                            floatBuffer.put(firebaseVisionPoint.getX() / width);
                            floatBuffer.put(1 - firebaseVisionPoint.getY() / height);
                        }
                        eyesInfo.setLeftFace(floatBuffer.array());
                    }
                    if (faceContour.getPoints().size() > 0) {
                        FloatBuffer floatBuffer = FloatBuffer.allocate(14);
                        for (int i = 10; i < 17; i++) {
                            FirebaseVisionPoint firebaseVisionPoint =
                                    faceContour.getPoints().get(i);
                            floatBuffer.put(firebaseVisionPoint.getX() / width);
                            floatBuffer.put(1 - firebaseVisionPoint.getY() / height);
                        }
                        eyesInfo.setRightFace(floatBuffer.array());
                        float[] deltaArray = new float[7];

                        //todo 控制点,注意:针对脸型的不同点设置(比如脸蛋和下巴)
                        for (int i = 0; i < 7; i++) {
                            deltaArray[i] = 0.5f;
                        }
                        eyesInfo.setDeltaArray(deltaArray);
                    }
                }
                FirebaseFaceDataManage.getInstance().setEyesInfoData(eyesInfo);

                glImageView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        glImageView.requestRender();
                        onDetectFinish.onFinished(true);
                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                        onDetectFinish.onFinished(false);
            }
        });
    }

    private static void fixFaceRect(FirebaseVisionFace face) {
        Rect faceRect = face.getBoundingBox();
        int widthOffset = (int)(faceRect.width() / 11f);
        int heightOffset = (int)(faceRect.height() / 11f);
        int left = faceRect.left + widthOffset;
        int right = faceRect.right - widthOffset;
        int bottom = faceRect.bottom + heightOffset;
        faceRect.set(left, faceRect.top, right, bottom);
    }

    private static boolean isFaceInImage(Rect faceBounds, Rect imageBounds) {
        return imageBounds.contains(faceBounds);
    }

    public interface OnDetectResult {
        void onDetectSuccess(String originalPath, FaceFunctionBean faceFunctionBean);

        void onDetectMultiFaces(@NonNull String originalPath, @NonNull Bitmap originBitmap,
                @NonNull List<FirebaseVisionFace> faces, @NonNull OnDetectResult onDetectResult);

        void onDetectFail(String originalPath, int errorCode);
    }

    public interface OnDetectFinish{
        void onFinished(boolean success);
    }
}
