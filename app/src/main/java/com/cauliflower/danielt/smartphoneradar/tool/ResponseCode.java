package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.Context;
import android.util.Log;
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
    public static final String SEND_VERIFICATION_CODE_SUCCESS = "108";
    public static final String UPDATE_PASSWORD_SUCCESS = "109";

    public static final String PARAMETER_ERROR = "201";
    public static final String USER_ALREADY_EXISTS = "202";
    public static final String IMEI_ALREADY_EXISTS = "2021";
    public static final String INSERT_USER_ERROR = "203";
    public static final String INSERT_PHONE_INFO_ERROR = "204";
    public static final String UPDATE_LOCATION_ERROR = "205";
    public static final String LOGIN_ERROR = "206";
    public static final String SEND_VERIFICATION_CODE_FAILED = "208";
    public static final String UPDATE_PASSWORD_FAILED = "209";

    private Context context;

    public ResponseCode(Context context) {
        this.context = context;
    }

    //傳回 true，代表該次目的成功，如註冊帳號、查詢位置、登入等等...
    //反之，false 代表該次目的失敗
    public boolean checkCode(String code) {

        switch (code) {
//            case INSERT_USER_SUCCESS:
            case INSERT_PHONE_INFO_SUCCESS:
            case UPDATE_LOCATION_SUCCESS:
            case LOGIN_SUCCESS:
            case SEND_VERIFICATION_CODE_SUCCESS:
            case UPDATE_PASSWORD_SUCCESS: {
                return true;
            }

            case PARAMETER_ERROR: {
                Log.d(context.getClass().getSimpleName(),"Response code "+PARAMETER_ERROR+",PARAMETER_ERROR");
                return false;
            }
            case USER_ALREADY_EXISTS: {
                Log.d(context.getClass().getSimpleName(),"Response code "+USER_ALREADY_EXISTS+",USER_ALREADY_EXISTS");
                Toast.makeText(context, context.getString(R.string.USER_ALREADY_EXISTS), Toast.LENGTH_SHORT).show();
                return false;
            }
            case IMEI_ALREADY_EXISTS: {
                Log.d(context.getClass().getSimpleName(),"Response code "+IMEI_ALREADY_EXISTS+",IMEI_ALREADY_EXISTS");
                Toast.makeText(context, context.getString(R.string.IMEI_ALREADY_EXISTS), Toast.LENGTH_SHORT).show();
                return false;
            }
            case INSERT_USER_ERROR: {
                Log.d(context.getClass().getSimpleName(),"Response code "+INSERT_USER_ERROR+",INSERT_USER_ERROR");
                return false;
            }
            case INSERT_PHONE_INFO_ERROR: {
                Log.d(context.getClass().getSimpleName(),"Response code "+INSERT_PHONE_INFO_ERROR+",INSERT_PHONE_INFO_ERROR");
                return false;
            }
            case UPDATE_LOCATION_ERROR: {
                Log.d(context.getClass().getSimpleName(),"Response code "+UPDATE_LOCATION_ERROR+",UPDATE_LOCATION_ERROR");
                return false;
            }
            case LOGIN_ERROR: {
                Log.d(context.getClass().getSimpleName(),"Response code "+LOGIN_ERROR+",LOGIN_ERROR");
                Toast.makeText(context, context.getString(R.string.LOGIN_ERROR), Toast.LENGTH_SHORT).show();
                return false;
            }
            case SEND_VERIFICATION_CODE_FAILED: {
                Log.d(context.getClass().getSimpleName(),"Response code "+SEND_VERIFICATION_CODE_FAILED+",SEND_VERIFICATION_CODE_FAILED");
                Toast.makeText(context, context.getString(R.string.SEND_VERIFICATION_CODE_FAILED), Toast.LENGTH_SHORT).show();
                return false;
            }
            case UPDATE_PASSWORD_FAILED: {
                Log.d(context.getClass().getSimpleName(),"Response code "+UPDATE_PASSWORD_FAILED+",UPDATE_PASSWORD_FAILED");
                return false;
            }

        }
        return false;
    }
}
