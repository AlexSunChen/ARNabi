package jp.campus_ar.campusar.model;

import java.util.ArrayList;

public class Segment {

	final public static double kOuterRouteWidth = 2;
	final public static double kArrowSheer = 2.4;
	final public static double kArrowWidth = 3;

	public Side north;
	public Side south;
	public Side west;
	public Side east;

	public double dx;
	public double dy;
	public double r;
	public double dnx;
	public double dny;
	public double asx;
	public double asy;
	public double awx;
	public double awy;
	public double orx;
	public double ory;

	public Position begin;
	public Position end;

	public Segment(Position s, Position e, double width) {
		this.begin = s;
		this.end = e;

		this.dx = this.end.x - this.begin.x;
		this.dy = this.end.y - this.begin.y;
		this.r = Math.sqrt(this.dx * this.dx + this.dy * this.dy);
		this.dnx = this.dx / this.r;
		this.dny = this.dy / this.r;
		this.asx = this.dnx * kArrowSheer;
		this.asy = this.dny * kArrowSheer;
		this.awx = this.dnx * kArrowWidth;
		this.awy = this.dny * kArrowWidth;
		this.orx = this.dnx * kOuterRouteWidth;
		this.ory = this.dny * kOuterRouteWidth;

		double dnx = this.dnx * width;
		double dny = this.dny * width;

		this.north = new Side(e.x - dny, e.y + dnx, e.x + dny, e.y - dnx);
		this.south = new Side(s.x - dny, s.y + dnx, s.x + dny, s.y - dnx);
		this.west = new Side(s.x - dny, s.y + dnx, e.x - dny, e.y + dnx);
		this.east = new Side(s.x + dny, s.y - dnx, e.x + dny, e.y - dnx);
	}

	public ArrayList<Position> generateArrow(double offset) {
		double x = this.begin.x + offset * this.dnx;
		double y = this.begin.y + offset * this.dny;

		Position p0 = new Position(x, y);
		Position p1 = new Position(x - this.ory - this.asx, y + this.orx - this.asy);
		Position p2 = new Position(p1.x - this.awx, p1.y - this.awy);
		Position p3 = new Position(p0.x - this.awx, p0.y - this.awy);
		Position p5 = new Position(x + this.ory - this.asx, y - this.orx - this.asy);
		Position p4 = new Position(p5.x - this.awx, p5.y - this.awy);

		ArrayList<Position> result = new ArrayList<>();
		result.add(p0);
		result.add(p1);
		result.add(p2);
		result.add(p3);
		result.add(p4);
		result.add(p5);
		result.add(p0);
		return result;
	}
}
