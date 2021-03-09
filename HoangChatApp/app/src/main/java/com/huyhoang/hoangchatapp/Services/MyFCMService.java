package com.huyhoang.hoangchatapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.huyhoang.hoangchatapp.Common.Common;

import java.util.Map;
import java.util.Random;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataReceive = remoteMessage.getData();
        if(dataReceive!=null){
            Common.showNotification(this, new Random().nextInt(),
                    dataReceive.get(Common.NOTI_TITLE), dataReceive.get(Common.NOTI_CONTENT),
                    dataReceive.get(Common.NOTI_SENDER), dataReceive.get(Common.ROOM_ID), null
            );
        }
    }
}
