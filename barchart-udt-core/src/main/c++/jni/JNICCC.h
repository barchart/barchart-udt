/*
 * JNICCC.h
 *
 *  Created on: 18 Dec 2012
 *      Author: ceri.coburn
 */

#ifndef JNICCC_H_
#define JNICCC_H_

#include "ccc.h"
#include <jni.h>

class JNICCCFactory;

class JNICCC: public CCC {

	friend JNIEXPORT void JNICALL Java_com_barchart_udt_CCC_initNative (JNIEnv *env, jobject self);

private:

	jobject _objCCC;
	JavaVM* _javaVM;

	jmethodID _methodInit;
	jmethodID _methodClose;
	jmethodID _methodOnACK;
	jmethodID _methodOnLoss;
	jmethodID _methodTimeout;
	jmethodID _methodOnPktSent;
	jmethodID _methodProcessCustomMsg;

public:

	virtual ~JNICCC();

	jobject getJavaCCC();

	//we need to override the visibility
	//of the setXXX calls since we are
	//emulating class inheritance from Java
	void setACKTimer(const int& msINT);

	void setACKInterval(const int& pktINT);

	void setRTO(const int& usRTO);

	void setPacketSndPeriod(const double sndPeriod);

	void setCWndSize(const double cWndSize);

	const CPerfMon* getPerfInfo();

private:

	//we don't want to allow any overrides on this
	//class since it will be emulated in the Java layer

	JNICCC(JNIEnv* env, jobject objCCC);

	void init();

	void close();

	void onACK(const int32_t&);

	void onLoss(const int32_t*, const int&);

	void onTimeout();

	void onPktSent(const CPacket*);

	void onPktReceived(const CPacket*);

	void processCustomMsg(const CPacket*);

};

#endif /* JNICCC_H_ */
