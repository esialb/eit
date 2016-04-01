package com.robinkirkman.eit.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.TimeUnit;

import com.robinkirkman.eit.processor.EitLog.Sensor;

public class Calibrate {

	public static void main(String[] args) throws Exception {
		File f = new File(args[0]);

		Calibration cal = calibrate(f);
		cal.write(System.out, args[0] + " (" + TimeUnit.SECONDS.convert(cal.sn, TimeUnit.NANOSECONDS) + " seconds)");
	}

	public static Calibration calibrate(File f) throws IOException {
		FileInputStream fin = new FileInputStream(f);
		FileChannel fc = fin.getChannel();
		MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, f.length());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		fin.close();
		
		long sa = 0;
		long sg = 0;
		long sn = 0;
		
		XYZ ao = new XYZ();
		XYZ go = new XYZ();
		
		EitLog elog = new EitLog();
		
		while(EitLog.canRead(buf)) {
			elog.read(buf);
			sn = elog.nanos;
			if(elog.sensor == Sensor.ACCEL) {
				ao.offset(elog.x, elog.y, elog.z);
				sa++;
			} else if(elog.sensor == Sensor.GYRO) {
				go.offset(elog.x, elog.y, elog.z);
				sg++;
			}
		}

		ao.multiply(1./sa);
		go.multiply(1./sg);
		
		double av = 0;
		double gv = 0;
		
		buf.position(0);

		double avmax = 0;
		double gvmax = 0;
		
		while(EitLog.canRead(buf)) {
			elog.read(buf);
			if(elog.nanos < 1000000000L)
				continue;
			if(elog.sensor == Sensor.ACCEL) {
				double d = ao.distance(elog.x, elog.y, elog.z);
				avmax = Math.max(avmax, d);
				av += d;
			} else if(elog.sensor == Sensor.GYRO) {
				double d = go.distance(elog.x, elog.y, elog.z);
				gvmax = Math.max(gvmax, d);
				gv += d;
			}
		}
		
		av /= sa;
		gv /= sg;
		
		System.out.println("avmax: " + avmax + " gvmax:" + gvmax);
		
		return new Calibration(ao, av, sa, go, gv, sg, sn);
	}
	
}
