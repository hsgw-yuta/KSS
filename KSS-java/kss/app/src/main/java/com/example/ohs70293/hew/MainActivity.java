package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Locale;

/**************************************************************/
/* クラス名     :MainActivity
/* 機能名       :ログイン処理
/* 機能概要     :氏名、身長、体重を入力し、
                 ログオンボタン押すと、メイン画面へ遷移する。
/* 作成日       :2019/2/20         長谷川　勇太      新規作成
/**************************************************************/


public class MainActivity extends AppCompatActivity {

    //main変数-------------------------------------------------------
    EditText input_name;                //氏名入力
    EditText h_num_text;                //伸長表示
    EditText w_num_text;                //体重表示
    String h_num;                       //伸長文字列
    String w_num;                       //体重文字列
    Button login_btn;                   //ログインボタン
    private String Login_Data;        //情報格納

    public static int gif_change;

     /**************************************************************/
     /* 関数名       :onCreate
     /* 機能名       :クラスが呼ばれると最初に呼ばれる
     /* 機能概要     :ウィジェットや、変数などの初期設定
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初期化-------------------------------------------------------

        //入力
        input_name = (EditText)findViewById(R.id.name);
        input_name.setText("");

        //ログインボタン
        login_btn = (Button)findViewById(R.id.button_ok);

        // SeekBar
        final SeekBar seekBar_h = (SeekBar) findViewById(R.id.seekBar_height);
        final SeekBar seekBar_w = (SeekBar) findViewById(R.id.seekBar_weight);
        h_num_text = (EditText) findViewById(R.id.h_num) ;
        w_num_text = (EditText) findViewById(R.id.w_num) ;


        //SeekBar初期化
        seekBar_h.setProgress(160);
        seekBar_w.setProgress(50);

        h_num_text.setText(String .valueOf(seekBar_h.getProgress()));
        w_num_text.setText(String.valueOf(seekBar_w.getProgress()));
        h_num_text.setSelection(h_num_text.getText().length());
        w_num_text.setSelection(w_num_text.getText().length());



        h_num_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    //Update Seekbar value after entering a number

                    seekBar_h.setProgress(Integer.parseInt(s.toString()));
                    h_num_text.setSelection(h_num_text.getText().length());

                } catch (Exception ex) {
                }
            }
        });

        w_num_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    //Update Seekbar value after entering a number
                    seekBar_w.setProgress(Integer.parseInt(s.toString()));
                    w_num_text.setSelection(w_num_text.getText().length());
                } catch (Exception ex) {
                }
            }
        });

        //seekBar押下時-----------------------------------------------------------
        //height
        seekBar_h.setOnSeekBarChangeListener
                (new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(
                    SeekBar seekBar, int progress, boolean fromUser) {
                // 68 % のようにフォーマト、
                // この場合、Locale.USが汎用的に推奨される
                h_num = String.valueOf(progress);
                h_num_text.setText(h_num);
            }
            //ツマミがタッチされた時に呼ばれる
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            //ツマミがリリースされた時に呼ばれる
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //weight
        seekBar_w.setOnSeekBarChangeListener
                (new SeekBar.OnSeekBarChangeListener(){
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int progress, boolean fromUser) {
                        // 68 % のようにフォーマト、
                        // この場合、Locale.USが汎用的に推奨される
                        w_num = String.valueOf(progress);
                        w_num_text.setText(w_num);
                    }
                    //ツマミがタッチされた時に呼ばれる
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    //ツマミがリリースされた時に呼ばれる
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        //Loginbutton押下時------------------------------------------------
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ログイン情報に漏れがないか確認
                if(!input_name.getText().toString().equals("")){

                    if(Integer.parseInt(h_num_text.getText().toString()) > 180){
                        //height_h
                        gif_change = 0;
                    }else if(Integer.parseInt(w_num_text.getText().toString()) > 120){
                        //weight_h
                        gif_change = 1;
                    }else if(Integer.parseInt(h_num_text.getText().toString()) < 170){
                        //height_l
                        gif_change = 2;
                    }else{
                        //weight_l
                        gif_change=3;
                    }
                    //基本情報格納
                    Login_Data = "|  名前  :"+ input_name.getText().toString()
                            + "\n|  身長  :" + h_num_text.getText().toString()
                            +"\n|  体重  :" + w_num_text.getText().toString()+ "\n"
                     + "-----------------------------";

                    //PipeActivityに遷移
                    Intent intent = new Intent
                            (getApplication(), PipeActivity.class);
                    intent.putExtra("login_data",Login_Data);
                    startActivity(intent);

                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),
                            "氏名が入力されていません", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
