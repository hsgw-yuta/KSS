package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.R.attr.data;
import static android.R.attr.fingerprintAuthDrawable;
import static android.R.attr.left;
import static android.R.attr.right;
import static android.R.attr.settingsActivity;

/**************************************************************/
/* クラス名     :Record_MainActivity
/* 機能名       :記録確認画面
/* 機能概要     :追加ボタンを実行するとデータベースに書き込まれ、
                閲覧で全情報を確認できる
/* 作成日       :2019/2/20         長谷川　勇太      新規作成
/**************************************************************/
public class Record_MainActivity extends AppCompatActivity {

    
    TextView text;
    ImageButton add_bt;                                // 追加ボタン処理用
    ImageButton view_bt;                              // 閲覧ボタン
    Button all_delite;                                // 全て削除
    private int[] in_data = new int[4];             // 入力内容配列 
    private String input_data;                      // 基本情報 
    private String days;                            // 入力日付 
    private String st;                              // データベース格納情報
    private boolean add_flg = false;               //遷移元判断

    /**************************************************************/
     /* 関数名       :onCreate
     /* 機能名       :クラスが呼ばれると最初に呼ばれる
     /* 機能概要     :ウィジェットや、変数などの初期設定
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_main);
        text = (TextView)findViewById(R.id.entry_text);

        //ヘルパークラスを作成
         MyOpenHelper helper = new MyOpenHelper(this);
        //データベースクラスの作成
        final SQLiteDatabase db = helper.getWritableDatabase();

        // データベース機能ボタン
        add_bt = (ImageButton) findViewById(R.id.Add_bt);
        view_bt = (ImageButton) findViewById(R.id.Reading_bt);
        all_delite = (Button)findViewById(R.id.All_Delete_bt);

        //チェックビットの受け取り
        Intent data_intent = this.getIntent();
        final boolean check = data_intent.getBooleanExtra("check",true);
        if(check == true) {
            in_data[1] = data_intent.getIntExtra("rightdata", 0);
            in_data[0] = data_intent.getIntExtra("leftdata", 0);
        }else{
            in_data[0] = data_intent.getIntExtra("count",0);
        }

        in_data[2] = data_intent.getIntExtra("setdata", 0);
        in_data[3] = data_intent.getIntExtra("intervaldata", 0);
        days = data_intent.getStringExtra("days");
        input_data = getIntent().getStringExtra("in_data");
        add_flg = getIntent().getBooleanExtra("add_flg", false);

        if(add_flg == true)
        {
            add_bt.setVisibility(View.GONE);
        }

        //追加ボタン押下時
        add_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(check == true) {
                    st = "|  左    :" + String.valueOf(in_data[0]) + "回"+ "\n"
                            + "|  右    :" + String.valueOf(in_data[1])+ "回" + "\n"
                            + "|  セット :" + String.valueOf(in_data[2])+ "セット" + "\n"
                            + "|  インターバル :" + String.valueOf(in_data[3]) + "秒" + "\n";
                }else if(check == false){
                    st = "|  回数  ：" + String.valueOf(in_data[0]) + "回" + "\n"
                            + "|  セット：" + String.valueOf(in_data[2])+ "セット"  + "\n"
                            + "|  インターバル：" + String.valueOf(in_data[3]) + "秒" + "\n";
                }

                st = input_data + "\n" + st ;
                text.setText(st  + "\n" + days);

                ContentValues values = new ContentValues();

                values.put("key", days);
                values.put("text", st);
                long ret;

                try {
                    ret = db.insert("my_table", null, values);
                } finally {
                    //db.close();
                }
                if (ret == -1) {
                    Toast.makeText(getApplication(), "追加失敗しました。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplication(), "追加されました。", Toast.LENGTH_SHORT).show();
                }
                add_bt.setVisibility(View.GONE);
            }
        });

        view_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
    
                Intent intent  = new Intent(getApplication(), ShowDataBase.class);
                startActivity(intent);
            }
        });


        all_delite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete("my_table", null, null);
            }
        });

    }

}
