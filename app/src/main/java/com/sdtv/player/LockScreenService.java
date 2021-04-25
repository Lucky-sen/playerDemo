package com.sdtv.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * 作者：admin016
 * 日期时间: 2021/4/16 14:55
 * 内容描述:
 */
public class LockScreenService extends Service {

    ScreenBroadcastReceiver screenBroadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        screenBroadcastReceiver = new ScreenBroadcastReceiver();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenBroadcastReceiver, filter);

    }

    public class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            handleCommandIntent(intent);
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action) ){
            Intent lockScreen = new Intent(this, LockScreenActivity.class);
//            intent.putExtra("musicName", musicName);
            lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(lockScreen);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenBroadcastReceiver);
        screenBroadcastReceiver = null;
    }

}
