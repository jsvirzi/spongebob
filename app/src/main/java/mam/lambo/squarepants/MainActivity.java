package mam.lambo.squarepants;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import dataAcquisition.*; // OperationStatus;

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
    TextView textViewTextLidarEventCount;
    TextView textViewGpsEventCount;
    TextView textViewImuEventCount;
    TextView textViewTotalEvents;
    TextView textViewFullEvents;
    TextView textViewMissedEvents;
    TextView textViewPosStatus;
    TextView textViewTextPosStatus;
    TextView textViewGpsStatus;
    TextView textViewGpsLat;
    TextView textViewGpsLon;
    TextView textViewInsStatus;
    TextView textViewInsLat;
    TextView textViewInsLon;
    TextView textViewTextInsStatus;
    TextView textViewTextExtStatus;
    TextView textViewExtStatus;
    TextView textViewRcvStatus;
    TextView textViewTextRcvStatus;
    TextView textViewTimeStatus;

    ProgressBar progressBarSigmaLatFine;
    ProgressBar progressBarSigmaLonFine;
    ProgressBar progressBarSigmaLatCoarse;
    ProgressBar progressBarSigmaLonCoarse;

    Button buttonLaunchThea;
    int buttonLaunchTheaColor = Color.LTGRAY;

    Button buttonDisplayThea;
    Button buttonExitThea;

    Button buttonLaunchSquidward;
    int buttonLaunchSquidwardColor = Color.LTGRAY;

    Button buttonExitSquidward;

    Button buttonIns;

    Button buttonAln;

    Button buttonSat1;

    Button buttonSat2;

    Button buttonPos1;

    Button buttonPos2;

    Button buttonConnectThea;
    boolean connectedThea;
    int buttonConnectTheaColor;

    Button buttonConnectSquidward;
    boolean connectedSquidward;
    int buttonConnectSquidwardColor;

    RadioGroup radioGroupHardware;
    RadioButton radioButtonGroundTruth;
    RadioButton radioButtonSensorBoard;
    final int HardwareGroundTruth = 0;
    final int HardwareSensorBoard = 1;
    int hardwareSelect = HardwareGroundTruth;

    Handler handler;
    HandlerThread thread;

    HttpClient httpClient;
    HttpPost httpPost;

    String theaIpAddress = "192.168.2.200";
    int theaPort = 8086;
    int squidwardPort = 8086;
    String squidwardIpAddress = "192.168.2.100";
    int rocketLauncherPort = 8087;

    boolean launchTheaRequest = false;
    boolean theaLaunched = false;
    boolean launchSquidwardRequest = false;
    boolean squidwardLaunched = false;

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
        textViewTextLidarEventCount = (TextView) findViewById(R.id.textlidarevents);
        textViewImageEventCount = (TextView) findViewById(R.id.imageevents);
        textViewGpsEventCount = (TextView) findViewById(R.id.gpsevents);
        textViewImuEventCount = (TextView) findViewById(R.id.imuevents);
        textViewFullEvents = (TextView) findViewById(R.id.fullevents);
        textViewMissedEvents = (TextView) findViewById(R.id.missevents);
        textViewTotalEvents = (TextView) findViewById(R.id.totalevents);
        textViewPosStatus = (TextView) findViewById(R.id.posstat);
        textViewTextPosStatus = (TextView) findViewById(R.id.textposstat);
        textViewGpsStatus = (TextView) findViewById(R.id.gpsstat);
        textViewGpsLat = (TextView) findViewById(R.id.gpslat);
        textViewGpsLon = (TextView) findViewById(R.id.gpslon);
        textViewInsStatus = (TextView) findViewById(R.id.insstat);
        textViewInsLat = (TextView) findViewById(R.id.inslat);
        textViewInsLon = (TextView) findViewById(R.id.inslon);
        textViewTextInsStatus = (TextView) findViewById(R.id.textinsstat);
        textViewExtStatus = (TextView) findViewById(R.id.extstat);
        textViewTextExtStatus = (TextView) findViewById(R.id.textextstat);
        textViewRcvStatus = (TextView) findViewById(R.id.rcvstat);
        textViewTextRcvStatus = (TextView) findViewById(R.id.textrcvstat);
        textViewTimeStatus = (TextView) findViewById(R.id.timestat);

        buttonIns = (Button) findViewById(R.id.dispInsStatus);
        buttonAln = (Button) findViewById(R.id.dispAlnStatus);
        buttonSat1 = (Button) findViewById(R.id.dispNumberSatellites1);
        buttonSat2 = (Button) findViewById(R.id.dispNumberSatellites2);
        buttonPos1 = (Button) findViewById(R.id.dispPosStatus1);
        buttonPos2 = (Button) findViewById(R.id.dispPosStatus2);

        radioGroupHardware = (RadioGroup) findViewById(R.id.hardware);
        radioGroupHardware.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.groundtruth) {
                    setupGroundTruth();
                } else if (checkedId == R.id.sensorboard) {
                    setupSensorBoard();
                }
            }
        });


        radioButtonGroundTruth = (RadioButton) findViewById(R.id.groundtruth);
        radioButtonSensorBoard = (RadioButton) findViewById(R.id.sensorboard);

        buttonConnectThea = (Button) findViewById(R.id.connectthea);
        connectedThea = false;
        buttonConnectTheaColor = Color.LTGRAY;
        setButtonColor(buttonConnectThea, buttonLaunchTheaColor);
        buttonConnectThea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String url = String.format("http://%s:%d/status?state=self", theaIpAddress, rocketLauncherPort);
                        HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                                if (inputStream.equals("running")) {
                                    buttonConnectTheaColor = Color.GREEN;
                                    setButtonColor(buttonConnectThea, buttonConnectTheaColor);
                                }
                            }
                        } catch (Exception e) {
                            buttonConnectTheaColor = Color.RED;
                            setButtonColor(buttonConnectThea, buttonConnectTheaColor);
                            e.printStackTrace();
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        buttonConnectSquidward = (Button) findViewById(R.id.connectsquidward);
        connectedSquidward = false;
        buttonConnectSquidwardColor = Color.LTGRAY;
        setButtonColor(buttonConnectSquidward, buttonLaunchSquidwardColor);
        buttonConnectSquidward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String url = String.format("http://%s:%d/status?state=self", squidwardIpAddress, rocketLauncherPort);
                        HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                                if (inputStream.equals("running")) {
                                    buttonConnectSquidwardColor = Color.GREEN;
                                    setButtonColor(buttonConnectSquidward, buttonConnectSquidwardColor);
                                }
                            }
                        } catch (Exception e) {
                            buttonConnectSquidwardColor = Color.RED;
                            setButtonColor(buttonConnectSquidward, buttonConnectSquidwardColor);
                            e.printStackTrace();
                        } finally {
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        if (hardwareSelect == HardwareGroundTruth) {
            radioButtonGroundTruth.setChecked(true);
        } else if (hardwareSelect == HardwareSensorBoard) {
            radioButtonSensorBoard.setChecked(true);
        } else { /* shouldn't be here */
            hardwareSelect = HardwareGroundTruth;
            radioButtonGroundTruth.setChecked(true);
        }

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
        setButtonColor(buttonLaunchThea, buttonLaunchTheaColor);
        buttonLaunchThea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String url = String.format("http://%s:%d/command?launch=thea", theaIpAddress, rocketLauncherPort);
                        if (radioButtonGroundTruth.isChecked()) {
                            url += "&hardware=groundtruth";
                        } else if (radioButtonSensorBoard.isChecked()) {
                            url += "&hardware=sensorboard";
                        }
                        /* begin TODO */
                        theaLaunched = false;
                        launchTheaRequest = true;
                        /* end TODO */
                        HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
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

        buttonLaunchSquidward = (Button) findViewById(R.id.launchsquidward);
        buttonLaunchSquidward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String url = String.format("http://%s:%d/command?launch=squidward", squidwardIpAddress, rocketLauncherPort);
                        HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                                theaLaunched = false;
                                launchTheaRequest = true;
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
                        String url = String.format("http://%s:%d/command?action=terminate", theaIpAddress, theaPort);
                        HttpPost httpPost = new HttpPost(url); /* goes directly to thea */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        buttonExitSquidward = (Button) findViewById(R.id.exitsquidward);
        buttonExitSquidward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        HttpClient httpClient = new DefaultHttpClient();
                        String url = String.format("http://%s:%d/command?action=terminate", squidwardIpAddress, theaPort);
                        HttpPost httpPost = new HttpPost(url); /* goes directly to squidward */
                        HttpResponse response = null;
                        try {
                            response = httpClient.execute(httpPost);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                InputStream inputStream = entity.getContent();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                handler.post(runnable);
            }
        });

        progressBarSigmaLonFine.setProgress(10);
        progressBarSigmaLonCoarse.setProgress(100);

        httpClient = new DefaultHttpClient();
