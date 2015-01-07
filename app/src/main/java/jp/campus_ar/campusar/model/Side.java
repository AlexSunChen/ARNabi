package jp.campus_ar.campusar.model;

public class Side {

	public double a;
	public double b;
	public double c;

	public Side(double x0, double y0, double x1, double y1) {
		a = y0 - y1;
		b = x1 - x0;
		c = x0 * y1 - x1 * y0;
	}

	public Position intersect(Side side) {
		double x = (this.b * side.c - side.b * this.c) / (this.a * side.b - side.a * this.b);
		double y = (this.a * side.c - side.a * this.c) / (side.a * this.b - this.a * side.b);
		return new Position(x, y);
	}

}
