//============================================================================
// Name        : eit-logger.cpp
// Author      : Robin Kirkman
// Version     :
// Copyright   : BSD
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <unistd.h>

#include <boost/program_options.hpp>

#include "eit-logger.hpp"

#include "SFE_LSM9DS0.h"
#include "timestamp.hpp"

namespace po = boost::program_options;

#define B_SENSOR 0
#define B_TS (B_SENSOR + sizeof(char))
#define B_X (B_TS + sizeof(int64_t))
#define B_Y (B_X + sizeof(float))
#define B_Z (B_Y + sizeof(float))
#define B_LEN (B_Z + sizeof(float))

static LSM9DS0 *imu;

static bool ascii_output = false;

static struct eit_log_t *a_log;
static struct eit_log_t *g_log;
static struct eit_log_t *m_log;

static bool parse_options(int argc, char** argv);

static void init();

static bool read_accel();
static bool read_gyro();
static bool read_mag();

static void adjust_accel();
static void adjust_gyro();
static void adjust_mag();

static void write_log(struct eit_log_t* log);

int main(int argc, char** argv) {
	if(!parse_options(argc, argv))
		return 0;
	init();
	for(;;) {
		bool a, g, m;
		if((a = read_accel())) {
			if(!a_log->overflow)
				write_log(a_log);
			adjust_accel();
		}
		if((g = read_gyro())) {
			if(!g_log->overflow)
				write_log(g_log);
			adjust_gyro();
		}
		if((m = read_mag())) {
			if(!m_log->overflow)
				write_log(m_log);
			adjust_mag();
		}
		if(!a && !g && !m)
			usleep(100);
	}
	return 0;
}

static bool parse_options(int argc, char** argv) {
	po::options_description desc("Options");
	desc.add_options()
			("help", "show help")
			("ascii", "output in ascii")
			;
	po::variables_map vm;
	po::store(po::parse_command_line(argc, argv, desc), vm);
	po::notify(vm);
	if(vm.count("help")) {
		std::cout << desc << "\n";
		return false;
	}
	if(vm.count("ascii")) {
		ascii_output = true;
	}
	return true;
}

static void init() {
	imu = new LSM9DS0(0x6B, 0x1D);
	a_log = (struct eit_log_t *) malloc(sizeof(struct eit_log_t));
	g_log = (struct eit_log_t *) malloc(sizeof(struct eit_log_t));
	m_log = (struct eit_log_t *) malloc(sizeof(struct eit_log_t));

	a_log->sensor = S_ACCEL;
	a_log->config.accel.scale = LSM9DS0::A_SCALE_2G;
	a_log->config.accel.odr = LSM9DS0::A_ODR_200;
	a_log->config.accel.abw = LSM9DS0::A_ABW_194;

	g_log->sensor = S_GYRO;
	g_log->config.gyro.scale = LSM9DS0::G_SCALE_245DPS;
	g_log->config.gyro.odr = LSM9DS0::G_ODR_190_BW_70;

	m_log->sensor = S_MAG;
	m_log->config.mag.scale = LSM9DS0::M_SCALE_2GS;
	m_log->config.mag.odr = LSM9DS0::M_ODR_100;

	imu->begin(
			g_log->config.gyro.scale,
			a_log->config.accel.scale,
			m_log->config.mag.scale,
			g_log->config.gyro.odr,
			a_log->config.accel.odr,
			m_log->config.mag.odr);

	imu->setAccelABW(a_log->config.accel.abw);
}

static void write_log_ascii(struct eit_log_t* log);
static void write_log_binary(struct eit_log_t* log);

static void write_log(struct eit_log_t* log) {
	if(ascii_output)
		write_log_ascii(log);
	else
		write_log_binary(log);
}

static void write_log_ascii(struct eit_log_t* log) {
	std::cout << "[" << log->sensor << ",";
	switch(log->sensor) {
	case S_ACCEL:
		std::cout << "[" << log->config.accel.scale;
		std::cout << "," << log->config.accel.odr;
		std::cout << "," << log->config.accel.abw << "]";
		break;
	case S_GYRO:
		std::cout << "[" << log->config.gyro.scale;
		std::cout << "," << log->config.gyro.odr << "]";
		break;
	case S_MAG:
		std::cout << "[" << log->config.mag.scale;
		std::cout << "," << log->config.mag.odr << "]";
		break;
	}
	std::cout << "," << log->timestamp_ns;
	std::cout << "," << log->x;
	std::cout << "," << log->y;
	std::cout << "," << log->z;
	std::cout << "," << log->overflow << "]\n";
}

static void write_log_binary(struct eit_log_t* log) {
	char cbuf[B_LEN];
	union {
		int64_t i;
		char c[sizeof(int64_t)];
	} i;
	union {
		float f;
		char c[sizeof(float)];
	} f;

	*(cbuf + B_SENSOR) = log->sensor;

	i.i = log->timestamp_ns;
	memcpy(cbuf + B_TS, i.c, sizeof(int64_t));

	f.f = log->x;
	memcpy(cbuf + B_X, f.c, sizeof(float));

	f.f = log->y;
	memcpy(cbuf + B_Y, f.c, sizeof(float));

	f.f = log->z;
	memcpy(cbuf + B_Z, f.c, sizeof(float));

	std::cout.write(cbuf, sizeof(cbuf));
}


static bool read_accel() {
	if(!imu->newXData())
		return false;
	a_log->timestamp_ns = timestamp_ns();
	imu->readAccel();
	a_log->overflow = imu->xDataOverflow();
	a_log->x = imu->calcAccel(imu->ax);
	a_log->y = imu->calcAccel(imu->ay);
	a_log->z = imu->calcAccel(imu->az);
	return true;
}

