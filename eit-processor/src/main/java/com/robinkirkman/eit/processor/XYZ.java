package com.robinkirkman.eit.processor;

import static java.lang.StrictMath.*;

public class XYZ {
	public String prefix = "";
	
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
	
	public XYZ extend(double d) {
		double m = magnitude();
		if(m > 0) {
			m += d;
			normalize().multiply(m);
		}
		return this;
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
	 * @param rx degrees
	 * @param ry degrees
	 * @param rz degrees
	 * @return
	 */
	public XYZ rotateXYZ(double rx, double ry, double rz) {
		rx *= Constants.DEGREES_TO_RADIANS;
		ry *= Constants.DEGREES_TO_RADIANS;
		rz *= Constants.DEGREES_TO_RADIANS;
		double c1 = cos(rx), s1 = sin(rx);
		double c2 = cos(ry), s2 = sin(ry);
		double c3 = cos(rz), s3 = sin(rz);
		double xp = x*c2*c3 + y*(-c2*s3) + z*s2;
		double yp = x*(c1*s3+c3*s1*s2) + y*(c1*c3-s1*s2*s3) + z*(-c2*s1);
		double zp = x*(s1*s3-c1*c3*s2) + y*(c3*s1+c1*s2*s3) + z*(c1*c2);
		x = xp;
		y = yp;
		z = zp;
		return this;
	}
	
	/**
	 * rotate around all three axes
	 * @param r degrees
	 * @return
	 */
	public XYZ rotateXYZ(XYZ r) {
		return rotateXYZ(r.x, r.y, r.z);
	}
	
	@Override
	public String toString() {
		return "(" + prefix + x + ", " + y + ", " + z + ", ||=" + magnitude() + ")";
	}
}

