package com.example.study;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import java.util.List;

public class Box{
    private List<MatOfPoint> Contours;
    private Mat hierarchy;
    public Box(List<MatOfPoint> list, Mat mat)
    {
        this.Contours = list;
        this.hierarchy = mat;
    }

    public List<MatOfPoint> getList() {
        return Contours;
    }

    public void setList(List<MatOfPoint> list) {
        this.Contours = list;
    }

    public Mat getMat() {
        return hierarchy;
    }

    public void setMat(Mat mat) {
        this.hierarchy = mat;
    }
}