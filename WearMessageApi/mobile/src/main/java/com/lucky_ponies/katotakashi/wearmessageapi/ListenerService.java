package com.lucky_ponies.katotakashi.wearmessageapi;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String TAG = "Wear Service";
    private String messageKeyAccelarate =  "accelerate";
    private String messageKeyHeatBeat =  "heatBeat";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.v(TAG, "onDataChanged");
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.v(TAG, "onMessageReceived");
//        showToast(messageEvent.getPath());
        final String message = new String(messageEvent.getData());

        if (messageEvent.getPath().equals(messageKeyAccelarate)) {
            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display
//            showToast(message);
            //MainActivityへ送信
            sendBroadCast(messageEvent.getPath(), message);

        }else if (messageEvent.getPath().equals(messageKeyHeatBeat)) {
            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display
//            showToast(message);
            //MainActivityへ送信
            sendBroadCast(messageEvent.getPath(), message);

        }else if (messageEvent.getPath().equals("/path")) {
            showToast(message);
        }
        else {
            super.onMessageReceived(messageEvent);
//            showToast(messageEvent.getPath());
            Log.d(TAG, messageEvent.getPath());
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //MainActivityへ送信
    protected void sendBroadCast(String messageTag, String message) {
        Intent broadcastIntent = new Intent();
        Log.d("sendBroadCast tag", messageTag);
        Log.d("sendBroadCast intent", message);
        broadcastIntent.putExtra(messageTag, message);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);
    }

}
