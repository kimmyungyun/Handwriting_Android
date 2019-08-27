package com.example.study;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private LinearLayout main;
    private NotepadView n;
    private LinearLayout m;
    private Button calc_btn, clear_btn;
    private TextView answer_tv;
    // Used to load the 'native-lib' library on application startup.
    //tessBaseAPI   Link : https://cosmosjs.blog.me/220937785735
    private String datapath = "";   //언어 데이터 세이브 경로
    private final int PERMISSIONS_REQUEST_RESULT = 1;

    static {
        System.loadLibrary("native-lib");
    }
    public void onResume()
    {
        super.onResume();
        // Link : https://stackoverflow.com/questions/35090838/no-implementation-found-for-long-org-opencv-core-mat-n-mat-error-using-opencv
        /****************** OpenCV 에러 때문에 작성한 코드 ****************/
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        /***************************************************************/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        request_Permission();
        init();

    }
    Button.OnClickListener onClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
            int id_number = view.getId();
            if(id_number == R.id.calc_btn){

                FindNumberInImage(n.getCanvasBitmap());

                //MediaStore.Images.Media.insertImage(getContentResolver(), n.getCanvasBitmap(), "title", "descripton");

                Log.d("Calc_btn", "Btn click");
            }
            else if(id_number == R.id.clear_btn){
                Log.d("Clear"," Btn click");
                n.Clear();
            }
        }
    };
    protected LinearLayout.LayoutParams initLayout(int weight){
        LinearLayout.LayoutParams result = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        result.weight = weight;

        return result;
    }

    protected void init(){
        main = (LinearLayout)findViewById(R.id.main_activity);
        n = new NotepadView(this, Color.BLACK);    //붓색깔을 검은색으로
        m = (LinearLayout) View.inflate(this, R.layout.btnview, null);
        answer_tv = (TextView)m.findViewById(R.id.answer_textView);

        n.setLayoutParams(initLayout(3));
        m.setLayoutParams(initLayout(1));
        main.addView(n);
        main.addView(m);
        calc_btn = (Button)findViewById(R.id.calc_btn);
        clear_btn = (Button)findViewById(R.id.clear_btn);
        calc_btn.setOnClickListener(onClickListener);
        clear_btn.setOnClickListener(onClickListener);
        /*********** 상태바 없애는 코드 ************/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /***********************/

        /**** TessData initialize*******/
        init_Tessdata(getFilesDir());
        /******************************/
    }
    private void init_Tessdata(File Dir){
        //언어파일 경로
        datapath = Dir+"/tesseract/";
        //트레이닝데이터가 카피되어 있는지 체크
        checkFile(new File(datapath+"tessdata/"));
    }
    protected void FindNumberInImage(Bitmap Image)
    {
        Bitmap result;
        Mat img = new Mat();
        Mat Gray_img = new Mat();
        Utils.bitmapToMat(Image, img);
        img.convertTo(Gray_img, CvType.CV_8UC1);
        //saveBitmapToJpeg(Image, "Test");
        MediaStore.Images.Media.insertImage(getContentResolver(), Image, "title", "descripton");

        Find_Number find_number = new Find_Number(Image, this.datapath);
        Log.d("TAGTAG1111", find_number.getAnswer());
        String ans = find_number.getAnswer();
        answer_tv.setText(ans);
/*
        result = Bitmap.createBitmap(img.cols(),
                img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(Gray_img, result);
        */
        result = find_number.getResult();
        n.setImageBitmap(result);
    }
    public native String stringFromJNI();
    //Opencv 에러 때문에 작성한 코드
    // Link : https://stackoverflow.com/questions/35090838/no-implementation-found-for-long-org-opencv-core-mat-n-mat-error-using-opencv
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    //Link : https://black-jin0427.tistory.com/179
    private void saveBitmapToJpeg(Bitmap bitmap, String name) {

        //내부저장소 캐시 경로를 받아옵니다.
        File storage = getCacheDir();

        //저장할 파일 이름
        String fileName = name + ".jpg";

        //storage 에 파일 인스턴스를 생성합니다.
        File tempFile = new File(storage, fileName);

        try {

            // 자동으로 빈 파일을 생성합니다.
            tempFile.createNewFile();

            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(tempFile);

            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            // 스트림 사용후 닫아줍니다.
            out.close();

        } catch (FileNotFoundException e) {
            Log.e("MyTag","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag","IOException : " + e.getMessage());
        }
    }

    //https://hyoseung930.tistory.com/60
    private void request_Permission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //권한을 거절하면 재 요청을 하는 함수
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_RESULT);
            }
        }
    }
    //https://hyoseung930.tistory.com/60
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (PERMISSIONS_REQUEST_RESULT == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "권한 요청을 해주세요.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }
    }

    //copy file to device
    //Link : https://cosmosjs.blog.me/220937785735
    private void copyFiles() {
        try{
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Link : https://cosmosjs.blog.me/220937785735
    private void checkFile(File dir){
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }

}
