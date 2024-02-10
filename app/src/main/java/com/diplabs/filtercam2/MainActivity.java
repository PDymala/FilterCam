package com.diplabs.filtercam2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "FilterCam";
    private CustomCameraView javaCameraView;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int activeCamera =  CameraBridgeViewBase.CAMERA_ID_BACK;
    private BaseLoaderCallback baseLoaderCallback;

    static {
        System.loadLibrary("opencv_java4");
    }


    private int initialColor = 0xffffff;
    private int filterColor = initialColor;
    private double redPercent = 1.0;
    private double greenPercent = 1.0;
    private double bluePercent = 1.0;
    private int filterColorType = 0;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        javaCameraView = findViewById(R.id.color_blob_detection_activity_surface_view);

        permisions();


        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {

                    case BaseLoaderCallback.SUCCESS:
                        javaCameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }
        };

        javaCameraView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                swapCamera();
            }
        });

    }




    private void permisions() {
        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera(javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }

    private void initializeCamera(JavaCamera2View javaCameraView, int activeCamera) {


        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);


    }



    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is Configured or Connected successfully.");
                baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);

        } else {
            Log.d(TAG, "OpenCV not Working or Loaded.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }
    Mat mRgba;



    public void colorPicker() {

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, filterColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                filterColor = color;
                redPercent = Color.red(filterColor) / 255.0;
                greenPercent = Color.green(filterColor) / 255.0;
                bluePercent = Color.blue(filterColor) / 255.0;
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

        });

        dialog.show();
    }


    public void showMenuColorFilter(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.mwnu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.none:
                redPercent = 1.0;
                greenPercent = 1.0;
                bluePercent = 1.0;
                return true;
            case R.id.red:
                redPercent = 1.0;
                greenPercent = 0.0;
                bluePercent = 0.0;
                return true;
            case R.id.green:
                redPercent = 0.0;
                greenPercent = 1.0;
                bluePercent = 0.0;
                return true;
            case R.id.blue:
                redPercent = 0.0;
                greenPercent = 0.0;
                bluePercent = 1.0;
                return true;
            case R.id.custom:
                colorPicker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void redButtonClick(View view){
        redPercent = 1.0;
        greenPercent = 0.0;
        bluePercent = 0.0;
    }

    public void greenButtonClick(View view){
        redPercent = 0.0;
        greenPercent = 1.0;
        bluePercent = 0.0;
    }
    public void blueButtonClick(View view){
        redPercent = 0.0;
        greenPercent = 0.0;
        bluePercent = 1.0;
    }

    public void noButtonClick(View view){
        redPercent = 1.0;
        greenPercent = 1.0;
        bluePercent = 1.0;
    }

    boolean hidden = false;

    public void toggleHide(View view){


    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Core.multiply(mRgba,new Scalar(redPercent,greenPercent,bluePercent),mRgba);
        return mRgba;

    }


    private void swapCamera() {
        if (activeCamera ==  CameraBridgeViewBase.CAMERA_ID_BACK){
            activeCamera =  CameraBridgeViewBase.CAMERA_ID_FRONT;;
        } else{
            activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
        }
        javaCameraView.disableView();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.enableView();
    }

    public void flashOnOff(View view) throws CameraAccessException {

        javaCameraView.toggleFlashMode();
    }

    public void zoomUp(View view) throws CameraAccessException {
        javaCameraView.zoomUpCamera();
    }
}