/*
 * timestamp.cpp
 *
 *  Created on: Mar 31, 2016
 *      Author: robin
 */

#include "timestamp.hpp"

#include <time.h>

static bool initialized = false;

static int64_t clockstart_ns;

int64_t timestamp_ns() {
	struct timespec clk;
	clock_gettime(CLOCK_MONOTONIC, &clk);
	int64_t ns = clk.tv_sec;
	ns *= 1000000000;
	ns += clk.tv_nsec;
	if(initialized) {
		return ns - clockstart_ns;
	} else {
		clockstart_ns = ns;
		initialized = true;
		return 0;
	}
}
