package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;

/*****************************************************************************/
/*
 * クラス名 :PipeActivity /* 機能名 :メイン画面 /* 機能概要 :トレーニング、ストップウォッチ、カレンダー、カメラ機能が存在
 * し、機能する。 /* [トレーニング] : ダンベル、腹筋機能を実装 [ストップウォッチ] : ラップタイムも計測可能 [カレンダー] :
 * カレンダーの日付ごとにトレーニング 内容閲覧可能 [カメラ] : 撮影された画像表示される /* 作成日 :2019/2/20 長谷川 勇太 新規作成 /
 *****************************************************************************/
public class PipeActivity extends AppCompatActivity implements Runnable, View.OnClickListener {

    // 変数、ウィジェット宣言--------------------------------------------------------------
    // record遷移ボタン
    ImageButton record;

    // ストップウォッチクラス
    // ストップウォッチ変数
    TextView timerText; // タイム描画
    Button startButton; // スタートボタン
    Button stopButton; // ストップボタン
    private final Handler handler = new Handler(); // runハンドラー
    private SimpleDateFormat dataFormat = // タイマ表記形式
            new SimpleDateFormat("mm:ss.SS", Locale.JAPAN);
    ArrayAdapter<String> adapter; // ラップリスト
    ListView listView; // ラップタイムリスト
    private int rap_count = 0; // ラップリストカウント
    private long startTime = 0; // タイム開始時間
    private int r_flg = 0; // ラップ・リセットflg
    private int l_flg = 0;
    private volatile boolean stopRun = false;

    // トレーニング遷移関連ボタン
    ImageButton danbel;
    ImageButton body;
    ImageButton run;
    ImageButton left_btn;
    ImageButton right_btn;
    private int flg = 0;

    // Login画像からの値受け取り
    String login_data;

    // 追加ボタン表示の有無
    boolean add_flg = true;

    // カメラ変数
    ImageButton camera_btn;
    ImageView imageView;
    private final static int RESULT_CAMERA = 1001;

    // カレンダー変数
    CalendarView calenderView;
    TextView Start;
    private String days;
    // private String days_key;
    ArrayAdapter<String> adapter_data;

    // 今日の日付をキーにする。
    Calendar rightNow = Calendar.getInstance();

