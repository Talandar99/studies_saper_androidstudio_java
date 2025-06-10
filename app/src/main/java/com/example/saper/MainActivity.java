package com.example.saper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private MyService myService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = new Intent(this, MyService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            Log.println(Log.INFO,"MainActivity","onServiceConnected Board Updated");
            updateButtonsWithBoard();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void updateButtonsWithBoard() {
        Log.println(Log.INFO,"MainActivity","updateButtonsWithBoard");
        if (!isBound) return;
        Log.println(Log.INFO,"MainActivity","True");
        char[][] plansza = myService.getPlansza();
        boolean[][] odkryte = myService.getOdkryte();

        for (int x = 0; x < plansza.length; x++) {
            for (int y = 0; y < plansza[x].length; y++) {

                String buttonID = "button" + x + "_" + y;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                Button btn = findViewById(resID);
                btn.setTag(plansza[x][y]);

                if (odkryte[x][y]) {
                    char val = plansza[x][y];
                    if (val == 'B') {
                        btn.setText("💣");
                        btn.setTextColor(Color.RED);
                    } else if (val == '0') {
                        btn.setText("");
                    } else {
                        btn.setText(String.valueOf(val));
                        btn.setTextColor(getColorForValue(val));
                    }
                    btn.setEnabled(false);
                } else {
                    btn.setText("");
                    btn.setEnabled(true);
                }

                final int finalX = x;
                final int finalY = y;

                btn.setOnClickListener(v -> {
                    char val = (char) v.getTag();
                    if (val == 'B') {
                        btn.setText("💣");
                        btn.setTextColor(Color.RED);
                        btn.setEnabled(false);
                        myService.setOdkryte(finalX, finalY);
                    } else if (val == '0') {
                        revealArea(finalX, finalY, plansza);
                    } else {
                        btn.setText(String.valueOf(val));
                        btn.setEnabled(false);
                        btn.setTextColor(getColorForValue(val));
                        myService.setOdkryte(finalX, finalY);
                    }
                });
            }
        }
    }

    private void revealArea(int x, int y, char[][] plansza) {
        if (x < 0 || y < 0 || x >= plansza.length || y >= plansza[0].length) return;

        boolean[][] odkryte = myService.getOdkryte();
        if (odkryte[x][y]) return;

        String buttonID = "button" + x + "_" + y;
        int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
        Button btn = findViewById(resID);
        if (btn == null || !btn.isEnabled()) return;

        char val = plansza[x][y];
        if (val == 'B') return;

        myService.setOdkryte(x, y);

        if (val == '0') {
            btn.setText("");
        } else {
            btn.setText(String.valueOf(val));
            btn.setTextColor(getColorForValue(val));
        }

        btn.setEnabled(false);

        if (val == '0') {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        revealArea(x + dx, y + dy, plansza);
                    }
                }
            }
        }

    }

    private int getColorForValue(char val) {
        switch (val) {
            case '1': return Color.BLUE;
            case '2': return Color.MAGENTA;
            case '3': return Color.RED;
            case '4': return Color.MAGENTA;
            case '5': return Color.DKGRAY;
            default: return Color.BLACK;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isBound) {
            updateButtonsWithBoard();
        }
    }
}
