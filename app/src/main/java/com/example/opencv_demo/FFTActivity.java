package com.example.opencv_demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jtransforms.fft.DoubleFFT_2D;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class FFTActivity extends AppCompatActivity {
    private String TAG = FFTActivity.class.getSimpleName();
    private Bitmap bitmap;
    private ImageView iv_timeDomain2, iv_frequencyDomain2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fftactivity);

        //Object Create
        iv_timeDomain2 = (ImageView)this.findViewById(R.id.iv_timeDomain2);
        iv_frequencyDomain2 = (ImageView)this.findViewById(R.id.iv_frequencyDomain2);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.allen_0_001);
        double  coverImageDoubleArray[][] = new double[bitmap.getWidth()][bitmap.getHeight()];

        for (int y = 0; y < bitmap.getHeight()-1; y++)
        {
            for (int x = 0; x < bitmap.getWidth()-1; x++)
            {
                coverImageDoubleArray[x][y] = bitmap.getPixel(x, y);
            }
        }
        iv_timeDomain2.setImageBitmap(bitmap);

        DoubleFFT_2D fft = new DoubleFFT_2D(bitmap.getHeight(), bitmap.getWidth());
        fft.complexForward(coverImageDoubleArray);
        Log.d(TAG, "onCreate: " + bitmap.getHeight() +"\t" +  bitmap.getWidth());
//        mat = opencvBitmapToMat(bitmap);
    }

    public Bitmap opencvMatToBitmap(@NonNull Mat mat){
        Bitmap bitmap  = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public Mat opencvBitmapToMat(@NonNull Bitmap bitmap){
        Mat mat = new Mat();
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(newBitmap, mat);
        return mat;
    }

//    public static Bitmap bitmapFromArray(double[][] pixels2d){
//        int width = pixels2d.length;
//        int height = pixels2d[0].length;
//        double[] pixels = new double[width * height];
//        int pixelsIndex = 0;
//        for (int i = 0; i < width; i++)
//        {
//            for (int j = 0; j < height; j++)
//            {
//                pixels[pixelsIndex] = pixels2d[i][j];
//                pixelsIndex ++;
//            }
//        }
//        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
//    }

}