    /**************************************************************/
    /*
     * 関数名 :onCreate /* 機能名 :クラスが呼ばれると最初に呼ばれる /* 機能概要 :ウィジェットや、変数などの初期設定 /* 作成日
     * :2019/2/20 長谷川 勇太 新規作成 /
     **************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipe);

        // 秒数表示-----------------------------------------------------------------
        timerText = (TextView) findViewById(R.id.timer);
        timerText.setText(dataFormat.format(0));

        // ListViewオブジェクトの取得-----------------------------------------------
        listView = (ListView) findViewById(R.id.rap_time);
        // ArrayAdapterオブジェクト生成
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        // Adapterのセット
        listView.setAdapter(adapter);
        // スタートストップボタン
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
        stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(this);

        // 記録遷移
        record = (ImageButton) findViewById(R.id.button);
        record.setOnClickListener(this);
        Start = (TextView) findViewById(R.id.start);

        // カレンダー機能からのデータベースの参照
        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getReadableDatabase();

        // Login画面からの値受け取り------------------------------------------------------
        Intent intent = getIntent();
        login_data = intent.getStringExtra("login_data");

        // 切り替えボタン--------------------------------------------------------------
        left_btn = (ImageButton) findViewById(R.id.chang_left);
        left_btn.setOnClickListener(this);
        right_btn = (ImageButton) findViewById(R.id.chang_right);
        right_btn.setOnClickListener(this);

        // ダンベルボタン--------------------------------------------------------------
        danbel = (ImageButton) findViewById(R.id.btn_danbel);
        danbel.setOnClickListener(this);

        // 腹筋ボタン----------------------------------------------------------------
        body = (ImageButton) findViewById(R.id.btn_fukkin);
        body.setOnClickListener(this);
        body.setVisibility(View.GONE);

        // ランニングボタン-------------------------------------------------------------
        run = (ImageButton) findViewById(R.id.btn_run);
        run.setOnClickListener(this);
        run.setVisibility(View.GONE);

        // 写真表示-----------------------------------------------------------------
        imageView = (ImageView) findViewById(R.id.image_view);
        camera_btn = (ImageButton) findViewById(R.id.camera_button);
        camera_btn.setOnClickListener(this);

        // カレンダー設定--------------------------------------------------------------
        calenderView = (CalendarView) findViewById(R.id.calenderView);
        ListView listView_data = (ListView) findViewById(R.id.db_data);
        adapter_data = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView_data.setAdapter(adapter_data);

        days = (String.valueOf(rightNow.get(rightNow.YEAR)) + "年" + String.valueOf(rightNow.get(rightNow.MONTH) + 1)
                + "月" + String.valueOf(rightNow.get(rightNow.DAY_OF_MONTH)) + "日");

        /************************************/
        /* カレンダーの日付をクリック押下時 */
        /************************************/
        calenderView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {

                String key = "";
                String text = "";
                Start.setVisibility(View.GONE);

                // days_keyで検索をかける

                Cursor cursor = null; // 読込んだTable内容の格納領域

                try {
                    cursor = db.rawQuery("SELECT * FROM my_table", null); // 全件検索実行

                    String result = ""; // 表示の為の編集領域

                    while (cursor.moveToNext()) { // cursorを次のRecordに進める,無くなればFalseになる
                        // Cursorから結果を取得
                        key = cursor.getString(cursor.getColumnIndex("key")); // key dataをGet
                        text = cursor.getString(cursor.getColumnIndex("text")); // text dataをGet

                        result = key + " \n " + text + "\n"; // 検索結果を\n区切りでresult Stringに蓄積
                        adapter_data.remove(
                                "-----------------------------\n" + key + "\n-----------------------------\n" + text);

                    }

                    cursor = db.rawQuery("SELECT *  FROM my_table  WHERE key = ? ", new String[] { String.valueOf(year)
                            + "年" + String.valueOf(month + 1) + "月" + String.valueOf(dayOfMonth) + "日" });

                    result = "";
                    while (cursor.moveToNext()) { // cursorを次のRecordに進める,無くなればFalseになる
                        // Cursorから結果を取得
                        key = cursor.getString(cursor.getColumnIndex("key")); // key dataをGet
                        text = cursor.getString(cursor.getColumnIndex("text")); // text dataをGet

                        // 検索結果を\n区切りでresult Stringに蓄積
                        result = key + " \n " + text + "\n";
                        adapter_data.add(
                                "-----------------------------\n" + key + "\n-----------------------------\n" + text);

                    }
                    if (result == "") {
                        Start.setVisibility(View.VISIBLE);
                        Start.setText("さぼり");
                        Start.setTextSize(100);
                    }
                } finally {
                    // Cursorをクローズ
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
        // TabHostの初期化および設定処理
        initTabs();
    }

    /**************************************************************/
    /*
     * 関数名 :onActivityResult /* 機能名 :レスポンス関数 /* 機能概要 :requestCodeを送ると戻ってくる関数 /* 作成日
     * :2019/2/20 長谷川 勇太 新規作成 /
     **************************************************************/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CAMERA) {
            Bitmap bitmap;
            // cancelしたケースも含む
            if (data.getExtras() == null) {
                return;
            } else {
                bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    // 画像サイズを計測
                    int bmpWidth = bitmap.getWidth();
                    int bmpHeight = bitmap.getHeight();
                }
            }
            imageView.setImageBitmap(bitmap);

