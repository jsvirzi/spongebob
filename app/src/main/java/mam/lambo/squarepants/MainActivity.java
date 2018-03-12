package mam.lambo.squarepants;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dataAcquisition.status.*; // OperationStatus;

public class MainActivity extends AppCompatActivity {

    Timer timer;
    HeartBeat heartBeat;

    long lastlidarEventCount = 0;
    long lastImageEventCount = 0;
    long lastGpsEventCount = 0;
    long lastImuEventCount = 0;

    long lidarEventCount = 0;
    long imageEventCount = 0;
    long gpsEventCount = 0;
    long imuEventCount = 0;

    boolean displayThea = false;

    boolean blinkingIns = false;
    boolean blinkingAln = false;
    boolean blinkingSat1 = false;
    boolean blinkingSat2 = false;
    boolean blinkingPos1 = false;
    boolean blinkingPos2 = false;

    boolean prevBlinkingIns = false;
    boolean prevBlinkingAln = false;
    boolean prevBlinkingSat1 = false;
    boolean prevBlinkingSat2 = false;
    boolean prevBlinkingPos1 = false;
    boolean prevBlinkingPos2 = false;

    int colorIns = Color.GRAY;
    int colorAln = Color.GRAY;
    int colorSat1 = Color.GRAY;
    int colorSat2 = Color.GRAY;
    int colorPos1 = Color.GRAY;
    int colorPos2 = Color.GRAY;

    ObjectAnimator blinkerIns;
    ObjectAnimator blinkerAln;
    ObjectAnimator blinkerSat1;
    ObjectAnimator blinkerSat2;
    ObjectAnimator blinkerPos1;
    ObjectAnimator blinkerPos2;

    String runName;
    String posStatus;
    String insStatus;
    String gpsStatus;
    String rcvStatus;
    String timeStatus;
    int extStatus = 0;
    int totalEvents = 0;
    int fullEvents = 0;
    int lateEvents = 0;

    final long kExpectedEvents = 20;
    final long kMininumEvents = kExpectedEvents / 2;

    TextView textViewImageEventCount;
    TextView textViewLidarEventCount;
    TextView textViewGpsEventCount;
    TextView textViewImuEventCount;
    TextView textViewTotalEvents;
    TextView textViewFullEvents;
    TextView textViewMissedEvents;
    TextView textViewPosStatus;
    TextView textViewGpsStatus;
    TextView textViewInsStatus;
    TextView textViewExtStatus;
    TextView textViewRcvStatus;
    TextView textViewTimeStatus;

    ProgressBar progressBarSigmaLatFine;
    ProgressBar progressBarSigmaLonFine;
    ProgressBar progressBarSigmaLatCoarse;
    ProgressBar progressBarSigmaLonCoarse;

    Button buttonLaunchThea;
    Button buttonDisplayThea;
    Button buttonExitThea;
    Button buttonLaunchSquidward;
    Button buttonExitSquidward;
    Button buttonIns;
    Button buttonAln;
    Button buttonSat1;
    Button buttonSat2;
    Button buttonPos1;
    Button buttonPos2;

    Handler handler;
    HandlerThread thread;

    HttpClient httpClient;
    HttpPost httpPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        progressBarSigmaLatFine = (ProgressBar) findViewById(R.id.sigmaLatFine);
        progressBarSigmaLonFine = (ProgressBar) findViewById(R.id.sigmaLonFine);
        progressBarSigmaLatCoarse = (ProgressBar) findViewById(R.id.sigmaLatCoarse);
        progressBarSigmaLonCoarse = (ProgressBar) findViewById(R.id.sigmaLonCoarse);

        textViewLidarEventCount = (TextView) findViewById(R.id.lidarevents);
        textViewImageEventCount = (TextView) findViewById(R.id.imageevents);
        textViewGpsEventCount = (TextView) findViewById(R.id.gpsevents);
        textViewImuEventCount = (TextView) findViewById(R.id.imuevents);
        textViewFullEvents = (TextView) findViewById(R.id.fullevents);
        textViewMissedEvents = (TextView) findViewById(R.id.missevents);
        textViewTotalEvents = (TextView) findViewById(R.id.totalevents);
        textViewPosStatus = (TextView) findViewById(R.id.posstat);
        textViewGpsStatus = (TextView) findViewById(R.id.gpsstat);
        textViewInsStatus = (TextView) findViewById(R.id.insstat);
        textViewExtStatus = (TextView) findViewById(R.id.extstat);
        textViewRcvStatus = (TextView) findViewById(R.id.rcvstat);
        textViewTimeStatus = (TextView) findViewById(R.id.timestat);

