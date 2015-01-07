package jp.campus_ar.campusar.util;

import jp.campus_ar.campusar.model.Facility;

public class FacilityUtil {

	final public static Facility[] FACILITIES = {
			new Facility(1, "筑波大学"),
			new Facility(5, "愛媛大学 城北地区"),
			new Facility(2, "東京工業大学 大岡山キャンパス"),
			new Facility(3, "東京ディズニーランド"),
			new Facility(4, "東京ディズニーシー"),
	};

	public static Facility[] getFacilities() {
		return FACILITIES;
	}

	public static Facility getFacilityByIndex(int index) {
		for (int i = 0, ii = FACILITIES.length; i < ii; i++) {
			if (FACILITIES[i].id == index) {
				return FACILITIES[i];
			}
		}
		return null;
	}
}
