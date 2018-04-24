package com.cauliflower.danielt.smartphoneradar.tool;

import android.app.AlertDialog;
import android.content.Context;

import com.cauliflower.danielt.smartphoneradar.R;

import static com.cauliflower.danielt.smartphoneradar.tool.ConnectDb.NO_INTERNET;
import static com.cauliflower.danielt.smartphoneradar.tool.ConnectDb.NO_RESPONSE;

public class MyDialogBuilder extends AlertDialog.Builder {

    public MyDialogBuilder(Context context, String exception) {
        super(context);
        this.setCancelable(false);
        this.setTitle("Something bad ＠_＠");

        switch (exception) {
            case NO_INTERNET: {
                this.setMessage(getContext().getString(R.string.no_internet));
                break;
            }
            case NO_RESPONSE: {
                this.setMessage(getContext().getString(R.string.no_response));
                break;
            }
        }
    }

}