//        httpPost = new HttpPost("http://192.168.2.200:8086/status");

        buttonLaunchThea.setEnabled(false);
        buttonLaunchSquidward.setEnabled(false);
        buttonExitThea.setEnabled(false);
        buttonExitSquidward.setEnabled(false);
        buttonDisplayThea.setEnabled(false);

        /* this must be last item as it uses all resources */
        heartBeat = new HeartBeat();
        timer = new Timer("HeartBeatTimer");
        timer.schedule(heartBeat, 1000, 1000);
    }

    private void setupGroundTruth() {
        TextView textView;

        textView = textViewTextExtStatus;
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextRcvStatus;
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextLidarEventCount;
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextPosStatus;
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextInsStatus;
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setupSensorBoard() {
        TextView textView;

        textView = textViewTextExtStatus;
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextRcvStatus;
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextLidarEventCount;
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextPosStatus;
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        textView = textViewTextInsStatus;
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setButtonColor(final Button button, final int color) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                button.setBackgroundColor(color);
            }
        };
        MainActivity.this.runOnUiThread(runnable);
    }

    private void updateLaunchTheaRequestStatus() {
        if ((launchTheaRequest == true) && (theaLaunched == false)) {
            HttpClient httpClient = new DefaultHttpClient();
            String url = String.format("http://%s:%d/status?state=self", theaIpAddress, theaPort);
            HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    if (inputStream.equals("running")) {
                        launchTheaRequest = false;
                        theaLaunched = true;
                        buttonLaunchTheaColor = Color.GREEN;
                        setButtonColor(buttonLaunchThea, buttonLaunchTheaColor);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                buttonLaunchTheaColor = (buttonLaunchTheaColor == Color.LTGRAY) ? Color.RED : Color.LTGRAY;
                setButtonColor(buttonLaunchThea, buttonLaunchTheaColor);
            }
        }
    }

    private void updateLaunchSquidwardRequestStatus() {
        if ((launchSquidwardRequest == true) && (squidwardLaunched == false)) {
            HttpClient httpClient = new DefaultHttpClient();
            String url = String.format("http://%s:%d/status?state=self", squidwardIpAddress, theaPort);
            HttpPost httpPost = new HttpPost(url); /* goes to rocket launcher */
            HttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    if (inputStream.equals("running")) {
                        launchSquidwardRequest = false;
                        squidwardLaunched = true;
                        buttonLaunchSquidwardColor = Color.GREEN;
                        setButtonColor(buttonLaunchSquidward, buttonLaunchSquidwardColor);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                buttonLaunchSquidwardColor = (buttonLaunchSquidwardColor == Color.LTGRAY) ? Color.RED : Color.LTGRAY;
                setButtonColor(buttonLaunchSquidward, buttonLaunchSquidwardColor);
            }
        }
    }

    private Status.OperationStatus updateTheaStatus() {
        Status.OperationStatus operationStatus = null;
        if (displayThea == true) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://192.168.2.200:8086/status");
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
            }
        }
        return operationStatus;
    }

    private class HeartBeat extends TimerTask {
        public void run() {

            updateLaunchTheaRequestStatus();

            updateLaunchSquidwardRequestStatus();

            if (displayThea == false) { return; }

            Status.OperationStatus operationStatus = updateTheaStatus();

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
            if ((timeStatus != null) && timeStatus.equalsIgnoreCase("finesteering")) {
                timeStatusStringColor = Color.GREEN;
            } else {
                timeStatusStringColor = Color.RED;
            }

            final int posStatusStringColor;
            if (hardwareSelect == HardwareGroundTruth) {
                if ((posStatus != null) && posStatus.equalsIgnoreCase("ins_rtkfixed")) {
                    posStatusStringColor = Color.GREEN;
                } else {
                    posStatusStringColor = Color.RED;
                }
            } else if (hardwareSelect == HardwareSensorBoard) {
                posStatusStringColor = Color.GRAY;
            } else {
                posStatusStringColor = Color.GRAY;
            }

            final int insStatusStringColor;
            if (hardwareSelect == HardwareGroundTruth) {
                if ((insStatus != null) && insStatus.equalsIgnoreCase("ins_solution_good")) {
                    insStatusStringColor = Color.GREEN;
                } else {
                    insStatusStringColor = Color.RED;
                }
            } else if (hardwareSelect == HardwareSensorBoard) {
                insStatusStringColor = Color.GRAY;
            } else {
                insStatusStringColor = Color.GRAY;
            }

            final String totalEventsString = String.format("%d", totalEvents);
            double ratio = 0;
            if (totalEvents != 0) {
                ratio = (double) fullEvents / (double) totalEvents;
            }
            final String fullEventsString = String.format("%d (%8.6f)", fullEvents, ratio);
            final String missedEventsString = String.format("%d", totalEvents - fullEvents);

            long eventCounter;

            eventCounter = lidarEventCount - lastlidarEventCount;
            lastlidarEventCount = lidarEventCount;
            final int lidarEventCountColor;
            if (hardwareSelect == HardwareGroundTruth) {
                if (eventCounter >= (kExpectedEvents - 1)) {
                    lidarEventCountColor = Color.GREEN;
                } else if (eventCounter > kMininumEvents) {
                    lidarEventCountColor = Color.YELLOW;
                } else {
                    lidarEventCountColor = Color.RED;
                }
            } else if (hardwareSelect == HardwareSensorBoard) {
                lidarEventCountColor = Color.GRAY;
            } else {
                lidarEventCountColor = Color.GRAY;
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

            final float gpsLat = operationStatus.getGpsLat();
            final float gpsLon = operationStatus.getGpsLon();
            final double insLat = operationStatus.getInsLat();
            final double insLon = operationStatus.getInsLon();

            final String gpsLatString = String.format("%.8f", gpsLat);
            final String gpsLonString = String.format("%.8f", gpsLon);
            final String insLatString = String.format("%.8f", insLat);
            final String insLonString = String.format("%.8f", insLon);

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
                    textViewGpsLat.setText(gpsLatString);
                    textViewGpsLon.setText(gpsLonString);
                    textViewInsLat.setText(insLatString);
                    textViewInsLon.setText(insLonString);
                }
            };
            MainActivity.this.runOnUiThread(runnable);
        }
    }
}
