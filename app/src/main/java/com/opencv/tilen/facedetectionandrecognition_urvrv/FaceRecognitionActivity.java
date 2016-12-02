package com.opencv.tilen.facedetectionandrecognition_urvrv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class FaceRecognitionActivity extends Activity {
    public final static String RESOURCENAME = "resource_name";
    public final static String FACENUMBER = "face_number;";

    private ImageView mImage;

    private Bitmap[] facePictures;
    private String pictureName;

    private static final int SPEECH_REQUEST = 0;

    private TextToSpeech textToSpeech;

    private FaceRecognition faceRecognition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener(){

            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        faceRecognition = FaceRecognition.getInstance(this);
        initializeFaceImages();
        textToSpeech.setSpeechRate(1);

        opencv_core.IplImage[] iplImages = getImages();
        Mat image = MyUtils.bitmapToMat(facePictures[0]);

        String names = "";

        for(int i = 0; i < iplImages.length; i++) {
            String name = faceRecognition.predict(iplImages[i]);
            Core.putText(image, name, MyUtils.GetRecs()[i].tl(), Core.FONT_HERSHEY_COMPLEX, 1.0, new Scalar(0, 255, 0));
            if(i != 0)
                names += ", ";
            names += name;
        }

        final String namestoSay = names;


        mImage = new ImageView(this);
        mImage.setImageBitmap(MyUtils.matToBitmap(image));
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(mImage);

        Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() { // Function runs every MINUTES minutes.
                // Run the code you want here
                textToSpeech.speak(namestoSay, TextToSpeech.QUEUE_FLUSH, // old API level method, since we use 19 is ok (deprecated in 21)
                        null);
                Log.d("asd", namestoSay);
            }
        }, 1000 * 3);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() { // Function runs every MINUTES minutes.
                // Run the code you want here
                finish();
            }
        }, 1000 * 10);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS||
                featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.menu_faces, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    private void initializeFaceImages()
    {
        Intent intent = getIntent();
        pictureName = intent.getStringExtra(RESOURCENAME);
        int numberOfFaces = intent.getIntExtra(FACENUMBER, -1);
        facePictures = MyUtils.loadBitmaps(numberOfFaces, this);

    }

    private opencv_core.IplImage[] getImages()
    {
        opencv_core.IplImage[] iplImages = new opencv_core.IplImage[facePictures.length - 1];
        for(int i = 0; i < facePictures.length - 1; i++)
            if(facePictures[i + 1] != null)
            iplImages[i] = MyUtils.BitmapToIplImage(facePictures[i + 1]);
        return iplImages;
    }
}
