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
    private String messageKey = "/path";

    //sensor関連
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mStepCountSensor;
    private Sensor mStepDetectSensor;
    private Sensor mAccelerometer;
    private float x, y, z;
    private final float GAIN = 0.9f;
    private float hbBpm;

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

        //message apiのための準備
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //センサー取得の準備
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    private void setupWidgets() {
        findViewById(R.id.btn_toast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mobileへ送信メッセージ
                String sendMessage = "Test Data API: " + counter;
                counter++;

                // UI Thread がブロックする可能性があるので新しいThreadを使う
                new SendToDataLayerThread(messageKey, sendMessage).start();
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

    @Override
    protected void onResume() {
        super.onResume();
        /*SENSOR_DELAY_FASTEST 最高速でのセンサ読み出し
        SENSOR_DELAY_GAME	高速ゲーム向け
        SENSOR_DELAY_NORMAL 通常モード
        SENSOR_DELAY_UI 低速。ユーザインターフェイス向け*/
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);

        googleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        googleApiClient.disconnect();
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
        //        センサー値取得時の処理
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = (x * GAIN + event.values[0] * (1 - GAIN));
            y = (y * GAIN + event.values[1] * (1 - GAIN));
            z = (z * GAIN + event.values[2] * (1 - GAIN));

            Log.i("加速度センサー：", String.format("X : %f\nY : %f\nZ : %f\n", x, y, z));
            Log.i("心拍数：", String.valueOf(event.values[0]));

//            if (acceleroTextView != null)
//                acceleroTextView.setText(String.format("加速度\nX : %f\nY : %f\nZ : %f\n", x, y, z));
//            Log.i("加速度センサー：", String.format("X : %f\nY : %f\nZ : %f\n", x, y, z));
//        }
//
//        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
////            Log.i("心拍数：", String.valueOf(event.values[0]));
//            hbBpm = event.values[0];
//
//            if (hbTextView != null)
//                hbTextView.setText(String.format("心拍数\nbpm : %f", hbBpm));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //送信タスク　スレッド
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
