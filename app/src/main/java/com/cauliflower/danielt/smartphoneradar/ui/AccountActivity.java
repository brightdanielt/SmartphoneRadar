package com.cauliflower.danielt.smartphoneradar.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.cauliflower.danielt.smartphoneradar.R;
import com.cauliflower.danielt.smartphoneradar.data.MainDb;
import com.cauliflower.danielt.smartphoneradar.firebase.RadarAuthentication;
import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


import static com.cauliflower.danielt.smartphoneradar.data.RadarContract.UserEntry;

public class AccountActivity extends AppCompatActivity {

    public static final String TAG = AccountActivity.class.getSimpleName();
    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    private static final int REQUEST_CODE_SIGNIN_FIREBASE_AUTH = 102;

    private String mIMEI, mModel;

    private Button mBtn_signIn, mBtn_addTragetTracked;
    private TextView mTv_userInfo, mTv_hintTargetTracked;
    private ListView mListView_targetTracked;
    private List<User> mTargetTrackedList = new ArrayList<>();
    private TargetTrackedAdapter mAdapter_targetTracked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        makeViewWork();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getPhoneInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeViewWork() {
        mBtn_signIn = findViewById(R.id.btn_singIn);
        mBtn_addTragetTracked = findViewById(R.id.btn_addTargetTracked);
        mTv_userInfo = findViewById(R.id.tv_userInfo);
        mTv_hintTargetTracked = findViewById(R.id.tv_hintTargetTracked);
        mListView_targetTracked = findViewById(R.id.listView_targetTracked);

        mTv_userInfo.setText("info from firebase auth");
        mTargetTrackedList.addAll(MainDb.searchUser(this, UserEntry.USED_FOR_GETLOCATION));
        mAdapter_targetTracked = new TargetTrackedAdapter(mTargetTrackedList);
        mListView_targetTracked.setAdapter(mAdapter_targetTracked);

        updateView();
    }

    private void updateView() {

        mTargetTrackedList.clear();

        mTv_userInfo.setText("user info from firebase auth");
        mTargetTrackedList.addAll(MainDb.searchUser(AccountActivity.this, UserEntry.USED_FOR_GETLOCATION));

        if (!(FirebaseAuth.getInstance().getCurrentUser().getEmail().trim()).equals("")) {
            mBtn_signIn.setVisibility(View.INVISIBLE);
        } else {
            //綁定帳號處，顯示登入按鈕
            mBtn_signIn.setVisibility(View.VISIBLE);
        }
        if (mTargetTrackedList.size() > 0) {
            mTv_hintTargetTracked.setVisibility(View.GONE);
        }
        mAdapter_targetTracked.notifyDataSetChanged();

    }

    public void clickBtn(View view) {
        switch (view.getId()) {
            case R.id.btn_singIn: {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    RadarAuthentication.signOut(AccountActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mTv_userInfo.setText(R.string.notSignIn);
                            }
                        }
                    });
                }
                RadarAuthentication.signIn(AccountActivity.this, REQUEST_CODE_SIGNIN_FIREBASE_AUTH);
                break;
            }
            case R.id.btn_addTargetTracked: {
                break;
            }
        }
    }

    private void getPhoneInfo() {
        String manufacturer = Build.MANUFACTURER;
        mModel = Build.MODEL;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE);
            }

            return;
        }
        //若未取得權限，getDeviceId 回傳 null
        mIMEI = telephonyManager.getDeviceId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_PHONE_STATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneInfo();
        }
    }

    public class TargetTrackedAdapter extends BaseAdapter {
        List<User> userList;

        TargetTrackedAdapter(List<User> userList) {
            this.userList = userList;
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = LayoutInflater.from(AccountActivity.this).inflate(R.layout.list_view_item, null);
            TextView tv_account = v.findViewById(R.id.ckBox_account);
            final String account = userList.get(position).getAccount();
            final String password = userList.get(position).getPassword();
            final String usedFor = userList.get(position).getUsedFor();
            String in_use = userList.get(position).getIn_use();
            tv_account.setText(account);
            if (in_use.equals(UserEntry.IN_USE_YES)) {
                tv_account.append("  已登入");
            }
            convertView = v;

            if (usedFor.equals(UserEntry.USED_FOR_GETLOCATION)) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(AccountActivity.this)
                                .setTitle("問問你～")
                                .setMessage(Html.fromHtml("以 <font color=\"blue\">" + account + "</font> 的身份登入嗎？"))
                                .setCancelable(false)
                                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //要求 RadarDbHelper 更改該 User 的 in_use 為 yes
                                        MainDb.updateUser_in_use(AccountActivity.this, account);
                                        updateView();
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();

                    }
                });
            }
            return convertView;
        }
    }


}
