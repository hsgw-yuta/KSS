package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**************************************************************/
/* クラス名     :Danbel_CntActivity
/* 機能名       :カウントダウン画面
/* 機能概要     :入力した内容を用いてトレーニングをスタートする。
                ・Bluetoothを接続
                ・インターバル中は切断
                ・セット回数が終了するとコンティニューイベントボタン表示
/* 作成日       :2019/2/20         長谷川　勇太      新規作成
/**************************************************************/
public class CountDownActivity extends AppCompatActivity implements View.OnClickListener {

    // GUIアイテム
    private Button mButton_Connect;        // 接続ボタン
    private Button mButton_Disconnect;    // 切断ボタン
    private boolean check;
    // Bluetooth定数
    private static final int READBUFFERSIZE = 1024;    // 受信バッファーのサイズ

    //アニメーション画像-----------------------------------------
    private ImageView back_view;

    //カウントダウンフラグ
    boolean low_flg = false;            //伸長時のフラグ
    boolean up_flg = true;             //縮曲時のグラフ
    //カウントダウン有無
    boolean count_down = true;

    //応援アニメーションが画像
    ImageView animetion;

    //現状態表示テキスト
    TextView state;

    // Bluetoothメンバー変数
    private String mDeviceAddress = "";                          // デバイスアドレス
    private BluetoothService mBluetoothService;                  // Bluetoothデバイスとの通信処理を担う
    private  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private byte[] mReadBuffer    = new byte[READBUFFERSIZE];   //byte型で値が来る
    private int    mReadBufferCounter = 0;
    private String st_y = "";
    private int y_data =0;
    private String bluetooth_adress = "";

    //main画面メンバー変数-----------------------------------------------
    TextView in_data_text[] = new TextView[4];                  //入力内容表示
    TextView tv ;                                                //筋トレ時の演出テキスト
    Button events_btn[] = new Button[3];                        //イベントボタン
    private int[] in_data = new int[4];                        //入力内容受け取り
    private int[] in_save = new int[4];                        //入力値保管
    private int count_flg = 0;                                 //左右の判断
    private String input_data;
    private String days;                                                //入力日情報

