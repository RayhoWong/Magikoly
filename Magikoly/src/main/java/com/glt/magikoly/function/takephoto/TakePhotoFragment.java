package com.glt.magikoly.function.takephoto;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.glt.magikoly.FaceAppState;
import com.glt.magikoly.FaceEnv;
import com.glt.magikoly.MagikolyActivity;
import com.glt.magikoly.dialog.TipsDialog;
import com.glt.magikoly.event.CameraTransitionEvent;
import com.glt.magikoly.event.ChangeTabEvent;
import com.glt.magikoly.function.main.MainFragment;
import com.glt.magikoly.function.views.FaceCommonToolBar;
import com.glt.magikoly.mvp.BaseSupportFragment;
import com.glt.magikoly.permission.OnPermissionResult;
import com.glt.magikoly.permission.PermissionHelper;
import com.glt.magikoly.permission.PermissionSettingPage;
import com.glt.magikoly.pref.PrefConst;
import com.glt.magikoly.pref.PrivatePreference;
import com.glt.magikoly.statistic.BaseSeq103OperationStatistic;
import com.glt.magikoly.statistic.Statistic103Constant;
import com.glt.magikoly.thread.FaceThreadExecutorProxy;
import com.glt.magikoly.utils.BitmapUtils;
import com.glt.magikoly.utils.Machine;
import com.glt.magikoly.utils.RomCheckUtil;
import com.glt.magikoly.utils.ViewUtils;
import com.glt.magikoly.view.GlobalProgressBar;
import com.glt.magikoly.view.ProgressBarEvent;
import com.xfeng.beautyfacelib.Filter.camerafiter.Beauty;
import com.xfeng.beautyfacelib.Filter.camerafiter.LookupFilter;
import com.xfeng.cameralibrary.camera.FrameCallback;
import com.xfeng.cameralibrary.camera.Renderer;
import com.xfeng.cameralibrary.camera.TextureController;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import magikoly.magiccamera.R;
import me.yokeyword.fragmentation.SupportActivity;

import static android.content.Context.CAMERA_SERVICE;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW;
import static com.glt.magikoly.ext.BaseExtKt.postEvent;
import static com.glt.magikoly.ext.BaseExtKt.registerEventObserver;
import static com.glt.magikoly.ext.BaseExtKt.unregisterEventObserver;
import static com.glt.magikoly.statistic.Statistic103Constant.ENTRANCE_MAIN;
import static com.glt.magikoly.view.ProgressBarEvent.EVENT_HIDE;
import static com.xfeng.cameralibrary.camera.TextureController.DEFAULT_RENDER_HEIGHT;
import static com.xfeng.cameralibrary.camera.TextureController.DEFAULT_RENDER_WIDTH;

/**
 * @desc:
 * @auther:duwei
 * @date:2019/1/9
 */

