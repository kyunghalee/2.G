package com.arduino.bluetooth;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by samsung on 2016-11-16.
 */
public class GpsActivity extends Activity implements LocationListener, TextToSpeech.OnInitListener, View.OnClickListener, SensorEventListener{

    TextView textView3;

    String dbName = "st_file.db";
    int dbVersion = 3;
    private DBManager helper;
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log의 tag 로 사용
    String tableName = "student"; // DB의 table 명

    // Button bSelect;
    TextView tv;   // 결과창

    Button button;
    TextToSpeech tts;
    boolean init;

    GoogleMap googleMap;
    MapFragment fragment;

    LocationManager lm;
    Marker marker;

    private  long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static  final  int SHAKE_THRESHOLD = 950;
    private static  final  int DATA_X = SensorManager.DATA_X;
    private static  final  int DATA_Y = SensorManager.DATA_Y;
    private static  final  int DATA_Z = SensorManager.DATA_Z;


    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        fragment = (MapFragment)getFragmentManager().findFragmentById(R.id.fragment);
        googleMap = fragment.getMap();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        textView3 = (TextView)findViewById(R.id.textView3);
        tv      = (TextView)findViewById(R.id.textView4);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        init = false;
        tts = new TextToSpeech(this, this);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        helper = new DBManager(
                this,  // 현재 화면의 제어권자
                dbName,  // 데이터베이스 이름
                null, // 커서팩토리 - null 이면 표준 커서가 사용됨
                dbVersion);  // 데이터베이스 버전

        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag, "데이터 베이스를 열수 없음");
            finish();
        }

        insert ("대한민국 서울특별시 동작구 만양로8길", 250, 20);
        insert ("대한민국 서울특별시 동작구 노량진1동", 150, 40);
        insert ("대한민국 서울특별시 동작구 노량진동", 200, 100);
        insert ("대한민국 경기도 수원시 원천동", 250, 20);
        insert ("대한민국 경기도 수원시 이의동", 150, 40);


    }  // end of onCreate


    void insert (String address, int outradius, int innerradius) {
        ContentValues values = new ContentValues();
        // 키,값의 쌍으로 데이터 입력
        values.put("address", address);
        values.put("outradius", outradius);
        values.put("innerradius", innerradius);
        long result = db.insert(tableName, null, values);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(sensorManager!=null)
            sensorManager.unregisterListener(this);
    }



    @Override
    protected void onResume() {
        super.onResume();
        String provider = lm.getBestProvider(new Criteria(), true);

        if(provider ==null){
            Toast.makeText(this, "위치정보를 사용 가능한 상태가 아냐", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = lm.getLastKnownLocation(provider);
        if(location!= null){
            onLocationChanged(location);
        }
        lm.requestLocationUpdates(provider,1000, 1,this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }

    public String getAddress(double lat, double lng){
        String address = null;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);

            if (list.size() > 0){
                Address addr = list.get(0);
                address = addr.getCountryName() + " "
                        + addr.getAdminArea() + " "
                        + addr.getLocality() + " "
                        + addr.getThoroughfare() + " "
                        + addr.getFeatureName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  address;
    }

    public String getAddress2(double lat, double lng){
        String address2 = null;

        Geocoder geocoder2 = new Geocoder(this, Locale.getDefault());

        List<Address> list2 = null;

        try {
            list2 = geocoder2.getFromLocation(lat, lng, 1);

            if (list2.size() > 0){
                Address addr2 = list2.get(0);
                address2 = addr2.getCountryName() + " "
                        + addr2.getAdminArea() + " "
                        + addr2.getLocality() + " "
                        + addr2.getThoroughfare() ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  address2;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        LatLng position = new LatLng(lat, lng);

        textView3.setText(getAddress(lat, lng));

        Cursor c = db.query(tableName, null, null, null, null, null, null);
        while(c.moveToNext()) {
            int _id = c.getInt(0);
            String address = c.getString(1);
            int outradius = c.getInt(2);
            int innerradius = c.getInt(3);

            String re = getAddress2(lat, lng);

            if (re.equals(address)){
                Log.d(tag,"_id:"+_id+", address: "+address
                        +", outradius: "+outradius+", innerradius: "+innerradius);
                tv.setText("\n" + "    address: " + address + "\n"
                        + "    outradius: " + outradius + "  ,  innerradius: " + innerradius);
            }
        }

        if(marker == null){
            MarkerOptions options = new MarkerOptions();
            options.position(position);
            options.title(getAddress(lat, lng));
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.smallhat));
            marker = googleMap.addMarker(options);
        }else {
            marker.setPosition(position);
            marker.setTitle(getAddress(lat, lng));
        }
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(position, 18);
        googleMap.animateCamera(camera);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(this, provider + " - Status Changed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        if(!init){
            Toast.makeText(this, "아직 초기화 되지 않음", Toast.LENGTH_SHORT).show();
            return;
        }
        String msg = textView3.getText().toString().trim();

        if(msg. equals("")){
            Toast.makeText(this, "내용입력해라라", Toast.LENGTH_SHORT).show();
            return;
        }
        Locale loc = Locale.KOREA;
        int available = tts.isLanguageAvailable(loc);
        if(available<0){
            Toast.makeText(this, "지정되지 않은 언어", Toast.LENGTH_SHORT).show();
            return;
        }
        tts.setLanguage(loc);
        tts.setPitch(1);
        tts.setSpeechRate(1);
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {
        init = (status == TextToSpeech.SUCCESS);
        String msg = null;
        if(init){
            msg = "엔진 초기화";
        }else {
            msg = "엔진 초기화에 실패";
        }
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if(gabOfTime >100){
                lastTime = currentTime;
                x=event.values[SensorManager.DATA_X];
                y=event.values[SensorManager.DATA_Y];
                z=event.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if(speed > SHAKE_THRESHOLD){
                    Toast.makeText(this, "흔들림 감지!", Toast.LENGTH_SHORT).show();
                    if(!init){
                        Toast.makeText(this, "아직 초기화 되지 않음", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String msg = textView3.getText().toString().trim();

                    if(msg. equals("")){
                        Toast.makeText(this, "내용입력해라라", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Locale loc = Locale.KOREA;
                    int available = tts.isLanguageAvailable(loc);
                    if(available<0){
                        Toast.makeText(this, "지정되지 않은 언어", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tts.setLanguage(loc);
                    tts.setPitch(1);
                    tts.setSpeechRate(1);
                    tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
                }
                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
