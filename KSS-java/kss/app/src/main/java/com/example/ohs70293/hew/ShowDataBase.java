package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**************************************************************/
/* クラス名     :ShowDataBase
/* 機能名       :DataBaseの全情報表示
/* 機能概要     :取得した情報をListViewで表示
/* 作成日       :2019/2/20         長谷川　勇太      新規作成
/**************************************************************/
public class ShowDataBase extends Activity {

    ArrayAdapter<String> adapter;

    /**************************************************************/
    /* 関数名       :onCreate
    /* 機能名       :クラスが呼ばれると最初に呼ばれる
    /* 機能概要     :ウィジェットや、変数などの初期設定
    /* 作成日       :2019/2/20         長谷川　勇太      新規作成
    /**************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_database);

        ListView listView = (ListView)findViewById(R.id.db_data);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        //データベースのヘルパークラス
        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = null;         // 読込んだTable内容の格納領域

        //全件検索
        try {
            cursor = db.rawQuery("SELECT * FROM my_table", null);       // 全件検索実行

            String result = "";             // 表示の為の編集領域
            while (cursor.moveToNext()) {  // cursorを次のRecordに進める,無くなればFalseになる
                // Cursorから結果を取得
                String key = cursor.getString(cursor.getColumnIndex("key"));           // key dataをGet
                String text = cursor.getString(cursor.getColumnIndex("text"));   // text dataをGet

                result = key + " \n " + text + "\n";       // 検索結果を\n区切りでresult Stringに蓄積

                adapter.add("-----------------------------\n"
                            + key
                            + "\n-----------------------------\n"
                            + text);
            }

        }finally {
            // Cursorをクローズ
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    
}
