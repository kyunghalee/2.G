package com.arduino.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Set;

/**
 * 메인 엑티비티
 */
public class MainActivity extends BaseActivity {
    private BluetoothAdapter mBluetoothAdapter = null;    // 블루투스 아답터
    // 블루투스 디바이스 객체
    private BluetoothDevice mBluetoothDevice;
    // 연결 쓰레드 클래스
    private ConnectedThread mConnectedThread;
    private Context mContext;
    protected static final int SHOW_TOAST = 0;
    private WebView mWebview;

    // 블루투스 장치 설정
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("TAGGGG", "ACTION_DISCOVERY_FINISHED");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Log.i("TAGGGG", "ACTION_ACL_DISCONNECT_REQUESTED");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i(DEBUG_TAG, "ACTION_ACL_DISCONNECTED");
                if (mBluetoothDevice != null) {
                    if (device.getAddress().equals(mBluetoothDevice.getAddress())) {
                        Toast.makeText(MainActivity.this, "장치가 끊겼습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    // ui 쓰레드용
    // ui 메시지 전송을 위한 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_TOAST: {    // 토스트 메시지 표시
                    Toast toast = Toast.makeText(MainActivity.this,
                            (String) msg.obj, Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
            }
        }
    };

    @SuppressLint("JavascriptInterface")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = this;
        // 화면이 꺼지지 않게 설정
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //	블루투스 지원 여부
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // null 이면 지원하지 않음..
            Toast.makeText(mContext, "단말기에서 블루투스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();   // 종료
        }

        // 블루투스 상태 체크 리시버
        checkBluetoothStatus();

        mWebview = (WebView) findViewById(R.id.webview);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl("file:///android_asset/index.html");


        mWebview.setWebViewClient(new myWebClient());
        mWebview.addJavascriptInterface(new WebViewInterface(), "Android"); //JavascriptInterface 객체화
    }

    public class WebViewInterface {
        @JavascriptInterface
        public void connect () {
            searchDeivce();
            Log.i("tag", "aaa");
        }

