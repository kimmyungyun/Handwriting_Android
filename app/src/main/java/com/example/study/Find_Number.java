package com.example.study;

import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Find_Number {
    private Bitmap result;
    private Mat Gray_img;
    private Box box;
//    private PriorityQueue<Roi_class> ROI;
    List<Roi_class> ROI;
    private String language = "eng";
    //tessBaseAPI   Link : https://cosmosjs.blog.me/220937785735
    private TessBaseAPI mTess;
    private String datapath;
    private String answer="";
    public Find_Number(Bitmap Image, String datapath)
    {
        this.datapath = datapath;
        Mat img = new Mat();
        Gray_img = new Mat();
        Utils.bitmapToMat(Image, img);
        //OCR TessAPI 초기화
        TessAPI_init();

        //그레이 영역으로 변환
        Imgproc.cvtColor(img, Gray_img, Imgproc.COLOR_BGR2GRAY);

        //이미지 흑백 반전을 위한 작업
        Mat reverse_img = new Mat();
        Imgproc.threshold(Gray_img, reverse_img, 0.5, 1, Imgproc.THRESH_BINARY_INV);

        //외곽선 검색
        box = Find_Contours(reverse_img);

        //관심 영역 추출
        ROI = ROI_Extractor(box);
//        PriorityQueue<Roi_class> tmp_rois;
        List<Roi_class> tmp_rois;
        mTess.setImage(Image);
        String AAA = mTess.getUTF8Text();
        Log.d("Answer Answer", AAA);
        /****************Tess API 실행*******************/
        Rect tmp_rect;
        Mat tmp_mat;
        if(ROI != null)
        {
//            tmp_rois = new PriorityQueue<>(ROI);
            tmp_rois = new ArrayList<>(ROI);
//            int i=0;
//            while(!tmp_rois.isEmpty())
            for(int i=0; i<tmp_rois.size(); i++)
            {
                //tmp_rect = tmp_rois.poll().rect;
                tmp_rect = tmp_rois.get(i).rect;
                tmp_mat = img.submat(tmp_rect);
                Bitmap tmp_bitmap = Bitmap.createBitmap(tmp_rect.width, tmp_rect.height, Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(tmp_mat, tmp_bitmap);
                mTess.setImage(tmp_bitmap);
                answer += mTess.getUTF8Text();
                i++;
            }
            Log.d("Answer", "answer : "+answer);
        }
        /******************************************************/
        /*************  Draw ROI Method...******/
        if(ROI != null) {
//            tmp_rois = new PriorityQueue<>(ROI);
            tmp_rois = new ArrayList<>(ROI);

            Log.d("Size : ", "tmp_rois size : "+tmp_rois.size());
//            int i= 0;
            for(int i=0; i<tmp_rois.size(); i++){
//            while(!tmp_rois.isEmpty()){
//                tmp_rect = tmp_rois.poll().rect;
                tmp_rect = tmp_rois.get(i).rect;
                Imgproc.rectangle(img, tmp_rect, new Scalar(0, 0, 0));

                Imgproc.putText(img, ""+i, new Point(tmp_rect.x, tmp_rect.y), 1, 4, new Scalar(255, 0, 0));
                Log.d("Rect", "Rect X" + tmp_rect.x + " Rect Y" + tmp_rect.y + " Rect width " + tmp_rect.width + " Rect height " + tmp_rect.height);
                //i++;
            }
        }
        /*********************************/
        result = Bitmap.createBitmap(img.cols(),
                img.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(img, result);
    }
    //Contours 찾아내는 함수
    //Link : https://yeolco.tistory.com/57
    public Box Find_Contours(Mat img){
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        //Log.d("Type", "Type : "+img.type());
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //Log.d("Type", "Size : "+contours.size());
        Box result = new Box(contours, hierarchy);
        return result;
    }

    //ROI 추출 함수
    //Link : https://yeolco.tistory.com/57
//    public PriorityQueue<Roi_class> ROI_Extractor(Box b){
//        List<MatOfPoint> contours = b.getList();
//        PriorityQueue<Roi_class> result_Rect = new PriorityQueue<>();
//        MatOfPoint tmp_point;
//        Roi_class tmp_rect;
//        if(contours.size() == 0)
//            return null;
//
//        for(int idx = 0; idx>=0; idx = (int)b.getMat().get(0, idx)[0])
//        {
//            tmp_point = contours.get(idx);
//            tmp_rect = new Roi_class(Imgproc.boundingRect(tmp_point));
//
//            result_Rect.offer(tmp_rect);
//        }
//        return result_Rect;
//    }

    public List<Roi_class> ROI_Extractor(Box b){
        List<MatOfPoint> contours = b.getList();
        List<Roi_class> result_Rect = new ArrayList<>();
        MatOfPoint tmp_point;
        Roi_class tmp_rect;
        if(contours.size() == 0)
            return null;

        for(int idx = 0; idx>=0; idx = (int)b.getMat().get(0, idx)[0])
        {
            tmp_point = contours.get(idx);
            tmp_rect = new Roi_class(Imgproc.boundingRect(tmp_point));

            result_Rect.add(tmp_rect);
        }
        return result_Rect;
    }

    public void TessAPI_init()
    {
        mTess = new TessBaseAPI();
        mTess.init(this.datapath, language);
        mTess.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!?@#$%&*\"\\<>_:;'");
        mTess.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

    }
    public Bitmap getResult() {
        return result;
    }
    public void setResult(Bitmap result) {
        this.result = result;
    }

}
