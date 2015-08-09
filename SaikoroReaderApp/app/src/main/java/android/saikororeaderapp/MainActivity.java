package android.saikororeaderapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.nkzawa.socketio.client.IO.*;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "SaikoroOmikuji";

    private boolean doSend = false;
    private int saikoroPoint = 0;
    private long startTime = 0;

    private boolean inProc = false;
    private Mat mRgba;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // socket.io
        mSocket.on("saikoro start", onNewMessage);
        mSocket.connect();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        if(mRgba == null) {
            mRgba = inputFrame.rgba();
        }

        if (inProc) {
            return mRgba;
        }

        if (doSend == false) {
            return mRgba;
        }

        inProc = true;

        Mat imgSource = inputFrame.gray();
        Mat imgOutput = inputFrame.rgba();
        Mat imgCirclesOut = new Mat();

        Imgproc.GaussianBlur( imgSource, imgSource, new Size(5, 5), 2, 2 );
        Imgproc.HoughCircles( imgSource, imgCirclesOut, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 90, 35, 0, 100 );

        float circle[] = new float[3];

        for (int i = 0; i < imgCirclesOut.cols(); i++)
        {
            imgCirclesOut.get(0, i, circle);
//            Log.d("Circle", "Circle:" + circle[0] + ", " + circle[1] + ", " + circle[2]);
            org.opencv.core.Point center = new org.opencv.core.Point();
            center.x = circle[0];
            center.y = circle[1];
            Imgproc.circle(imgOutput, center, (int) circle[2], new Scalar(255,0,0,255), 4);
        }

        if(imgCirclesOut.cols() >= 4 && startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        if(saikoroPoint < imgCirclesOut.cols()) {
            saikoroPoint = imgCirclesOut.cols();
        }


        if(saikoroPoint >= 4 && System.currentTimeMillis() - startTime > 3000) {
            doSend = false;
            try {
                JSONObject json = new JSONObject();
                json.put("saikoro_point", saikoroPoint);
                post("http://nodeomikuji-dev.elasticbeanstalk.com/regist", json.toString());
            } catch (IOException ie) {
                Log.e("ERROR", ie.getMessage(), ie);
            } catch (JSONException je) {
                Log.e("ERROR", je.getMessage(), je);
            }
        }

        imgCirclesOut.release();
        imgSource.release();
        mRgba = imgOutput;
        inProc = false;
        return imgOutput;
    }

    private Socket mSocket;
    {
        try {
            mSocket = socket("http://nodeomikuji-dev.elasticbeanstalk.com/");
        } catch (URISyntaxException e) {}
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("SOCKET.IO", "RECV NEW MESSAGE");
            if(doSend) {
                return;
            }

            startTime = 0;
            saikoroPoint = 0;
            doSend = true;
        }
    };

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
