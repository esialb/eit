package com.robinkirkman.eit.processor;

public class XYZ {
	public double x;
	public double y;
	public double z;
	
	public XYZ() {
		this(0, 0, 0);
	}
	
	public XYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public XYZ(XYZ o) {
		this(o.x, o.y, o.z);
	}
	
	public XYZ set(double ox, double oy, double oz) {
		x = ox;
		y = oy;
		z = oz;
		return this;
	}
	
	public XYZ set(XYZ o) {
		return set(o.x, o.y, o.z);
	}
	
	public double distance(double ox, double oy, double oz) {
		return Math.sqrt((x-ox)*(x-ox) + (y-oy)*(y-oy)+(z-oz)*(z-oz));
	}
	
	public double distance(XYZ o) {
		return distance(o.x, o.y, o.z);
	}
	
	public double magnitude() {
		return distance(0, 0, 0);
	}
	
	public XYZ normalize() {
		return multiply(1 / magnitude());
	}
	
	public XYZ multiply(double d) {
		x *= d;
		y *= d;
		z *= d;
		return this;
	}
	
	public XYZ offset(double ox, double oy, double oz) {
		x += ox;
		y += oy;
		z += oz;
		return this;
	}
	
	public XYZ offset(XYZ o) {
		return offset(o.x, o.y, o.z);
	}
	
	/**
	 * rotate around all three axes
	 * @param rx radians
	 * @param ry radians
	 * @param rz radians
	 * @return
	 */
	public XYZ rotateXYZ(double rx, double ry, double rz) {
		XYZ rvx = new XYZ(this).rotateX(rx);
		XYZ rvy = new XYZ(this).rotateY(ry);
		XYZ rvz = new XYZ(this).rotateZ(rz);
		XYZ sum = new XYZ().offset(rvx).offset(rvy).offset(rvz);
		return this.set(sum.normalize().multiply(this.magnitude()));
	}
	
	/**
	 * rotate around all three axes
	 * @param r radians
	 * @return
	 */
	public XYZ rotateXYZ(XYZ r) {
		return rotateXYZ(r.x, r.y, r.z);
	}
	
	/**
	 * rotate around the x axis
	 * @param r radians
	 */
	public XYZ rotateX(double r) {
		double yp = y * StrictMath.cos(r) - z * StrictMath.sin(r);
		double zp = y * StrictMath.sin(r) + z * StrictMath.cos(r);
		y = yp;
		z = zp;
		return this;
	}
	
	/**
	 * rotate around the y axis
	 * @param r radians
	 */
	public XYZ rotateY(double r) {
		double xp = x * StrictMath.cos(r) + z * StrictMath.sin(r);
		double zp = -x * StrictMath.sin(r) + z * StrictMath.cos(r);
		x = xp;
		z = zp;
		return this;
	}
	
	/**
	 * rotate around the z axis
	 * @param r radians
	 */
	public XYZ rotateZ(double r) {
		double xp = x * StrictMath.cos(r) - y * StrictMath.sin(r);
		double yp = x * StrictMath.sin(r) + y * StrictMath.cos(r);
		x = xp;
		y = yp;
		return this;
	}
}

