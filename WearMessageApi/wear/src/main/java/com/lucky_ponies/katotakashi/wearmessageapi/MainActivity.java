package com.lucky_ponies.katotakashi.wearmessageapi;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener {
    //MessageApi関連
    private static final String TAG = "MainActivity";
    private GoogleApiClient googleApiClient;
    private int counter = 0;
    //sensor関連
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mStepCountSensor;
    private Sensor mStepDetectSensor;
    private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                setupWidgets();
                Log.d("click", "クリックしたよ");
            }
        });

        Log.d(TAG, "onCreate()");


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    private void setupWidgets() {
        findViewById(R.id.btn_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sendMessage = "Test Data API: " + counter;
                counter++;

                // UI Thread がブロックする可能性があるので新しいThreadを使う
                new SendToDataLayerThread("/path", sendMessage).start();

                Log.d(TAG, "SendToDataLayerThread()");
            }
        });
    }

    // Activity start で data layer に接続
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        googleApiClient.connect();
    }

    // data layer connection
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    // Activity stopで接続解除
    @Override
    protected void onStop() {
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class SendToDataLayerThread extends Thread {
        // Path とメッセージを定義
        String path;
        String handheldMessage;

        SendToDataLayerThread(String pth, String message) {
            path = pth;
            handheldMessage = message;
        }

        public void run() {
            Log.d(TAG, "SendToDataLayerThread()");

            NodeApi.GetConnectedNodesResult nodeResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
            for (Node node : nodeResult.getNodes()) {
                // 同期して呼び出し
                MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), path, handheldMessage.getBytes()).await();

                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "To: " + node.getDisplayName());
                    Log.d(TAG, "Message = " + handheldMessage);
                } else {
                    Log.d(TAG, "Send error");
                }
            }
        }
    }
}
