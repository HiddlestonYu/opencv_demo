package com.example.opencv_demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class FourierActivity extends AppCompatActivity {
    private ImageView iv_timeDomain, iv_frequencyDomain;
    private Button btn_dft_idft;
    private Bitmap bitmap, resultBitmap;
    private String TAG = FourierActivity.class.getSimpleName();
    private Mat mat, mat_1 ,resultMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourier);

        if (!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            Log.d(TAG, "initDebug not : ");

        }else{
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            Log.d(TAG, "initDebug: ");
        }
        // setObjectView
        iv_timeDomain = (ImageView) this.findViewById(R.id.iv_timeDomain);
        iv_frequencyDomain = (ImageView)this.findViewById(R.id.iv_frequencyDomain);
        btn_dft_idft = (Button) this.findViewById(R.id.btn_dft_idft);
        // android Drawable直接讀取bitmap , 尚未改成opencv imread(file path)



        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.allen_0_001);
        iv_timeDomain.setImageBitmap(bitmap);
        mat = opencvBitmapToMat(bitmap);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

//        resultMat = myDFT(mat);

        resultMat = getDFT(mat);
        resultBitmap = opencvMatToBitmap(resultMat);
        iv_frequencyDomain.setImageBitmap(resultBitmap);


    }

    private Mat getDFT(Mat singleChannel) {
//        Log.d(TAG, "InputData Type: " + singleChannel.type());
        singleChannel.convertTo(mat, CvType.CV_64FC1);
//        Log.d(TAG, "mat type: " + mat.type());
        int m = Core.getOptimalDFTSize(mat.rows());
        int n = Core.getOptimalDFTSize(mat.cols()); // on the border add zero values Imgproc.copyMakeBorder(image1, padded, 0, m - image1.rows(), 0, n )

        Mat padded = new Mat(new Size(n, m), CvType.CV_64FC1); // expand input
        // image to
        // optimal size

        Core.copyMakeBorder(mat, padded, 0, m - singleChannel.rows(), 0,
                n - singleChannel.cols(), Core.BORDER_CONSTANT, Scalar.all(0));

        List<Mat> planes = new ArrayList<Mat>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        Mat complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        Mat complexI2 = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);
        //IDFT
       /* Mat complexI3 = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);
        Mat complexI4 = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);*/

        Core.merge(planes, complexI); // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI2); // this way the result may fit in the source matrix
//        Log.d(TAG, "Diff DFT Time: " + (stop-start)* 1e-9);


        // compute the magnitude and switch to logarithmic scale
        // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
        Core.split(complexI2, planes); // planes[0] = Re(DFT(I)), planes[1] = Im(DFT(I))

        Mat mag = new Mat(planes.get(0).size(), planes.get(0).type());

        Core.magnitude(planes.get(0), planes.get(1), mag);// planes[0] = magnitude

        Mat magI = mag;
        Mat magI2 = new Mat(magI.size(), magI.type());
        Mat magI3 = new Mat(magI.size(), magI.type());
        Mat magI4 = new Mat(magI.size(), magI.type());
        Mat magI5 = new Mat(magI.size(), magI.type());

        Core.add(magI, Mat.ones(padded.rows(), padded.cols(), CvType.CV_64FC1),
                magI2); // switch to logarithmic scale
        Core.log(magI2, magI3);

        Mat crop = new Mat(magI3, new Rect(0, 0, magI3.cols() & -2,
                magI3.rows() & -2));

        magI4 = crop.clone();

        // rearrange the quadrants of Fourier image so that the origin is at the
        // image center
        int cx = magI4.cols() / 2;
        int cy = magI4.rows() / 2;

        Rect q0Rect = new Rect(0, 0, cx, cy);
        Rect q1Rect = new Rect(cx, 0, cx, cy);
        Rect q2Rect = new Rect(0, cy, cx, cy);
        Rect q3Rect = new Rect(cx, cy, cx, cy);

        Mat q0 = new Mat(magI4, q0Rect); // Top-Left - Create a ROI per quadrant
        Mat q1 = new Mat(magI4, q1Rect); // Top-Right
        Mat q2 = new Mat(magI4, q2Rect); // Bottom-Left
        Mat q3 = new Mat(magI4, q3Rect); // Bottom-Right

        Mat tmp = new Mat(); // swap quadrants (Top-Left with Bottom-Right)
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);

        q1.copyTo(tmp); // swap quadrant (Top-Right with Bottom-Left)
        q2.copyTo(q1);
        tmp.copyTo(q2);

        Core.normalize(magI4, magI5, 0, 255, Core.NORM_MINMAX);

        Mat realResult = new Mat(magI5.size(), CvType.CV_8UC1);

        magI5.convertTo(realResult, CvType.CV_8UC1);

        List<Mat> planesIDFT = new ArrayList<Mat>();
        planesIDFT.add(padded);
        planesIDFT.add(padded);
        Mat invDFTcvt = new Mat(padded.size(), CvType.CV_8UC1);
        Mat complexI3 = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);
        Core.idft(complexI2, complexI3);
        Core.split(complexI3, planesIDFT); // planes[0] = Re(DFT(I)), planes[1] = Im(DFT(I))
        Core.normalize(planesIDFT.get(0), invDFTcvt, 0, 255, Core.NORM_MINMAX);
        invDFTcvt.convertTo(invDFTcvt, CvType.CV_8UC1);
        return invDFTcvt;


       /* //IDFT
        Mat IDFTResult = Mat.zeros(realResult.rows(), realResult.cols(), CvType.CV_64FC2);

        Core.idft(IDFTResult, IDFTResult);

        Mat restoredImage = new Mat();
        Core.split(IDFTResult, planes);
        Core.normalize(planes.get(0), restoredImage, 0, 255, Core.NORM_MINMAX);

        Mat realResultIDFT = new Mat(restoredImage.size(), CvType.CV_8UC1);

        restoredImage.convertTo(realResultIDFT, CvType.CV_8UC1);*/

