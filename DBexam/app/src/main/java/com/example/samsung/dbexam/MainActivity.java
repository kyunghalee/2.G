package com.example.samsung.dbexam;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String dbName = "st_file.db";
    int dbVersion = 3;
    private DBManager helper;
    private SQLiteDatabase db;
    String tag = "SQLite"; // Log의 tag 로 사용
    String tableName = "student"; // DB의 table 명

   // Button bSelect;
    TextView tv;   // 결과창

    TextView textViewaddress; // 주소short 창

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

   //     bSelect = (Button)  findViewById(R.id.button4);
        tv      = (TextView)findViewById(R.id.textView4);

        textViewaddress      = (TextView)findViewById(R.id.textViewaddress);

        helper = new DBManager(
                this,  // 현재 화면의 제어권자
                dbName,  // 데이터베이스 이름
                null, // 커서팩토리 - null 이면 표준 커서가 사용됨
                dbVersion);  // 데이터베이스 버전

        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.e(tag,"데이터 베이스를 열수 없음");
            finish();
        }

                insert ("대한민국 서울특별시 동작구 만양로8길", 250, 20);
                insert ("대한민국 서울특별시 동작구 노량진1동", 150, 40);
                insert ("대한민국 서울특별시 동작구 노량진동", 200, 100);
                insert ("대한민국 경기도 수원시 원천동", 250, 0);


//        bSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("");//기존에 textView에 있는 값을 지우고 보여주기
                select();
//            }
//        });

    } // end of onCreate


    void select () {
        Cursor c = db.query(tableName, null, null, null, null, null, null);
        while(c.moveToNext()) {
            int _id = c.getInt(0);
            String address = c.getString(1);
            int outradius = c.getInt(2);
            int innerradius = c.getInt(3);

            String re = textViewaddress.getText().toString();

            if (re.equals(address)){
                Log.d(tag,"_id:"+_id+", address: "+address
                        +", outradius: "+outradius+", innerradius: "+innerradius);
                tv.append("\n" + "_id:" + _id + ",address:" + address
                        + ",outradius:" + outradius + ",innerradius:" + innerradius);
            }

            // 키보드 내리기
            InputMethodManager imm =
                    (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow
                    (getCurrentFocus().getWindowToken(), 0);
        }
    }

    void insert (String address, int outradius, int innerradius) {
        ContentValues values = new ContentValues();
        // 키,값의 쌍으로 데이터 입력
        values.put("address", address);
        values.put("outradius", outradius);
        values.put("innerradius", innerradius);
        long result = db.insert(tableName, null, values);
        Log.d(tag, result + "번째 row insert 성공했음");
        tv.setText(result + "번째 row insert 성공했음");
        select(); // insert 후에 select 하도록
    }
}
