package com.robinkirkman.eit.processor;

import java.nio.ByteBuffer;

public class EitLog {
	public static enum Sensor {
		ACCEL,
		GYRO,
		MAG,
		;
		
		public static Sensor peek(ByteBuffer buf) {
			byte sb = buf.get(buf.position());
			if(sb == 0)
				return ACCEL;
			if(sb == 1)
				return GYRO;
			if(sb == 2)
				return MAG;
			throw new IllegalArgumentException("invalid sensor byte: " + sb);
		}
	}
	
	
	public static boolean canRead(ByteBuffer buf) {
		return buf.remaining() >= 21;
	}
	
	public Sensor sensor;
	public long nanos;
	public double x;
	public double y;
	public double z;
	
	public void read(ByteBuffer buf) {
		byte sb = buf.get();
		if(sb == 0)
			sensor = Sensor.ACCEL;
		else if(sb == 1)
			sensor = Sensor.GYRO;
		else if(sb == 2)
			sensor = Sensor.MAG;
		else
			throw new IllegalArgumentException("invalid sensor byte:" + sb);
		nanos = buf.getLong();
		x = buf.getFloat();
		y = buf.getFloat();
		z = buf.getFloat();
	}
}
