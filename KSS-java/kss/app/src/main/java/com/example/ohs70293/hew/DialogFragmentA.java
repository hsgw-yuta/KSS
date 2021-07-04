package com.example.ohs70293.hew;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;


public class DialogFragmentA extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //ダイアログのタイトル
        builder.setTitle("アプリを終了しますか");
        //アプリを終了するボタン
        builder.setPositiveButton("アプリを終了する", new DialogFragmentA.DialogButtonClickListener());
        //このボタンを押した場合のみダイアログを終了したい
        builder.setNeutralButton("キャンセル", new DialogFragmentA.DialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }
    private class DialogButtonClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch (which) {
                //アプリを終了する処理
                case DialogInterface.BUTTON_POSITIVE:
                    //moveTaskToBack(true);エラー: シンボルを見つけられません
                    //finish();エラー: シンボルを見つけられません
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                    break;
                //ダイアログを終了する処理
                case DialogInterface.BUTTON_NEGATIVE:

                    break;
            }
        }
    }
}
