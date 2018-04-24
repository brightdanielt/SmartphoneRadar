package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.Context;
import android.widget.Toast;

import com.cauliflower.danielt.smartphoneradar.R;

/**
 * Created by danielt on 2018/3/27.
 */

public class ResponseCode {

    public static final String INSERT_USER_SUCCESS = "103";
    public static final String INSERT_PHONE_INFO_SUCCESS = "104";
    public static final String UPDATE_LOCATION_SUCCESS = "105";
    public static final String LOGIN_SUCCESS = "106";

    public static final String PARAMETER_ERROR = "201";
    public static final String USER_ALREADY_EXISTS = "202";
    public static final String INSERT_USER_ERROR = "203";
    public static final String INSERT_PHONE_INFO_ERROR = "204";
    public static final String UPDATE_LOCATION_ERROR = "205";
    public static final String LOGIN_ERROR = "206";

    private Context context;

    public ResponseCode(Context context) {
        this.context = context;
    }

    //傳回 true，代表該次目的成功，如註冊帳號、查詢位置、登入等等...
    //反之，false 代表該次目的失敗
    public boolean checkCode(String code) {
//        if (code == null) {
//            return false;
//        }
        switch (code) {
            case INSERT_USER_SUCCESS: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.INSERT_USER_SUCCESS), Toast.LENGTH_SHORT).show();
                return true;
            }
            case INSERT_PHONE_INFO_SUCCESS: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.INSERT_PHONE_INFO_SUCCESS), Toast.LENGTH_SHORT).show();
                return true;
            }
            case UPDATE_LOCATION_SUCCESS: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.UPDATE_LOCATION_SUCCESS), Toast.LENGTH_SHORT).show();
                return true;
            }
            case LOGIN_SUCCESS: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.LOGIN_SUCCESS), Toast.LENGTH_SHORT).show();
                return true;
            }
            case PARAMETER_ERROR: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.PARAMETER_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }
            case USER_ALREADY_EXISTS: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.USER_ALREADY_EXISTS), Toast.LENGTH_SHORT).show();
                return false;
            }
            case INSERT_USER_ERROR: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.INSERT_USER_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }
            case INSERT_PHONE_INFO_ERROR: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.INSERT_PHONE_INFO_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }
            case UPDATE_LOCATION_ERROR: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.UPDATE_LOCATION_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }
            case LOGIN_ERROR: {
                Toast.makeText(
                        context, context.getResources().getString(R.string.LOGIN_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }

        }
        return false;
    }
}
