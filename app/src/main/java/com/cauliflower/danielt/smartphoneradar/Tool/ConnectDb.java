package com.cauliflower.danielt.smartphoneradar.Tool;

import android.content.Context;
import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.Interface.SocketInterface;
import com.cauliflower.danielt.smartphoneradar.Interface.Updater;
import com.cauliflower.danielt.smartphoneradar.UI.MapsActivity;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by danielt on 2018/3/19.
 */

public class ConnectDb implements SocketInterface {

    private static final String AUTHENTICATION_SERVER_ADDRESS = "http://114.34.203.58/SmartphoneRadar/index.php";

    private static final String HTTP_REQUEST_FAILED = null;

    private Context context;

    public ConnectDb(Context context) {
        this.context = context;
    }

    @Override
    public String sendHttpRequest(String params) {
        URL url;
        String result = new String();
        try {
            url = new URL(AUTHENTICATION_SERVER_ADDRESS);
            HttpURLConnection connection;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);

            PrintWriter out = new PrintWriter(connection.getOutputStream());

            out.println(params);
            out.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                result = result.concat(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result.length() == 0) {
            result = HTTP_REQUEST_FAILED;
        }

        return result;
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


    public String signUp(String account, String password) throws
            UnsupportedEncodingException {

        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("signUp", "UTF-8") +
                "&";
        Log.i("PARAMS", params);

        String response = sendHttpRequest(params);

        Log.i("response", response);
        return response;

    }

    //logIn 用於驗證該組帳密是否存在
    public String logIn(String account, String password) throws
            UnsupportedEncodingException {


        String params = "account=" + URLEncoder.encode(account, "UTF-8") +
                "&password=" + URLEncoder.encode(password, "UTF-8") +
                "&action=" + URLEncoder.encode("login", "UTF-8") +
                "&";
        Log.i("PARAMS", params);

        String response = sendHttpRequest(params);

        Log.i("response", response);
        return response;

    }

    public void getLatLngFromServer(String account, String password) throws UnsupportedEncodingException {
        String params = "account=" + URLEncoder.encode("", "UTF-8") +
                "&password=" + URLEncoder.encode("", "UTF-8") +
                "&time=" + URLEncoder.encode("", "UTF-8") +
                "&action=" + URLEncoder.encode("getLocation", "UTF-8") +
                "&";

        Log.i("PARAMS", params);
        String response = sendHttpRequest(params);

        try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
            sp.parse(new ByteArrayInputStream(response.getBytes()), new HandlerXML((Updater) context));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
