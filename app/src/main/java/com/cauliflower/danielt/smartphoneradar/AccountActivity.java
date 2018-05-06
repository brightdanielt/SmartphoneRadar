package com.cauliflower.danielt.smartphoneradar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_LOGIN_TO_GET_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_LOGIN_TO_SEND_LOCATION;
import static com.cauliflower.danielt.smartphoneradar.AccountActivity.LoadingTask.TASK_SIGN_UP;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_IN_USE_NO;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_IN_USE_YES;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_GETLOCATION;
import static com.cauliflower.danielt.smartphoneradar.tool.MyDbHelper.VALUE_USER_USEDFOR_SENDLOCATION;


public class AccountActivity extends AppCompatActivity {
    private MyDbHelper dbHelper;
    private ConnectDb connectDb;
    private ResponseCode responseCode;

    private ProgressDialog dialog_loading;
    private MyDialogBuilder dialogBuilder_logIn;
    private AlertDialog dialog_logIn = null;

    private String account_sendLocation, password_sendLocation,
            account_getLocation, password_getLocation;

    private String imei, model;

    private Button btn_sendLocation_logIn, btn_sendLocation_signUp, btn_getLocation_logIn;
    private TextView tv_hint_sendLocation, tv_hint_getLocation;
    private ListView listView_sendLocation, listView_getLocation;
    private List<User> userList_sendLocation = new ArrayList<>();
    private List<User> userList_getLocation = new ArrayList<>();
    private MyAdapter adapter_sendLocation, adapter_getLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        dbHelper = new MyDbHelper(AccountActivity.this);
        connectDb = new ConnectDb(AccountActivity.this);
        responseCode = new ResponseCode(AccountActivity.this);
        makeViewWork();
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
        userList_sendLocation.addAll(dbHelper.searchUser(VALUE_USER_USEDFOR_SENDLOCATION));
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

    public class LoadingTask extends AsyncTask<Integer, Object, String> {
        public static final int TASK_SIGN_UP = 101;
        public static final int TASK_LOGIN_TO_SEND_LOCATION = 102;
        public static final int TASK_LOGIN_TO_GET_LOCATION = 103;
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
                        model = "model00011";
                        imei = "imei000011";
                        response = connectDb.signUp(account_sendLocation, password_sendLocation, model, imei);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case TASK_LOGIN_TO_SEND_LOCATION:
                case TASK_LOGIN_TO_GET_LOCATION: {
                    try {
                        //要求 Server 驗證該組帳密
                        response = connectDb.logIn(account_getLocation, password_getLocation);
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
                        Toast.makeText(AccountActivity.this, account_sendLocation + "註冊定位用帳號成功", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        //要判斷是否已存在該筆資料
                        //資料結構可能要調整
                        dbHelper.addUser(account_sendLocation, password_sendLocation, VALUE_USER_USEDFOR_SENDLOCATION, VALUE_USER_IN_USE_NO);
                        Toast.makeText(AccountActivity.this, account_sendLocation + "登入定位用帳號成功", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        dbHelper.addUser(account_getLocation, password_getLocation, VALUE_USER_USEDFOR_GETLOCATION, VALUE_USER_IN_USE_NO);
                        Toast.makeText(AccountActivity.this, account_getLocation + "登入查詢用帳號成功", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                updateView();
            } else {
                //Server不存在該帳密，應重新輸入帳密以登入、查詢手機位置或註冊
                Toast.makeText(AccountActivity.this, R.string.wrong_user, Toast.LENGTH_SHORT).show();
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

        public MyButtonClickListener(int resId_dialogTitle) {
            resId_title = resId_dialogTitle;
        }

        @Override
        public void onClick(View v) {
            dialogBuilder_logIn = new MyDialogBuilder(AccountActivity.this, resId_title);
            switch (v.getId()) {
                case R.id.btn_sendLocation_signUp: {
                    dialogBuilder_logIn.setOnOkButtonClickListener(
                            new LogInDialogListener(TASK_SIGN_UP, getString(R.string.ok)));
                    dialogBuilder_logIn.setOnCancelButtonClickListener(
                            new LogInDialogListener(TASK_SIGN_UP, getString(R.string.cancel)));
                    break;
                }
                case R.id.btn_sendLocation_logIn: {
                    dialogBuilder_logIn.setOnOkButtonClickListener(
                            new LogInDialogListener(TASK_LOGIN_TO_SEND_LOCATION, getString(R.string.ok)));
                    dialogBuilder_logIn.setOnCancelButtonClickListener(
                            new LogInDialogListener(TASK_LOGIN_TO_SEND_LOCATION, getString(R.string.cancel)));
                    break;
                }
                case R.id.btn_getLocation_logIn: {
                    dialogBuilder_logIn.setOnOkButtonClickListener(
                            new LogInDialogListener(TASK_LOGIN_TO_GET_LOCATION, getString(R.string.ok)));
                    dialogBuilder_logIn.setOnCancelButtonClickListener(
                            new LogInDialogListener(TASK_LOGIN_TO_GET_LOCATION, getString(R.string.cancel)));
                    break;
                }
            }
            dialog_logIn = dialogBuilder_logIn.create();
            dialog_logIn.show();
        }
    }

    //dialog_logIn 的按鈕監聽器
    class LogInDialogListener implements View.OnClickListener {
        int task;
        String whichButton;

        public LogInDialogListener(int task, String whichButton) {
            this.task = task;
            this.whichButton = whichButton;
        }

        @Override
        public void onClick(View v) {
            if (whichButton.equals(getString(R.string.cancel))) {
                dialog_logIn.dismiss();
            } else if (whichButton.equals(getString(R.string.ok))) {
                switch (task) {
                    case TASK_SIGN_UP: {
                        account_sendLocation = dialogBuilder_logIn.getAccount();
                        password_sendLocation = dialogBuilder_logIn.getPassword();
                        if (account_sendLocation != null && password_sendLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_SIGN_UP);
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_SEND_LOCATION: {
                        account_sendLocation = dialogBuilder_logIn.getAccount();
                        password_sendLocation = dialogBuilder_logIn.getPassword();
                        if (account_sendLocation != null && password_sendLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_SEND_LOCATION);
                        }
                        break;
                    }
                    case TASK_LOGIN_TO_GET_LOCATION: {
                        account_getLocation = dialogBuilder_logIn.getAccount();
                        password_getLocation = dialogBuilder_logIn.getPassword();
                        if (account_getLocation != null && password_getLocation != null) {
                            dialog_logIn.dismiss();
                            new LoadingTask().execute(TASK_LOGIN_TO_GET_LOCATION);
                        }
                        break;
                    }
                }
            }
            dialog_logIn = null;
            dialogBuilder_logIn = null;
        }
    }
}
