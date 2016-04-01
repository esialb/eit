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
		
		long sa = 0;
		long sg = 0;
		long sn = 0;
		
		XYZ ao = new XYZ();
		XYZ go = new XYZ();
		
		EitLog next = new EitLog();
		EitLog elog = new EitLog();
		
		while(EitLog.canRead(buf)) {
			next.read(buf);
			sn = elog.nanos;
			if(elog.sensor == Sensor.ACCEL) {
				ao.offset(elog.x, elog.y, elog.z);
				sa++;
			} else if(elog.sensor == Sensor.GYRO) {
				go.offset(elog.x, elog.y, elog.z);
				sg++;
			}
			EitLog tmp = next;
			next = elog;
			elog = tmp;
		}

		ao.multiply(1./sa);
		go.multiply(1./sg);
		
		Calibration cal = new Calibration(ao, go, sa, sg, sn);
		cal.write(System.out, args[0] + " (" + TimeUnit.SECONDS.convert(sn, TimeUnit.NANOSECONDS) + " seconds)");
	}

}
