package com.diplabs.filtercam2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "FilterCam";
    private CustomCameraView javaCameraView;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int activeCamera =  CameraBridgeViewBase.CAMERA_ID_BACK;

    static {
        System.loadLibrary("opencv_java4");
    }

    private double redPercent = 1.0;
    private double greenPercent = 1.0;
    private double bluePercent = 1.0;

    private boolean autoFilter = false;


    ImageButton imageButtonHide;
    ImageButton imageButtonFlash;
    ImageButton imageButtonZoom;
    ImageButton imageButtonRedFilter;
    ImageButton imageButtonGreenFilter;
    ImageButton imageButtonBlueFilter;
    ImageButton imageButtonNoFilter;

    ImageButton imageButtonFilterAim;

    ImageView imageViewAutoFilterAim;
    boolean hidden = false;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        javaCameraView = findViewById(R.id.cameraView);

        permisions();
        initUI();



        javaCameraView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                swapCamera();
            }


        });


    }

    private void initUI() {

         imageButtonHide = findViewById(R.id.imageButtonHide);
         imageButtonFlash = findViewById(R.id.imageButtonFlash);
         imageButtonZoom = findViewById(R.id.imageButtonZoom);
         imageButtonRedFilter = findViewById(R.id.imageButtonRedFilter);
         imageButtonGreenFilter = findViewById(R.id.imageButtonGreenFilter);
         imageButtonBlueFilter = findViewById(R.id.imageButtonBlueFilter);
         imageButtonNoFilter = findViewById(R.id.imageButtonNoFilter);
        imageButtonFilterAim = findViewById(R.id.imageButtonFilterAim);
        imageViewAutoFilterAim = findViewById(R.id.imageViewAutoFilterAim);

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
        javaCameraView.enableView();


    }



    @Override
    protected void onResume() {
        super.onResume();


        if (javaCameraView != null) {
            javaCameraView.enableView();
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


    public void toggleAutoFilter(View view) {
        if (!autoFilter) {
            imageViewAutoFilterAim.setVisibility(View.VISIBLE);
        } else{
            imageViewAutoFilterAim.setVisibility(View.INVISIBLE);
        }
        autoFilter = !autoFilter;
    }


    public void redButtonClick(View view){
        if (autoFilter) toggleAutoFilter(view);
        redPercent = 1.0;
        greenPercent = 0.0;
        bluePercent = 0.0;
    }

    public void greenButtonClick(View view){
        if (autoFilter) toggleAutoFilter(view);
        redPercent = 0.0;
        greenPercent = 1.0;
        bluePercent = 0.0;
    }
    public void blueButtonClick(View view){
        if (autoFilter) toggleAutoFilter(view);
        redPercent = 0.0;
        greenPercent = 0.0;
        bluePercent = 1.0;
    }

    public void noButtonClick(View view){
        if (autoFilter) toggleAutoFilter(view);
        redPercent = 1.0;
        greenPercent = 1.0;
        bluePercent = 1.0;
    }


    public void toggleHide(View view){
        if (!hidden){

            imageButtonFlash.setVisibility(View.INVISIBLE);
            imageButtonZoom.setVisibility(View.INVISIBLE);
            imageButtonRedFilter.setVisibility(View.INVISIBLE);
            imageButtonGreenFilter.setVisibility(View.INVISIBLE);
            imageButtonBlueFilter.setVisibility(View.INVISIBLE);
            imageButtonNoFilter.setVisibility(View.INVISIBLE);
//            imageViewAutoFilterAim.setVisibility(View.INVISIBLE);
            imageButtonFilterAim.setVisibility(View.INVISIBLE);

            hidden = true;

        } else{

            imageButtonFlash.setVisibility(View.VISIBLE);
            imageButtonZoom.setVisibility(View.VISIBLE);
            imageButtonRedFilter.setVisibility(View.VISIBLE);
            imageButtonGreenFilter.setVisibility(View.VISIBLE);
            imageButtonBlueFilter.setVisibility(View.VISIBLE);
            imageButtonNoFilter.setVisibility(View.VISIBLE);
//            imageViewAutoFilterAim.setVisibility(View.VISIBLE);
            imageButtonFilterAim.setVisibility(View.VISIBLE);
            hidden = false;
        }



    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (autoFilter) {

            // Get image dimensions
            int rows = mRgba.rows();
            int cols = mRgba.cols();

            // Calculate the center pixel coordinates
            int centerX = cols / 2;
            int centerY = rows / 2;

            // Access the center pixel
            double[] centerPixel = mRgba.get(centerY, centerX);

            // Extract RGB components
            redPercent = centerPixel[0]/255.0;
            greenPercent = (int) centerPixel[1]/255.0;
            bluePercent = (int) centerPixel[2]/255.0;
        }

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