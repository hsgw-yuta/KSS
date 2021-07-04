package com.example.ohs70293.hew;

/**************************************************************/
/* import                                                     */
/**************************************************************/
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**************************************************************/
/* クラス名     :MyOpenHelper
/* 機能名       :DataBaseのテーブル構築
/* 機能概要     :"key"や"text"などのテーブル指定
/* 作成日       :2019/2/20         長谷川　勇太      新規作成
/**************************************************************/
public class MyOpenHelper extends SQLiteOpenHelper {

    //コンストラクタ
    public MyOpenHelper(Context context) {
        super(context, "ms.db", null, 1);
    }

     /**************************************************************/
     /* 関数名       :onCreate
     /* 機能名       :クラスが呼ばれると最初に呼ばれる
     /* 機能概要     :ウィジェットや、変数などの初期設定
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table my_table ("
                + "_id  integer primary key autoincrement not null,"  // _id はSystem予約,場所や名前を変えない事
                + "key  integer not null,"                                // Recordの "key" 領域を Integerで作成
                + "text text not null )");                                 // Recordの "text"領域を Stringで作成
    }
    /**************************************************************/
     /* 関数名       :onUpgrade
     /* 機能名       :アップグレード
     /* 機能概要     :新しいバージョンのデータベースを作成するときに使用
     /* 作成日       :2019/2/20         長谷川　勇太      新規作成
     /**************************************************************/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}