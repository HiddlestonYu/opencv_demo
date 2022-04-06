package com.example.opencv_demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wx.wheelview.widget.WheelView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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

    private WheelView wheelView, wheelView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //opencv

//        wheelView = (WheelView) findViewById(R.id.wheelview);
//        wheelView2 = (WheelView) findViewById(R.id.wheelview2);
//        wheelView.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
//        wheelView2.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
//        wheelView.setSkin(WheelView.Skin.Common); // common皮肤
//        wheelView2.setSkin(WheelView.Skin.Common); // common皮肤
//        wheelView.setWheelData(createMainDatas());  // 数据集合
//        wheelView2.setWheelData(createFingerDatas());  // 数据集合

        String Filename = Environment.getExternalStorageDirectory() + "/123.png";
        File imgFile = new File(Filename);        Mat img2 = Imgcodecs.imread(Filename);
        Log.d(TAG, "img2 size: " + img2.size());
        Log.d(TAG, "imgFile exist: " + imgFile.exists());

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
        grayBtn.callOnClick();
    }
    private List<String> createMainDatas() {
        String[] strings = {
                "黑龙江",
                "吉林",
                "辽宁"
        };
        return Arrays.asList(strings);
    }

    private List<String> createFingerDatas() {
        String[] strings = {
                "1",
                "2",
                "3",
                "4",
                "5",
                "6"
        };
        return Arrays.asList(strings);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_gary:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish();
                break;

            case R.id.action_Fourier:
                Intent fourierIntent = new Intent(this, FourierActivity.class);
                startActivity(fourierIntent);
                break;

            case R.id.action_FFT:
                Intent FFTIntent = new Intent(this, FFTActivity.class);
                startActivity(FFTIntent);
                break;
            case R.id.action_performance:
                Intent PerformanceIntent = new Intent(this, PerformanceActivity.class);
                startActivity(PerformanceIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
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
            //let opencv mat release
            mat.release();
            matGray.release();

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