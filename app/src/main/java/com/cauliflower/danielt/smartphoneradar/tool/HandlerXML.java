package com.cauliflower.danielt.smartphoneradar.tool;

import android.util.Log;

import com.cauliflower.danielt.smartphoneradar.interfacer.Updater;
import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class HandlerXML extends DefaultHandler {

    private List<SimpleLocation> mLocations = new ArrayList<>();
    private Updater mUpdater;

    public HandlerXML(Updater updater) {
        super();
        this.mUpdater = updater;
    }

    public void endDocument() throws SAXException {
        this.mUpdater.updateData(mLocations);

        super.endDocument();
    }

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        switch (localName) {
            //用於查詢手機位置
            case "location": {
                if ("207".equals(attributes.getValue("code"))) {
                    Log.i(HandlerXML.class.getSimpleName(), "Get no new location");
                } else {
                    String time = attributes.getValue(SimpleLocation.TIME);
                    double latitude = Double.valueOf(attributes.getValue(SimpleLocation.LATITUDE));
                    double longitude = Double.valueOf(attributes.getValue(SimpleLocation.LONGITUDE));

                    mLocations.add(new SimpleLocation(time, latitude, longitude));
                }
                break;
            }
        }

        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void startDocument() throws SAXException {
        this.mLocations.clear();
        super.startDocument();
    }


}

