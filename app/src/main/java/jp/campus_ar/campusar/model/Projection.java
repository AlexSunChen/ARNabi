package jp.campus_ar.campusar.model;

public class Projection {

	final public static double kInnerRouteWidth = 1.3;
	final public static double kArrowInterval = 10;
	final public static double kEyeLevel = 3;
	final public static double kEyeLevel2 = kEyeLevel * kEyeLevel;

	public double pitch;
	public double yaw;

	public double a;
	public double b;
	public double c;
	public double cl;
	public double d;

	public double lnx;
	public double lny;
	public double lnz;
	public double lnr;

	public double tnx;
	public double tny;
	public double tnz;
	public double tnr;

	public Projection() {
		setYawPitch(0, 0);
	}

	public void setYawPitch(double y, double p) {
		yaw = y;
		pitch = p;

		double psin = Math.sin(pitch);
		this.a = Math.sin(yaw) * psin;
		this.b = Math.cos(yaw) * psin;
		this.c = -Math.cos(pitch);
		this.d = -1.0;
		this.cl = -kEyeLevel * this.c + this.d - 1;

		// 平面と並行な左向きのベクトル
		this.lnx = Math.sin(yaw - Math.PI * 0.5) * psin;
		this.lny = Math.cos(yaw - Math.PI * 0.5) * psin;
		this.lnz = c;

		// 平面と並行な上向きのベクトル
		pitch += Math.PI * 0.5;
		psin = Math.sin(pitch);
		this.tnx = Math.sin(yaw) * psin;
		this.tny = Math.cos(yaw) * psin;
		this.tnz = -Math.cos(pitch);
	}

	public Ray calcRay(double x, double y, double size) {
		// 原点Oと(floorX, floorY, -kEyeLevel)を結ぶ直線と、この平面との交点を計算
		double as = Math.sqrt(x * x + y * y + kEyeLevel2);
		double ax = x / as;
		double ay = y / as;
		double az = -kEyeLevel / as;
		double t = this.a * ax + this.b * ay + this.c * az;
		if (t > 0) {
			// 交点
			double ix = ax / t;
			double iy = ay / t;
			double iz = az / t;

			// この平面上における、平面の中心から見た交点の位置を計算
			double dx = this.a - ix;
			double dy = this.b - iy;
			double dz = this.c - iz;

			double x2 = (this.lnx * dx + this.lny * dy + this.lnz * dz) + 0.5;
			double y2 = (this.tnx * dx + this.tny * dy + this.tnz * dz) + 0.5;

			Ray ray = new Ray();
			ray.moved = false;
			ray.x = x2 * size;
			ray.y = y2 * size;
			return ray;
		}
		return null;
	}

	public Ray calcRay(Position p, double size) {
		return calcRay(p.x, p.y, size);
	}

	public Ray calcRay(Position target, Position other, double size) {
		Ray ray = calcRay(target, size);
		if (ray != null) {
			ray.moved = false;
			return ray;
		}

		Ray ray2 = calcRay(other, size);
		if (ray2 == null) return null;

		// targetとotherを結ぶ直線
		double a1 = target.y - other.y;
		double b1 = other.x - target.x;
		double c1 = target.x * other.y - other.x * target.y;

		// projection平面と地面（-kEyeLevel）の交線
		double a2 = this.a;
		double b2 = this.b;
		double c2 = this.cl;

		double x = (b1 * c2 - b2 * c1) / (a1 * b2 - a2 * b1);
		double y = (a1 * c2 - a2 * c1) / (a2 * b1 - a1 * b2);

		ray = calcRay(x, y, size);
		ray.moved = true;
		return ray;
	}
}
