package com.example.opencv_demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FourierActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = FourierActivity.class.getSimpleName();
    private ImageView iv_timeDomain, iv_frequencyDomain;
    private Button btn_dft_idft;
    private TextView tv_frequency;

    private Bitmap bitmap, resultBitmap;
    private Mat mat ,DFTResultMat;
    private Boolean isDFT = false;
    private Mat complexI2 , IDFTRealResult;
    private Mat DFTData, IDFTData;
    private Mat DFTRealResult;
    private ArrayList<Mat> planes;

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
        btn_dft_idft.setOnClickListener(this);
        tv_frequency = (TextView)this.findViewById(R.id.textView2);
        // android Drawable直接讀取bitmap , 尚未改成opencv imread(file path)


        //load bitmap
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.allen_0_001);
        iv_timeDomain.setImageBitmap(bitmap);
        mat = opencvBitmapToMat(bitmap);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);// gray image
        //init DFT image
        DFTResultMat = getDFT(mat);
        resultBitmap = opencvMatToBitmap(DFTResultMat);
        iv_frequencyDomain.setImageBitmap(resultBitmap);
    }

    private Mat DFTShift(Mat mag) {
        Mat magI = mag;
        Mat magI2 = new Mat(magI.size(), magI.type());
        Mat magI3 = new Mat(magI.size(), magI.type());
        Mat magI4 = new Mat(magI.size(), magI.type());

        Core.add(magI, Mat.ones(mag.rows(), mag.cols(), CvType.CV_64FC1),
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

        return magI4;
    }

    private Mat getDFT(Mat image) {
        image.convertTo(image, CvType.CV_64FC1);
        int m = Core.getOptimalDFTSize(image.rows());
        int n = Core.getOptimalDFTSize(image.cols()); // on the border add zero values Imgproc.copyMakeBorder(image1, padded, 0, m - image1.rows(), 0, n )

        Mat padded = new Mat(new Size(n, m), CvType.CV_64FC1); // expand input image to optimal size

        Core.copyMakeBorder(image, padded, 0, m - image.rows(), 0,
                n - image.cols(), Core.BORDER_CONSTANT, Scalar.all(0));

        planes = new ArrayList<Mat>();
        planes.add(padded);
        planes.add(Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC1));

        Mat complexI = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);

        complexI2 = Mat.zeros(padded.rows(), padded.cols(), CvType.CV_64FC2);
        Core.merge(planes, complexI); // Add to the expanded another plane with zeros
        Core.dft(complexI, complexI2); // this way the result may fit in the source matrix

        // compute the magnitude and switch to logarithmic scale
        // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
        Core.split(complexI2, planes); // planes[0] = Re(DFT(I)), planes[1] = Im(DFT(I))

        Mat mag = new Mat(planes.get(0).size(), planes.get(0).type());

        Core.magnitude(planes.get(0), planes.get(1), mag);// planes[0] = magnitude
        Mat magI4 = new Mat(mag.size(), mag.type());
        DFTData = new Mat(mag.size(), mag.type());

        magI4 = DFTShift(mag);

        Core.normalize(magI4, DFTData, 0, 255, Core.NORM_MINMAX);

        DFTRealResult = new Mat(DFTData.size(), CvType.CV_8UC1);
        DFTData.convertTo(DFTRealResult, CvType.CV_8UC1);

        isDFT = true;
        return DFTRealResult;

    }

    private Mat getIDFT(Mat DFTimage){
        //shift frequency mag Data

        DFTimage.convertTo(DFTimage, CvType.CV_64FC1);
//        DFTimage = DFTShift(DFTimage);


        Mat Re = new Mat(DFTimage.size(), CvType.CV_64FC1); // expand input image to optimal size
        Mat Im = new Mat(DFTimage.size(), CvType.CV_64FC1); // expand input image to optimal size

        Mat ph = new Mat();
//        Core.phase(planes.get(0), planes.get(1), ph);

        Core.polarToCart(DFTimage, planes.get(1), Re, Im);
        List<Mat> planesIDFT = new ArrayList<Mat>();
        planesIDFT.add(Re);
        planesIDFT.add(Im);

        Mat complexI = Mat.zeros(Re.rows(), Re.cols(), CvType.CV_64FC2);
        Mat complexI3 = Mat.zeros(Re.rows(), Re.cols(), CvType.CV_64FC2);

        Core.merge(planesIDFT, complexI); // Add to the expanded another plane with zeros

        Mat invDFTcvt = new Mat(Re.size(), CvType.CV_8UC1);
//        Core.idft(complexI2, complexI3);
        Core.idft(complexI, complexI3);
        Core.split(complexI3, planesIDFT); // planes[0] = Re(DFT(I)), planes[1] = Im(DFT(I))
        Core.normalize(planesIDFT.get(0), invDFTcvt, 0, 255, Core.NORM_MINMAX);
        invDFTcvt.convertTo(invDFTcvt, CvType.CV_8UC1);
        isDFT = false;

        return invDFTcvt;
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

    @Override
    public void onClick(View view) {
        if (isDFT){
            long start = System.nanoTime();
            IDFTRealResult = getIDFT(DFTResultMat);
            long end = System.nanoTime();
            double timeDiff = (end - start) * 1e-9;
            DecimalFormat df = new DecimalFormat("###.#####");
            resultBitmap = opencvMatToBitmap(IDFTRealResult);
            iv_frequencyDomain.setImageBitmap(resultBitmap);
            Toast.makeText(this, "IDFT Image", Toast.LENGTH_SHORT).show();
            tv_frequency.setText("Frequency domain\n" + "Elapsed time in seconds:\n " + df.format(timeDiff));

        }else{
            long start = System.nanoTime();
            DFTResultMat =  getDFT(IDFTRealResult);
            long end = System.nanoTime();
            double timeDiff = (end - start) * 1e-9;
            DecimalFormat df = new DecimalFormat("###.#####");
            resultBitmap = opencvMatToBitmap(DFTResultMat);
            Toast.makeText(this, "DFT Image", Toast.LENGTH_SHORT).show();
            iv_frequencyDomain.setImageBitmap(resultBitmap);
            tv_frequency.setText("Frequency domain\n" + "Elapsed time in seconds:\n " + df.format(timeDiff));
        }
    }
}