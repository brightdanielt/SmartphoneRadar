package com.cauliflower.danielt.smartphoneradar.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cauliflower.danielt.smartphoneradar.R;

public class MyDialogBuilder extends AlertDialog.Builder {

    private EditText edTxt_email, edTxt_password;
    Button btn_ok, btn_cancel;

    //用於添加追蹤目標
    public MyDialogBuilder(Context context, int resId_title) {
        super(context);
        this.setCancelable(false);
        this.setTitle(context.getString(resId_title));

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_target_tracked, null);
        edTxt_email = view.findViewById(R.id.dialog_edTxt_email);
        edTxt_password = view.findViewById(R.id.dialog_edTxt_password);
        btn_ok = view.findViewById(R.id.dialog_btn_ok);
        btn_cancel = view.findViewById(R.id.dialog_btn_cancel);

        this.setView(view);
    }

    public String getEmail() {
        if (edTxt_email != null) {
            String email = edTxt_email.getText().toString();
            if (!(email.trim()).equals("")) {
                return email;
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
    }

}
