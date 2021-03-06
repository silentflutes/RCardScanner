package com.scanner.cardreader;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    @SuppressWarnings("deprecation")
    private static final String tag = "CameraActivity";
    private Camera camera;
    SurfaceView surfaceView;

    SurfaceHolder surfaceHolder;

    Camera.PictureCallback jpegCallBack;
   Camera.ShutterCallback shutterCallback;


    private int cameraId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.

        surfaceHolder.addCallback(this);

// deprecated setting, but required on Android versions prior to 3.0

        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ImageButton button  = (ImageButton) findViewById(R.id.imageButton);
        //        Check if there is a camera on the device

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Toast.makeText(this,"No camera present on this device", Toast.LENGTH_LONG).show();
        }
        else{
            cameraId = findRearFacingCamera();
            if(cameraId < 0){
                Toast.makeText(this, "Rear facing camera not found", Toast.LENGTH_LONG).show();
            }
            else{
                camera = Camera.open(cameraId);
            }
        }

        jpegCallBack = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                try{
                    if(bytes != null){
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Log.d(tag,Integer.toString(bitmap.getByteCount()));

                    }
                }
                catch(Exception e){
                    System.out.println(e);
                }


            }
        };
        shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                MediaActionSound sound = new MediaActionSound();
                sound.play(MediaActionSound.SHUTTER_CLICK);

            }
        };




//Take a picture on button click
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(shutterCallback, null,
                        jpegCallBack);
            }
        });
    }

    //    Reinitialize the camera API
    public void refreshCamera(){
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }

    }
    //Check if a rear camera is available
    private int findRearFacingCamera() {
        int cameraId = 0;
//        Searching for the rear facing camera
        int noOfCameras = Camera.getNumberOfCameras();
        for(int i = 0; i<noOfCameras; i++){
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                cameraId = i;
                break;
            }

        }
        return cameraId;
    }
    //Called when app is paused by android
    @Override
    protected void onPause() {
        if(camera != null){
            camera.release();
            camera = null;
        }
        super.onPause();
    }
    //Called when app is resumed
    @Override
    protected void onPostResume() {
        if(camera == null){
            camera = Camera.open(findRearFacingCamera());

        }
        super.onPostResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            Camera.open(findRearFacingCamera());

        }
        catch(RuntimeException e)
        {
            System.out.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

//        Modify camera surface parameters
        param.set("orientation", "portrait");

        param.setPreviewSize( surfaceView.getWidth(), surfaceView.getHeight());

        camera.setParameters(param);


        try{
//            The surfaceView has created a surface,
//            Now tell the camera where to draw the picture

            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch(Exception e){
            System.out.println(e);
            return;

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.

        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            Log.d("Orientation", Integer.toString(this.getResources().getConfiguration().orientation));
            camera.setDisplayOrientation(90);

        }
        else{
            camera.setDisplayOrientation(0);
        }
        refreshCamera();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            camera.stopPreview();
            camera.release();
            camera.setPreviewCallback(null);
            camera = null;
        }
        catch(Exception e){
            Log.v(tag, String.valueOf(e));
        }


    }


}
