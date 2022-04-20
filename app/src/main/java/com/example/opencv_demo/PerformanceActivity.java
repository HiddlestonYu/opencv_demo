package com.example.opencv_demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class PerformanceActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_cal;
    private TextView tv_info;

    static int n = 512;
    static double[][] A = new double[n][n];      
    static double[][] B = new double[n][n];      
    static double[][] C = new double[n][n];
    private long start = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);
        //Object inital
        ObjectInital();
        //populate the matrices with random values between 0.0 and 1.0
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = r.nextDouble();
                B[i][j] = r.nextDouble();
                C[i][j] = 0;
            }
        }
    }

    private void ObjectInital() {
        btn_cal = (Button) this.findViewById(R.id.btn_cal_performance);
        tv_info = (TextView) this.findViewById(R.id.tv_info);
        btn_cal.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        start = System.nanoTime();
//        NestedLoop();
        ArrayMultiple(A, B);
        long stop = System.nanoTime();
        double timeDiff = (stop - start) * 1e-9;
        System.out.println("Elapsed time in seconds: " + timeDiff);
        tv_info.setText("Elapsed time in seconds: " + String.valueOf(timeDiff));
    }

    private void ArrayMultiple(double[][] A, double [][] B){
        int a_row = A.length;
        int a_column = A[0].length;
        int b_row = B.length;
        int b_column = B[0].length;
        double result[][] = new double[a_row][b_column];
        for (int i=0;i<a_row;i++){
            for(int j=0;j<b_column;j++){
                for(int k=0;k<a_column;k++){
                    result[i][j] += A[i][k]*B[k][j];
                }
                System.out.println(result[i][j] +"  ");;
            }
            System.out.println();
        }


    }

    private void NestedLoop() {
        //matrix multiplication
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        long stop = System.nanoTime();
        double timeDiff = (stop - start) * 1e-9;
        System.out.println("Elapsed time in seconds: " + timeDiff);
        tv_info.setText("Elapsed time in seconds: " + String.valueOf(timeDiff));
    }
}