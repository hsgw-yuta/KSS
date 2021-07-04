package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

/**********************************************************/
/* クラス名     :Body_Activity
/* 機能名       :腹筋機能の入力設定
/* 機能概要     :[回数].[セット].[インターバル]を入力する。
/* 作成日       :2019/2/21         長谷川　勇太      新規作成
/***********************************************************/
public class Body_Activity extends AppCompatActivity implements View.OnClickListener{

    /* 変数 */
    //入力情報
    NumberPicker numPicker_count,numPicker_set,numberPicker_loop;
    //開始、リセットボタン
    Button num_button_ok,num_button_reset;
    TextView num_textView;
    //DataBase情報
    private String login_data;
    private String days_st;

    /**************************************************************/
     /* 関数名       :onCreate
     /* 機能名       :クラスが呼ばれると最初に呼ばれる
     /* 機能概要     :ウィジェットや、変数などの初期設定
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body);

        findViewById(R.id.button_reset).setOnClickListener(this);
        findViewById(R.id.button_ok).setOnClickListener(this);

        //PipeActivityからの値受け取り
        Intent intent = getIntent();
        login_data = intent.getStringExtra("login_data");
        days_st = intent.getStringExtra("days");

        //スピナーのクラス
        findViews();

    }

    /**************************************************************/
     /* 関数名       :findViews
     /* 機能名       :ピッカー初期設定
     /* 機能概要     :最大最小値、初期設定値を設定する。
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /*************************************************************/
    private void findViews(){
        numPicker_count = (NumberPicker)findViewById(R.id.numPicker);
        numPicker_set =  (NumberPicker)findViewById(R.id.numPicker_set);
        numberPicker_loop = (NumberPicker)findViewById(R.id.numPicker_interval);

        num_button_ok = (Button)findViewById(R.id.button_ok);
        num_button_reset = (Button)findViewById(R.id.button_reset);
        num_textView = (TextView)findViewById(R.id.textView2);

        //最大値・最小値・初期値の設定

        numPicker_count.setMaxValue(20);
        numPicker_count.setMinValue(1);
        numPicker_count.setValue(10);

        numPicker_set.setMaxValue(10);
        numPicker_set.setMinValue(1);
        numPicker_set.setValue(2);

        numberPicker_loop.setMaxValue(300);
        numberPicker_loop.setMinValue(10);
        numberPicker_loop.setValue(30);
    }

     /**************************************************************/
     /* 関数名       :onClick
     /* 機能名       :クリック処理
     /* 機能概要     :ボタンがクリックされたとき呼ばれる関数
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/    @Override
    public void onClick(View v) {

        final int []in_data = new int[3];

        in_data[0] = numPicker_count.getValue();             //左の情報
        in_data[1] = numPicker_set.getValue();              //セットの情報
        in_data[2] = numberPicker_loop.getValue();          //インターバルの情報

        switch (v.getId()){

            case R.id.button_ok:
                // 確認ダイアログの作成
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Body_Activity.this)
                        .setCancelable(false);
                alertDialog.setTitle("準備");
                alertDialog.setMessage("装着品を装着しましたか？\n");
                alertDialog.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean check = false;
                Intent intent_cnt = new Intent(getApplication(), CountDownActivity.class);
                intent_cnt.putExtra(getString(R.string.i_check_name),check);
                intent_cnt.putExtra(getString(R.string.i_days_name),days_st);
                intent_cnt.putExtra(getString(R.string.i_count_name),in_data[0]);
                intent_cnt.putExtra(getString(R.string.i_setdata_name),in_data[1]);
                intent_cnt.putExtra(getString(R.string.i_intervaldata_name),in_data[2]);
                intent_cnt.putExtra(getString(R.string.i_indata_name),login_data);
                startActivity(intent_cnt);
                finish();
                    }
                });
                alertDialog.create().show();

                break;

            case R.id.button_reset:             //値初期化
                numPicker_count.setValue(1);
                numPicker_set.setValue(2);
                numberPicker_loop.setValue(10);
                break;
        }
    }
}

