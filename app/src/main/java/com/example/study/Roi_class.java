package com.example.study;

import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.Rect;


class Roi_class{
    Rect rect;
    Bitmap bitmap;
    Mat mat;
    public Roi_class(Rect rect, Bitmap bitmap, Mat mat){
        this.rect = rect;
        this.bitmap = bitmap;
        this.mat = mat;
    }

}