            // カレンダーに貼りたい
        }
    }

    /**************************************************************/
    /*
     * 関数名 :onClick /* 機能名 :クリック処理 /* 機能概要 :ボタンがクリックされたとき呼ばれる関数 /* 作成日 :2019/2/20
     * 長谷川 勇太 新規作成 /
     **************************************************************/
    @Override
    public void onClick(View v) {
        Thread thread;
        switch (v.getId()) {
        case R.id.start_button: // ストップウォッチスタートボタン--------
            if (r_flg == 0) {
                stopRun = false;
                startButton.setText("stop");
                stopButton.setText("ラップ");
                thread = new Thread(this);
                thread.start();
                startTime = System.currentTimeMillis();
                r_flg = 1;
                l_flg = 1;
            } else if (r_flg == 1) {
                stopRun = true;
                startButton.setText("start");
                stopButton.setText("リセット");
                timerText.setText(dataFormat.format(0));
                r_flg = 0;
                l_flg = 0;
            }
            break;
        case R.id.stop_button: // ストップウォッチストップボタン--------
            if (l_flg == 0) {
                stopRun = true;
                timerText.setText(dataFormat.format(0));

                for (int i = 0; i < (rap_count); i++) {
                    String item = (String) listView.getItemAtPosition(0);
                    adapter.remove(item);
                }
                rap_count = 0;
            } else if (l_flg == 1) {
                long rap = System.currentTimeMillis();
                rap = rap - startTime;
                adapter.add(String.valueOf(dataFormat.format(rap)));
                // リストの個数
                rap_count++;
            }
            break;
        case R.id.camera_button: // カメラ起動ボタン-----------------
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, RESULT_CAMERA);
            break;
        case R.id.btn_danbel: // ダンベル入力ボタン----------------
            Intent d_intent = new Intent(getApplication(), Danbel_Activity.class);
            d_intent.putExtra("in_data", login_data);
            d_intent.putExtra("days", days);
            startActivity(d_intent);
            break;
        case R.id.btn_fukkin: // 腹筋入力ボタン------------------
            Intent f_intent = new Intent(getApplication(), Body_Activity.class);
            f_intent.putExtra("login_data", login_data);
            f_intent.putExtra("days", days);
            startActivity(f_intent);
            break;
        case R.id.chang_left: // image画像切換え（左）------------
            flg--;
            if (flg == -1) {
                left_btn.setVisibility(View.GONE);
                danbel.setVisibility(View.GONE);
                body.setVisibility(View.VISIBLE);
            } else if (flg == 0) {
                left_btn.setVisibility(View.VISIBLE);
                right_btn.setVisibility(View.VISIBLE);
                body.setVisibility(View.GONE);
                run.setVisibility(View.GONE);
                danbel.setVisibility(View.VISIBLE);
            }
            break;
        case R.id.chang_right: // image画像切換え（右）---------
            flg++;
            if (flg == 1) {
                right_btn.setVisibility(View.GONE);
                run.setVisibility(View.VISIBLE);
                body.setVisibility(View.GONE);
                danbel.setVisibility(View.GONE);
            } else if (flg == 0) {
                left_btn.setVisibility(View.VISIBLE);
                right_btn.setVisibility(View.VISIBLE);
                body.setVisibility(View.GONE);
                run.setVisibility(View.GONE);
                danbel.setVisibility(View.VISIBLE);
            }
            break;
        case R.id.button: // record遷移ボタン-----------------------------
            Intent record_intent = new Intent(getApplication(), Record_MainActivity.class);
            record_intent.putExtra("add_flg", true);
            startActivity(record_intent);
            break;
        default:

            break;
        }
    }

    /**************************************************************/
    /*
     * 関数名 :run /* 機能名 :ラン /* 機能概要 :ストップウォッチの処理を行う /* 作成日 :2019/2/20 長谷川 勇太 新規作成 /
     **************************************************************/
    @Override
    public void run() {
        // 10 msec order
        int period = 10;

        while (!stopRun) {
            // sleep: period msec
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
                stopRun = true;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long endTime = System.currentTimeMillis();
                    // カウント時間 = 経過時間 - 開始時間
                    long diffTime = (endTime - startTime);

                    timerText.setText(dataFormat.format(diffTime));
                }
            });
        }
    }

    /**************************************************************/
    /*
     * 関数名 :initTabs /* 機能名 :タブ欄の処理 /* 機能概要 :タブを切り替えるときの命名 /* 作成日 :2019/2/20 長谷川 勇太
     * 新規作成 /
     **************************************************************/
    protected void initTabs() {
        try {
            TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
            tabhost.setup();

            TabHost.TabSpec tab1 = tabhost.newTabSpec("tab1");
            tab1.setIndicator("トレーニング");
            tab1.setContent(R.id.traning);
            tabhost.addTab(tab1);

            TabHost.TabSpec tab2 = tabhost.newTabSpec("tab2");
            tab2.setIndicator("ストップウォッチ");
            tab2.setContent(R.id.watch);
            tabhost.addTab(tab2);

            TabHost.TabSpec tab3 = tabhost.newTabSpec("tab3");
            tab3.setIndicator("カレンダー");
            tab3.setContent(R.id.carender);
            tabhost.addTab(tab3);

            TabHost.TabSpec tab4 = tabhost.newTabSpec("tab4");
            tab4.setIndicator("カメラ");
            tab4.setContent(R.id.camera);
            tabhost.addTab(tab4);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}
