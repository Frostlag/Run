package com.example.dingtao.run;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Dingtao on 6/17/2015.
 */
public class DialogManager {
    public static void Quit(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_new_eatList_title).setNegativeButton(R.string.dialog_cancel,null).setPositiveButton(R.string.dialog_confirm,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Model.get().newEatListConfirmed(((TextView)view.findViewById(R.id.dialog_new_eatList_content)).getText().toString());
                    }
                });
        builder.setView(view);
        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
