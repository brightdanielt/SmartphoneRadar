package com.cauliflower.danielt.smartphoneradar.interfacer;

/**
 * Created by danielt on 2018/3/19.
 */

public interface SocketInterface {
    public String sendHttpRequest(String params, String string_url);

    public int startListening(int port);

    public void stopListening();

    public void exit();

    public int getListeningPort();

}
