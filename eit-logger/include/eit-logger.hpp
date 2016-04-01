/*
 * eit-logger.hpp
 *
 *  Created on: Mar 31, 2016
 *      Author: robin
 */

#pragma once

#include <stdint.h>

#include "SFE_LSM9DS0.h"

enum eit_sensor_t {
	S_ACCEL = 0,
	S_GYRO = 1,
	S_MAG = 2,
};

struct accel_config_t {
	enum LSM9DS0::accel_scale scale;
	enum LSM9DS0::accel_odr odr;
	enum LSM9DS0::accel_abw abw;
};

struct gyro_config_t {
	enum LSM9DS0::gyro_scale scale;
	enum LSM9DS0::gyro_odr odr;
};

struct mag_config_t {
	enum LSM9DS0::mag_scale scale;
	enum LSM9DS0::mag_odr odr;
};

struct eit_log_t {
	enum eit_sensor_t sensor;
	union {
		struct accel_config_t accel;
		struct gyro_config_t gyro;
		struct mag_config_t mag;
	} config;
	int64_t timestamp_ns;
	float x;
	float y;
	float z;
	bool overflow;
};
