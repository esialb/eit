package com.robinkirkman.eit.processor;

import com.robinkirkman.eit.processor.EitLog.Sensor;

public class Track {
	public Calibration cal;
	
	public long accelcount;
	public long accelns;
	public XYZ accel;
	public XYZ vel;
	public XYZ pos;
	
	public long gyrocount;
	public long gyrons;
	public XYZ gyro;
	public XYZ turn;
	
	public Track(Calibration cal) {
		this.cal = cal;
		
		accel = new XYZ();
		vel = new XYZ();
		pos = new XYZ();
		
		gyro = new XYZ();
		turn = new XYZ();
		
		accelcount = 0;
		gyrocount = 0;
		
		accelns = 0;
		gyrons = 0;
	}
	
	public Track log(EitLog log) {
		if(log.sensor == Sensor.ACCEL)
			logAccel(log.x, log.y, log.z, log.nanos);
		else if(log.sensor == Sensor.GYRO)
			logGyro(log.x, log.y, log.z, log.nanos);
		return this;
	}
	
	public Track logAccel(double ax, double ay, double az, long ans) {
		double as = (ans - accelns) / 1000000000.;
		pos.offset(vel.x * as, vel.y * as, vel.z * as);
		vel.offset(accel.x * as, accel.y * as, accel.z * as);
		accel.set(ax, ay, az).rotateXYZ(-turn.x, -turn.y, -turn.z).offset(cal.ao).multiply(Constants.GRAVITIES_TO_MPS2);
		accelns = ans;
		accelcount++;
		return this;
	}
	
	public Track logGyro(double gx, double gy, double gz, long gns) {
		double gs = (gns - gyrons) / 1000000000.;
		turn.offset(gyro.x * gs, gyro.y * gs, gyro.z * gs);
		gyro.set(gx, gy, gz).offset(cal.go).multiply(Constants.DEGREES_TO_RADIANS);
		gyrons = gns;
		gyrocount++;
		return this;
	}
	
}
