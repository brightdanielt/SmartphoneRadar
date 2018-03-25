package com.cauliflower.danielt.smartphoneradar;

/**
 * Created by danielt on 2018/3/19.
 */

public interface SocketInterface {
    public String sendHttpRequest(String params);

    public int startListening(int port);

    public void stopListening();

    public void exit();

    public int getListeningPort();

}