static bool read_gyro() {
	if(!imu->newGData())
		return false;
	g_log->timestamp_ns = timestamp_ns();
	imu->readGyro();
	g_log->overflow = imu->gDataOverflow();
	g_log->x = imu->calcGyro(imu->gx);
	g_log->y = imu->calcGyro(imu->gy);
	g_log->z = imu->calcGyro(imu->gz);
	return true;
}

static bool read_mag() {
	if(!imu->newMData())
		return false;
	m_log->timestamp_ns = timestamp_ns();
	imu->readMag();
	m_log->overflow = imu->mDataOverflow();
	m_log->x = imu->calcMag(imu->mx);
	m_log->y = imu->calcMag(imu->my);
	m_log->z = imu->calcMag(imu->mz);
	return true;
}

static void adjust_accel() {
	if(a_log->overflow) {
		enum LSM9DS0::accel_scale scale = LSM9DS0::A_SCALE_16G;

		switch(a_log->config.accel.scale) {
		case LSM9DS0::A_SCALE_2G: scale = LSM9DS0::A_SCALE_4G; break;
		case LSM9DS0::A_SCALE_4G: scale = LSM9DS0::A_SCALE_6G; break;
		case LSM9DS0::A_SCALE_6G: scale = LSM9DS0::A_SCALE_8G; break;
		case LSM9DS0::A_SCALE_8G: scale = LSM9DS0::A_SCALE_16G; break;
		case LSM9DS0::A_SCALE_16G: scale = LSM9DS0::A_SCALE_16G; break;
		}

		if(a_log->config.accel.scale != scale) {
			imu->setAccelScale(a_log->config.accel.scale = scale);
		}
	} else {
		enum LSM9DS0::accel_scale scale = LSM9DS0::A_SCALE_2G;

		switch(a_log->config.accel.scale) {
		case LSM9DS0::A_SCALE_2G: scale = LSM9DS0::A_SCALE_2G; break;
		case LSM9DS0::A_SCALE_4G: scale = LSM9DS0::A_SCALE_2G; break;
		case LSM9DS0::A_SCALE_6G: scale = LSM9DS0::A_SCALE_4G; break;
		case LSM9DS0::A_SCALE_8G: scale = LSM9DS0::A_SCALE_6G; break;
		case LSM9DS0::A_SCALE_16G: scale = LSM9DS0::A_SCALE_8G; break;
		}

		if(a_log->config.accel.scale != scale) {
			imu->setAccelScale(a_log->config.accel.scale = scale);
		}
	}
}

static void adjust_gyro() {
	if(g_log->overflow) {
		enum LSM9DS0::gyro_scale scale = LSM9DS0::G_SCALE_2000DPS;

		switch(g_log->config.gyro.scale) {
		case LSM9DS0::G_SCALE_245DPS: scale = LSM9DS0::G_SCALE_500DPS; break;
		case LSM9DS0::G_SCALE_500DPS: scale = LSM9DS0::G_SCALE_2000DPS; break;
		case LSM9DS0::G_SCALE_2000DPS: scale = LSM9DS0::G_SCALE_2000DPS; break;
		}

		if(g_log->config.gyro.scale != scale) {
			imu->setGyroScale(g_log->config.gyro.scale = scale);
		}
	} else {
		enum LSM9DS0::gyro_scale scale = LSM9DS0::G_SCALE_245DPS;

		switch(g_log->config.gyro.scale) {
		case LSM9DS0::G_SCALE_245DPS: scale = LSM9DS0::G_SCALE_245DPS; break;
		case LSM9DS0::G_SCALE_500DPS: scale = LSM9DS0::G_SCALE_245DPS; break;
		case LSM9DS0::G_SCALE_2000DPS: scale = LSM9DS0::G_SCALE_500DPS; break;
		}

		if(g_log->config.gyro.scale != scale) {
			imu->setGyroScale(g_log->config.gyro.scale = scale);
		}
	}
}

static void adjust_mag() {
	if(m_log->overflow) {
		enum LSM9DS0::mag_scale scale = LSM9DS0::M_SCALE_12GS;

		switch(m_log->config.mag.scale) {
		case LSM9DS0::M_SCALE_2GS: scale = LSM9DS0::M_SCALE_4GS; break;
		case LSM9DS0::M_SCALE_4GS: scale = LSM9DS0::M_SCALE_8GS; break;
		case LSM9DS0::M_SCALE_8GS: scale = LSM9DS0::M_SCALE_12GS; break;
		case LSM9DS0::M_SCALE_12GS: scale = LSM9DS0::M_SCALE_12GS; break;
		}

		if(m_log->config.mag.scale != scale) {
			imu->setMagScale(m_log->config.mag.scale = scale);
		}
	} else {
		enum LSM9DS0::mag_scale scale = LSM9DS0::M_SCALE_2GS;

		switch(m_log->config.mag.scale) {
		case LSM9DS0::M_SCALE_2GS: scale = LSM9DS0::M_SCALE_2GS; break;
		case LSM9DS0::M_SCALE_4GS: scale = LSM9DS0::M_SCALE_2GS; break;
		case LSM9DS0::M_SCALE_8GS: scale = LSM9DS0::M_SCALE_4GS; break;
		case LSM9DS0::M_SCALE_12GS: scale = LSM9DS0::M_SCALE_8GS; break;
		}

		if(m_log->config.mag.scale != scale) {
			imu->setMagScale(m_log->config.mag.scale = scale);
		}
	}
}
