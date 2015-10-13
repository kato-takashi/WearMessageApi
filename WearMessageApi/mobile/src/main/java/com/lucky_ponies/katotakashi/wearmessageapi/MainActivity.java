package com.lucky_ponies.katotakashi.wearmessageapi;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {
    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    private TextView acceleroTextView;
    private TextView hbTextView;
    private UpdateReceiver upReceiverAc;
    private UpdateReceiver upReceiverHb;
    private IntentFilter intentFilterAc;
    private String messageTagAc = "accelerate";
    private String messageTagHb = "heatBeat";
    private IntentFilter intentFilterHb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //加速度
        //Serviceからの取得メッセージ
        upReceiverAc = new UpdateReceiver(messageTagAc);
        intentFilterAc = new IntentFilter();
        intentFilterAc.addAction("UPDATE_ACTION");
        registerReceiver(upReceiverAc, intentFilterAc);
        //画面の更新
        upReceiverAc.registerHandler(updateHandler);

        //心拍数
        //Serviceからの取得メッセージ
        upReceiverHb = new UpdateReceiver(messageTagHb);
        intentFilterHb = new IntentFilter();
        intentFilterHb.addAction("UPDATE_ACTION");
        registerReceiver(upReceiverHb, intentFilterHb);
        //画面の更新
        upReceiverHb.registerHandler(updateHandler);

        //センサー値の出力
        acceleroTextView = (TextView) findViewById(R.id.acceleroText);
        hbTextView = (TextView) findViewById(R.id.hbText);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();

    }
    // サービスから値を受け取ったら動かしたい内容を書く
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if(bundle.getString(messageTagAc) != null){
                String message = bundle.getString("accelerate");
                Log.d("Activityの名前", "はんどらーだよac" + message);
                acceleroTextView.setText(message);
            }else if(bundle.getString("heatBeat") != null){
                String message = bundle.getString("heatBeat");
                Log.d("Activityの名前", "はんどらーだよhb" + message);
                hbTextView.setText("BPM: " + message + "BPM");
            }else if(bundle.getString("/path") != null){
                String message = bundle.getString("/path");
                Log.d("Activityの名前", "はんどらーだよpath" + message);
            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Mobile onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Mobile onConnectionSuspended");

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "Mobile onMessageReceived : " + messageEvent.getPath());
    }
}
