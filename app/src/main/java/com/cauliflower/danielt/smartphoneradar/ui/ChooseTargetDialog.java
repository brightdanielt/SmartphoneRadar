package com.cauliflower.danielt.smartphoneradar.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.RadarPreferences;

import java.util.Arrays;

/**
 * Choose which target to track
 */
public class ChooseTargetDialog extends AppCompatDialogFragment {

    //選項
    private CharSequence[] mItemsCharSequences;
    private DialogListener mListener;

    private String selectedEmail;
    // Default is no item checked
    private int mDefaultIndex = -1;
    private AlertDialog.Builder mBuilder;

    //點擊按鈕的 callback
    public interface DialogListener {
        void onPositiveClick(DialogInterface dialogInterface, String email);

        void onNegativeClick(DialogInterface dialogInterface);
    }

    public void setItemCharSequences(CharSequence[] itemCharSequences) {
        this.mItemsCharSequences = itemCharSequences;
    }

    public void setListener(DialogListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mBuilder = new AlertDialog.Builder(getContext());

        if (mItemsCharSequences == null || mListener == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        //User has no target
        if (mItemsCharSequences.length == 0) {
            return noTargetDialog();
        }

        String trackingTargetEmail = RadarPreferences.getTrackingTargetEmail(getContext());

        if (!trackingTargetEmail.equals("")) {
            //Get index of default selected item
            mDefaultIndex = Arrays.binarySearch(mItemsCharSequences, trackingTargetEmail);
        }

        //Set default selectedEmail
        //如果沒有選，按 ok 時直接回傳 null 更方便，所以不給預設值
        /*selectedEmail = (String) mItemCharSequences[defaultIndex];*/

        mBuilder.setTitle(getString(R.string.changeTrackingTarget));

        mBuilder.setSingleChoiceItems(mItemsCharSequences, mDefaultIndex,
                (dialog, which) -> selectedEmail = (String) mItemsCharSequences[which]);

        mBuilder.setPositiveButton(R.string.ok,
                (dialog, which) -> mListener.onPositiveClick(dialog, selectedEmail));

        mBuilder.setNegativeButton(R.string.cancel,
                (dialog, which) -> mListener.onNegativeClick(dialog));

        return mBuilder.create();
    }

    /**
     * 提醒使用者還沒有任何追蹤對象
     */
    private Dialog noTargetDialog() {
        mBuilder.setTitle(R.string.QQ);
        mBuilder.setMessage(R.string.pleaseAddTargetFirst);
        mBuilder.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent i = new Intent(getContext(), AccountActivity.class);
            startActivity(i);
        });
        /*mBuilder.setNegativeButton(R.string.cancel, null);*/
        return mBuilder.create();
    }

}
