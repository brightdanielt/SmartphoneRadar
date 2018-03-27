package com.cauliflower.danielt.smartphoneradar.Tool;

import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.Interface.Updater;
import com.cauliflower.danielt.smartphoneradar.Obj.SimpleLocation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class HandlerXML extends DefaultHandler {

    private List<SimpleLocation> locations = new ArrayList<>();
    private Updater updater;

    public HandlerXML(Updater updater) {
        super();
        this.updater = updater;
    }

    public void endDocument() throws SAXException {

        this.updater.updateData(locations);
        super.endDocument();
    }

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        switch (localName) {
            case "success": {
                switch (attributes.getValue("success")) {
                    case "103": {
                        Log.i("Response.success:", "INSERT USER SUCCESS");
                        break;
                    }
                    case "104": {
                        Log.i("Response.success:", "INSERT PHONE INFO SUCCESS");
                        break;
                    }
                    case "105": {
                        Log.i("Response.success:", "UPDATE LOCATION SUCCESS");
                        break;
                    }
                }
                break;
            }
            case "error": {
                switch (attributes.getValue("error")) {
                    case "201": {
                        Log.i("Response.error:", "PARAMETER ERROR");
                        break;
                    }
                    case "202": {
                        Log.i("Response.error:", "USER ALREADY EXISTS");
                        break;
                    }
                    case "203": {
                        Log.i("Response.error:", "INSERT USER ERROR");
                        break;
                    }
                    case "204": {
                        Log.i("Response.error:", "INSERT PHONE INFO ERROR");
                        break;
                    }
                    case "205": {
                        Log.i("Response.error:", "UPDATE LOCATION ERROR");
                        break;
                    }
                }
                break;
            }
            //用於查詢手機位置
            case "location": {
                String time = attributes.getValue(SimpleLocation.TIME);
                double latitude = Double.valueOf(attributes.getValue(SimpleLocation.LATITUDE));
                double longitude = Double.valueOf(attributes.getValue(SimpleLocation.LONGITUDE));

                locations.add(new SimpleLocation(time, latitude, longitude));
                break;
            }
        }

        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void startDocument() throws SAXException {
        this.locations.clear();
        super.startDocument();
    }


}