        buttonIns = (Button) findViewById(R.id.dispInsStatus);
        buttonAln = (Button) findViewById(R.id.dispAlnStatus);
        buttonSat1 = (Button) findViewById(R.id.dispNumberSatellites1);
        buttonSat2 = (Button) findViewById(R.id.dispNumberSatellites2);
        buttonPos1 = (Button) findViewById(R.id.dispPosStatus1);
        buttonPos2 = (Button) findViewById(R.id.dispPosStatus2);

        thread = new HandlerThread("worker");
        thread.start();
        handler = new Handler(thread.getLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if ((prevBlinkingIns == false) && (blinkingIns == true)) {
                        blinkerIns.start();
                    } else if ((prevBlinkingIns == true) && (blinkingIns == true)) {
                        blinkerIns.end();
                    }
                    prevBlinkingIns = blinkingIns;
                }
            }
        };
//        handler.post(runnable);

//        runOnUiThread(runnable);

        blinkerIns = ObjectAnimator.ofInt(buttonIns, "textColor", Color.BLACK, Color.TRANSPARENT);
        buttonIns.setBackgroundColor(Color.GREEN);
        blinkerIns.setDuration(500); //duration of flash
        blinkerIns.setEvaluator(new ArgbEvaluator());
        blinkerIns.setRepeatCount(ValueAnimator.INFINITE);
        blinkerIns.setRepeatMode(ValueAnimator.REVERSE);
        blinkerIns.start();

        buttonAln.setBackgroundColor(Color.GREEN);
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(200);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        buttonAln.startAnimation(mAnimation);

        buttonLaunchThea = (Button) findViewById(R.id.launchthea);
        buttonLaunchThea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        // HttpPost httpPost = new HttpPost("http://192.168.2.200:8087/launchthea"); /* goes to rocket launcher */
                        HttpPost httpPost = new HttpPost("http://192.168.2.200:8087/command?launch=thea"); /* goes to rocket launcher */
                        // HttpPost httpPost = new HttpPost("192.168.2.200:8087/command?launch=thea"); /* goes to rocket launcher */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        buttonDisplayThea = (Button) findViewById(R.id.displaythea);
        buttonDisplayThea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayThea = !displayThea;
                if (displayThea) {
                    buttonDisplayThea.setText("MUTE");
                } else {
                    buttonDisplayThea.setText("DISPLAY");
                }
            }
        });

        buttonExitThea = (Button) findViewById(R.id.exitthea);
        buttonExitThea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://192.168.2.200:8086/command?action=terminate"); /* goes directly to thea */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
