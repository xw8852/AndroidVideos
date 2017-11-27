package msx.androidvideos.activitys;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import java.io.IOException;
import java.util.List;

import msx.androidvideos.R;

import static android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;

public class CameraActivity extends Activity {
    FloatingActionButton fab;

    SurfaceView mSurfaceView;

    Camera.Size size = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        if (getActionBar() != null) getActionBar().hide();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 切换前后摄像头
                    ++cameraIndex;
                    cameraIndex = cameraIndex % cameraCount;
                    camera.stopPreview();
                    camera.release();
                    camera = Camera.open(cameraIndex);
                    camera.setDisplayOrientation(calculateRatotion());
                    camera.setPreviewDisplay(mSurfaceView.getHolder());
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setPreviewSize(size.width, size.height);
                    parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
                    camera.setParameters(parameters);
                    camera.cancelAutoFocus();
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        });
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                init();
                if (camera == null) return;
                int degrees = calculateRatotion();
                camera.setDisplayOrientation(degrees);
                size = getPreviewSize(degrees);
                int height;
                int width = mSurfaceView.getMeasuredWidth();

                if (degrees % 180 != 0) {
                    height = width * size.width / size.height;
                } else {
                    height = width * size.height / size.width;
                }

                mSurfaceView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));


                Camera.Parameters parameters = camera.getParameters();
                parameters.setPreviewSize(size.width, size.height);
                parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
                camera.cancelAutoFocus();
                camera.startPreview();
            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    boolean isPauseCamera;

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            isPauseCamera = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (isPauseCamera) {
                isPauseCamera = false;
                camera.reconnect();
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 计算合适的预览角度
     * @return
     */
    public int calculateRatotion() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraIndex, info);

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            degrees = (info.orientation + degrees) % 360;
            degrees = (360 - degrees) % 360;
        } else {
            degrees = (info.orientation - degrees + 360) % 360;
        }
        return degrees;
    }

    /**
     * 获取合适的预览尺寸
     * @param degrees
     * @return
     */
    public Camera.Size getPreviewSize(int degrees) {
        Camera.Size size = null;
        int width = mSurfaceView.getMeasuredWidth();
        int height = mSurfaceView.getMeasuredHeight();
        if (degrees % 180 != 0) {
            width = height;
            height = mSurfaceView.getMeasuredWidth();
        }

        float ratio = 0;
        int piexls = width * height;

        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        float originRatio = width / (height + 0.0f);
        for (Camera.Size _size : sizeList) {
            if (size == null) {
                ratio = (_size.width + 0.0f) / _size.height;
                size = _size;
            }
            //防止预览尺寸过小
            if (_size.width * _size.height < piexls / 2) continue;
            float _ratio = (_size.width + 0.0f) / _size.height;
            // 匹配误差最小的预览尺寸
            if (Math.abs(originRatio - _ratio) < Math.abs(originRatio - ratio)) {
                if (_size.width * _size.height > width * height) {
                    size = _size;
                    ratio = (_size.width + 0.0f) / _size.height;
                }
            }
        }


        return size;
    }

    Camera camera;
    int cameraIndex = 0;
    int cameraCount = 0;

    /**
     * 实例化摄像头 仅surfaceCreated调用
     */
    void init() {
        cameraCount = Camera.getNumberOfCameras();
        if (cameraCount <= 0) {
            ViewCompat.postOnAnimationDelayed(mSurfaceView, new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(CameraActivity.this).setTitle("提示").setMessage("没有检测到摄像头").setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();
                }
            }, 1000);

            return;
        }
        camera = Camera.open(cameraIndex);
        try {
            camera.setPreviewDisplay(mSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 摄像头旋转，重新设置参数
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        camera.stopPreview();
        camera.setDisplayOrientation(calculateRatotion());
        int degrees = calculateRatotion();
        camera.setDisplayOrientation(degrees);
        size = getPreviewSize(degrees);
        int height;
        int width = mSurfaceView.getMeasuredWidth();

        if (degrees % 180 != 0) {
            height = width * size.width / size.height;
        } else {
            height = width * size.height / size.width;
        }
        mSurfaceView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(size.width, size.height);
        parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        camera.cancelAutoFocus();
        camera.startPreview();
    }
}
