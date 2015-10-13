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
    private UpdateReceiver upReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Serviceからの取得メッセージ
        upReceiver = new UpdateReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_ACTION");
        registerReceiver(upReceiver, intentFilter);
        //画面の更新
        upReceiver.registerHandler(updateHandler);

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
            String message = bundle.getString("message");
            Log.d("Activityの名前", "はんどらーだよ" + message);
            acceleroTextView.setText(message);

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
