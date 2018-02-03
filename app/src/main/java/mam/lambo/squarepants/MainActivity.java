package mam.lambo.squarepants;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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
import java.util.Timer;
import java.util.TimerTask;

import dataAcquisition.status.*; // OperationStatus;

public class MainActivity extends AppCompatActivity {

    HttpPost httpPost;
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

    final long kExpectedEvents = 20;
    final long kMininumEvents = kExpectedEvents / 2;

    TextView textViewImageEventCount;
    TextView textViewLidarEventCount;
    TextView textViewGpsEventCount;
    TextView textViewImuEventCount;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLidarEventCount = (TextView) findViewById(R.id.lidarevents);
        textViewImageEventCount = (TextView) findViewById(R.id.imageevents);
        textViewGpsEventCount = (TextView) findViewById(R.id.gpsevents);
        textViewImuEventCount = (TextView) findViewById(R.id.imuevents);

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

        if (false) {
            Status.OperationStatus operationStatus;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            httpPost = new HttpPost("192.168.2.201:8086/status");
            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    int byteOne = instream.read();
                    int byteTwo = instream.read();
                    // Do not need the rest
                }
            } catch (IOException ex) {
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }

        heartBeat = new HeartBeat();
        timer = new Timer("HeartBeatTimer");
        timer.schedule(heartBeat, 1000, 1000);
    }

    private class HeartBeat extends TimerTask {
        public void run() {
            long eventCounter;
            final String lidarEventCountString = String.format("%d", lidarEventCount);
            final String imageEventCountString = String.format("%d", imageEventCount);
            final String gpsEventCountString = String.format("%d", gpsEventCount);
            final String imuEventCountString = String.format("%d", imuEventCount);

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
                }
            };

            MainActivity.this.runOnUiThread(runnable);
        }
    }
}
