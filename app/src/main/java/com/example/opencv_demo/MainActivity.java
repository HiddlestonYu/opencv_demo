package com.example.opencv_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String TAG = MainActivity.class.getSimpleName();
    private Button grayBtn;
    private ImageView img;
    private Bitmap image;
    private Boolean isClick = false;
    private Bitmap bitmapProcess;
    private Mat matImage;

    //Testing OpenCV Library
    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCv is loading");
        }else{
            Log.d(TAG, "OpenCv is not loading");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grayBtn = (Button) this.findViewById(R.id.grayBtn);
        img = (ImageView) this.findViewById(R.id.imageView);
        image = ((BitmapDrawable)img.getDrawable()).getBitmap();
//        //init opencv lib API
        if (!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            Log.d(TAG, "initDebug not : ");

        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            Log.d(TAG, "initDebug: ");
        }

        grayBtn.setOnClickListener(this);
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            super.onManagerConnected(status);
        }
    };

    @Override
    public void onClick(View view) {
        isClick = !isClick;
        if (isClick == true){
            Mat mat = opencvBitmapToMat(image);
            Log.d(TAG, "MatType: " + mat.type());
            Mat matGray = new Mat();
            Imgproc.cvtColor(mat, matGray, Imgproc.COLOR_BGR2GRAY);
            bitmapProcess = opencvMatToBitmap(matGray);
            img.setImageBitmap(bitmapProcess);

        }else{
            img.setImageBitmap(image);
        }
    }

    public Bitmap opencvMatToBitmap(Mat mat){
        Bitmap bitmap  = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public Mat opencvBitmapToMat(Bitmap bitmap){
        Mat mat = new Mat();
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(newBitmap, mat);
        return mat;
    }
}