//                        operationStatus = Status.OperationStatus.parseFrom(inputStream);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
//                    if (response != null) {
//                        try {
//                            response.();
//                        } catch (IOException ex) {
//                        }
//                    }
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        progressBarSigmaLonFine.setProgress(10);
        progressBarSigmaLonCoarse.setProgress(100);

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost("http://192.168.2.200:8086/status");

        /* this must be last item as it uses all resources */
        heartBeat = new HeartBeat();
        timer = new Timer("HeartBeatTimer");
        timer.schedule(heartBeat, 1000, 1000);
    }

    private class HeartBeat extends TimerTask {
        public void run() {

            if (displayThea == false) { return; }

            Status.OperationStatus operationStatus = null;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://192.168.2.200:8086/status");

            if (true) {
                // CloseableHttpClient httpClient = HttpClients.createDefault();

//                List<NameValuePair> params = new ArrayList<NameValuePair>();
//                params.add(new BasicNameValuePair("username", "John"));
//                params.add(new BasicNameValuePair("password", "pass"));
//                try {
//                    httpPost.setEntity(new UrlEncodedFormEntity(params));
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
                // httpPost = new HttpPost("http://abcya.com");
                // CloseableHttpResponse response = null;
                HttpResponse response;
                try {
                    response = httpClient.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream inputStream = entity.getContent();
                        operationStatus = Status.OperationStatus.parseFrom(inputStream);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                if (response != null) {
//                    try {
//                        response.close();
//                    } catch (IOException ex) {
//                    }
//                }
                }
            }

            if (operationStatus != null) {
                lidarEventCount = operationStatus.getLidarCount();
                imageEventCount = operationStatus.getImageCount();
                gpsEventCount = operationStatus.getGpsCount();
                imuEventCount = operationStatus.getImuCount();
                runName = operationStatus.getRunName();
                posStatus = operationStatus.getPosStatus();
                insStatus = operationStatus.getInsStatus();
                gpsStatus = operationStatus.getGpsStatus();
                rcvStatus = operationStatus.getReceiverStatus();
                extStatus = operationStatus.getExtStatus();
                timeStatus = operationStatus.getTimeStatus();
                totalEvents = operationStatus.getTotalPb();
                fullEvents = operationStatus.getFullPb();
                lateEvents = operationStatus.getLatePb();
            }

            final String lidarEventCountString = String.format("%d", lidarEventCount);
            final String imageEventCountString = String.format("%d", imageEventCount);
            final String gpsEventCountString = String.format("%d", gpsEventCount);
            final String imuEventCountString = String.format("%d", imuEventCount);
            final String posStatusString = String.format("%s", posStatus);
            final String insStatusString = String.format("%s", insStatus);
            final String gpsStatusString = String.format("%s", gpsStatus);
            final String rcvStatusString = String.format("%s", rcvStatus);
            final String timeStatusString = String.format("%s", timeStatus);

            final int timeStatusStringColor;
            if (timeStatus.equalsIgnoreCase("finesteering")) {
                timeStatusStringColor = Color.GREEN;
            } else {
                timeStatusStringColor = Color.RED;
            }

            final int posStatusStringColor;
            if (posStatus.equalsIgnoreCase("ins_rtkfixed")) {
                posStatusStringColor = Color.GREEN;
            } else {
                posStatusStringColor = Color.RED;
            }

            final int insStatusStringColor;
            if (insStatus.equalsIgnoreCase("ins_solution_good")) {
                insStatusStringColor = Color.GREEN;
            } else {
                insStatusStringColor = Color.RED;
            }

            final String totalEventsString = String.format("%d", totalEvents);
            double ratio = (double) fullEvents / (double) totalEvents;
            final String fullEventsString = String.format("%d (%8.6f)", fullEvents, ratio);
            final String missedEventsString = String.format("%d", totalEvents - fullEvents);

            long eventCounter;

            eventCounter = lidarEventCount - lastlidarEventCount;
            lastlidarEventCount = lidarEventCount;
            final int lidarEventCountColor;
            if (eventCounter >= (kExpectedEvents - 1)) {
                lidarEventCountColor = Color.GREEN;
            } else if (eventCounter > kMininumEvents) {
                lidarEventCountColor = Color.YELLOW;
            } else {
                lidarEventCountColor = Color.RED;
            }

            eventCounter = imageEventCount - lastImageEventCount;
            lastImageEventCount = imageEventCount;
            final int imageEventCountColor;
            if (eventCounter >= (kExpectedEvents - 1)) {
                imageEventCountColor = Color.GREEN;
            } else if (eventCounter > kMininumEvents) {
                imageEventCountColor = Color.YELLOW;
            } else {
                imageEventCountColor = Color.RED;
            }

            eventCounter = gpsEventCount - lastGpsEventCount;
            lastGpsEventCount = gpsEventCount;
            final int gpsEventCountColor;
            if (eventCounter >= (kExpectedEvents - 1)) {
                gpsEventCountColor = Color.GREEN;
            } else if (eventCounter > kMininumEvents) {
                gpsEventCountColor = Color.YELLOW;
            } else {
                gpsEventCountColor = Color.RED;
            }

            eventCounter = imuEventCount - lastImuEventCount;
            lastImuEventCount = imuEventCount;
            final int imuEventCountColor;
            if (eventCounter >= (kExpectedEvents - 1)) {
                imuEventCountColor = Color.GREEN;
            } else if (eventCounter > kMininumEvents) {
                imuEventCountColor = Color.YELLOW;
            } else {
                imuEventCountColor = Color.RED;
            }

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    textViewLidarEventCount.setText(lidarEventCountString);
                    textViewLidarEventCount.setBackgroundColor(lidarEventCountColor);
                    textViewImageEventCount.setText(imageEventCountString);
                    textViewImageEventCount.setBackgroundColor(imageEventCountColor);
                    textViewGpsEventCount.setText(gpsEventCountString);
                    textViewGpsEventCount.setBackgroundColor(gpsEventCountColor);
                    textViewImuEventCount.setText(imuEventCountString);
                    textViewImuEventCount.setBackgroundColor(imuEventCountColor);
                    textViewTotalEvents.setText(totalEventsString);
                    textViewFullEvents.setText(fullEventsString);
                    textViewMissedEvents.setText(missedEventsString);
                    textViewGpsStatus.setText(gpsStatusString);
                    textViewInsStatus.setText(insStatusString);
                    textViewInsStatus.setBackgroundColor(insStatusStringColor);
                    textViewPosStatus.setText(posStatusString);
                    textViewPosStatus.setBackgroundColor(posStatusStringColor);
                    textViewRcvStatus.setText(rcvStatusString);
                    textViewTimeStatus.setText(timeStatusString);
                    textViewTimeStatus.setBackgroundColor(timeStatusStringColor);
                }
            };

            MainActivity.this.runOnUiThread(runnable);
        }
    }
}
