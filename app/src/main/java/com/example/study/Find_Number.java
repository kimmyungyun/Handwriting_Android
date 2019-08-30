package com.example.study;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Find_Number {
    private static final char Label[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q'
            ,'R','S','T','U','V','W','X','Y','Z','a','b','d','e','f','g','h','n','q','r','t'};
    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 28;
    public static final int IMG_WIDTH = 28;
    private static final int NUM_CHANNEL = 1;
    private static final int NUM_CLASSES = 47;


    private Bitmap result;
    private Mat Gray_img;
    private Box box;
    private Interpreter tf_lite;
    List<Roi_class> ROI;
    private ByteBuffer mImageData;
    private final int[] mImagePixels = new int[IMG_WIDTH * IMG_HEIGHT];
    private final float[][] mResult = new float[1][NUM_CLASSES];
    private float[][][][] Test_Img = new float[1][IMG_HEIGHT][IMG_WIDTH][NUM_CHANNEL];

    private String answer="";
    public Find_Number(Bitmap Image, String datapath, Activity activity)
    {
        /**** Keras Model Load*******/
        try {
            tf_lite = getTfliteInterpreter(datapath, activity);
            mImageData = ByteBuffer.allocate(
                    4 * 1 * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL
            );
            mImageData.order(ByteOrder.nativeOrder());
        }catch(Exception e){
            e.printStackTrace();
        }


        Mat img = new Mat(Image.getWidth(), Image.getHeight(), CvType.CV_8U);
        Gray_img = new Mat();
        Utils.bitmapToMat(Image, img);

        //그레이 영역으로 변환
        Imgproc.cvtColor(img, Gray_img, Imgproc.COLOR_BGR2GRAY);
        Mat edge = new Mat();
        Imgproc.Canny(Gray_img, edge, 50, 50);

        Imgproc.dilate(edge, edge,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(8, 8)), new Point(), 2);
        edge.copyTo(img);

        //외곽선 검색
        box = Find_Contours(img);

        //관심 영역 추출
        ROI = ROI_Extractor(box, img);
        for(int i=0; i<ROI.size(); i++) {
            long startTime = SystemClock.uptimeMillis();

            int x = classify(ROI.get(i).mat);
            //int x = classify(ROI.get(i).bitmap);
            long endTime = SystemClock.uptimeMillis();
            long timeCost = endTime - startTime;
            //MediaStore.Images.Media.insertImage(activity.getContentResolver(), ROI.get(i).bitmap, "title", "descripton");

            Log.d("Answer", ": "+x+" Time Cost "+timeCost);
            answer += Label[x];
        }


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


    //ROI Rect 정보, 1*28*28 로 리사이즈된 Mat
    public List<Roi_class> ROI_Extractor(Box b, Mat img){
        List<MatOfPoint> contours = b.getList();
        List<Roi_class> result_Rect = new ArrayList<>();
        MatOfPoint tmp_point;
        Roi_class tmp_Roi;
        Rect tmp_rect;
        Mat tmp_mat_src, tmp_mat_dst;
        if(contours.size() == 0)
            return null;

        for(int idx = 0; idx>=0; idx = (int)b.getMat().get(0, idx)[0])
        {
            tmp_point = contours.get(idx);
            tmp_rect = Imgproc.boundingRect(tmp_point);

            tmp_mat_src = new Mat(img, tmp_rect);

            tmp_mat_dst = new Mat();
            //모델에 넣기위한 이미지 리사이즈
            Imgproc.resize(tmp_mat_src, tmp_mat_dst, new Size(IMG_WIDTH, IMG_HEIGHT));
            //Log.d("Img", tmp_mat_dst.dump());
            Core.multiply(tmp_mat_dst, new Scalar(255), tmp_mat_dst);
            tmp_mat_dst = tmp_mat_dst.t();
            Bitmap tmp_bitmap = Bitmap.createBitmap(IMG_WIDTH,
                    IMG_HEIGHT, Bitmap.Config.RGB_565);
            Utils.matToBitmap(tmp_mat_dst, tmp_bitmap);
            tmp_Roi = new Roi_class(tmp_rect, tmp_bitmap, tmp_mat_dst);
            result_Rect.add(tmp_Roi);
        }
        return result_Rect;
    }
    // 모델 파일 인터프리터를 생성하는 공통 함수
    // loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
    private Interpreter getTfliteInterpreter(String modelPath, Activity activity) {
        try {
            return new Interpreter(loadModelFile(activity, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    public Bitmap getResult() {
        return result;
    }

    public String getAnswer() {
        return answer;
    }

    public int classify(Mat mat)
    {
        //convertBitmapToByteBuffer(bitmap);
        convertBitmapToByteBuffer_v2(mat);
        Log.d("DeepLearning","Start");
        tf_lite.run(Test_Img, mResult);
        Log.d("DeepLearning","End");
        int mNumber = argmax(mResult[0]);

        return mNumber;
    }
    private void convertBitmapToByteBuffer_v2(Mat mat) {
        if (mImageData == null) {
            Log.d("mImageData", "null");
            return;
        }
        mImageData.rewind();


        for (int i = 0; i < IMG_HEIGHT; ++i) {
            for (int j = 0; j < IMG_WIDTH; ++j) {
                double[] temp = mat.get(i, j);
                float q =  (float)temp[0];

                //mImageData.putFloat(q);
                Test_Img[0][i][j][0] = q/255.0f;
            }
        }
    }

    private static int argmax(float[] probs){
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }
}