        @JavascriptInterface
        public void outDistance(final String distance) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("tag", "out:" + distance);
                    sendMessage("out:" + distance);

                }
            });
        }

        @JavascriptInterface
        public void inDistance(final String distance) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("tag", "in:" + distance);
                    sendMessage("in:" + distance);

                }
            });
        }

        @JavascriptInterface
        public void setting() {
            Intent intent = new Intent(getBaseContext(), SettingActivity.class);
            startActivityForResult(intent, SEARCH_DEVICE);
        }

        @JavascriptInterface
        public void gps() {
            Intent intent = new Intent(getBaseContext(), GpsActivity.class);
            startActivityForResult(intent, SEARCH_DEVICE);
        }

        @JavascriptInterface
        public void startGps() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // gps start

                }
            });
        }


        @JavascriptInterface
        public void finish () {
            finishConfrim();
        }
    }

    private void initArduinoTime(){
        Calendar cal = Calendar.getInstance();
        cal.get(Calendar.HOUR_OF_DAY);
        String msg = String.format("s:%02d%02d%02d\n",
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        //Toast.makeText(mContext, msg +" 로 시간 설정", Toast.LENGTH_SHORT).show();
        sendMessage(msg);
    }


    /**
     * 블루수트 상태 체크 리시버 등록
     */
    private void checkBluetoothStatus() {
        // 블루투스 상태 체크 리시버
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter1);
        registerReceiver(mReceiver, filter2);
        registerReceiver(mReceiver, filter3);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 블루투스 검색
     */
    private void searchDeivce() {
        Intent intent = new Intent(getBaseContext(), SearchBluetoothDeviceActivity.class);
        startActivityForResult(intent, SEARCH_DEVICE);
    }


    /**
     * resume 되면 테이블상태 재조회
     */
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //	loadState();
        // 블루투스가 활성화가 안되어 있으면
        if (!mBluetoothAdapter.isEnabled()) {
            // 블루투스 권한요청하기
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE);
        }
    }


    /**
     * 블루투스 응답
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE) {    // 블루투스 활성화가 정상이면
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.i(DEBUG_TAG, "device name-->" + device.getName());
                }
            }
        } else if (requestCode == REQUEST_ENABLE && resultCode == Activity.RESULT_CANCELED) {
//            Toast.makeText(this, "블루투스를 연결하지 않아 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
//            finish();
        } else if (requestCode == SEARCH_DEVICE) {
            //	Intent로 블루투스 디바이스 객체 얻기
            mBluetoothDevice = data.getParcelableExtra("device");
            mBluetoothDevice.getAddress();
            Log.i("Tag", "bluetooth start");
            // 연결 쓰레드 시작
            ConnectThread c = new ConnectThread(mBluetoothDevice);
            c.start();
        }
    }

    /*
     * back 버튼이면 타이머(2초)를 이용하여 다시한번 뒤로 가기를
     * 누르면 어플리케이션이 종료 되도록한다.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finishConfrim();
            }
        }
        return false;
    }


    /**
     * 연결 쓰레드로 상태 메세지 보내기
     *
     * @param message
     */
    private void sendMessage(String stateMessage) {
        if (mConnectedThread != null) {
            mConnectedThread.write(stateMessage.getBytes());
        }else{
//            Toast.makeText(MainActivity.this, "블루투스가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 받은 소캣을 통해 데이터송수신할 쓰레드 시작하기
     *
     * @param socket ConnectThread 혹은 AcceptThread로부터 받은 소캣
     */
    private void startConnectedThread(BluetoothSocket socket) {
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();     // 소캣 쓰레드 시작
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 연결후 아두이노 시간 설정
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initArduinoTime();      // 아두이노 시간 설정
                    }
                }, 2000);
            }
        });
    }

    /**
     * 클라이언트 쓰레드로 서버와 연결이 되면
     * ConnectedThread에 블루투스 소캣을 보낸다.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                // 클라이언트 소캣을 만들기 위한 메소드
                // 서버 소캣의 UUID값이 같아야 한다.
                tmp = device.createRfcommSocketToServiceRecord(BT_UUID);
            } catch (IOException e) {
            }
            socket = tmp;
            if (socket == null) {
                finish();
            }
        }

        public void run() {
            // 장치 검색이 실행되고 있는지 확인하여 종료합니다. 장치 검색이
            // 실행 중일때 연결을 맺으면 연결 속도가 느려질 것입니다.
            mBluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();    // 서버와  연결
            } catch (IOException connectException) {
                connectException.printStackTrace();
                try {
                    socket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            // ConnectedThread로 데이터 송수신을 하기 위해서 소캣을 보낸다.
            // 해당 소캣으로 소캣통신 시작 이하 tcp통신과 같음..
            startConnectedThread(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 서버 소캣 혹은 클라이언트 소캣을 통해
     * 데이터 송수신을 할 쓰레드
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;    // 블루투스 소캣
        private final InputStream mmInStream;    // 입력 스트림
        private final OutputStream mmOutStream;    // 출력 스트림

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tmpIn = null;    //임시 스트림
            OutputStream tmpOut = null;
            try {
                // 소캣에서 입력 및 출력 스트림을 얻어온다.
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            // 토스트 메세지를 띄운다.
            showToast("아두이노에 연결되었습니다.");
            // InputStream 으로부터 입력을 읽어들입니다.
            String msg = "";
            while (true) {

                try {
                    byte[] buffer = new byte[4096];
                    // Read from the InputStream
                    //mmInStream.read(buffer);
                    //	InputStreamReader isr = new InputStreamReader(mmInStream);
                    mmInStream.read(buffer);
                    msg += new String(buffer).trim();
                    //Log.i(DEBUG_TAG, "수신된 메세지->" + msg);
                    if (msg.contains("tilt;")) {
                        final String sendData = msg.trim();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendSms();
                                //mWebView.loadUrl("javascript:sendData('" + sendData + "');");

                            }
                        });
                        Log.i(DEBUG_TAG, "수신된 메세지->" + sendData);
                        msg = "";
                    }

                    if (msg.contains("sw;")) {
                        final String sendData = msg.trim();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendSms();
                                //mWebView.loadUrl("javascript:sendData('" + sendData + "');");

                            }
                        });
                        Log.i(DEBUG_TAG, "수신된 메세지->" + sendData);
                        msg = "";
                    }



                } catch (IOException e) {
                    break;
                }
            }
        }

        /**
         * outputstream의 write를 통해 메세지 내용을 쓴다.
         *
         * @param bytes
         */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /**
         * 소캣 닫기
         */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    private void sendSms(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String tel = prefs.getString("tel", "");
        if(TextUtils.isEmpty(tel)){
            Toast.makeText(mContext, "연락처가 설정되지 않았습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(tel, null, "사용자가 도움요청을 보냈습니다.", null, null);
    }

    /**
     * 서버 연결 쓰레드로 rfcomm 채널을 통해 서버 소캣을 만들어 준다.
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(Context context) {
            BluetoothServerSocket tmp = null;    // 임시 서버 소캣
            try {
                // UUID를 사용하여 서버 소켓을 만듭니다.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "My Bluetooth", BT_UUID);
            } catch (IOException e) {
                showToast("서버 소켓을 만드는데 실패하였습니다. " + e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            showToast("클라이언트를 기다리는 중입니다.");
            BluetoothSocket socket = null;
            // 클라이언트가 접속을 시도할때까지 기다립니다.
            while (true) {
                try {
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                    }
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // 클라이언트와 연결되고 소켓이 생성되면
                    // 소켓을 통해 데이터 송수신을 시작합니다.
                    startConnectedThread(socket);
                    showToast("클라이언트와 연결되었습니다.");
                    try {
                        if (mmServerSocket != null) {
                            mmServerSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast("서버 소켓을 종료하는 중 에러가 발생하였습니다. " + e.toString());
                    }
                    break;
                }
            }
        }

        // 리스닝 소켓을 닫고 스레드를 종료합니다.
        public void cancel() {
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * 핸들러를 통해 토스트 메세지 보여주기
     *
     * @param msg 메세지
     */
    private void showToast(String msg) {
        Message message = handler.obtainMessage();
        message.what = SHOW_TOAST;
        message.arg1 = 0;
        message.arg2 = 0;
        message.obj = msg;
        handler.sendMessage(message);
    }

    //flipscreen not loading again
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void finishConfrim() {
        /*
         * 앱 종료 다이얼로그
		 */
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("").setMessage("종료 하시겠습니까?")
                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                                        final int which) {
                        // TODO Auto-generated method stub
                        // 서비스중지
                        /*
                         * Intent serviceIntent = new
						 * Intent(getApplicationContext(),
						 * LocationService.class); stopService(serviceIntent);
						 * Log.i(DEBUG_TAG, "service start!!");
						 */
                        if (mConnectedThread != null) {
                            mConnectedThread.cancel();
                        }
                        finishAffinity();
                        // android.os.Process.killProcess(android.os.Process.myPid()
                        // );
                    }
                }).setNegativeButton("취소", null).show();
    }



    private class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);

            //progressBar.setVisibility(View.GONE);
        }

    }


    public static String StreamToString(InputStream in) throws IOException {
        if(in == null) {
            return "";
        }
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
        }
        return writer.toString();
    }

}
