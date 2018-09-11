package com.cauliflower.danielt.smartphoneradar.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class ChooseTargetDialog extends AppCompatDialogFragment {

    //選項
    private CharSequence[] itemCharSequences;
    private DialogListener listener;

    //點擊按鈕的 callback
    public interface DialogListener{
        void onPositiveClick(DialogInterface dialogInterface,String selectValue);
        void onNegativeClick(DialogInterface dialogInterface);
    }

    public void setItemCharSequences(CharSequence[] itemCharSequences) {
        this.itemCharSequences = itemCharSequences;
    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }
}
