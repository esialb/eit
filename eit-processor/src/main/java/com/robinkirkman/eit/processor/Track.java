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

public class Track {

	public static void main(String[] args) throws Exception {
		Calibration cal = Calibrate.calibrate(new File(args[0]));
		
		File f = new File(args[1]);
		
		track(cal, f);
	}
	
	public static InertialState track(Calibration cal, File f) throws IOException {
		FileInputStream fin = new FileInputStream(f);
		FileChannel fc = fin.getChannel();
		MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, f.length());
		buf.order(ByteOrder.LITTLE_ENDIAN);
		fin.close();
		
		EitLog elog = new EitLog();
		
		InertialState istate = new InertialState(cal);
		
		long prevs = -1;
		
		while(EitLog.canRead(buf)) {
			elog.read(buf);

			if(elog.nanos < 1000000000L)
				continue;
			
			istate.log(elog);
			
			double ms = elog.nanos / 1000000.;
			ms = Math.round(ms);
			double s = ms / 1000.;
			
			if(prevs != (long) s)
				System.out.println(String.format("%03.3f %s %s %s", s, istate.pos, istate.vel, istate.accel));
			prevs = (long) s;
			
		}
		
		return istate;
	}

}
