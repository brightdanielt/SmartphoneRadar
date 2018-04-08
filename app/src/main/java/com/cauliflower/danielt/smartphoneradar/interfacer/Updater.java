package com.cauliflower.danielt.smartphoneradar.interfacer;

import com.cauliflower.danielt.smartphoneradar.obj.SimpleLocation;

import java.util.List;

public interface Updater {
	public void updateData(List<SimpleLocation> locations);

}
