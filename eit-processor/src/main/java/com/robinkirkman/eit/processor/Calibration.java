package com.robinkirkman.eit.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Calibration {
	
	
	public static Calibration read(InputStream in) throws IOException {
		Properties p = new Properties();
		p.load(in);
		XYZ ao = new XYZ(dget(p, "a.ox"), dget(p, "a.oy"), dget(p, "a.oz"));
		XYZ go = new XYZ(dget(p, "g.ox"), dget(p, "g.oy"), dget(p, "g.oz"));
		long sa = lget(p, "s.a");
		long sg = lget(p, "s.g");
		long sn = lget(p, "s.n");
		return new Calibration(ao, go, sa, sg, sn);
	}

	private static double dget(Properties p, String key) {
		return Double.parseDouble(p.getProperty(key));
	}
	
	private static long lget(Properties p, String key) {
		return Long.parseLong(p.getProperty(key));
	}
	
	/**
	 * accel offset
	 */
	public XYZ ao;
	
	/**
	 * gyro offset
	 */
	public XYZ go;
	
	/**
	 * number of accel samples
	 */
	public final long sa;
	/**
	 * number of gyro samples
	 */
	public final long sg;
	
	/**
	 * duration of calibration (ns)
	 */
	public final long sn;
	
	public Calibration(XYZ ao, XYZ go, long sa, long sg, long sn) {
		this.ao = ao;
		this.go = go;
		this.sa = sa;
		this.sg = sg;
		this.sn = sn;
	}
	
	public void write(OutputStream out, String comment) throws IOException {
		Properties p = new Properties() {
			public synchronized Enumeration<Object> keys() {
				List<Object> keys = new ArrayList<Object>();
				Enumeration<Object> e = super.keys();
				while(e.hasMoreElements())
					keys.add(e.nextElement());
				Collections.sort((List) keys);
				return Collections.enumeration(keys);
			}
		};
		p.setProperty("ao.x", "" + ao.x);
		p.setProperty("ao.y", "" + ao.y);
		p.setProperty("ao.z", "" + ao.z);
		
		p.setProperty("go.x", "" + go.x);
		p.setProperty("go.y", "" + go.y);
		p.setProperty("go.z", "" + go.z);
		
		p.setProperty("s.a", "" + sa);
		p.setProperty("s.g", "" + sg);
		p.setProperty("s.n", "" + sn);
		p.store(out, comment);
	}
}
