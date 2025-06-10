package com.example.saper;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    private final IBinder binder = new LocalBinder();

    private final char[][] plansza = {
            {'0', '1', 'B', '2', '1', '0'},
            {'1', '2', '1', '2', 'B', '1'},
            {'B', '1', '0', '1', '2', '2'},
            {'1', '1', '1', '1', '2', 'B'},
            {'0', '0', '1', 'B', '2', '1'},
            {'0', '0', '1', '2', 'B', '1'}
    };

    private final boolean[][] odkryte = new boolean[plansza.length][plansza[0].length];

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.println(Log.INFO,"MyService","onBind");
        return binder;
    }

    public char[][] getPlansza() {
        return plansza;
    }

    public boolean[][] getOdkryte() {
        return odkryte;
    }

    public boolean isOdkryte(int x, int y) {
        return odkryte[x][y];
    }

    public void setOdkryte(int x, int y) {
        odkryte[x][y] = true;
    }
}
