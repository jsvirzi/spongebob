package mam.lambo.squarepants;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    long lastlidarEventCount;
    long lastImageEventCount;
    long lastGpsEventCount;
    long lastImuEventCount;

    long lidarEventCount;
    long imageEventCount;
    long gpsEventCount;
    long imuEventCount;

    String runName;
    String posStatus;
    String insStatus;
    String gpsStatus;
    String rcvStatus;
    String timeStatus;
    int extStatus;
    int totalEvents;
    int fullEvents;
    int lateEvents;

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

    Handler handler;

    HttpClient httpClient;
    HttpPost httpPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        handler = new Handler();

        setup();
    }

    void initializeCounters() {
        lastlidarEventCount = 0;
        lastImageEventCount = 0;
        lastGpsEventCount = 0;
        lastImuEventCount = 0;
        lidarEventCount = 0;
        imageEventCount = 0;
        gpsEventCount = 0;
        imuEventCount = 0;
    }

    void setup() {

        initializeCounters();

        httpClient = new DefaultHttpClient();
        httpPost = new HttpPost("http://192.168.2.200:8086/status");

        heartBeat = new HeartBeat();
        timer = new Timer("HeartBeatTimer");
        timer.schedule(heartBeat, 1000, 1000);
    }

    private class HeartBeat extends TimerTask {
        public void run() {

            Status.OperationStatus operationStatus = null;
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
