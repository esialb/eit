package com.robinkirkman.eit.processor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.TimeUnit;

import com.robinkirkman.eit.processor.EitLog.Sensor;

public class Calibrate {

	public static void main(String[] args) throws Exception {
		File f = new File(args[0]);
		FileInputStream fin = new FileInputStream(f);
		FileChannel fc = fin.getChannel();
		MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, f.length());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		
		long as = 0;
		long gs = 0;
		double ag = 0;
		double gxo = 0;
		double gyo = 0;
		double gzo = 0;
		
		long nanos = 0;
		
		EitLog next = new EitLog();
		EitLog elog = new EitLog();
		
		while(EitLog.canRead(buf)) {
			next.read(buf);
			nanos = Math.max(nanos, elog.nanos);
			if(elog.sensor == Sensor.ACCEL) {
				ag += Math.sqrt(elog.x * elog.x + elog.y * elog.y + elog.z * elog.z);
				as++;
			} else if(elog.sensor == Sensor.GYRO) {
				gxo += elog.x;
				gyo += elog.y;
				gzo += elog.z;
				gs++;
			}
			EitLog tmp = next;
			next = elog;
			elog = tmp;
		}
		
		ag /= as;
		gxo /= gs;
		gyo /= gs;
		gzo /= gs;
		
		Calibration cal = new Calibration(ag, gxo, gyo, gzo, as, gs, nanos);
		cal.write(System.out, args[0] + " (" + TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS) + " seconds)");
	}

}
