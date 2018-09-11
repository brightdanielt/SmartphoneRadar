package com.cauliflower.danielt.smartphoneradar.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;

import java.util.Arrays;

public class ChooseTargetDialog extends AppCompatDialogFragment {

    //選項
    private CharSequence[] mItemCharSequences;
    private DialogListener mListener;

    private String selectedValue;

    //點擊按鈕的 callback
    public interface DialogListener {
        void onPositiveClick(DialogInterface dialogInterface, String selectValue);

        void onNegativeClick(DialogInterface dialogInterface);
    }

    public void setItemCharSequences(CharSequence[] itemCharSequences) {
        this.mItemCharSequences = itemCharSequences;
    }

    public void setListener(DialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (mItemCharSequences == null || mListener == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String trackingTargetEmail = RadarPreferences.getTrackingTargetEmail(getContext());

        //Get index of default selected item
        int defaultIndex = Arrays.binarySearch(mItemCharSequences, trackingTargetEmail);

        //Set default selectedValue
        //如果沒有選，按 ok 時直接回傳 null 更方便，所以不給預設值
        /*selectedValue = (String) mItemCharSequences[defaultIndex];*/

        builder.setTitle(getString(R.string.changeTrackingTarget));

        builder.setSingleChoiceItems(mItemCharSequences, defaultIndex,
                (dialog, which) -> selectedValue = (String) mItemCharSequences[which]);

        builder.setPositiveButton(R.string.ok,
                (dialog, which) -> mListener.onPositiveClick(dialog, selectedValue));

        builder.setNegativeButton(R.string.cancel,
                (dialog, which) -> mListener.onNegativeClick(dialog));

        return builder.create();
    }
}
