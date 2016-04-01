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
		XYZ ao = new XYZ(dget(p, "ao.x"), dget(p, "ao.y"), dget(p, "ao.z"));
		XYZ go = new XYZ(dget(p, "go.x"), dget(p, "go.y"), dget(p, "go.z"));
		double av = dget(p, "ao.v");
		double gv = dget(p, "go.v");
		long sa = lget(p, "s.a");
		long sg = lget(p, "s.g");
		long sn = lget(p, "s.n");
		return new Calibration(ao, av, sa, go, gv, sg, sn);
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
	public final XYZ ao;
	
	/**
	 * gyro offset
	 */
	public final XYZ go;
	
	public final double av;
	
	public final double gv;
	
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
	
	public Calibration(XYZ ao, double av, long sa, XYZ go, double gv, long sg, long sn) {
		this.ao = ao;
		this.av = av;
		this.sa = sa;
		this.go = go;
		this.gv = gv;
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
		p.setProperty("ao.v", "" + av);
		
		p.setProperty("go.x", "" + go.x);
		p.setProperty("go.y", "" + go.y);
		p.setProperty("go.z", "" + go.z);
		p.setProperty("go.v", "" + gv);
		
		p.setProperty("s.a", "" + sa);
		p.setProperty("s.g", "" + sg);
		p.setProperty("s.n", "" + sn);
		p.store(out, comment);
	}
}
