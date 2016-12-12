package com.arduino.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by samsung on 2016-11-30.
 */
public class HomeActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(5000); // 5초
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);

                finish();
            }
        }).start();


    }
}
