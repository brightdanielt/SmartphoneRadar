package com.cauliflower.danielt.smartphoneradar.interfacer;

import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;

import java.util.List;

/**
 * 該介面用於向伺服器查詢座標時，傳回查得的座標集合
* */
public interface Updater {
    void updateData(List<SimpleLocation> locations);

}
