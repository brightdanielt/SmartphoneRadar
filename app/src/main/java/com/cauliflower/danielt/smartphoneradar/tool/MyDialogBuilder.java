package com.cauliflower.danielt.smartphoneradar.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

    private EditText edTxt_account, edTxt_password;
    Button btn_ok, btn_cancel,btn_forgetPassword;

    //用於登入帳號
    public MyDialogBuilder(Context context,int resId_title) {
        super(context);
        this.setCancelable(false);
        this.setTitle(context.getString(resId_title));

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_login, null);
        edTxt_account = view.findViewById(R.id.dialog_edTxt_account);
        edTxt_password = view.findViewById(R.id.dialog_edTxt_password);
        btn_ok = view.findViewById(R.id.dialog_btn_ok);
        btn_cancel = view.findViewById(R.id.dialog_btn_cancel);
        btn_forgetPassword = view.findViewById(R.id.dialog_btn_forgetPassword);

        this.setView(view);
    }

    public String getAccount() {
        if (edTxt_account != null) {
            String account = edTxt_account.getText().toString();
            if (!(account.trim()).equals("")) {
                return account;
            }
        }
        return null;
    }

    public String getPassword() {
        if (edTxt_password != null) {
            String password = edTxt_password.getText().toString();
            if (!(password.trim()).equals("")) {
                return password;
            }
        }
        return null;
    }

    public void setOnButtonClickListener(View.OnClickListener listener) {
        btn_ok.setOnClickListener(listener);
        btn_cancel.setOnClickListener(listener);
        btn_forgetPassword.setOnClickListener(listener);
    }

}
