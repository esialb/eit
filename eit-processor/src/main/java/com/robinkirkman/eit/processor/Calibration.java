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
		double ag = Double.parseDouble(p.getProperty("a.gravity"));
		long as = Long.parseLong(p.getProperty("a.samples"));
		double gxo = Double.parseDouble(p.getProperty("g.x-offset"));
		double gyo = Double.parseDouble(p.getProperty("g.y-offset"));
		double gzo = Double.parseDouble(p.getProperty("g.z-offset"));
		long gs = Long.parseLong(p.getProperty("g.samples"));
		long nanos = Long.parseLong(p.getProperty("t.nanos"));
		return new Calibration(ag, gxo, gyo, gzo, as, gs, nanos);
	}
	
	/**
	 * acceleration of gravity
	 */
	public final double ag;
	/**
	 * gyro-x offset
	 */
	public final double gxo;
	/**
	 * gyro-y offset
	 */
	public final double gyo;
	/**
	 * gyro-z offset
	 */
	public final double gzo;
	/**
	 * number of accel samples
	 */
	public final long as;
	/**
	 * number of gyro samples
	 */
	public final long gs;
	
	/**
	 * duration of calibration (ns)
	 */
	public final long nanos;
	
	public Calibration(double ag, double gxo, double gyo, double gzo, long as, long gs, long nanos) {
		this.ag = ag;
		this.gxo = gxo;
		this.gyo = gyo;
		this.gzo = gzo;
		this.as = as;
		this.gs = gs;
		this.nanos = nanos;
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
		p.setProperty("a.gravity", "" + ag);
		p.setProperty("a.samples", "" + as);
		p.setProperty("g.x-offset", "" + gxo);
		p.setProperty("g.y-offset", "" + gyo);
		p.setProperty("g.z-offset", "" + gzo);
		p.setProperty("g.samples", "" + gs);
		p.setProperty("t.nanos", "" + nanos);
		p.store(out, comment);
	}
}