//        return realResult;
    }

    private Mat myDFT(Mat singleChannel) {

        singleChannel.convertTo(mat, CvType.CV_64FC1);

        Mat padded = new Mat();                     //expand input image to optimal size
        int m = Core.getOptimalDFTSize( mat.rows() );//whose size is a product of 2's, 3's, and 5's
        int n = Core.getOptimalDFTSize( mat.cols() ); // on the border add zero values
        Core.copyMakeBorder(mat, padded, 0, m - mat.rows(), 0, n - mat.cols(), Core.BORDER_CONSTANT, Scalar.all(0));
        Log.d(TAG, "mat size and padded size: " + mat.size() +"    "  + padded.size());

        List<Mat> planes = new ArrayList<Mat>();
        padded.convertTo(padded, CvType.CV_32F); // store these usually at least in a float format.
        planes.add(padded);
        planes.add(Mat.zeros(padded.size(), CvType.CV_32F));
        Mat complexI = new Mat();
        Core.merge(planes, complexI);  // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI);// this way the result may fit in the source matrix

        Core.split(complexI, planes);                               // planes.get(0) = Re(DFT(I) // planes.get(1) = Im(DFT(I))
        Core.magnitude(planes.get(0), planes.get(1), planes.get(0));// planes.get(0) = magnitude
        Mat magI = planes.get(0);

        Mat matOfOnes = Mat.ones(magI.size(), magI.type());
        Core.add(matOfOnes, magI, magI);         // switch to logarithmic scale
        Core.log(magI, magI);

        // crop the spectrum, if it has an odd number of rows or columns
        magI = magI.submat(new Rect(0, 0, magI.cols() & -2, magI.rows() & -2));
        // rearrange the quadrants of Fourier image  so that the origin is at the image center
        int cx = magI.cols()/2;
        int cy = magI.rows()/2;
        Mat q0 = new Mat(magI, new Rect(0, 0, cx, cy));   // Top-Left - Create a ROI per quadrant
        Mat q1 = new Mat(magI, new Rect(cx, 0, cx, cy));  // Top-Right
        Mat q2 = new Mat(magI, new Rect(0, cy, cx, cy));  // Bottom-Left
        Mat q3 = new Mat(magI, new Rect(cx, cy, cx, cy)); // Bottom-Right
        Mat tmp = new Mat();               // swap quadrants (Top-Left with Bottom-Right)
        q0.copyTo(tmp);
        q3.copyTo(q0);
        tmp.copyTo(q3);
        q1.copyTo(tmp);                    // swap quadrant (Top-Right with Bottom-Left)
        q2.copyTo(q1);
        tmp.copyTo(q2);

        Core.normalize(magI, magI, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1); // Transform the matrix with float values
        // into a viewable image form (float between
        // values 0 and 255).

        Mat realResult = new Mat();
        magI.convertTo(realResult, CvType.CV_8UC1);
        return realResult;
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            super.onManagerConnected(status);
        }
    };

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