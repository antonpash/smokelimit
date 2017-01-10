package com.antonpash.smokelimit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.antonpash.smokelimit.services.MyIntentService;

public class OnBootReceiver extends BroadcastReceiver {
    public OnBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MyIntentService.class);
        context.startService(serviceIntent);
    }
}
