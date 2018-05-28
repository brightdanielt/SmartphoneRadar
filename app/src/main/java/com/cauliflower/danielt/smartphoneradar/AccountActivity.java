package com.cauliflower.danielt.smartphoneradar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.obj.User;
import com.cauliflower.danielt.smartphoneradar.tool.ConnectDb;
import com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper;
import com.cauliflower.danielt.smartphoneradar.tool.MyDialogBuilder;
import com.cauliflower.danielt.smartphoneradar.tool.ResponseCode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_LOGIN_TO_GET_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_LOGIN_TO_SEND_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_SEND_VERIFICATION_CODE;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_SIGN_UP;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_UPDATE_PASSWORD;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_IN_USE_NO;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_IN_USE_YES;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION;


public class AccountActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_READ_PHONE_STATE = 101;
    private MyDbHelper dbHelper;
    private ConnectDb connectDb;
    private ResponseCode responseCode;

    private ProgressDialog dialog_loading;
    private MyDialogBuilder dialogBuilder_logIn;
    private AlertDialog dialog_logIn = null;

    private String account_sendLocation, password_sendLocation,
            account_getLocation, password_getLocation,
            account_forgetPassword;

    private String imei, model, email;
    private int verification_code;

    private Button btn_sendLocation_logIn, btn_sendLocation_signUp, btn_getLocation_logIn;
    private Button btn_forgetPassword_ok, btn_forgetPassword_cancel, btn_forgetPassword_send_verification_code;
    private EditText edTxt_forgetPassword_account, edTxt_forgetPassword_email, edTxt_forgetPassword_verificationCode;
    private TextView tv_hint_sendLocation, tv_hint_getLocation;
    private ListView listView_sendLocation, listView_getLocation;
    private List<User> userList_sendLocation = new ArrayList<>();
    private List<User> userList_getLocation = new ArrayList<>();
    private MyAdapter adapter_sendLocation, adapter_getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new MyDbHelper(AccountActivity.this);
        connectDb = new ConnectDb(AccountActivity.this);
        responseCode = new ResponseCode(AccountActivity.this);
        makeViewWork();
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

        btn_sendLocation_logIn = findViewById(R.id.btn_sendLocation_logIn);
        btn_sendLocation_signUp = findViewById(R.id.btn_sendLocation_signUp);
        btn_getLocation_logIn = findViewById(R.id.btn_getLocation_logIn);
        //按鈕註冊監聽器
        btn_sendLocation_logIn.setOnClickListener(new MyButtonClickListener(R.string.title_logIn));
        btn_sendLocation_signUp.setOnClickListener(new MyButtonClickListener(R.string.title_signUp));
        btn_getLocation_logIn.setOnClickListener(new MyButtonClickListener(R.string.title_logIn));

        tv_hint_sendLocation = findViewById(R.id.tv_hint_sendLocation);
        tv_hint_getLocation = findViewById(R.id.tv_hint_getLocation);

        listView_sendLocation = findViewById(R.id.listView_sendLocation);
        listView_getLocation = findViewById(R.id.listView_getLocation);

        userList_sendLocation.addAll(dbHelper.searchUser(VALUE_USER_USEDFOR_SENDLOCATION));
        userList_getLocation.addAll(dbHelper.searchUser(VALUE_USER_USEDFOR_GETLOCATION));
        adapter_sendLocation = new MyAdapter(userList_sendLocation);
        adapter_getLocation = new MyAdapter(userList_getLocation);
        listView_sendLocation.setAdapter(adapter_sendLocation);
        listView_getLocation.setAdapter(adapter_getLocation);

        updateView();
    }

    private void updateView() {
        userList_sendLocation.clear();
        userList_getLocation.clear();
//        userList_sendLocation.addAll(dbHelper.searchUser(VALUE_USER_USEDFOR_SENDLOCATION));
        userList_getLocation.addAll(dbHelper.searchUser(VALUE_USER_USEDFOR_GETLOCATION));

        if (userList_sendLocation.size() > 0) {
            tv_hint_sendLocation.setVisibility(View.GONE);
            btn_sendLocation_logIn.setVisibility(View.INVISIBLE);
            btn_sendLocation_signUp.setVisibility(View.INVISIBLE);
        } else {
            //綁定帳號處，顯示登入或註冊按鈕
            btn_sendLocation_logIn.setVisibility(View.VISIBLE);
            btn_sendLocation_signUp.setVisibility(View.VISIBLE);
        }
        if (userList_getLocation.size() > 0) {
            tv_hint_getLocation.setVisibility(View.GONE);
        }
        adapter_sendLocation.notifyDataSetChanged();
        adapter_getLocation.notifyDataSetChanged();

    }

    private void getPhoneInfo() {
        String manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
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
        imei = telephonyManager.getDeviceId();
    }

    public class LoadingTask extends AsyncTask<Integer, Object, String> {
        public static final int TASK_SIGN_UP = 101;
        public static final int TASK_LOGIN_TO_SEND_LOCATION = 102;
        public static final int TASK_LOGIN_TO_GET_LOCATION = 103;
        public static final int TASK_SEND_VERIFICATION_CODE = 104;
        public static final int TASK_UPDATE_PASSWORD = 105;
        private int whichTask = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog_loading = ProgressDialog.show(AccountActivity.this, "",
                    getString(R.string.loading), true);
        }

        @Override
        protected String doInBackground(Integer... tasks) {
            whichTask = tasks[0];
            String response = "";
            switch (whichTask) {
                case TASK_SIGN_UP: {
                    try {
                        //向 Server 註冊該組帳密
                        response = connectDb.signUp(account_sendLocation, password_sendLocation, model, imei);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_SEND_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = connectDb.logIn_sendLocation(
                                account_sendLocation, password_sendLocation, model, imei);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_GET_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = connectDb.logIn_getLocation(account_getLocation, password_getLocation);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_SEND_VERIFICATION_CODE: {
                    try {
                        //要求 Server 發送驗證碼到 Email
                        Random random = new Random();
                        verification_code = 1 + random.nextInt(10000);
                        response = connectDb.sendVerificationCodeToEmail(
                                account_forgetPassword, model, imei, email, String.valueOf(verification_code));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_UPDATE_PASSWORD: {
                    try {
                        //更改密碼
                        response = connectDb.updatePassword(
                                account_forgetPassword, String.valueOf(verification_code));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response.contains("Exception")) {
                //應該是網路或伺服器有問題，跳出對話筐，要求稍後再試試
                MyDialogBuilder dialogBuilder = new MyDialogBuilder(AccountActivity.this, response);
                dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (whichTask) {
                            case TASK_SIGN_UP: {
                                new LoadingTask().execute(TASK_SIGN_UP);
                                break;
                            }
                            case TASK_LOGIN_TO_SEND_LOCATION: {
                                new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                                break;
                            }
                            case TASK_LOGIN_TO_GET_LOCATION: {
                                new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                                break;
                            }
                        }
                    }
                }).show();
            } else if (responseCode.checkCode(response)) {
                //根據回傳值，得知目的成功與否
                switch (whichTask) {
                    case TASK_SIGN_UP: {
                        dbHelper.addUser(account_sendLocation, password_sendLocation, VALUE_USER_USEDFOR_SENDLOCATION, VALUE_USER_IN_USE_YES);
                        Toast.makeText(AccountActivity.this, account_sendLocation + getString(R.string.sighUp_sendLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        //要判斷是否已存在該筆資料
                        //資料結構可能要調整
                        dbHelper.addUser(account_sendLocation, password_sendLocation, VALUE_USER_USEDFOR_SENDLOCATION, VALUE_USER_IN_USE_NO);
                        Toast.makeText(AccountActivity.this, account_sendLocation + getString(R.string.logIn_sendLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        dbHelper.addUser(account_getLocation, password_getLocation, VALUE_USER_USEDFOR_GETLOCATION, VALUE_USER_IN_USE_NO);
                        Toast.makeText(AccountActivity.this, account_getLocation + getString(R.string.logIn_getLocation_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_SEND_VERIFICATION_CODE: {
                        edTxt_forgetPassword_account.setEnabled(false);
                        edTxt_forgetPassword_email.setEnabled(false);
                        edTxt_forgetPassword_verificationCode.setEnabled(true);
                        btn_forgetPassword_send_verification_code.setEnabled(false);
                        btn_forgetPassword_ok.setEnabled(true);
                        Toast.makeText(AccountActivity.this, getString(R.string.please_receive_verification_code), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_UPDATE_PASSWORD: {
                        account_sendLocation = account_forgetPassword;
                        password_sendLocation = String.valueOf(verification_code);
                        dbHelper.addUser(account_sendLocation, password_sendLocation, VALUE_USER_USEDFOR_SENDLOCATION, VALUE_USER_IN_USE_YES);
                        dbHelper.updatePassword(account_forgetPassword, String.valueOf(verification_code));
                        Toast.makeText(AccountActivity.this, account_sendLocation + getString(R.string.update_password_success), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                updateView();
            } else {
                //Server不存在該帳密，應重新輸入帳密以登入、查詢手機位置或註冊
//                Toast.makeText(AccountActivity.this, R.string.wrong_user, Toast.LENGTH_SHORT).show();
            }
            dialog_loading.dismiss();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog_loading != null && dialog_loading.isShowing()) {
            dialog_loading.dismiss();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_PHONE_STATE & grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneInfo();
        }
    }

    public class MyAdapter extends BaseAdapter {
        List<User> userList;

        public MyAdapter(List<User> userList) {
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
            if (in_use.equals(VALUE_USER_IN_USE_YES)) {
                tv_account.append("  已登入");
            }
            convertView = v;

            if (usedFor.equals(MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION)) {
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
                                        //要求 MyDbHelper 更改該 User 的 in_use 為 yes
                                        dbHelper.updateUser_in_use(account);
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

    //AccountActivity的登入、註冊按鈕監聽器
    class MyButtonClickListener implements View.OnClickListener {
        int resId_title;

        MyButtonClickListener(int resId_dialogTitle) {
            resId_title = resId_dialogTitle;
        }

        @Override
        public void onClick(View v) {
            dialogBuilder_logIn = new MyDialogBuilder(AccountActivity.this, resId_title);
            switch (v.getId()) {
                //點擊定位用的註冊按鈕
                case R.id.btn_sendLocation_signUp: {
                    getPhoneInfo();
                    dialogBuilder_logIn.setOnButtonClickListener(
                            new LogInDialogButtonListener(TASK_SIGN_UP));
                    break;
                }
                //點擊定位用的登入按鈕
                case R.id.btn_sendLocation_logIn: {
                    getPhoneInfo();
                    dialogBuilder_logIn.setBtn_forgetPasswordVisibility(View.VISIBLE);
                    dialogBuilder_logIn.setOnButtonClickListener(
                            new LogInDialogButtonListener(TASK_LOGIN_TO_SEND_LOCATION));
                    break;
                }
                //點擊查詢用的登入按鈕
                case R.id.btn_getLocation_logIn: {
                    dialogBuilder_logIn.setOnButtonClickListener(
                            new LogInDialogButtonListener(TASK_LOGIN_TO_GET_LOCATION));
                    break;
                }
            }
            dialog_logIn = dialogBuilder_logIn.create();
            dialog_logIn.show();
        }
    }

    //dialog_logIn 的按鈕監聽器
    class LogInDialogButtonListener implements View.OnClickListener {
        int task;

        public LogInDialogButtonListener(int task) {
            this.task = task;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.dialog_btn_cancel) {
                dialog_logIn.dismiss();
            } else if (v.getId() == R.id.dialog_btn_ok) {
                switch (task) {
                    case TASK_SIGN_UP: {
                        //註冊定位用的帳號
                        account_sendLocation = dialogBuilder_logIn.getAccount();
                        password_sendLocation = dialogBuilder_logIn.getPassword();
                        if (account_sendLocation != null && password_sendLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_SIGN_UP);
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        //登入定位用的帳號
                        account_sendLocation = dialogBuilder_logIn.getAccount();
                        password_sendLocation = dialogBuilder_logIn.getPassword();
                        if (account_sendLocation != null && password_sendLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        //登入定查詢的帳號
                        account_getLocation = dialogBuilder_logIn.getAccount();
                        password_getLocation = dialogBuilder_logIn.getPassword();
                        if (account_getLocation != null && password_getLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                        }
                        break;
                    }
                }
            } else if (v.getId() == R.id.dialog_btn_forgetPassword) {
                dialog_logIn.dismiss();
                final AlertDialog dialog_forgetPassword;
                View view = LayoutInflater.from(
                        AccountActivity.this).inflate(R.layout.dialog_forget_password, null);
                edTxt_forgetPassword_account = view.findViewById(R.id.dialog_forgetPassword_edTxt_account);
                edTxt_forgetPassword_email = view.findViewById(R.id.dialog_forgetPassword_edTxt_email);
                edTxt_forgetPassword_verificationCode = view.findViewById(R.id.dialog_forgetPassword_edTxt_verificationCode);
                btn_forgetPassword_ok = view.findViewById(R.id.dialog_forgetPassword_btn_ok);
                btn_forgetPassword_cancel = view.findViewById(R.id.dialog_forgetPassword_btn_cancel);
                btn_forgetPassword_send_verification_code = view.findViewById(R.id.dialog_forgetPassword_btn_send_verification_code);
                AlertDialog.Builder dialogBuilder_forgetPassword = new AlertDialog.Builder(AccountActivity.this);
                dialogBuilder_forgetPassword.setView(view);
                dialogBuilder_forgetPassword.setTitle("忘記密碼");
                dialogBuilder_forgetPassword.setCancelable(false);
                dialog_forgetPassword = dialogBuilder_forgetPassword.create();
                btn_forgetPassword_send_verification_code.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        account_forgetPassword = edTxt_forgetPassword_account.getText().toString();
                        email = edTxt_forgetPassword_email.getText().toString();
                        if (!(account_forgetPassword.trim()).equals("") && !(email.trim()).equals("")
                                && !(model.trim()).equals("") && !(imei.trim()).equals("")) {
                            //送出 HttpRequest，使用者請到 email 收驗證碼
                            new LoadingTask().execute(TASK_SEND_VERIFICATION_CODE);
                        }
                    }
                });
                btn_forgetPassword_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String verification_code = edTxt_forgetPassword_verificationCode.getText().toString();
                        if (!(verification_code.trim()).equals("")) {
                            if (verification_code.equals(verification_code)) {
                                new LoadingTask().execute(TASK_UPDATE_PASSWORD);
                                dialog_forgetPassword.dismiss();
                            }
                        }
                    }
                });
                btn_forgetPassword_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_forgetPassword.dismiss();
                        edTxt_forgetPassword_account = null;
                        edTxt_forgetPassword_email = null;
                        edTxt_forgetPassword_verificationCode = null;
                        btn_forgetPassword_send_verification_code = null;
                        btn_forgetPassword_cancel = null;
                        btn_forgetPassword_ok = null;
                    }
                });
                dialog_forgetPassword.show();
            }
            dialog_logIn.dismiss();
            dialog_logIn = null;
            dialogBuilder_logIn = null;
        }
    }
}
