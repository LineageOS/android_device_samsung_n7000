package com.cyanogenmod.settings.device;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class PenRotationService extends Service
{
    static final String TAG = "PenRotationService";
    private static final String FILE = "/sys/class/sec/sec_epen/epen_rotation";

    Display display;
    int currentRotation;

    public static void start(Context context) {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction("com.cyanogenmod.settings.device.PenRotationService");
        context.startService(serviceIntent);
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG,"creating service");
        if (!isSupported())
        {
            Log.w(TAG,"epen_rotation attribute not available. Aborting...");
            stopSelf();
            return;
        }
        WindowManager wm = (WindowManager)getSystemService("window");
        display=wm.getDefaultDisplay();
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        Log.d(TAG,"starting service");
        currentRotation=display.getRotation();
        Utils.writeValue(FILE, Integer.toString(currentRotation));
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG,"destroy service");
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        int newRotation=display.getRotation();
        if (newRotation!=currentRotation)
        {
            Log.d(TAG,"configuration changed: old rotation="+currentRotation+" new rotation ="+newRotation);
            currentRotation=newRotation;
            Utils.writeValue(FILE, Integer.toString(currentRotation));
        }
    }

    public static boolean isSupported() {
        return Utils.fileExists(FILE);
    }

}