    /**************************************************************/
     /* 関数名       :onCreate
     /* 機能名       :クラスが呼ばれると最初に呼ばれる
     /* 機能概要     :ウィジェットや、変数などの初期設定
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danbel_cnt);


        // GUIアイテム
        mButton_Connect = (Button)findViewById( R.id.button_connect );
        mButton_Connect.setOnClickListener( this );
        mButton_Disconnect = (Button)findViewById( R.id.button_disconnect );
        mButton_Disconnect.setOnClickListener( this );

        // Bluetoothアダプタの取得
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if( null == mBluetoothAdapter )
        {    // Android端末がBluetoothをサポートしていない
            finish();    // アプリ終了宣言
            return;
        }
        //bluetoothアダプターの有無
        if(mBluetoothAdapter == null) {
            Toast.makeText(this,"Device does not support Bluetooth",Toast.LENGTH_SHORT).show();
        }

        //ペアリング済みの端末セットの問い合わせ
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                // mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //ペアリング済みのデバイス名とMACアドレスの表示
                bluetooth_adress = device.getName() + "\n" + device.getAddress();
                mDeviceAddress = device.getAddress();
            }
        }

        //スクロール文字-------------------------------------------------------------------------------------
        tv = (TextView) findViewById(R.id.scroll);
        tv.setSingleLine();                                     //文字列の一行表示
        tv.setFocusableInTouchMode(true);                       //Touchモード時にＶｉｅｗがフォーカスを取得するか

        tv.setEllipsize(TextUtils.TruncateAt.MARQUEE); // 文字列を表示し切れないときにはスクロールする

        //入力数値表示
        in_data_text[0] = (TextView) findViewById(R.id.leftcnt);
        in_data_text[1] = (TextView) findViewById(R.id.rightcnt);
        in_data_text[2] = (TextView) findViewById(R.id.setcnt);
        in_data_text[3] = (TextView) findViewById(R.id.intervalcnt);

        //イベントボタン
        events_btn[0] = (Button) findViewById(R.id.again);
        events_btn[1] = (Button) findViewById(R.id.back);
        events_btn[2] = (Button) findViewById(R.id.write);
        for(int i = 0;i<3;i++){
            events_btn[i].setOnClickListener(this);
        }

        Intent data_intent = this.getIntent();
        check =  data_intent.getBooleanExtra(getString(R.string.i_check_name),false);

        //ダンベル値受け取り---------------------------------------------------------------------------
        if(check == true) {
            in_save[0] = in_data[0] = data_intent.getIntExtra(getString(R.string.i_left_name), 0);
            in_save[1] = in_data[1] = data_intent.getIntExtra(getString(R.string.i_right_name), 0);
            in_save[2] = in_data[2] = data_intent.getIntExtra(getString(R.string.i_setdata_name),0);
            in_save[3] = in_data[3] = data_intent.getIntExtra(getString(R.string.i_intervaldata_name), 0);
            input_data = getIntent().getStringExtra(getString(R.string.i_indata_name));
            days =  data_intent.getStringExtra(getString(R.string.i_days_name));

            //入力数値表示
            in_data_text[0].setText(String.valueOf("左" + in_data[0]));
            in_data_text[1].setText(String.valueOf("右" +in_data[1]));
            in_data_text[2].setText("残り" + String.valueOf(in_data[2]) + "セット");
            in_data_text[3].setText(String.valueOf(in_data[3]));

            in_data_text[1].setVisibility(View.GONE);          //右非表示
            in_data_text[3].setVisibility(View.GONE);          //インターバル非表示

        }
        //腹筋値受け取り---------------------------------------------------------------------------------
        else if(check == false) {
            in_save[0] = in_data[0] = data_intent.getIntExtra(getString(R.string.i_count_name), 0);
            in_save[2] = in_data[2] = data_intent.getIntExtra(getString(R.string.i_setdata_name), 0);
            in_save[3] = in_data[3] = data_intent.getIntExtra(getString(R.string.i_intervaldata_name), 0);
            input_data = getIntent().getStringExtra(getString(R.string.i_indata_name));
            days =  data_intent.getStringExtra(getString(R.string.i_days_name));

            //入力数値表示
            in_data_text[0].setText(String.valueOf("回数" + in_data[0]));
            in_data_text[2].setText("残り" + String.valueOf(in_data[2]) + "セット");
            in_data_text[3].setText(String.valueOf(in_data[3]));

            in_data_text[1].setVisibility(View.GONE);          //右非表示
            in_data_text[3].setVisibility(View.GONE);          //インターバル非表示
        }
        //再挑戦確認ボタンを非表示にする
        for(int loop = 0; loop < 3 ; loop++) {
            events_btn[loop].setVisibility(View.GONE);
        }

        /* 現状態表示テキスト */
        state = (TextView)findViewById(R.id.state);

