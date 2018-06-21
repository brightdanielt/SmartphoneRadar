package com.cauliflower.danielt.smartphoneradar.tool;

import android.content.Context;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.interfacer.SocketInterface;
import com.cauliflower.danielt.smartphoneradar.interfacer.Updater;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by danielt on 2018/3/19.
 */

public class ConnectServer implements SocketInterface {

    private static final String TAG = ConnectServer.class.getSimpleName();

    private static final String SERVER_ADDRESS_INDEX = "http://114.34.203.58/SmartphoneRadar/index.php";
    private static final String SERVER_ADDRESS_FORGET_PASSWORD = "http://114.34.203.58/SmartphoneRadar/forget_password.php";

    public static final String NO_INTERNET = "ConnectException";
    public static final String NO_RESPONSE = "SocketTimeoutException";

    private Context mContext;

    public ConnectServer(Context context) {
        this.mContext = context;
    }

    @Override
    public String sendHttpRequest(final String params, final String string_url) {
        final String[] result = {""};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(string_url);
                    HttpURLConnection connection;
                    connection = (HttpURLConnection) url.openConnection();
                    //HttpURLConnection 預設是false，因為要送出資料，所以改為true
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(5000);
                    PrintWriter out = new PrintWriter(connection.getOutputStream());

                    out.println(params);
                    out.close();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        result[0] = result[0].concat(inputLine);
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ConnectException e) {
                    result[0] = NO_INTERNET;
                    e.printStackTrace();
                } catch (SocketTimeoutException e) {
                    result[0] = NO_RESPONSE;
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            //等待 thread 執行完，得到 result
            //否則會回傳 result = ""
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    @Override
    public int startListening(int port) {
        return 0;
    }

    @Override
    public void stopListening() {

    }

    @Override
    public void exit() {

    }

    @Override
    public int getListeningPort() {
        return 0;
    }

    /**
     * 透過連線結果，判斷伺服器是否能連上
     *
     * @return {@code true} 連線成功，伺服器可連上
     * {@code false} 連線失敗，伺服器無法連上
     */
    public static boolean checkServerOnline() {
        URLConnection connection = null;
        try {
            URL url = new URL(SERVER_ADDRESS_INDEX);
            connection = url.openConnection();
            //超過5秒則連線逾時
            connection.setConnectTimeout(5000);
            connection.connect();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //註冊
    public String signUp(String account, String password, String model, String imei_1) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&model=" + URLEncoder.encode(model, "UTF-8") +
                "&imei_1=" + URLEncoder.encode(imei_1, "UTF-8") +
                "&action=" + URLEncoder.encode("signUp", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_INDEX);
        Log.i(TAG, "Response: " + response);

        return response;

    }

    //logIn 用於驗證該組定位帳密是否存在、手機型號與 IMEI 是否正確
    public String logIn_sendLocation(String account, String password, String model, String imei) throws
            UnsupportedEncodingException {
        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&model=" + URLEncoder.encode(model, "UTF-8") +
                "&imei=" + URLEncoder.encode(imei, "UTF-8") +
                "&action=" + URLEncoder.encode("login", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_INDEX);
        Log.i(TAG, "Response: " + response);

        return response;

    }

    //logIn 用於驗證該組查詢帳密是否存在
    public String logIn_getLocation(String account, String password) throws
            UnsupportedEncodingException {
        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("login", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_INDEX);
        Log.i(TAG, "Response: " + response);

        return response;

    }

    public String sendLocationToServer(String username, String password, String time, double latitude, double longitude) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(username, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&time=" + URLEncoder.encode(time, "UTF-8") +
                "&latitude=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8") +
                "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8") +
                "&action=" + URLEncoder.encode("updateLocation", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_INDEX);
        Log.i(TAG, "Response: " + response);
        return response;

    }

    public void getLocationFromServer(String account, String password, String time) throws UnsupportedEncodingException {
        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&time_to_compare=" + URLEncoder.encode(time, "UTF-8") +
                "&action=" + URLEncoder.encode("getLocation", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_INDEX);
        try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
            sp.parse(new ByteArrayInputStream(response.getBytes()), new HandlerXML((Updater) mContext));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            throw new ClassCastException(mContext.toString() + " must implement Updater.");
        }
    }

    //驗證帳號，成功則傳送驗證碼到信箱
    public String sendVerificationCodeToEmail(String account, String model, String imei, String email, String verification_code) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&model=" + URLEncoder.encode(model, "UTF-8") +
                "&imei=" + URLEncoder.encode(imei, "UTF-8") +
                "&target_email=" + URLEncoder.encode(email, "UTF-8") +
                "&verification_code=" + URLEncoder.encode(verification_code, "UTF-8") +
                "&action=" + URLEncoder.encode("sendVerificationCodeToEmail", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_FORGET_PASSWORD);
        Log.i(TAG, "Response: " + response);
        return response;
    }

    //更改密碼
    public String updatePassword(String account, String password) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("update_password", "UTF-8") +
                "&";

        Log.i(TAG, "Params: " + params);
        String response = sendHttpRequest(params, SERVER_ADDRESS_FORGET_PASSWORD);
        Log.i(TAG, "Response: " + response);
        return response;
    }

}
