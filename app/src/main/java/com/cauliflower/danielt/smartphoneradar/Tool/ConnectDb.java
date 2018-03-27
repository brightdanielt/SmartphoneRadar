package com.cauliflower.danielt.smartphoneradar.Tool;

import com.cauliflower.danielt.smartphoneradar.Interface.SocketInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by danielt on 2018/3/19.
 */

public class ConnectDb implements SocketInterface {

    private static final String AUTHENTICATION_SERVER_ADDRESS = "http://114.34.203.58/SmartphoneRadar/index.php";

    private static final String HTTP_REQUEST_FAILED = null;

    public ConnectDb() {
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
}
