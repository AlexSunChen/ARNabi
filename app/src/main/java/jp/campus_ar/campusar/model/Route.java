package jp.campus_ar.campusar.model;

public class Route {
	public static class Coordinate {
		public double lat;
		public double lng;
	}

	public String alert;
	public String length;
	public Coordinate[] coordinates;
}
