package com.robinkirkman.eit.processor;

import com.robinkirkman.eit.processor.EitLog.Sensor;

public class InertialState {
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
	
	public XYZ down;
	
	public InertialState(Calibration cal) {
		this.cal = cal;
		
		accel = new XYZ();
		vel = new XYZ();
		pos = new XYZ();
		
		accel.prefix = "accel sensor: ";
		vel.prefix = "velocity: ";
		pos.prefix = "position: ";
		
		gyro = new XYZ();
		turn = new XYZ();
		
		turn.prefix = "turn: ";
		gyro.prefix = "gyro sensor: ";
		
		down = new XYZ(cal.ao);
		down.prefix = "down: ";
		
		accelcount = 0;
		gyrocount = 0;
		
		accelns = 0;
		gyrons = 0;
	}
	
	public InertialState log(EitLog log) {
		if(log.sensor == Sensor.ACCEL)
			logAccel(log.x, log.y, log.z, log.nanos);
		else if(log.sensor == Sensor.GYRO)
			logGyro(log.x, log.y, log.z, log.nanos);
		return this;
	}
	
	public InertialState logAccel(double ax, double ay, double az, long ans) {
		double as = (ans - accelns) / 1000000000.;
		pos.offset(vel.x * as, vel.y * as, vel.z * as);
		vel.offset(accel.x * as, accel.y * as, accel.z * as);
		accel.set(ax, ay, az);
		accel.multiply(Constants.GRAVITIES_TO_MPS2);
		accel.offset(-down.x, -down.y, -down.z);
		accelns = ans;
		accelcount++;
		return this;
	}
	
	public InertialState logGyro(double gx, double gy, double gz, long gns) {
		double gs = (gns - gyrons) / 1000000000.;
		gyro.multiply(gs);
		turn.offset(gyro);
		down.set(cal.ao).rotateXYZ(turn).multiply(Constants.GRAVITIES_TO_MPS2);
		gyro.set(gx, gy, gz).offset(-cal.go.x, -cal.go.y, -cal.go.z);
		gyro.multiply(180./145);
		gyrons = gns;
		gyrocount++;
		return this;
	}
	
}
