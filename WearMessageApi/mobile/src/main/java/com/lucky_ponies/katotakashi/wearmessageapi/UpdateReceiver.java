package com.lucky_ponies.katotakashi.wearmessageapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by katotakashi on 15/10/13.
 */

public class UpdateReceiver extends BroadcastReceiver {
    public static Handler handler;
    private String messageTag;

    UpdateReceiver(String neWmessageTag){
        messageTag = neWmessageTag;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        String message = bundle.getString(messageTag);


        if (handler != null) {
            Message msg = new Message();

            Bundle data = new Bundle();
            data.putString(messageTag, message);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * メイン画面の表示を更新
     */
    public void registerHandler(Handler locationUpdateHandler) {
        handler = locationUpdateHandler;
    }

}
