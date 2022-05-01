package com.diplabs.filtercam2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "FilterCam";
    private JavaCamera2View javaCameraView;
//    CascadeClassifier faceDetector;

    //    File caseFile;
    static {
        System.loadLibrary("opencv_java4");
    }

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int activeCamera =  CameraBridgeViewBase.CAMERA_ID_BACK;
    private BaseLoaderCallback baseLoaderCallback;


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

//        face detection
//        baseLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                switch (status) {
//                    case LoaderCallbackInterface.SUCCESS: {
//                        try{
//                            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
//                            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
//                            caseFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
//
//                            FileOutputStream fos = new FileOutputStream(caseFile);
//
//                            byte[] buffer = new byte[4096];
//                            int bytesRead;
//
//                            while ((bytesRead = is.read(buffer)) != -1) {
//                                fos.write(buffer, 0, bytesRead);
//                            }
//                            is.close();
//                            fos.close();
//
//                            faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
//                            if (faceDetector.empty()) {
//                                faceDetector = null;
//                            } else {
//                                cascadeDir.delete();
//                            }
//                            javaCameraView.enableView();
//                        }catch (IOException e){
//
//                        }
//
//                        }
//                        break;
//
//                        default:
//                            super.onManagerConnected(status);
//
//                }
//            }
//
//        };





        javaCameraView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                swapCamera();
            }
        });
    }


   private int initialColor = 0xffffff;
    private int filterColor = initialColor;
    private double redPercent = 1.0;
    private double greenPercent = 1.0;
    private double bluePercent = 1.0;


    public void colorPicker(View view) {

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, filterColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // color is the color selected by the user.
                filterColor = color;
                redPercent = Color.red(filterColor) / 255.0;
                greenPercent = Color.green(filterColor) / 255.0;
                bluePercent = Color.blue(filterColor) / 255.0;
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
            }

        });
        dialog.show();
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

//        javaCameraView.enableFpsMeter();

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



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();



        Core.multiply(mRgba,new Scalar(redPercent,greenPercent,bluePercent),mRgba);




//detect Face
//        MatOfRect facedetections = new MatOfRect();
//        faceDetector.detectMultiScale(mRgba,facedetections);
//
//        for(Rect react: facedetections.toArray()){
//            Imgproc.rectangle(mRgba, new Point(react.x,react.y),
//                    new Point(react.x + react.width, react.y + react.height),
//                    new Scalar(255,0,0));
//        }











        return mRgba;

    }



    private void swapCamera() {

        if (activeCamera ==  CameraBridgeViewBase.CAMERA_ID_BACK){
            activeCamera =  CameraBridgeViewBase.CAMERA_ID_FRONT;;
        } else{
            activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
        }
//        activeCamera = activeCamera^1; //bitwise not operation to flip 1 to 0 and vice versa
        javaCameraView.disableView();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.enableView();
    }

}