public class TakePhotoFragment extends BaseSupportFragment<TakePhotoPresenter>
        implements View.OnClickListener, ITakePhoto, FrameCallback,
        TextureController.GLSurfaceListener {

    private FaceCommonToolBar mFaceCommonToolBar;
    private View mAlbum, mDiscovery, mTakePhoto;
    private TextView mDesc;
    private String startTag = "";
    private static final int REQUEST_CODE_PHOTO = 1;
    private static final String TAKE_PHOTO_SAVE_KEY_TAG = "TAKE_PHOTO_SAVE_KEY_TAG";
    private static boolean sIsPermissionCameraNever = false;
    private boolean isCameraSwitch;
    boolean visible = false;
    boolean hasPermission = false;
    private SurfaceView mSurfaceView;
    private TextureController mController;
    private Renderer mRenderer;
    private LookupFilter mLookupFilter;
    private Beauty mBeautyFilter;
    private int cameraId = 1;
    private String camera2Id = null;
    private static final int CAMERA_ONE = 1;
    private static final int CAMERA_TWO = 2;
    private int mCurrentSupportCamera = CAMERA_TWO;
    CameraManager mCameraManager;
    private Handler mHandler;
    private Camera mCamera;
    CameraDevice mDevice;
    private boolean mHasBeenInitCameraTwo = false;
    private boolean mIsFirstFrame = false;
    private long mStarTime;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState == null) {
            return;
        }

        outState.putString(TAKE_PHOTO_SAVE_KEY_TAG, startTag);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;

        }

        startTag = savedInstanceState.getString(TAKE_PHOTO_SAVE_KEY_TAG, "");
    }

    public static boolean startTakePhoto(final Activity activity, final String startTag,
                                         final OnPermissionResult callback) {
        if (PermissionHelper.hasCameraPermission(activity)) {
            TakePhotoFragment.startTakePhoto(startTag);
            sIsPermissionCameraNever = false;
            return true;
        } else {
            PermissionHelper.requestCameraPermission(activity, new OnPermissionResult() {

                @Override
                public void onPermissionDeny(String permission, boolean never) {
                    if (sIsPermissionCameraNever || PermissionHelper.isPermissionGroupDeny(
                            activity, permission)) {
                        showPermissionDenyNeverDialog(activity,
                                activity.getString(R.string.permission_tip_camera_never));
                    }
                    sIsPermissionCameraNever = never;
                    if (callback != null) {
                        callback.onPermissionDeny(permission, never);
                    }
                }

                @Override
                public void onPermissionGrant(String permission) {
                    TakePhotoFragment.startTakePhoto(startTag);
                    BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                            Statistic103Constant.CAMERA_PERMISSION_OBTAINED,
                            Statistic103Constant.ENTRANCE_CAMERA_CLICK);
                    sIsPermissionCameraNever = false;
                    if (callback != null) {
                        callback.onPermissionGrant(permission);
                    }
                }

            }, -1);
            BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                    Statistic103Constant.CAMERA_PERMISSION_REQUEST,
                    Statistic103Constant.ENTRANCE_CAMERA_CLICK);
        }
        return false;
    }

    private static void showPermissionDenyNeverDialog(final Activity activity, String content) {
        final TipsDialog dialog = new TipsDialog(activity);
        dialog.setContent(content);
        dialog.setupOKButton(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PermissionSettingPage.start(activity, false);
            }
        });
        dialog.show();
    }

    public static void startTakePhoto(String tag) {
        MagikolyActivity mainActivity = FaceAppState.getMainActivity();
        startTakePhoto(mainActivity, tag);
    }

    private static void startTakePhoto(SupportActivity activity, String tag) {
        if (activity != null && !activity.isFinishing()) {
            postEvent(new CameraTransitionEvent(CameraTransitionEvent.EVENT_SHOW));
            activity.start(newInstance(tag));
        }
    }

    public static TakePhotoFragment newInstance(String tag) {
        TakePhotoFragment takePhotoFragment = new TakePhotoFragment();
        takePhotoFragment.startTag = tag;
        return takePhotoFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        registerEventObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterEventObserver(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressBarEvent(ProgressBarEvent event) {
        if (event.getAction() == ProgressBarEvent.EVENT_CANCEL_BY_USER
                || event.getAction() == EVENT_HIDE) {
            if (visible) {
                restartCamera();
                if (mController != null) {
                    mController.onResume();
                }
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_takephoto, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //三星和魅族及5.0以下的需要选择camera1  如果crash多的话可以5.0及以下全部用camera2
            if (RomCheckUtil.checkIsMeizuRom() && Build.VERSION.SDK_INT <= 21) {
                mCurrentSupportCamera = CAMERA_ONE;
            } else {
                mCurrentSupportCamera = CAMERA_TWO;
            }
        } else {
            mCurrentSupportCamera = CAMERA_ONE;
        }
        mStarTime = System.currentTimeMillis();
        mTakePhoto = view.findViewById(R.id.activity_takephoto_take_);
        mTakePhoto.setOnClickListener(this);
        mAlbum = view.findViewById(R.id.activity_takephoto_album);
        mDiscovery = view.findViewById(R.id.activity_takephoto_discovery);
        mDesc = view.findViewById(R.id.activity_takephoto_desc);
        mDesc.setText(getString(R.string.takephoto_desc_ethnicity));
        mAlbum.setOnClickListener(this);
        mDiscovery.setOnClickListener(this);
        mFaceCommonToolBar = view.findViewById(R.id.face_common_toolbar);
        mFaceCommonToolBar.setMenuDrawable(getResources().getDrawable(R.drawable.icon_camera_rotate_selector));
        mSurfaceView = view.findViewById(R.id.mSurface);
        mController = new TextureController(getActivity(), this);
        onFilterSet(mController);
        mController.setFrameCallback(DEFAULT_RENDER_WIDTH, DEFAULT_RENDER_HEIGHT, this);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mController.surfaceCreated(holder);
                mController.setRenderer(mRenderer);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mController.surfaceChanged(width, height);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mController.surfaceDestroyed();
            }
        });


        PrivatePreference pre = PrivatePreference.getPreference(FaceAppState.getContext());
        boolean enable = pre.getBoolean(PrefConst.KEY_CAMERA_BEAUTY_WHETHER_ENABLE, true);
        if (enable) {
            mFaceCommonToolBar.setMenuLeftDrawable(getResources().getDrawable(R.drawable.icon_beauty_selected));
            mLookupFilter.setIntensity(0.5f);
            mBeautyFilter.setFlag(3);
        } else {
            mFaceCommonToolBar.setMenuLeftDrawable(getResources().getDrawable(R.drawable.icon_beauty_unselected));
            mLookupFilter.setIntensity(0.0f);
            mBeautyFilter.setFlag(0);

        }
        mFaceCommonToolBar.setTitle("");
        mFaceCommonToolBar.setOnTitleClickListener(new FaceCommonToolBar.OnTitleClickListener() {
            @Override
            public void onTitleClick(View view, boolean back) {
                if (back) {
//                    pop();
                    doBackPressedSupport();
                } else if (view.getId() == R.id.img_menu) {
                    changeCamera();

                } else if (view.getId() == R.id.img_menu_left) {
                    PrivatePreference pre = PrivatePreference.getPreference(FaceAppState.getContext());
                    boolean enable = pre.getBoolean(PrefConst.KEY_CAMERA_BEAUTY_WHETHER_ENABLE, true);
                    if (enable) {
                        mLookupFilter.setIntensity(0.0f);
                        mBeautyFilter.setFlag(0);
                        mFaceCommonToolBar.setMenuLeftDrawable(getResources().getDrawable(R.drawable.icon_beauty_unselected));
                        enable = false;

                    } else {
                        mLookupFilter.setIntensity(0.5f);
                        mBeautyFilter.setFlag(3);
                        mFaceCommonToolBar.setMenuLeftDrawable(getResources().getDrawable(R.drawable.icon_beauty_selected));
                        enable = true;
                    }
                    pre.putBoolean(PrefConst.KEY_CAMERA_BEAUTY_WHETHER_ENABLE, enable);
                    pre.commitSync();
                }
            }
        });

//        mPresenter.requestPermission(getActivity(), mFaceModel);
//        if (Statistic103Constant.ENTRANCE_MAIN.equals(startTag)) {
        mPresenter.uploadEnterStatistic(startTag);
//        } else if (Statistic103Constant.ENTRANCE_MAIN.equals(startTag)){
//            mPresenter.uploadEnterStatistic("1");
//        }

        visible = true;
    }

    private void checkPermission() {
        if (!PermissionHelper.hasCameraPermission(getContext())) {
            OnPermissionResult result = new OnPermissionResult() {
                @Override
                public void onPermissionGrant(String permission) {
                    hasPermission = true;
                    changeCameraWithSDKVersion();
                }

                @Override
                public void onPermissionDeny(String permission, boolean never) {
                    hasPermission = false;
                }
            };
            if (Build.MANUFACTURER.toLowerCase().contains("meizu") && !Machine.IS_SDK_ABOVE_6) {
                PermissionHelper.requestCameraPermission(getContext(), result, -1);
            } else {
                mPresenter.requestPermission(_mActivity, result);
            }
            FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    postEvent(new CameraTransitionEvent(CameraTransitionEvent.EVENT_HIDE));
                }
            }, 1000);
        } else {
            hasPermission = true;
            changeCameraWithSDKVersion();
        }
    }

    private void initCameraRenderer() {
        switch (mCurrentSupportCamera) {
            case CAMERA_ONE:
                mRenderer = new Camera1Renderer();
                break;
            case CAMERA_TWO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mRenderer = new Camera2Renderer();
                }
                break;
            default:
                break;
        }
    }

    protected void onFilterSet(TextureController controller) {
        mLookupFilter = new LookupFilter(getResources());
        mLookupFilter.setMaskImage("lookup/purity.png");
        mLookupFilter.setIntensity(0.3f);
        controller.addFilter(mLookupFilter);
        mBeautyFilter = new Beauty(getResources());
        mBeautyFilter.setFlag(2);
        controller.addFilter(mBeautyFilter);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mController != null) {
            mController.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mController != null) {
            mController.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.destroy();
        }
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        if (!visible) {
            if (PermissionHelper.hasCameraPermission(getContext())) {
                hasPermission = true;

                if (mRenderer != null) {
                    changeCameraWithSDKVersion();
                }
                if (mController != null) {
                    mController.onResume();
                }
            }
            visible = true;
        }
    }


    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
        if (visible) {
            if (mController != null) {
                mController.onPause();
            }
            closeCamera();
            visible = false;
            mHasBeenInitCameraTwo = false;
        }
    }

    private boolean isCameraOpened() {
        return mCamera != null || mDevice != null;
    }


    private void closeCamera() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doCloseCamera();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    doCloseCamera();
                }
            });
        }
    }

    private void doCloseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mDevice != null) {
            // 5.0以下的手机在进行类加载时当遇到CameraDevice.close()方法会抛出VerifyError，
            // 因此使用CameraDeviceCompat把这个方法封装
            CameraDeviceCompat.Companion.closeCameraDevice(mDevice);
            mDevice = null;
        }
    }

    @Override
    public void hideSelf() {
        pop();
    }

    @Override
    protected boolean doBackPressedSupport() {
        if (visible) {
            pop();
            return true;
        }
        return super.doBackPressedSupport();
    }

    @Override
    public void onClick(View view) {
        if (ViewUtils.INSTANCE.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.activity_takephoto_take_:
                if (mController != null) {
                    hasPermission = PermissionHelper.hasCameraPermission(getContext());
                    if (hasPermission) {
                        mController.onResume();
                        mController.takePhoto();
                    } else {
                        mPresenter.requestPermission(_mActivity, new OnPermissionResult() {
                            @Override
                            public void onPermissionGrant(String permission) {
                                hasPermission = true;
                                changeCameraWithSDKVersion();
                            }

                            @Override
                            public void onPermissionDeny(String permission, boolean never) {
                                hasPermission = false;
                            }
                        });

//                        PermissionHelper.requestCameraPermission(getContext(), new OnPermissionResult() {
//                            @Override
//                            public void onPermissionGrant(String permission) {
//
//                                mController.onResume();
//                                mController.takePhoto();
//                            }
//                            @Override
//                            public void onPermissionDeny(String permission, boolean never) {
//                            }
//                        }, -1);
                    }
                }

                mPresenter.uploadTakePhotoClick(cameraId == CAMERA_FACING_FRONT ? 1 : 2);

                PrivatePreference pre = PrivatePreference.getPreference(FaceAppState.getContext());
                boolean enable = pre.getBoolean(PrefConst.KEY_CAMERA_BEAUTY_WHETHER_ENABLE, true);
                BaseSeq103OperationStatistic.uploadSqe103StatisticData("",
                        Statistic103Constant.BEAUTY_CAM, "", enable ? "1" : "0");
                break;
            case R.id.activity_takephoto_album:
                pop();
                postEvent(new ChangeTabEvent(MainFragment.TAB_ALBUM, Statistic103Constant.ALBUM_ENTER_TAKE_PICTURE));
                break;
            case R.id.activity_takephoto_discovery:
                pop();
                postEvent(new ChangeTabEvent(MainFragment.TAB_DISCOVERY, Statistic103Constant.ALBUM_ENTER_TAKE_PICTURE));
                break;
        }
    }


    @Override
    public TakePhotoPresenter createPresenter() {
        return new TakePhotoPresenter();
    }


    //====================================================以下是相机处理逻辑====================================================
    @Override
    public void onFrame(final byte[] bytes, long time) {
        FaceThreadExecutorProxy.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = Bitmap.createBitmap(DEFAULT_RENDER_WIDTH, DEFAULT_RENDER_HEIGHT, Bitmap.Config.ARGB_8888);
                ByteBuffer b = ByteBuffer.wrap(bytes);
                bitmap.copyPixelsFromBuffer(b);


                closeCamera();
                mHasBeenInitCameraTwo = false;

                FaceThreadExecutorProxy.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        startScan(bitmap);
                    }
                });
            }
        });

    }

    public void startScan(Bitmap bitmap) {
        GlobalProgressBar.Companion.show(ENTRANCE_MAIN, true, false);

        final String absolutePath = FaceEnv.InternalPath
                .getCacheInnerFilePath(FaceAppState.getContext(),
                        FaceEnv.InternalPath.PHOTO_CROP_DIR + System.currentTimeMillis() +
                                "");

        boolean saveBitmap =
                BitmapUtils.saveBitmap(bitmap, absolutePath, Bitmap.CompressFormat.JPEG);
        if (saveBitmap) {
            mPresenter.onOkCrop(startTag, bitmap, absolutePath);
        }
    }


    @Override
    public void onGLSurfaceCreated() {
        post(new Runnable() {
            @Override
            public void run() {
                if (!isDetached()) {
                    initCameraRenderer();
                    checkPermission();
                }
            }
        });
    }

    private class Camera1Renderer implements Renderer {
        @Override
        public void onDestroy() {
            closeCamera();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }

    }

    /**
     * 尽量避免使用camera1
     */
    private void initCameraOne() {
        if (!hasPermission) {
            return;
        }
        if (mHasBeenInitCameraTwo) {
            return;
        }
        mHasBeenInitCameraTwo = true;
        if (isCameraOpened()) {
            closeCamera();
        }
        SurfaceTexture texture = mController.getTexture();
        mIsFirstFrame = true;
        try {
            mCamera = Camera.open(cameraId);
            mCamera.setDisplayOrientation(90);
            mController.setImageDirection(cameraId);
            List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size closestSize = findClosestSize(supportedPreviewSizes);
            mCamera.getParameters().setPreviewSize(closestSize.width, closestSize.height);
            mController.setDataSize(closestSize.height, closestSize.width);
            mCamera.setPreviewTexture(texture);
            texture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    mController.requestRender();
                    if (mIsFirstFrame) {
                        postEvent(new CameraTransitionEvent(
                                CameraTransitionEvent.EVENT_HIDE));
                        BaseSeq103OperationStatistic.uploadSqe103StatisticData(
                                (System.currentTimeMillis() - mStarTime) + "",
                                Statistic103Constant.BEAUTY_START);
                        mIsFirstFrame = false;
                    }
                }
            });
            mCamera.startPreview();
        } catch (Exception e) {
            closeCamera();
            if (_mActivity != null) {
                Toast.makeText(_mActivity,
                        _mActivity.getResources().getString(R.string.camera_error_tip),
                        Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
            pop();
            postEvent(new CameraTransitionEvent(CameraTransitionEvent.EVENT_HIDE, false));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class Camera2Renderer implements Renderer {

        Camera2Renderer() {
            mCameraManager = (CameraManager) FaceAppState.getContext().getSystemService(CAMERA_SERVICE);
            camera2Id = getFrontFacingCameraId();
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onDestroy() {
            closeCamera();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCameraTwo() {
        if (!hasPermission || mHasBeenInitCameraTwo) {
            return;
        }
        try {
            if (isCameraOpened()) {
                closeCamera();
            }
            mIsFirstFrame = true;
            if (getFrontFacingCameraId() == null) {
                onCamera2ErrorAndTryLaunchCamera1();
                return;
            }
            CameraCharacteristics c = mCameraManager.getCameraCharacteristics(camera2Id);
            String[] ids = mCameraManager.getCameraIdList();
            if (ids.length == 0) {
                if (_mActivity != null) {
                    Toast.makeText(_mActivity,
                            _mActivity.getResources().getString(R.string.camera_error_tip),
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            StreamConfigurationMap map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //自定义规则，选个大小
            final Size previewSize = findClosestSize(map.getOutputSizes(SurfaceHolder.class));
            mController.setDataSize(previewSize.getHeight(), previewSize.getWidth());

            mCameraManager.openCamera(camera2Id, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mDevice = camera;
                    try {
                        Surface surface = new Surface(mController
                                .getTexture());
                        final CaptureRequest.Builder builder = camera.createCaptureRequest
                                (TEMPLATE_PREVIEW);
                        builder.addTarget(surface);
                        mController.getTexture().setDefaultBufferSize(previewSize.getWidth(),
                                previewSize.getHeight());
                        camera.createCaptureSession(Arrays.asList(surface), new
                                CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(CameraCaptureSession session) {
                                        try {
                                            session.setRepeatingRequest(builder.build(),
                                                    new CameraCaptureSession.CaptureCallback() {
                                                        @Override
                                                        public void onCaptureProgressed(
                                                                CameraCaptureSession session,
                                                                CaptureRequest request,
                                                                CaptureResult partialResult) {
                                                        }

                                                        @Override
                                                        public void onCaptureCompleted(
                                                                CameraCaptureSession session,
                                                                CaptureRequest request,
                                                                TotalCaptureResult result) {
                                                            super.onCaptureCompleted(session,
                                                                    request, result);
                                                            mController.requestRender();
                                                            if (mIsFirstFrame) {
                                                                postEvent(new CameraTransitionEvent(
                                                                        CameraTransitionEvent.EVENT_HIDE));
                                                                BaseSeq103OperationStatistic
                                                                        .uploadSqe103StatisticData(
                                                                                (System.currentTimeMillis() -
                                                                                        mStarTime) +
                                                                                        "",
                                                                                Statistic103Constant.BEAUTY_START);
                                                                mIsFirstFrame = false;
                                                            }
                                                        }
                                                    }, mHandler);
                                        } catch (Exception e) {
                                            onCamera2ErrorAndTryLaunchCamera1();
                                        }
                                    }

                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession session) {

                                    }
                                }, mHandler);
                    } catch (CameraAccessException e) {
                        onCamera2ErrorAndTryLaunchCamera1();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    mDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    onCamera2ErrorAndTryLaunchCamera1();
                }
            }, mHandler);
        } catch (SecurityException | CameraAccessException e) {
            onCamera2ErrorAndTryLaunchCamera1();
            e.printStackTrace();
        }
        mHasBeenInitCameraTwo = true;
    }

    private void onCamera2ErrorAndTryLaunchCamera1() {
        closeCamera();
        mHasBeenInitCameraTwo = false;
        mCurrentSupportCamera = CAMERA_ONE;
        initCameraRenderer();
        changeCameraWithSDKVersion();
    }

    private void restartCamera() {
        closeCamera();
        mHasBeenInitCameraTwo = false;
        initCameraRenderer();
        changeCameraWithSDKVersion();
    }


    private Camera.Size findClosestSize(List<Camera.Size> sizes) {
        if (sizes == null) {
            return null;
        }
        float targetRatio = 1.0f * DEFAULT_RENDER_HEIGHT / DEFAULT_RENDER_WIDTH;
        int resultIndex = 0;
        float resultRatio = -1;
        for (int i = 0; i < sizes.size(); i++) {
            float longBound = Math.max(sizes.get(i).width, sizes.get(i).height);
            float shortBound = Math.min(sizes.get(i).width, sizes.get(i).height);
            float ratio = 1.0f * longBound / shortBound;
            if (resultRatio == -1 ||
                    Math.abs(ratio - targetRatio) < Math.abs(resultRatio - targetRatio)) {
                resultRatio = ratio;
                resultIndex = i;
            } else if (ratio == resultRatio) {
                float resultLongBound =
                        Math.max(sizes.get(resultIndex).width, sizes.get(resultIndex).height);
                if (Math.abs(longBound - DEFAULT_RENDER_HEIGHT) < Math.abs(resultLongBound - DEFAULT_RENDER_HEIGHT)) {
                    resultIndex = i;
                }
            }
        }
        return sizes.get(resultIndex);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Size findClosestSize(Size[] sizes) {
        if (sizes == null) {
            return null;
        }
        float targetRatio = 1.0f * DEFAULT_RENDER_HEIGHT / DEFAULT_RENDER_WIDTH;
        int resultIndex = 0;
        float resultRatio = -1;
        for (int i = 0; i < sizes.length; i++) {
            float longBound = Math.max(sizes[i].getWidth(), sizes[i].getHeight());
            float shortBound = Math.min(sizes[i].getWidth(), sizes[i].getHeight());
            float ratio = 1.0f * longBound / shortBound;
            if (resultRatio == -1 ||
                    Math.abs(ratio - targetRatio) < Math.abs(resultRatio - targetRatio)) {
                resultRatio = ratio;
                resultIndex = i;
            } else if (ratio == resultRatio) {
                float resultLongBound =
                        Math.max(sizes[resultIndex].getWidth(), sizes[resultIndex].getHeight());
                if (Math.abs(longBound - DEFAULT_RENDER_HEIGHT) < Math.abs(resultLongBound - DEFAULT_RENDER_HEIGHT)) {
                    resultIndex = i;
                }
            }
        }
        return sizes[resultIndex];
    }


    private void changeCamera() {
        if (mCurrentSupportCamera == CAMERA_ONE) {
            int cameraCount = 0;
            mHasBeenInitCameraTwo = false;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();

            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraId == 1) {
                    if (cameraInfo.facing == CAMERA_FACING_FRONT) {
                        cameraId = 0;
                        changeCameraWithSDKVersion();
                        break;
                    }
                } else {
                    //现在是前置， 变更为后置
                    if (cameraInfo.facing == CAMERA_FACING_BACK) {
                        cameraId = 1;
                        changeCameraWithSDKVersion();
                        break;
                    }
                }
            }
        } else {
            mHasBeenInitCameraTwo = false;
            if (camera2Id.equals(getFrontFacingCameraId())) {
                camera2Id = getFrontMainCameraId(mCameraManager);
                changeCameraWithSDKVersion();
            } else {
                //现在是前置， 变更为后置
                camera2Id = getFrontFacingCameraId();
                changeCameraWithSDKVersion();

            }
        }

    }

    private void changeCameraWithSDKVersion() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            doChangeCameraWidthSDKVersion();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    doChangeCameraWidthSDKVersion();
                }
            });
        }
    }

    private void doChangeCameraWidthSDKVersion() {
        switch (mCurrentSupportCamera) {
            case CAMERA_ONE:
                initCameraOne();
                break;
            case CAMERA_TWO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    initCameraTwo();
                }
                break;
            default:
                break;
        }
    }

    String getFrontFacingCameraId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                for (final String cameraId : mCameraManager.getCameraIdList()) {
                    CameraCharacteristics characteristics =
                            mCameraManager.getCameraCharacteristics(cameraId);
                    Integer cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation != null &&
                            cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                        return cameraId;
                    }
                }
            } catch (CameraAccessException e) {
                onCamera2ErrorAndTryLaunchCamera1();
            }
        }
        return null;
    }

    private String getFrontMainCameraId(CameraManager cManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                for (final String cameraId : cManager.getCameraIdList()) {
                    CameraCharacteristics characteristics =
                            cManager.getCameraCharacteristics(cameraId);
                    Integer cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation != null &&
                            cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                        return cameraId;
                    }
                }
            } catch (CameraAccessException e) {
                onCamera2ErrorAndTryLaunchCamera1();
            }
        }
        return null;
    }
}
