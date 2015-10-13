package com.lucky_ponies.katotakashi.wearmessageapi;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String TAG = "Wear Service";
    private String messageKey =  "/path";

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
        if (messageEvent.getPath().equals(messageKey)) {

            final String message = new String(messageEvent.getData());

            Log.d(TAG, "Message path received on watch is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display
//            showToast(message);
            //MainActivityへ送信
            sendBroadCast(message);

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
    protected void sendBroadCast(String message) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra("message", message);
        broadcastIntent.setAction("UPDATE_ACTION");
        getBaseContext().sendBroadcast(broadcastIntent);

    }

}