        //アニメーションgif
        animetion = (ImageView)findViewById(R.id.gif_image);
        GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(animetion);
        //ユーザー情報取得のため
        MainActivity main_gif= new MainActivity();
        //ユーザー情報判断
        switch (main_gif.gif_change){
            case 0://height_h
                Glide.with(this).load(R.raw.heigh_h).into(target);
                break;
            case 1://weight_h
                Glide.with(this).load(R.raw.weight_h).into(target);
                break;
            case 2://height_l
                Glide.with(this).load(R.raw.height_l).into(target);
                break;
            case 3://weight_l
                Glide.with(this).load(R.raw.weight_l).into(target);
                break;
        }


    }

    /**************************************************************/
     /* 関数名       :onClick
     /* 機能名       :クリック時アクション
     /* 機能概要     :buttonがクリックするとここに入ってくる
     /* 作成日       :2019/03/05         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    public void onClick( View v ) {
        switch (v.getId()) {
            case R.id.button_connect:
                mButton_Connect.setEnabled(false);    // 接続ボタンの無効化（連打対策）
                connect();            // 接続
                return;

            case R.id.button_disconnect:
                mButton_Disconnect.setEnabled(false);    // 切断ボタンの無効化（連打対策）
                disconnect();            // 切断
                return;

            case R.id.again:
                //全てリセット
                events_btn[0].setVisibility(View.GONE);
                events_btn[1].setVisibility(View.GONE);
                events_btn[2].setVisibility(View.GONE);

                for(int loop = 0; loop < 4 ; loop++) {
                    in_data[loop] = in_save[loop];
                }
                if(check == true) {
                    in_data_text[0].setText(String.valueOf("左" + in_data[0]));
                    in_data_text[1].setText(String.valueOf("右" + in_data[1]));
                    in_data_text[2].setText("残り" + String.valueOf(in_data[2]) + "セット");
                    in_data_text[3].setText(String.valueOf(in_data[3]));
                    tv.setText("がんばれー!!今左手中ですよー");
                    count_flg=0;
                }else if(check == false){
                    in_data_text[0].setText(String.valueOf("回数" + in_data[0]));
                    in_data_text[2].setText("残り" + String.valueOf(in_data[2]) + "セット");
                    in_data_text[3].setText(String.valueOf(in_data[3]));
                    tv.setText("がんばれー!!今腹筋中ですよー");
                }
                //再挑戦時カウント再表示
                in_data_text[0].setVisibility(View.VISIBLE);
                in_data_text[1].setVisibility(View.GONE);
                in_data_text[3].setVisibility(View.GONE);
                //カウント開始
                count_down = true;
                break;
            case R.id.back:
                finish();
                break;
            case R.id.write:
                Intent record_intent = new Intent(getApplication(), Record_MainActivity.class);

                if(check == false){
                    record_intent.putExtra(getString(R.string.i_count_name),in_save[0]);
                }else {
                    record_intent.putExtra(getString(R.string.i_left_name), in_save[0]);
                    record_intent.putExtra(getString(R.string.i_right_name), in_save[1]);
                }
                record_intent.putExtra(getString(R.string.i_setdata_name), in_save[2]);
                record_intent.putExtra(getString(R.string.i_intervaldata_name), in_save[3]);
                record_intent.putExtra(getString(R.string.i_check_name), check);
                record_intent.putExtra(getString(R.string.i_indata_name), input_data);
                record_intent.putExtra(getString(R.string.i_days_name), days);
                startActivity(record_intent);
                finish();
                break;
        }
    }

    /**************************************************************/
     /* 関数名       :danbel_count
     /* 機能名       :ダンベルトレーニング
     /* 機能概要     :ダンベルトレーニング時のカウントダウンアクション
     /* 作成日       :2019/03/05         長谷川　勇太      新規作成
     /**************************************************************/
    public void danbel_count(){
        //カウント終了時-------------------------------------------------------------
        if (in_data[count_flg] == 1) {
            in_data[count_flg] = 0;

            if (count_flg == 0 ) {          //左
                in_data_text[0].setVisibility(View.GONE);           //左非表示
                in_data_text[1].setVisibility(View.VISIBLE);        //右表示

                count_flg++;

                //カウントダウン停止
                count_down = false;

                // 確認ダイアログの作成
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CountDownActivity.this)
                .setCancelable(false);
                alertDialog.setTitle("確認");
                alertDialog.setMessage("持ちてを変えてください。");
                alertDialog.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("test","dialog");
                        //カウントダウン開始
                        count_down = true;
                    }
                });
                alertDialog.create().show();
                tv.setText("がんばれー!!今右手筋トレ中ですよー");
            }else{                          //右
                in_data_text[0].setVisibility(View.GONE);            //左非表示
                in_data_text[3].setVisibility(View.VISIBLE);         //インターバル表示
            }
        }
        //カウントダウン実行チュー-------------------------------------------------------
        else {
            if (count_flg == 0) {
                in_data_text[count_flg].setText(String.valueOf("左" + --in_data[count_flg]));
            } else {
                in_data_text[count_flg].setText(String.valueOf("右" + --in_data[count_flg]));
            }
        }

        //両方のカウント終了時-------------------------------------------------------------
        if (in_data[0] + in_data[1] == 0) {

            in_data_text[2].setText("残り" + String.valueOf(--in_data[2]) + "セット");

            in_data_text[1].setVisibility(View.GONE);            //右非表示

            if(in_data[2] != 0) {
                in_data_text[3].setVisibility(View.VISIBLE);
            }else{                                    //セット回数の残り1セットの処理　　　　　　　　　　　　　　　　　　　　　
                in_data_text[3].setVisibility(View.GONE);
            }

            //インターバル秒の処理
            if(in_data[2] > 0) {
                //カウントダウン停止
                count_down = false;
                in_data_text[2].setText("休憩");

                tv.setText("インターバル中～インターバル中～インターバル中～");
                final Danbel_Cnt_downActivity countdown = new Danbel_Cnt_downActivity(in_data[3] * 1000, 1000);
                countdown.start();
            }
        }
        //セット回数終了時の処理
        if (in_data[2] == 0) {
            //終了時にボタンを表示
            for(int loop = 0; loop < 3 ; loop++) {
                events_btn[loop].setVisibility(View.VISIBLE);
            }
            //カウントダウン停止
            count_down = false;

            in_data_text[2].setVisibility(View.INVISIBLE);
            in_data_text[3].setVisibility(View.VISIBLE);
            in_data_text[3].setText("終了");

            animetion.setVisibility(View.GONE);
            tv.setText("終了です～～");
        }

    }

    /**************************************************************/
     /* 関数名       :body_count
     /* 機能名       :腹筋トレーニング
     /* 機能概要     :腹筋トレーニング時のカウントダウンアクション
     /* 作成日       :2019/03/05         長谷川　勇太      新規作成
     /**************************************************************/
    private void body_count(){

        //カウント終了時-------------------------------------------------------------
        if (in_data[count_flg] == 1) {
            in_data[count_flg] = 0;

            if (count_flg == 0 ) {          //左
                in_data_text[0].setVisibility(View.GONE);           //左非表示
                in_data_text[3].setVisibility(View.VISIBLE);         //インターバル表示
                tv.setText("がんばれー!!今腹筋中ですよー");

            }
        }
        //カウントダウン実行チュー-------------------------------------------------------
        else {
                in_data_text[count_flg].setText(String.valueOf("回数" + --in_data[count_flg]));
            }
        //両方のカウント終了時-------------------------------------------------------------
        if (in_data[0] == 0) {

            in_data_text[2].setText("残り" + String.valueOf(--in_data[2]) + "セット");

            if(in_data[2] != 0) {
                in_data_text[3].setVisibility(View.VISIBLE);
            }else{                                    //セット回数の残り1セットの処理　　　　　　　　　　　　　　　　　　　　　
                in_data_text[3].setVisibility(View.GONE);
            }
            //インターバル秒の処理
            if(in_data[2] > 0) {
                tv.setText("インターバル中～");
                in_data_text[2].setVisibility(View.INVISIBLE);
                //カウントダウン停止
                count_down = false;
                final Danbel_Cnt_downActivity countdown = new Danbel_Cnt_downActivity(in_data[3] * 1000, 1000);
                countdown.start();
            }
        }
        //セット回数終了時の処理
        if (in_data[2] == 0) {
            //終了時にボタンを表示
            for(int loop = 0; loop < 3 ; loop++) {
                events_btn[loop].setVisibility(View.VISIBLE);
            }

            //カウントダウン停止
            count_down = false;

            in_data_text[2].setVisibility(View.INVISIBLE);
            in_data_text[3].setVisibility(View.VISIBLE);
            in_data_text[3].setText("終了");
            animetion.setVisibility(View.GONE);

            tv.setText("終了です～～");
        }else{
            //カウントダウン開始
            count_down = true;
        }

    }

    /**************************************************************/
    /* クラス名     :Danbel_Cnt_downActivity
    /* 機能名       :インターバルカウントダウン処理
    /* 機能概要     :millisInFuture        :カウントダウンの開始値
                    countDownInterval     :何秒毎に行うか
    /* 作成日       :2019/2/20         長谷川　勇太      新規作成
    /**************************************************************/
    public class Danbel_Cnt_downActivity extends CountDownTimer {

        public Danbel_Cnt_downActivity(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        /**************************************************************/
        /* 関数名       :onFinish
        /* 機能名       :インターバルカウントダウン終了時
        /* 機能概要     :セット回数が終了するとコンティニューイベントボタン表示
        /* 作成日       :2019/2/20         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onFinish() {
            Toast.makeText(getApplicationContext(), "タイマー満了", Toast.LENGTH_SHORT).show();
            ToneGenerator toneGenerator
                    = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);

            //値を取得
            in_data[0] = in_save[0];
            in_data[1] = in_save[1];
            in_data[3] = in_save[3];

            if(check == true) {
                in_data_text[0].setText(String.valueOf("左" + in_data[0]));
                in_data_text[1].setText(String.valueOf("右" + in_data[1]));
                in_data_text[3].setText(String.valueOf(in_data[3]));
                tv.setText("がんばれー!!今左手筋トレ中ですよー");
            }else if(check == false){
                in_data_text[0].setText(String.valueOf("回数" + in_data[0]));
                in_data_text[3].setText(String.valueOf(in_data[3]));
                tv.setText("がんばれー!!今腹筋中ですよー");
            }
            //インターバル終了時カウント再表示
            in_data_text[0].setVisibility(View.VISIBLE);
            in_data_text[1].setVisibility(View.GONE);
            in_data_text[2].setVisibility(View.VISIBLE);
            in_data_text[3].setVisibility(View.GONE);
            //最終セット
            if(in_data[2] == 1){
                in_data_text[2].setText("ファイナルセット");
            }
            count_flg = 0;

            //カウントダウン開始
            count_down = true;


            Log.d("timer","インターバルカウントダウン終了し再接続開始");
        }

        /**************************************************************/
        /* 関数名       :onTick
        /* 機能名       :インターバル毎の処理
        /* 機能概要     :一秒ごとに表示
        /* 作成日       :2019/2/20         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onTick(long millisUntilFinished) {
            // インターバル(countDownInterval)毎に呼ばれる
            in_data_text[3].setText(Long.toString(millisUntilFinished/1000/60) + ":" + Long.toString(millisUntilFinished/1000%60));
        }
    }

     /**************************************************************/
     /* 関数名       :Handler
     /* 機能名       :Bluetoothサービスから情報を取得するハンドラ
     /* 機能概要     :ハンドルメッセージ
                     UIスレッドの処理なので、UI処理について、
                     runOnUiThread対応は、不要。
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            switch( msg.what )
            {
                case BluetoothService.MESSAGE_STATECHANGE:
                    switch( msg.arg1 )
                    {
                        case BluetoothService.STATE_NONE:                       // 未接続
                            Log.d("Bluetoothhandle", "未接続");
                            state.setText("未接続");
                            break;
                        case BluetoothService.STATE_CONNECT_START:              // 接続開始
                            Log.d("Bluetoothhandle", "接続開始");
                            state.setText("接続開始");
                            break;
                        case BluetoothService.STATE_CONNECT_FAILED:            // 接続失敗
                            Log.d("Bluetoothhandle", "接続失敗");
                            state.setText("接続失敗");
                            break;
                        case BluetoothService.STATE_CONNECTED:                  // 接続完了
                            Log.d("Bluetoothhandle", "接続完了");
                            // GUIアイテムの有効無効の設定
                            // 切断ボタン、文字列送信ボタンを有効にする
                            mButton_Disconnect.setEnabled( true );
                            break;
                        case BluetoothService.STATE_CONNECTION_LOST:            // 接続ロスト
                            state.setText("接続ロスト");
                            Log.d("Bluetoothhandle", "接続ロスト");
                            break;
                        case BluetoothService.STATE_DISCONNECT_START:           //切断開始
                            state.setText("切断開始");
                            Log.d("Bluetoothhandle", "切断開始");
                            // GUIアイテムの有効無効の設定
                            // 切断ボタン、文字列送信ボタンを無効にする
                            mButton_Disconnect.setEnabled( false );
                            //Toast.makeText( MainActivity.this, "Lost connection to the device.", Toast.LENGTH_SHORT ).show();
                            break;
                        case BluetoothService.STATE_DISCONNECTED:               // 切断完了
                            state.setText("切断完了");
                            Log.d("Bluetoothhandle", "切断完了");
                            // GUIアイテムの有効無効の設定
                            // 接続ボタンを有効にする
                            mButton_Connect.setEnabled( true );
                            mBluetoothService = null;    // BluetoothServiceオブジェクトの解放
                            break;
                    }
                    break;

                case BluetoothService.MESSAGE_READ:         //Bluetoothモジュールからの文字列取得
                    state.setText("メッセージ受信中");
                    Log.d("Bluetoothhandle", "受信メッセージ");

                    byte[] abyteRead = (byte[])msg.obj;
                    int iCountBuf = msg.arg1;

                    for( int i = 0; i < iCountBuf; i++ )
                    {
                        byte c = abyteRead[i];
                        if( '\r' == c ) {
                               // 終端
                                mReadBuffer[mReadBufferCounter] = '\0';

                                //値格納
                            st_y = new String(mReadBuffer, 0, mReadBufferCounter);

                            try {
                                y_data = Integer.parseInt(st_y);
                            }finally {
                                Toast.makeText(getApplication(),"error",Toast.LENGTH_SHORT);
                            }
                            Log.d("log",st_y);

                            if(y_data < 20 && (low_flg&up_flg)==true ){
                                if(check == true) {
                                    if(count_down == true) {
                                        Toast.makeText(getApplication(),"カウント1",Toast.LENGTH_SHORT).show();
                                        danbel_count();             //ダンベル選択時のカウントダウン処理
                                    }
                                }else if(check == false){
                                    if(count_down == true) {
                                        body_count();              //腹筋選択時のカウントダウン処理
                                    }
                                }
                                low_flg = false;
                            }else if(y_data > 80 && (low_flg&up_flg)==false){
                                low_flg = up_flg = true;
                            }
                            // バッファーあふれ。初期化
                           mReadBufferCounter = 0;
                        }
                        else if( '\n' == c ) {
                ;
                        } else {    // 途中
                            if( ( READBUFFERSIZE - 1 ) > mReadBufferCounter )
                            {    // mReadBuffer[READBUFFERSIZE - 2] までOK。
                                // mReadBuffer[READBUFFERSIZE - 1] は、バッファー境界内だが、「\0」を入れられなくなるのでNG。
                                mReadBuffer[mReadBufferCounter] = c;

                                mReadBufferCounter++;
                            } else
                            {    // バッファーあふれ。初期化
                                mReadBufferCounter = 0;
                            }
                        }
                    }
                    break;
            }
        }
    };

    /**************************************************************/
    /* クラス名     :BluetoothService
    /* 機能名       :Bluetoothのサービス処理
    /* 機能概要     :Bluetoothの「接続」「切断」などのサービス処理
    /* 作成日       :2019/2/20         長谷川　勇太      新規作成
    /**************************************************************/
    static public class BluetoothService {

        // 定数（Bluetooth UUID）
        private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // 定数
        public static final int MESSAGE_STATECHANGE      = 1;
        public static final int STATE_NONE               = 0;
        public static final int STATE_CONNECT_START      = 1;
        public static final int STATE_CONNECT_FAILED     = 2;
        public static final int STATE_CONNECTED          = 3;
        public static final int STATE_CONNECTION_LOST    = 4;
        public static final int STATE_DISCONNECT_START   = 5;
        public static final int STATE_DISCONNECTED       = 6;
        public static final int MESSAGE_READ             = 7;

        // メンバー変数
        private int mState;
        private ConnectionThread mConnectionThread;
        private Handler mHandler;

        // 接続時処理用のスレッド------------------------------------------------------------------------------
        private class ConnectionThread extends Thread {

            private BluetoothSocket mBluetoothSocket;           //  BluetoothDeviceを取得し、socketを取得する
            private InputStream mInput;                         //メッセージinput

            // コンストラクタ---------------------------------------------------------------------------------
            public ConnectionThread(BluetoothDevice bluetoothdevice) {
                Log.d("Bluetooththread", "コンストラクタ");
                try {
                    mBluetoothSocket = bluetoothdevice.createRfcommSocketToServiceRecord(UUID_SPP);
                    mInput = mBluetoothSocket.getInputStream();
                } catch (IOException e) {
                    Log.e("BluetoothService", "failed : bluetoothdevice.createRfcommSocketToServiceRecord( UUID_SPP )", e);
                }
            }

            // 処理---------------------------------------------------------------------------------------------
            public void run() {
                while (STATE_DISCONNECTED != mState) {
                    switch (mState) {
                        case STATE_NONE:
                            break;
                        case STATE_CONNECT_START:    // 接続開始
                            try {
                                Log.d("BluetoothService", "Failed : mBluetoothSocket.connect()");
                                // BluetoothSocketオブジェクトを用いて、Bluetoothデバイスに接続を試みる。
                                mBluetoothSocket.connect();
                            } catch (IOException e) {    // 接続失敗
                                setState(STATE_CONNECT_FAILED);
                                cancel();    // スレッド終了。
                                return;
                            }
                            // 接続成功
                            setState(STATE_CONNECTED);
                            break;
                        case STATE_CONNECT_FAILED:        // 接続失敗
                            // 接続失敗時の処理の実体は、cancel()。
                            break;
                        case STATE_CONNECTED:        // 接続済み（Bluetoothデバイスから送信されるデータ受信）
                            byte[] buf = new byte[1024];
                            int bytes;
                            try{
                                //文字列読み込み
                                bytes = mInput.read( buf );
                                mHandler.obtainMessage( MESSAGE_READ, bytes, -1, buf ).sendToTarget();
                                Log.d("BluetoothService", "run読み込み");

                            }catch (IOException e){
                                Log.d("BluetoothService", "runスレッド終了");
                                setState( STATE_CONNECTION_LOST );
                                cancel();    // スレッド終了。
                                break;
                            }
                            break;
                        case STATE_CONNECTION_LOST:    // 接続ロスト
                            Log.d("BluetoothService", "run接続ロスト");
                            // 接続ロスト時の処理の実体は、cancel()。
                            break;
                        case STATE_DISCONNECT_START:    // 切断開始
                            Log.d("BluetoothService", "run切断開始");
                            // 切断開始時の処理の実体は、cancel()。
                            break;
                    }
                }
                synchronized (BluetoothService.this) {    // 親クラスが保持する自スレッドオブジェクトの解放（自分自身の解放）
                    mConnectionThread = null;
                }
            }

            /**************************************************************/
            /* 関数名       :cancel
            /* 機能名       :キャンセル（接続を終了する）
            /* 機能概要     :キャンセル（接続を終了する。ステータスを
                             STATE_DISCONNECTEDにすることによってスレッドも終了する
            /* 作成日       :2019/03/05         長谷川　勇太      新規作成
            /**************************************************************/
            public void cancel() {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e) {
                    Log.e("BluetoothService", "Failed : mBluetoothSocket.close()", e);
                }
                setState(STATE_DISCONNECTED);
            }
        }

        /**************************************************************/
        /* 関数名       :BluetoothService
        /* 機能名       :コンストラクタ
        /* 機能概要     :スレッド作成と開始を行う
                         (BluetoothServiceで初めに入ってくる)
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        public BluetoothService(Context context, Handler handler, BluetoothDevice device) {
            mHandler = handler;
            mState = STATE_NONE;
            Log.d("BluetoothService", "コンストラクタ");
            // 接続時処理用スレッドの作成と開始
            mConnectionThread = new ConnectionThread(device);
            mConnectionThread.start();
        }

        /**************************************************************/
        /* 関数名       :setState(synchronized)
        /* 機能名       :ステータス設定
        /* 機能概要     :ハンドラーにメッセージを送る
                        ※synchronizedは排他制御
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        public synchronized void setState(int state) {
            Log.d("BluetoothService", "ステータス設定");
            mState = state;
            mHandler.obtainMessage(MESSAGE_STATECHANGE, state, -1).sendToTarget();
            return;
        }

        /**************************************************************/
        /* 関数名       :connect(synchronized)
        /* 機能名       :接続開始時処理
        /* 機能概要     :2回目以降呼ばれない
                        １つのBluetoothServiceオブジェクトに対して、connect()は１回だけ呼べる。
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        public synchronized void connect() {
            if (STATE_NONE != mState) {
                return;
            }
            Log.d("BluetoothService", "コンストラクタコネクト");
            // ステータス設定
            setState(STATE_CONNECT_START);
        }

        /**************************************************************/
        /* 関数名       :disconnect(synchronized)
        /* 機能名       :切断開始時処理
        /* 機能概要     :接続中以外は、処理しない。
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        public synchronized void disconnect() {
            if (STATE_CONNECTED != mState) {
                return;
            }
            Log.d("BluetoothService", "disconnect");
            // ステータス設定
            setState(STATE_DISCONNECT_START);
            mConnectionThread.cancel();
        }
    }

        /**************************************************************/
        /* 関数名       :connect
        /* 機能名       :接続
        /* 機能概要     :デバイスアドレスの中身を見て、接続を開始する
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        private void connect()
        {
            if( mDeviceAddress.equals( "" ) )
            {    // DeviceAddressが空の場合は処理しない
                Toast.makeText(this,"空", Toast.LENGTH_SHORT ).show();
                return;
            }

        if( null != mBluetoothService )
            {    // mBluetoothServiceがnullでないなら接続済みか、接続中。
                state.setText("接続中");
                Toast.makeText(this,"接続中", Toast.LENGTH_SHORT ).show();
                return;
            }

            Toast.makeText(this,"本当の接続", Toast.LENGTH_SHORT ).show();
            Log.d("BluetoothService", "本当の接続");
            // 接続
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( mDeviceAddress );
            mBluetoothService = new BluetoothService( this, mHandler, device );
            mBluetoothService.connect();

        }

        /**************************************************************/
        /* 関数名       :disconnect
        /* 機能名       :切断
        /* 機能概要     :デバイスアドレスの中身を見て、切断を開始する
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        private void disconnect()
        {
            if( null == mBluetoothService )
            {    // mBluetoothServiceがnullなら切断済みか、切断中。
                Log.d("BluetoothService", "接続中か切断中");
                return;
            }
            Toast.makeText(this,"切断完了",Toast.LENGTH_SHORT).show();

            Log.d("BluetoothService", "本当の切断");
            // 切断
            mBluetoothService.disconnect();
            mBluetoothService = null;
        }

        /**************************************************************/
        /* 関数名       :onStart
        /* 機能名       :スタート
        /* 機能概要     :呼び出される度
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onStart(){
            super.onStart();
            connect();
            count_down = true;

            Log.d("BluetoothService", "onStart");
        }

        /**************************************************************/
        /* 関数名       :onRestart
        /* 機能名       :リスタート
        /* 機能概要     :onStop　   →　onRestart
                         onRestart  →　onStart
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onRestart(){
            super.onRestart();
            connect();
            count_down =true;

            Log.d("BluetoothService", "onRestart");
        }

        /**************************************************************/
        /* 関数名       :onResume
        /* 機能名       :リジウム
        /* 機能概要     :アクティビティが前面に来るとき
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onResume(){
            super.onResume();

            // GUIアイテムの有効無効の設定
            mButton_Connect.setEnabled( false );
            mButton_Disconnect.setEnabled( false );

            // デバイスアドレスが空でなければ、接続ボタンを有効にする。
            if( !mDeviceAddress.equals( "" ) )
            {
                mButton_Connect.setEnabled( true );
            }

            // 接続ボタンを押す
            mButton_Connect.callOnClick();
            Log.d("BluetoothService", "onResume");
            connect();
            count_down = true;

        }

        /**************************************************************/
        /* 関数名       :onPause
        /* 機能名       :ポーズ
        /* 機能概要     :ユーザーがActivityを離れる前に呼び出されます。
                         backGroundで動かす場合や閉じられる前にデータを保存しなければいけない場合はここで実施します。
                        また、この状態に入ったActivityをユーザーが再度呼び出した場合は、onResumeへと遷移します。
                        また、これが呼び出されたときに絶対にAcitivityが破棄されるわけではないので、
                        もし破棄された際の処理をしたい場合はonDestoryを呼ぶ必要があります。
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onPause() {
            super.onPause();  // Always call the superclass method first
            Log.d("BluetoothService", "onPause");
            count_down = false;
            if( null != mBluetoothService )
            {
                mBluetoothService.disconnect();
                mBluetoothService = null;
            }
        }

        /**************************************************************/
        /* 関数名       :onStop
        /* 機能名       :ストップ
        /* 機能概要     :Activityがユーザーから見えなくなったときに呼び出されます。
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        public void onStop(){
            super.onStop();
            Log.d("BluetoothService", "onStop");
            if( null != mBluetoothService )
            {
                mBluetoothService.disconnect();
                mBluetoothService = null;
            }
            count_down = false;

        }

        /**************************************************************/
        /* 関数名       :onDestroy
        /* 機能名       :デストロイ
        /* 機能概要     :Acitivytが終わるときに呼び出される
        /* 作成日       :2019/03/05         長谷川　勇太      新規作成
        /**************************************************************/
        @Override
        protected void onDestroy()
        {
            super.onDestroy();

            if( null != mBluetoothService )
            {
                mBluetoothService.disconnect();
                mBluetoothService = null;
            }
            count_down = false;
        }

}
