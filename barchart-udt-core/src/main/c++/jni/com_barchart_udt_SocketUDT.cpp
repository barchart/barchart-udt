/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * ARTIFACT='barchart-udt4';VERSION='1.0.2-SNAPSHOT';TIMESTAMP='2011-01-11_09-30-59';
 *
 * Copyright (C) 2009-2011, Barchart, Inc. (http://www.barchart.com/)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *     * Neither the name of the Barchart, Inc. nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Developers: Andrei Pozolotin;
 *
 * =================================================================================
 */
/*
 * NOTE: provides ONLY ipv4 implementation (not ipv6)
 *
 */

// ### keep outside [extern "C"]

#include "com_barchart_udt_SocketUDT.h"

#include "udt.h"

#include <cassert>
#include <climits>
#include <cstring>
#include <vector>
#include <algorithm>

// do not use cout; else will introduce GLIBCXX_3.4.9 dependency with 'g++ -O2'
//#include <iostream>

using namespace std;

// do not use to avoid ambiguity
//using namespace UDT;

// for JNI method signature compatibility
extern "C" { /* specify the C calling convention */

// ### keep inside extern "C"

// ########################################################

// to turn off compiler warnings
#define UNUSED(x) ((void)(x))

// ### JNI

#define JNI_UPDATE 0 // jni object release with copy
//


// ### JDK references

// JDK classes
static jclass jdk_clsBoolean; // java.lang.Boolean
static jclass jdk_clsInteger; // java.lang.Integer
static jclass jdk_clsLong; // java.lang.Long
static jclass jdk_clsInet4Address; // java.net.Inet4Address
static jclass jdk_clsInet6Address; // java.net.Inet6Address
static jclass jdk_clsInetSocketAddress; // java.net.InetSocketAddress
static jclass jdk_clsSocketException; // java.net.SocketException
static jclass jdk_clsSet; // java.util.Set
static jclass jdk_clsIterator; // java.util.Iterator

// JDK fields
static jfieldID isa_InetAddressID; // java.net.InetSocketAddress#addr
static jfieldID isa_PortID; // java.net.InetSocketAddress#port
static jfieldID ia_AddressID; // java.net.InetAddress#address

// JDK methods
static jmethodID jdk_clsBoolean_initID; // new Boolean(boolean x)
static jmethodID jdk_clsInteger_initID; // new Integer(int x)
static jmethodID jdk_clsLong_initID; // new Long(long x)
static jmethodID jdk_clsInet4Address_initID; // new InetAddress()
static jmethodID jdk_clsInetSocketAddress_initID; // new InetSocketAddress(InetAddress x)
static jmethodID jdk_clsSet_iteratorID; // Iterator set.iterator()
static jmethodID jdk_clsSet_addID; // boolean set.add(Object)
static jmethodID jdk_clsSet_containsID; // boolean set.contains(Object)
static jmethodID jdk_clsIterator_hasNextID; // boolean iterator.hasNext()
static jmethodID jdk_clsIterator_nextID; // Object iterator.next()

// ### UDT references

// UDT classes
static jclass udt_clsSocketUDT; // com.barchart.udt.SocketUDT
static jclass udt_clsTypeUDT; // com.barchart.udt.TypeUDT
static jclass udt_clsFactoryUDT; // com.barchart.udt.FactoryUDT
static jclass udt_clsMonitorUDT; // com.barchart.udt.MonitorUDT
static jclass udt_clsExceptionUDT; // com.barchart.udt.ExceptionUDT
static jclass udt_clsLingerUDT; // com.barchart.udt.ExceptionUDT

// UDT fields: com.barchart.udt.SocketUDT
static jfieldID udts_TypeID;
static jfieldID udts_RemoteSocketAddressID;
static jfieldID udts_LocalSocketAddressID;
static jfieldID udts_MonitorID;
static jfieldID udts_SocketID;
static jfieldID udts_IsSelectedReadID;
static jfieldID udts_IsSelectedWriteID;
static jfieldID udts_IsSelectedExceptionID;

// UDT fields: com.barchart.udt.TypeUDT
static jfieldID udtt_TypeCodeID;

// UDT methods
static jmethodID udt_clsSocketUDT_initID1; // new SocketUDT(int type, int id)
static jmethodID udt_clsExceptionUDT_initID0; // new ExceptionUDT(int code, String message)
static jmethodID udt_clsLingerUDT_initID; // new LingerUDT(int value)

// UDT fields: com.barchart.udt.MonitorUDT
//
// global measurements
static jfieldID udtm_msTimeStamp; // time since the UDT entity is started, in milliseconds
static jfieldID udtm_pktSentTotal; // total number of sent data packets, including retransmissions
static jfieldID udtm_pktRecvTotal; // total number of received packets
static jfieldID udtm_pktSndLossTotal; // total number of lost packets (sender side)
static jfieldID udtm_pktRcvLossTotal; // total number of lost packets (receiver side)
static jfieldID udtm_pktRetransTotal; // total number of retransmitted packets
static jfieldID udtm_pktSentACKTotal; // total number of sent ACK packets
static jfieldID udtm_pktRecvACKTotal; // total number of received ACK packets
static jfieldID udtm_pktSentNAKTotal; // total number of sent NAK packets
static jfieldID udtm_pktRecvNAKTotal; // total number of received NAK packets
static jfieldID udtm_usSndDurationTotal; // total time duration when UDT is sending data (idle time exclusive)
//
// local measurements
static jfieldID udtm_pktSent; // number of sent data packets, including retransmissions
static jfieldID udtm_pktRecv; // number of received packets
static jfieldID udtm_pktSndLoss; // number of lost packets (sender side)
static jfieldID udtm_pktRcvLoss; // number of lost packets (receiverer side)
static jfieldID udtm_pktRetrans; // number of retransmitted packets
static jfieldID udtm_pktSentACK; // number of sent ACK packets
static jfieldID udtm_pktRecvACK; // number of received ACK packets
static jfieldID udtm_pktSentNAK; // number of sent NAK packets
static jfieldID udtm_pktRecvNAK; // number of received NAK packets
static jfieldID udtm_mbpsSendRate; // sending rate in Mb/s
static jfieldID udtm_mbpsRecvRate; // receiving rate in Mb/s
static jfieldID udtm_usSndDuration; // busy sending time (i.e., idle time exclusive)
//
// instant measurements
static jfieldID udtm_usPktSndPeriod; // packet sending period, in microseconds
static jfieldID udtm_pktFlowWindow; // flow window size, in number of packets
static jfieldID udtm_pktCongestionWindow; // congestion window size, in number of packets
static jfieldID udtm_pktFlightSize; // number of packets on flight
static jfieldID udtm_msRTT; // RTT, in milliseconds
static jfieldID udtm_mbpsBandwidth; // estimated bandwidth, in Mb/s
static jfieldID udtm_byteAvailSndBuf; // available UDT sender buffer size
static jfieldID udtm_byteAvailRcvBuf; // available UDT receiver buffer size

// ########################################################

//#define EOL "\n"

// special UDT method return value
#define UDT_TIMEOUT 0

// TODO make more portable mingw / msvc
#ifdef _MSC_VER
#define __func__ __FUNCTION__
#endif

// null pointer safety
//
#define CHK_LOG(title,comment) printf ("%s function: %s comment: %s", title, __func__, comment);
// do not use cout; else will introduce GLIBCXX_3.4.9 dependency with 'g++ -O2'
//#define CHK_LOG(title,comment) cout << title << " function: " << __func__ << " comment: " << comment << endl;
//
#define CHK_NUL_RET_RET(reference,comment) if ((reference) == NULL) \
	{ CHK_LOG("CHK_NUL_RET_RET;",comment); return; }
//
#define CHK_NUL_RET_NUL(reference,comment) if ((reference) == NULL) \
	{ CHK_LOG("CHK_NUL_RET_NUL;",comment); return NULL; }
//
#define CHK_NUL_RET_FLS(reference,comment) if ((reference) == NULL) \
	{ CHK_LOG("CHK_NUL_RET_FLS;",comment); return false; }
//
#define CHK_NUL_RET_ERR(reference,comment) if ((reference) == NULL) \
	{ CHK_LOG("CHK_NUL_RET_ERR;",comment); return JNI_ERR; }
//
// c++ <-> java, bool <-> boolean conversions
#define BOOL(x) (((x) == JNI_TRUE) ? true : false) // from java to c++
#define BOOLEAN(x) ((jboolean) ( ((x) == true) ? JNI_TRUE : JNI_FALSE )) // from c++ to java
//
// free/malloc convenience
#define FREE(x) if ((x) != NULL) { free(x); x = NULL; }
#define MALLOC(var,type,size) type* var = (type*) malloc(sizeof(type) * size);

void X_InitClassReference(JNIEnv *env, jclass *classReference,
		const char *className) {
	CHK_NUL_RET_RET(env, "env");
	CHK_NUL_RET_RET(className, "className");
	CHK_NUL_RET_RET(classReference,"classReference");
	jclass klaz = env->FindClass(className);
	CHK_NUL_RET_RET(klaz, "klaz");
	*classReference = static_cast<jclass> (env->NewGlobalRef((jobject) klaz));
	CHK_NUL_RET_RET(*classReference, "*classReference");
}

// use native bool parameter
jobject X_NewBoolean(JNIEnv *env, bool value) {
	CHK_NUL_RET_NUL(env, "env");
	return env->NewObject(jdk_clsBoolean, jdk_clsBoolean_initID, BOOLEAN(value));
}

// use native 32 bit int parameter
jobject X_NewInteger(JNIEnv *env, int value) {
	CHK_NUL_RET_NUL(env, "env");
	return env->NewObject(jdk_clsInteger, jdk_clsInteger_initID, (jint) value);
}

// use native 64 bit long parameter
jobject X_NewLong(JNIEnv* env, int64_t value) {
	CHK_NUL_RET_NUL(env, "env");
	return env->NewObject(jdk_clsLong, jdk_clsLong_initID, (jlong) value);
}

// NOTE: ipv4 only
int X_InitSockAddr(sockaddr* sockAddr) {
	CHK_NUL_RET_ERR(sockAddr, "sockAddr");
	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;
	sockAddrIn->sin_family = AF_INET;
	memset(&(sockAddrIn->sin_zero), '\0', 8);
	return JNI_OK;
}

inline bool X_IsInRange(jlong min, jlong var, jlong max) {
	if (min <= var && var <= max) {
		return true;
	} else {
		return false;
	}
}

inline void X_ConvertMillisIntoTimeValue(const jlong millisTimeout,
		timeval* timeValue) {
	if (millisTimeout < 0) { // infinite wait
		timeValue->tv_sec = INT_MAX;
		timeValue->tv_usec = 0;
	} else if (millisTimeout > 0) { // finite wait
		timeValue->tv_sec = millisTimeout / 1000; // msvc C4244
		timeValue->tv_usec = (millisTimeout % 1000) * 1000;
	} else { // immediate return (not less the UDT event slice of 10 ms)
		timeValue->tv_sec = 0;
		timeValue->tv_usec = 0;
	}
}

// NOTE: ipv4 only
int X_ConvertInetSocketAddressToSockaddr(JNIEnv* env,
		jobject objInetSocketAddress, sockaddr* sockAddr) {

	CHK_NUL_RET_ERR(env,"env");
	CHK_NUL_RET_ERR(sockAddr,"sockAddr");
	CHK_NUL_RET_ERR(objInetSocketAddress,"objInetSocketAddress");

	jobject objInetAddress = env->GetObjectField(objInetSocketAddress,
			isa_InetAddressID);
	CHK_NUL_RET_ERR(objInetAddress,"objInetAddress");

	jint address = env->GetIntField(objInetAddress, ia_AddressID);
	jint port = env->GetIntField(objInetSocketAddress, isa_PortID);

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;

	sockAddrIn->sin_addr.s_addr = htonl(address);
	sockAddrIn->sin_port = htons(port); // msvc C4244

	return JNI_OK;

}

// NOTE: ipv4 only
int X_ConvertSockaddrToInetSocketAddress(JNIEnv* env, sockaddr* sockAddr,
		jobject objInetSocketAddress) {

	CHK_NUL_RET_ERR(env, "env");
	CHK_NUL_RET_ERR(sockAddr,"sockAddr");
	CHK_NUL_RET_ERR(objInetSocketAddress,"objInetSocketAddress");

	jobject objInetAddress = env->GetObjectField(objInetSocketAddress,
			isa_InetAddressID);
	CHK_NUL_RET_ERR(objInetAddress,"objInetAddress");

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;

	jint address = ntohl(sockAddrIn->sin_addr.s_addr);
	jint port = ntohs(sockAddrIn->sin_port);

	env->SetIntField(objInetAddress, ia_AddressID, address);
	env->SetIntField(objInetSocketAddress, isa_PortID, port);

	return JNI_OK;

}

// NOTE: only ipv4
jobject X_NewInetAddress(JNIEnv* env, jint address) {

	CHK_NUL_RET_NUL(env, "env");
	CHK_NUL_RET_NUL(jdk_clsInet4Address,"jdk_clsInet4Address");

	jobject objInetAddress = env->NewObject(jdk_clsInet4Address,
			jdk_clsInet4Address_initID);

	CHK_NUL_RET_NUL(objInetAddress,"objInetAddress");

	env->SetIntField(objInetAddress, ia_AddressID, address);

	return objInetAddress;

}

// NOTE: ipv4 only
jobject X_NewInetSocketAddress(JNIEnv* env, sockaddr* sockAddr) {

	CHK_NUL_RET_NUL(env,"env");
	CHK_NUL_RET_NUL(sockAddr,"sockAddr");

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;
	jint address = ntohl(sockAddrIn->sin_addr.s_addr);
	jint port = ntohs(sockAddrIn->sin_port);

	jobject objInetAddress = X_NewInetAddress(env, address);
	CHK_NUL_RET_NUL(objInetAddress,"objInetAddress");

	jobject objInetSocketAddress = env->NewObject(jdk_clsInetSocketAddress,
			jdk_clsInetSocketAddress_initID, objInetAddress, port);

	CHK_NUL_RET_NUL(objInetSocketAddress,"objInetSocketAddress");

	return objInetSocketAddress;

}

bool X_IsSockaddrEqualsInetSocketAddress(JNIEnv* env, sockaddr* sockAddr,
		jobject socketAddress) {

	CHK_NUL_RET_FLS(env,"env");
	CHK_NUL_RET_FLS(sockAddr,"sockAddr");
	CHK_NUL_RET_FLS(socketAddress,"socketAddress");

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;

	jint address1 = ntohl(sockAddrIn->sin_addr.s_addr);
	jint port1 = ntohs(sockAddrIn->sin_port);

	jobject objInetAddress = env->GetObjectField(socketAddress,
			isa_InetAddressID);

	CHK_NUL_RET_FLS(objInetAddress,"objInetAddress");

	jint address2 = env->GetIntField(objInetAddress, ia_AddressID);
	jint port2 = env->GetIntField(socketAddress, isa_PortID);

	if (address1 == address2 && port1 == port2) {
		return true;
	}

	return false;

}

// not used
void XXX_ThrowSocketExceptionMessage(JNIEnv* env, const char* message) {
	CHK_NUL_RET_RET(env,"env");
	CHK_NUL_RET_RET(message,"message");
	CHK_NUL_RET_RET(jdk_clsSocketException,"jdk_clsSocketException");
	env->ThrowNew(jdk_clsSocketException, message);
}

// ########################################################

// custom class for struct linger conversion
jobject UDT_NewLingerUDT(JNIEnv* env, linger* lingerValue) {
	CHK_NUL_RET_NUL(env,"env");
	CHK_NUL_RET_NUL(lingerValue,"lingerValue");
	int value;
	if (lingerValue->l_onoff == 0) {
		value = 0;
	} else {
		value = lingerValue->l_linger;
	}
	jobject objLinger = env->NewObject(udt_clsLingerUDT,
			udt_clsLingerUDT_initID, (jint) value);
	CHK_NUL_RET_NUL(objLinger,"objLinger");
	return objLinger;
}

// java wrapper exception error codes; keep in sync with ErrorUDT.java
#define UDT_WRAPPER_UNKNOWN -1 // WRAPPER_UNKNOWN(-1, "unknown error code"), //
#define UDT_WRAPPER_UNIMPLEMENTED -2 // WRAPPER_UNIMPLEMENTED(-2, "this feature is not yet implemented"), //
#define UDT_WRAPPER_MESSAGE -3 // WRAPPER_MESSAGE(-3, "wrapper generated error"), //
#define UDT_USER_DEFINED_MESSAGE -4 // USER_DEFINED_MESSAGE(-4, "user defined message"), //
//

// NOTE: socket id stored in java object SocketUDT
//
jint UDT_GetSocketID(JNIEnv* env, jobject self) {
	return env->GetIntField(self, udts_SocketID);
}
//
void UDT_SetSocketID(JNIEnv* env, jobject self, jint socketID) {
	env->SetIntField(self, udts_SocketID, socketID);
}

jthrowable UDT_NewExceptionUDT(JNIEnv* env, const jint socketID,
		jint errorCode, const char* message) {
	CHK_NUL_RET_NUL(env,"env");
	jstring messageString = env->NewStringUTF(message);
	CHK_NUL_RET_NUL(messageString,"messageString");
	jobject exception = env->NewObject(udt_clsExceptionUDT,
			udt_clsExceptionUDT_initID0, socketID, errorCode, messageString);
	return static_cast<jthrowable> (exception);
}

void UDT_ThrowExceptionUDT_Message(JNIEnv* env, const jint socketID,
		const char *comment) {
	CHK_NUL_RET_RET(env,"env");
	CHK_NUL_RET_RET(comment,"comment");
	jint code = UDT_WRAPPER_MESSAGE;
	jthrowable exception = UDT_NewExceptionUDT(env, socketID, code, comment);
	CHK_NUL_RET_RET(exception,"exception");
	env->Throw(exception);
}

// socketID == 0 means not applicable / not known id
void UDT_ThrowExceptionUDT_ErrorInfo(JNIEnv* env, const jint socketID,
		const char* comment, UDT::ERRORINFO* errorInfo) {
	CHK_NUL_RET_RET(env,"env");
	CHK_NUL_RET_RET(comment,"comment");
	CHK_NUL_RET_RET(errorInfo,"errorInfo");
	jint code = errorInfo->getErrorCode();
	jthrowable exception = UDT_NewExceptionUDT(env, socketID, code, comment);
	CHK_NUL_RET_RET(exception,"exception");
	env->Throw(exception);
}

//	const char *message = errorInfo->getErrorMessage();
//	char text[1024];
//	sprintf(text, "UDT Error : %s : %d %s", comment, code, message);

void UDT_InitFieldMonitor(JNIEnv* env) {

	const jclass cls = udt_clsMonitorUDT;

	// global measurements
	udtm_msTimeStamp = env->GetFieldID(cls, "msTimeStamp", "J"); // time since the UDT entity is started, in milliseconds
	udtm_pktSentTotal = env->GetFieldID(cls, "pktSentTotal", "J"); // total number of sent data packets, including retransmissions
	udtm_pktRecvTotal = env->GetFieldID(cls, "pktRecvTotal", "J"); // total number of received packets
	udtm_pktSndLossTotal = env->GetFieldID(cls, "pktSndLossTotal", "I"); // total number of lost packets (sender side)
	udtm_pktRcvLossTotal = env->GetFieldID(cls, "pktRcvLossTotal", "I"); // total number of lost packets (receiver side)
	udtm_pktRetransTotal = env->GetFieldID(cls, "pktRetransTotal", "I"); // total number of retransmitted packets
	udtm_pktSentACKTotal = env->GetFieldID(cls, "pktSentACKTotal", "I"); // total number of sent ACK packets
	udtm_pktRecvACKTotal = env->GetFieldID(cls, "pktRecvACKTotal", "I"); // total number of received ACK packets
	udtm_pktSentNAKTotal = env->GetFieldID(cls, "pktSentNAKTotal", "I"); // total number of sent NAK packets
	udtm_pktRecvNAKTotal = env->GetFieldID(cls, "pktRecvNAKTotal", "I"); // total number of received NAK packets
	udtm_usSndDurationTotal = env->GetFieldID(cls, "usSndDurationTotal", "J"); // total time duration when UDT is sending data (idle time exclusive)

	// local measurements
	udtm_pktSent = env->GetFieldID(cls, "pktSent", "J"); // number of sent data packets, including retransmissions
	udtm_pktRecv = env->GetFieldID(cls, "pktRecv", "J"); // number of received packets
	udtm_pktSndLoss = env->GetFieldID(cls, "pktSndLoss", "I"); // number of lost packets (sender side)
	udtm_pktRcvLoss = env->GetFieldID(cls, "pktRcvLoss", "I"); // number of lost packets (receiverer side)
	udtm_pktRetrans = env->GetFieldID(cls, "pktRetrans", "I"); // number of retransmitted packets
	udtm_pktSentACK = env->GetFieldID(cls, "pktSentACK", "I"); // number of sent ACK packets
	udtm_pktRecvACK = env->GetFieldID(cls, "pktRecvACK", "I"); // number of received ACK packets
	udtm_pktSentNAK = env->GetFieldID(cls, "pktSentNAK", "I"); // number of sent NAK packets
	udtm_pktRecvNAK = env->GetFieldID(cls, "pktRecvNAK", "I"); // number of received NAK packets
	udtm_mbpsSendRate = env->GetFieldID(cls, "mbpsSendRate", "D"); // sending rate in Mb/s
	udtm_mbpsRecvRate = env->GetFieldID(cls, "mbpsRecvRate", "D"); // receiving rate in Mb/s
	udtm_usSndDuration = env->GetFieldID(cls, "usSndDuration", "J"); // busy sending time (i.e., idle time exclusive)

	// instant measurements
	udtm_usPktSndPeriod = env->GetFieldID(cls, "usPktSndPeriod", "D"); // packet sending period, in microseconds
	udtm_pktFlowWindow = env->GetFieldID(cls, "pktFlowWindow", "I"); // flow window size, in number of packets
	udtm_pktCongestionWindow = env->GetFieldID(cls, "pktCongestionWindow", "I"); // congestion window size, in number of packets
	udtm_pktFlightSize = env->GetFieldID(cls, "pktFlightSize", "I"); // number of packets on flight
	udtm_msRTT = env->GetFieldID(cls, "msRTT", "D"); // RTT, in milliseconds
	udtm_mbpsBandwidth = env->GetFieldID(cls, "mbpsBandwidth", "D"); // estimated bandwidth, in Mb/s
	udtm_byteAvailSndBuf = env->GetFieldID(cls, "byteAvailSndBuf", "I"); // available UDT sender buffer size
	udtm_byteAvailRcvBuf = env->GetFieldID(cls, "byteAvailRcvBuf", "I"); // available UDT receiver buffer size

}

void UDT_InitClassRefAll(JNIEnv* env) {

	// JDK

	X_InitClassReference(env, &jdk_clsBoolean, "java/lang/Boolean");
	X_InitClassReference(env, &jdk_clsInteger, "java/lang/Integer");
	X_InitClassReference(env, &jdk_clsLong, "java/lang/Long");

	X_InitClassReference(env, &jdk_clsInet4Address, "java/net/Inet4Address");
	X_InitClassReference(env, &jdk_clsInet6Address, "java/net/Inet6Address");
	X_InitClassReference(env, &jdk_clsInetSocketAddress,
			"java/net/InetSocketAddress");

	X_InitClassReference(env, &jdk_clsSocketException,
			"java/net/SocketException");

	X_InitClassReference(env, &jdk_clsSet, "java/util/Set");
	X_InitClassReference(env, &jdk_clsIterator, "java/util/Iterator");

	// UDT

	X_InitClassReference(env, &udt_clsSocketUDT, //
			"com/barchart/udt/SocketUDT");
	X_InitClassReference(env, &udt_clsTypeUDT, //
			"com/barchart/udt/TypeUDT");
	X_InitClassReference(env, &udt_clsFactoryUDT, //
			"com/barchart/udt/FactoryUDT");
	X_InitClassReference(env, &udt_clsMonitorUDT, //
			"com/barchart/udt/MonitorUDT");
	X_InitClassReference(env, &udt_clsExceptionUDT, //
			"com/barchart/udt/ExceptionUDT");
	X_InitClassReference(env, &udt_clsLingerUDT, //
			"com/barchart/udt/LingerUDT");

}

void UDT_InitFieldRefAll(JNIEnv* env) {

	// JDK

	ia_AddressID = env->GetFieldID(jdk_clsInet4Address, "address", "I");
	isa_InetAddressID = env->GetFieldID(jdk_clsInetSocketAddress, "addr",
			"Ljava/net/InetAddress;");
	isa_PortID = env->GetFieldID(jdk_clsInetSocketAddress, "port", "I");

	// UDT SocketUDT

	udts_SocketID = env->GetFieldID(udt_clsSocketUDT, //
			"socketID", "I");
	udts_RemoteSocketAddressID = env->GetFieldID(udt_clsSocketUDT, //
			"remoteSocketAddress", "Ljava/net/InetSocketAddress;");
	udts_LocalSocketAddressID = env->GetFieldID(udt_clsSocketUDT, //
			"localSocketAddress", "Ljava/net/InetSocketAddress;");
	udts_TypeID = env->GetFieldID(udt_clsSocketUDT, //
			"type", "Lcom/barchart/udt/TypeUDT;");
	udts_MonitorID = env->GetFieldID(udt_clsSocketUDT, //
			"monitor", "Lcom/barchart/udt/MonitorUDT;");

	udts_IsSelectedReadID = env->GetFieldID(udt_clsSocketUDT, //
			"isSelectedRead", "Z");
	udts_IsSelectedWriteID = env->GetFieldID(udt_clsSocketUDT, //
			"isSelectedWrite", "Z");
	udts_IsSelectedExceptionID = env->GetFieldID(udt_clsSocketUDT, //
			"isSelectedException", "Z");

	// UDT TypeUDT

	udtt_TypeCodeID = env->GetFieldID(udt_clsTypeUDT, "code", "I");

	// UDT MonitorUDT

	UDT_InitFieldMonitor(env);

}

void X_InitMethodRef(JNIEnv* env, jmethodID *methodID, jclass klaz,
		const char* name, const char* signature) {

	*methodID = env->GetMethodID(klaz, name, signature);

	CHK_NUL_RET_RET(*methodID, name);

}

void UDT_InitMethodRefAll(JNIEnv* env) {

	// JDK

	jdk_clsBoolean_initID = env->GetMethodID(jdk_clsBoolean,//
			"<init>", "(Z)V");
	CHK_NUL_RET_RET(jdk_clsBoolean_initID,"jdk_clsBoolean_initID");

	jdk_clsInteger_initID = env->GetMethodID(jdk_clsInteger, //
			"<init>", "(I)V");
	CHK_NUL_RET_RET(jdk_clsInteger_initID,"jdk_clsInteger_initID");

	jdk_clsLong_initID = env->GetMethodID(jdk_clsLong, //
			"<init>", "(J)V");
	CHK_NUL_RET_RET(jdk_clsLong_initID,"jdk_clsLong_initID");

	jdk_clsInet4Address_initID = env->GetMethodID(jdk_clsInet4Address,
			"<init>", "()V");
	CHK_NUL_RET_RET(jdk_clsInet4Address_initID,"jdk_clsInet4Address_initID");

	jdk_clsInetSocketAddress_initID = env->GetMethodID(
			jdk_clsInetSocketAddress, //
			"<init>", "(Ljava/net/InetAddress;I)V");
	CHK_NUL_RET_RET(jdk_clsInetSocketAddress_initID,"jdk_clsInetSocketAddress_initID");

	// java.util.Set
	jdk_clsSet_iteratorID = env->GetMethodID(jdk_clsSet, //
			"iterator", "()Ljava/util/Iterator;");
	CHK_NUL_RET_RET(jdk_clsSet_iteratorID,"jdk_clsSet_iteratorID");
	jdk_clsSet_addID = env->GetMethodID(jdk_clsSet, //
			"add", "(Ljava/lang/Object;)Z");
	CHK_NUL_RET_RET(jdk_clsSet_addID,"jdk_clsSet_addID");
	jdk_clsSet_containsID = env->GetMethodID(jdk_clsSet, //
			"contains", "(Ljava/lang/Object;)Z");
	CHK_NUL_RET_RET(jdk_clsSet_containsID,"jdk_clsSet_containsID");

	// java.util.Iterator
	jdk_clsIterator_hasNextID = env->GetMethodID(jdk_clsIterator, //
			"hasNext", "()Z");
	CHK_NUL_RET_RET(jdk_clsIterator_hasNextID,"jdk_clsIterator_hasNextID");
	jdk_clsIterator_nextID = env->GetMethodID(jdk_clsIterator, //
			"next", "()Ljava/lang/Object;");
	CHK_NUL_RET_RET(jdk_clsIterator_nextID,"jdk_clsIterator_nextID");

	// UDT

	udt_clsSocketUDT_initID1 = env->GetMethodID(udt_clsSocketUDT, //
			"<init>", "(Lcom/barchart/udt/TypeUDT;I)V");
	CHK_NUL_RET_RET(udt_clsSocketUDT_initID1,"udt_clsSocketUDT_initID1");

	udt_clsExceptionUDT_initID0 = env->GetMethodID(udt_clsExceptionUDT, //
			"<init>", "(IILjava/lang/String;)V");
	CHK_NUL_RET_RET(udt_clsExceptionUDT_initID0,"udt_clsExceptionUDT_initID0");

	udt_clsLingerUDT_initID = env->GetMethodID(udt_clsLingerUDT, //
			"<init>", "(I)V");
	CHK_NUL_RET_RET(udt_clsLingerUDT_initID,"udt_clsLingerUDT_initID");

}

// ########################################################

/* signature is a monotonously increasing integer; set in java class SocketUDT */

// validate consistency of java code and native library
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_getSignatureJNI0(
		JNIEnv* env, jclass clsSocketUDT) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	//	printf("udt-getSignatureJNI0\n");

	return com_barchart_udt_SocketUDT_SIGNATURE_JNI;

}

// called on java class load
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_initClass0(JNIEnv* env,
		jclass clsSocketUDT) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	//	printf("native: udt-initClass0\n");

	UDT_InitClassRefAll(env);

	UDT_InitFieldRefAll(env);

	UDT_InitMethodRefAll(env);

	const int rv = UDT::startup();

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "initClass0:startup()",
				&errorInfo);
		return;
	}

}

// called on java class unload
void JNICALL Java_com_barchart_udt_SocketUDT_stopClass0(JNIEnv* env,
		jclass clsSocketUDT) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	// TODO release JNI global references

	const int rv = UDT::cleanup();

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "stopClass0:cleanup()",
				&errorInfo);
		return;
	}

}

// used by default constructor
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_initInstance0(
		JNIEnv* env, jobject self, jint typeCode) {

	//	printf("native: udt-initInstance0\n");

	int socketAddressFamily = AF_INET;
	int socketType = typeCode;

	//	printf("native: init instance; type=%d\n", type);

	const jint socketID = UDT::socket(socketAddressFamily, socketType, 0);

	// change private final field
	UDT_SetSocketID(env, self, socketID);

	if (socketID == UDT::INVALID_SOCK) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID,
				"initInstance0:INVALID_SOCK", &errorInfo);
		return JNI_ERR;
	}

	return socketID;

}

// used by accept constructor
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_initInstance1(
		JNIEnv* env, jobject self, const jint socketID) {

	UNUSED(self);

	//	printf("native: udt-initInstance1\n");

	if (socketID == UDT::INVALID_SOCK) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID,
				"initInstance1:INVALID_SOCK", &errorInfo);
		return JNI_ERR;
	}

	return socketID;

}

JNIEXPORT jobject JNICALL Java_com_barchart_udt_SocketUDT_accept0(JNIEnv* env,
		jobject self) {

	//	printf("native : accept\n");

	sockaddr remoteSockAddr;
	int remoteSockAddrSize = sizeof(remoteSockAddr);

	const jint socketID = UDT_GetSocketID(env, self);

	const jint socketACC = UDT::accept(socketID, &remoteSockAddr,
			&remoteSockAddrSize);

	if (socketACC == UDT::INVALID_SOCK) {

		UDT::ERRORINFO errorInfo = UDT::getlasterror();

		const jint errorCode = errorInfo.getErrorCode();

		if (errorCode == CUDTException::EASYNCRCV) {
			// not a java exception: non-blocking mode return, when not connections in the queue
		} else {
			// really exception
			UDT_ThrowExceptionUDT_ErrorInfo(env, socketID,
					"accept:INVALID_SOCK", &errorInfo);
		}

		return NULL;

	}

	jobject objTypeUDT = env->GetObjectField(self, udts_TypeID);

	CHK_NUL_RET_NUL(objTypeUDT,"objTypeUDT");

	jobject objSocketUDT = env->NewObject(udt_clsSocketUDT,
			udt_clsSocketUDT_initID1, objTypeUDT, socketACC);

	CHK_NUL_RET_NUL(objSocketUDT, "objSocketUDT");

	return objSocketUDT;

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_bind0(JNIEnv* env,
		jobject self, jobject objLocalSocketAddress) {

	const jint socketID = UDT_GetSocketID(env, self);

	int rv;

	sockaddr localSockAddr;

	rv = X_InitSockAddr(&localSockAddr);

	if (rv == JNI_ERR) {
		UDT_ThrowExceptionUDT_Message(env, socketID, "can not X_InitSockAddr");
		return;
	}

	rv = X_ConvertInetSocketAddressToSockaddr(env, objLocalSocketAddress,
			&localSockAddr);

	if (rv == JNI_ERR) {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"can not X_ConvertInetSocketAddressToSockaddr");
		return;
	}

	rv = UDT::bind(socketID, &localSockAddr, sizeof(localSockAddr));

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "bind0 failed",
				&errorInfo);
		return;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_close0(JNIEnv* env,
		jobject self) {

	const jint socketID = UDT_GetSocketID(env, self);

	const int rv = UDT::close(socketID);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "close0 failed",
				&errorInfo);
		return;
	}

}

//
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_connect0(JNIEnv* env,
		jobject self, jobject objRemoteSocketAddress) {

	const jint socketID = UDT_GetSocketID(env, self);

	if (objRemoteSocketAddress == NULL) {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"objRemoteSocketAddress == NULL");
		return;
	}

	int rv;

	sockaddr remoteSockAddr;

	rv = X_InitSockAddr(&remoteSockAddr);

	if (rv == JNI_ERR) {
		UDT_ThrowExceptionUDT_Message(env, socketID, "can not X_InitSockAddr");
		return;
	}

	rv = X_ConvertInetSocketAddressToSockaddr(env, //
			objRemoteSocketAddress, &remoteSockAddr);

	if (rv == JNI_ERR) {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"can not X_ConvertInetSocketAddressToSockaddr");
		return;
	}

	rv = UDT::connect(socketID, &remoteSockAddr, sizeof(remoteSockAddr));

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "connect0 failed",
				&errorInfo);
		return;
	}

}

JNIEXPORT jboolean JNICALL Java_com_barchart_udt_SocketUDT_hasLoadedRemoteSocketAddress(
		JNIEnv* env, jobject self) {

	//	printf("native: udt-hasLoadedRemoteSocketAddress\n");

	const jint socketID = UDT_GetSocketID(env, self);

	sockaddr remoteSockAddr;
	int remoteSockAddrSize = sizeof(remoteSockAddr);

	// "peer" means remote
	const int rv = UDT::getpeername(socketID, &remoteSockAddr,
			&remoteSockAddrSize);

	if (rv == UDT::ERROR) {
		// no exceptions
		return JNI_FALSE;
	}

	jobject objRemoteSocketAddress = env->GetObjectField(self,
			udts_RemoteSocketAddressID);

	if (objRemoteSocketAddress == NULL || !X_IsSockaddrEqualsInetSocketAddress(
			env, &remoteSockAddr, objRemoteSocketAddress)) {

		objRemoteSocketAddress = X_NewInetSocketAddress(env, &remoteSockAddr);

		env->SetObjectField(self, udts_RemoteSocketAddressID,
				objRemoteSocketAddress);

	}

	return JNI_TRUE;

}

JNIEXPORT jboolean JNICALL Java_com_barchart_udt_SocketUDT_hasLoadedLocalSocketAddress(
		JNIEnv* env, jobject self) {

	//	printf("native : hasLoadedLocalSocketAddress\n");

	const jint socketID = UDT_GetSocketID(env, self);

	sockaddr localSockAddr;
	int localSockAddrSize = sizeof(localSockAddr);

	// "sock" means local
	const int rv = UDT::getsockname(socketID, &localSockAddr,
			&localSockAddrSize);

	if (rv == UDT::ERROR) {
		// no exceptions
		return JNI_FALSE;
	}

	jobject objLocalSocketAddress = env->GetObjectField(self,
			udts_LocalSocketAddressID);

	if (objLocalSocketAddress == NULL || !X_IsSockaddrEqualsInetSocketAddress(
			env, &localSockAddr, objLocalSocketAddress)) {

		objLocalSocketAddress = X_NewInetSocketAddress(env, &localSockAddr);

		env->SetObjectField(self, udts_LocalSocketAddressID,
				objLocalSocketAddress);

	}

	return JNI_TRUE;

}

// struct that fits all UDT options types
union UDT_OptVal {
	void* factory;
	linger lingerValue;
	int64_t longValue;
	int intValue;
	bool boolValue;
};

JNIEXPORT jobject JNICALL Java_com_barchart_udt_SocketUDT_getOption0(
		JNIEnv *env, jobject self, jint enumCode, jclass klaz) {

	//	printf("native : getSocketOption\n");

	UDT::SOCKOPT optionName = (UDT::SOCKOPT) enumCode;
	UDT_OptVal optionValue;
	int optionValueSize = sizeof(optionValue);

	const jint socketID = UDT_GetSocketID(env, self);

	const int rv = UDT::getsockopt(socketID, 0, optionName,
			(void*) &optionValue, &optionValueSize);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "getsockopt", &errorInfo);
		return NULL;
	}

	if (env->IsSameObject(klaz, jdk_clsBoolean)) {
		//	printf("native: Boolean\n");
		return X_NewBoolean(env, optionValue.boolValue);
	} else if (env->IsSameObject(klaz, jdk_clsInteger)) {
		//	printf("native: Integer\n");
		return X_NewInteger(env, optionValue.intValue);
	} else if (env->IsSameObject(klaz, udt_clsLingerUDT)) {
		//	printf("native: Linger\n");
		return UDT_NewLingerUDT(env, &(optionValue.lingerValue));
	} else if (env->IsSameObject(klaz, jdk_clsLong)) {
		//	printf("native: Long\n");
		return X_NewLong(env, optionValue.longValue);
	} else if (env->IsSameObject(klaz, udt_clsFactoryUDT)) {
		//	printf("native: FactoryUDT\n");
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"not yet implemented: FactoryUDT");
		return NULL;
	} else {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"unsupported option class in OptionUDT");
		return NULL;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_setOption0(JNIEnv *env,
		jobject self, jint enumCode, jclass klaz, jobject objValue) {

	//	printf("native : setSocketOption\n");

	const jint socketID = UDT_GetSocketID(env, self);

	UDT::SOCKOPT optionName = (UDT::SOCKOPT) enumCode;
	UDT_OptVal optionValue;
	int optionValueSize = sizeof(optionValue);

	if (env->IsSameObject(klaz, jdk_clsBoolean)) {
		jmethodID methodID = env->GetMethodID(//
				jdk_clsBoolean, "booleanValue", "()Z");
		jboolean value = env->CallBooleanMethod(objValue, methodID);
		//		cout << "Boolean:" << BOOL(value) << "\n";
		optionValue.boolValue = BOOL(value);
		optionValueSize = sizeof(bool);
	} else if (env->IsSameObject(klaz, jdk_clsInteger)) {
		jmethodID methodID = env->GetMethodID(jdk_clsInteger, //
				"intValue", "()I");
		jint value = env->CallIntMethod(objValue, methodID);
		//		cout << "Integer:" << value << "\n";
		optionValue.intValue = value;
		optionValueSize = sizeof(int);
	} else if (env->IsSameObject(klaz, udt_clsLingerUDT)) {
		jmethodID methodID = env->GetMethodID(//
				udt_clsLingerUDT, "intValue", "()I");
		int value = env->CallIntMethod(objValue, methodID);
		//		cout << "Linger:" << value << EOL;
		if (value <= 0) {
			optionValue.lingerValue.l_onoff = 0;
			optionValue.lingerValue.l_linger = 0;
		} else {
			optionValue.lingerValue.l_onoff = 1;
			optionValue.lingerValue.l_linger = value; // msvc C4244
		}
		optionValueSize = sizeof(linger);
	} else if (env->IsSameObject(klaz, jdk_clsLong)) {
		jmethodID methodID = env->GetMethodID(//
				jdk_clsLong, "longValue", "()J");
		jlong value = env->CallLongMethod(objValue, methodID);
		//		cout << "Long:" << value << "\n";
		optionValue.longValue = value;
		optionValueSize = sizeof(int64_t);
	} else if (env->IsSameObject(klaz, udt_clsFactoryUDT)) {
		//		printf("FactoryUDT\n");
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"not yet implemented: FactoryUDT");
		return;
	} else {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"unsupported option class in OptionUDT");
		return;
	}

	const int rv = UDT::setsockopt(socketID, 0, optionName,
			(void*) &optionValue, optionValueSize);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "setOption0", &errorInfo);
		return;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_listen0(JNIEnv *env,
		jobject self, jint queueSize) {

	// printf("native : listen\n");

	const jint socketID = UDT_GetSocketID(env, self);

	const int rv = UDT::listen(socketID, queueSize);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "listen0", &errorInfo);
		return;
	}

}

// #########################################################################

bool X_IsValidRange(JNIEnv *env, jint socketID, //
		jlong position, jlong limit, jlong capacity) {
	if (!X_IsInRange(0, position, capacity)) {
		UDT_ThrowExceptionUDT_Message(env, socketID, "position is out of range");
		return false;
	}
	if (!X_IsInRange(0, limit, capacity)) {
		UDT_ThrowExceptionUDT_Message(env, socketID, "limit is out of range");
		return false;
	}
	if (position > limit) {
		UDT_ThrowExceptionUDT_Message(env, socketID, "position > limit");
		return false;
	}
	return true;
}

// #######################################################################

/*
 * receiveX call is shared for both receive() and receivemsg()
 */

// return values, if exception is NOT thrown
// -1 : nothing received (non-blocking only)
// =0 : timeout expired (blocking only)
// >0 : normal receive

jint UDT_ReturnReceiveError(JNIEnv *env, const jint socketID) {
	UDT::ERRORINFO errorInfo = UDT::getlasterror();
	int errorCode = errorInfo.getErrorCode();
	if (errorCode == CUDTException::EASYNCRCV) {
		// not a java exception: non-blocking mode return when nothing is received
	} else {
		// really exception
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "recv/recvmsg",
				&errorInfo);
	}
	return JNI_ERR;
}

// receive into complete array
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive0(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		jbyteArray arrayObj) {

	UNUSED(self);

	//	printf("native : receive0\n");

	jboolean isCopy; // whether JVM returns a reference or a copy
	jbyte* data = env->GetByteArrayElements(arrayObj, &isCopy); // note: must release
	const jsize size = env->GetArrayLength(arrayObj);

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-receive0; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::recv(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-receive0; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::recvmsg(socketID, (char*) data, (int) size);
		break;
	default:
		env->ReleaseByteArrayElements(arrayObj, data, JNI_ABORT); // do not copy back
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"recv/recvmsg : unexpected socketType");
		return JNI_ERR;
	}

	if (rv > 0) { // normal
		env->ReleaseByteArrayElements(arrayObj, data, JNI_UPDATE); //
		return rv;
	} else if (rv < 0) { // UDT::ERROR
		env->ReleaseByteArrayElements(arrayObj, data, JNI_ABORT); //
		return UDT_ReturnReceiveError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		env->ReleaseByteArrayElements(arrayObj, data, JNI_ABORT); //
		return UDT_TIMEOUT;
	}

}

// receive into a portion of an array
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive1(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		jbyteArray arrayObj, const jint position, const jint limit) {

	UNUSED(self);

	//	printf("native: udt-receive1\n");

	const jsize capacity = env->GetArrayLength(arrayObj);

	if (!X_IsValidRange(env, socketID, position, limit, capacity)) {
		return JNI_ERR;
	}

	const jsize size = limit - position;
	jbyte* data = (jbyte*) malloc(sizeof(jbyte) * size);
	if (data == NULL) {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"recv/recvmsg : can not allocate data array");
		return JNI_ERR;
	}

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-receive1; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::recv(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-receive1; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::recvmsg(socketID, (char*) data, (int) size);
		break;
	default:
		free(data);
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"recv/recvmsg : unexpected socketType");
		return JNI_ERR;
	}

	if (rv > 0) { // normal
		env->SetByteArrayRegion(arrayObj, position, rv, data);
		free(data);
		return rv;
	} else if (rv < 0) { // UDT::ERROR
		free(data);
		return UDT_ReturnReceiveError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		free(data);
		return UDT_TIMEOUT;
	}

}

// receive into direct byte buffer
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_receive2(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		const jobject bufferObj, const jint position, const jint limit) {

	UNUSED(self);

	//	printf("native: udt-receive2\n");

	const jlong capacity = env->GetDirectBufferCapacity(bufferObj);

	if (!X_IsValidRange(env, socketID, position, limit, capacity)) {
		return JNI_ERR;
	}

	jbyte* bufferAddress = static_cast<jbyte*> (env->GetDirectBufferAddress(
			bufferObj));

	const jbyte* data = bufferAddress + position;
	const jsize size = static_cast<jsize> (limit - position);

	// memory boundary test
	// assert(data[0] | 1);
	// assert(data[size - 1] | 1);

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-receive2; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::recv(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-receive2; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::recvmsg(socketID, (char*) data, (int) size);
		break;
	default:
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"recv/recvmsg : unexpected socketType");
		return JNI_ERR;
	}

	if (rv > 0) { // normal
		return rv;
	} else if (rv < 0) { //  UDT::ERROR
		return UDT_ReturnReceiveError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		return UDT_TIMEOUT;
	}

}

/*
 * sendXXX call is shared for both send() and sendmsg()
 */

// return values, if exception is NOT thrown
// -1 : no buffer space (non-blocking only )
// =0 : timeout expired (blocking only)
// >0 : normal send, byte count

jint UDT_ReturnSendError(JNIEnv *env, jint socketID) {
	UDT::ERRORINFO errorInfo = UDT::getlasterror();
	const int errorCode = errorInfo.getErrorCode();
	if (errorCode == CUDTException::EASYNCSND) {
		// not a java exception: non-blocking mode return when no space in UDT buffer
	} else {
		// really exception
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "send/sendmsg",
				&errorInfo);
	}
	return JNI_ERR;
}

// send from complete array
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send0(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		const jint timeToLive, const jboolean isOrdered, jbyteArray arrayObj) {

	// printf("native: udt-send0\n");
	UNUSED(self);

	jboolean isCopy; // whether JVM returned reference or copy
	jbyte *data = env->GetByteArrayElements(arrayObj, &isCopy); // note: must release
	const jsize size = env->GetArrayLength(arrayObj);

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-send0; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::send(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-send0; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::sendmsg(socketID, (char*) data, (int) size, (int) timeToLive,
				BOOL(isOrdered));
		break;
	default:
		env->ReleaseByteArrayElements(arrayObj, data, JNI_ABORT); // do not copy back
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"send/sendmsg : unexpected socketType");
		return JNI_ERR;
	}

	env->ReleaseByteArrayElements(arrayObj, data, JNI_ABORT); // do not copy back

	if (rv > 0) { // normal
		return rv;
	} else if (rv < 0) { // UDT::ERROR
		return UDT_ReturnSendError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		return UDT_TIMEOUT;
	}

}

// send from a portion of array
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send1(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		const jint timeToLive, const jboolean isOrdered, //
		jbyteArray arrayObj, const jint position, const jint limit) {

	// printf("native: udt-send1\n");
	UNUSED(self);

	const jsize capacity = env->GetArrayLength(arrayObj);

	if (!X_IsValidRange(env, socketID, position, limit, capacity)) {
		return JNI_ERR;
	}

	const jsize size = limit - position;

	jbyte* data = (jbyte*) malloc(sizeof(jbyte) * size);
	if (data == NULL) {
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"send/sendmsg : can not allocate data array");
		return JNI_ERR;
	}

	env->GetByteArrayRegion(arrayObj, position, size, data);

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-send1; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::send(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-send1; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::sendmsg(socketID, (char*) data, (int) size, (int) timeToLive,
				BOOL(isOrdered));
		break;
	default:
		free(data);
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"send/sendmsg : unexpected socketType");
		return JNI_ERR;
	}

	free(data);

	if (rv > 0) { // normal
		return rv;
	} else if (rv < 0) { // UDT::ERROR
		return UDT_ReturnSendError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		return UDT_TIMEOUT;
	}

}

// send direct byte buffer
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_send2(JNIEnv *env,
		jobject self, const jint socketID, const jint socketType,
		const jint timeToLive, const jboolean isOrdered, //
		jobject bufferObj, const jint position, const jint limit) {

	// printf("native: udt-send2\n");
	UNUSED(self);

	const jlong capacity = env->GetDirectBufferCapacity(bufferObj);

	if (!X_IsValidRange(env, socketID, position, limit, capacity)) {
		return JNI_ERR;
	}

	jbyte* bufferAddress = static_cast<jbyte*> (env->GetDirectBufferAddress(
			bufferObj));

	const jbyte* data = bufferAddress + position;
	const jsize size = static_cast<jsize> (limit - position);

	// memory boundary test
	// assert(data[0] | 1);
	// assert(data[size - 1] | 1);

	int rv;

	switch (socketType) {
	case SOCK_STREAM:
		// printf("native: udt-send2; SOCK_STREAM; socketID=%d\n", socketID);
		rv = UDT::send(socketID, (char*) data, (int) size, 0);
		break;
	case SOCK_DGRAM:
		// printf("native: udt-send2; SOCK_DGRAM; socketID=%d\n", socketID);
		rv = UDT::sendmsg(socketID, (char*) data, (int) size, (int) timeToLive,
				BOOL(isOrdered));
		break;
	default:
		UDT_ThrowExceptionUDT_Message(env, socketID,
				"send/sendmsg : unexpected socketType");
		return JNI_ERR;
	}

	if (rv > 0) { // normal
		return rv;
	} else if (rv < 0) { // UDT::ERROR
		return UDT_ReturnSendError(env, socketID);
	} else { // ==0; UDT_TIMEOUT
		return UDT_TIMEOUT;
	}

}

JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_getErrorCode0(
		JNIEnv *env, jobject self) {

	//	printf("native: udt-getErrorCode0\n");
	UNUSED(env);
	UNUSED(self);

	const jint errorCode = UDT::getlasterror().getErrorCode();

	return errorCode;

}

JNIEXPORT jstring JNICALL Java_com_barchart_udt_SocketUDT_getErrorMessage0(
		JNIEnv *env, jobject self) {

	//	printf("native: udt-getErrorMessage0\n");
	UNUSED(self);

	const char* errorMessage = UDT::getlasterror().getErrorMessage();

	return (errorMessage == NULL) ? env->NewStringUTF("<NONE>")
			: env->NewStringUTF(errorMessage);

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_clearError0(JNIEnv *env,
		jobject self) {

	//	printf("native: udt-clearError0\n")
	UNUSED(env);
	UNUSED(self);

	UDT::getlasterror().clear();

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_updateMonitor0(
		JNIEnv *env, jobject self, jboolean makeClear) {

	UDT::TRACEINFO monitor;

	const jint socketID = UDT_GetSocketID(env, self);

	const int rv = UDT::perfmon(socketID, &monitor, BOOL(makeClear));

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "updateMonitor0",
				&errorInfo);
		return;
	}

	const jobject objMonitor = env->GetObjectField(self, udts_MonitorID);

	CHK_NUL_RET_RET(objMonitor,"objMonitor");

	// global measurements
	env->SetLongField(objMonitor, udtm_msTimeStamp, monitor.msTimeStamp); // time since the UDT entity is started, in milliseconds
	env->SetLongField(objMonitor, udtm_pktSentTotal, monitor.pktSentTotal); // total number of sent data packets, including retransmissions
	env->SetLongField(objMonitor, udtm_pktRecvTotal, monitor.pktRecvTotal); // total number of received packets
	env->SetIntField(objMonitor, udtm_pktSndLossTotal, monitor.pktSndLossTotal); // total number of lost packets (sender side)
	env->SetIntField(objMonitor, udtm_pktRcvLossTotal, monitor.pktRcvLossTotal); // total number of lost packets (receiver side)
	env->SetIntField(objMonitor, udtm_pktRetransTotal, monitor.pktRetransTotal); // total number of retransmitted packets
	env->SetIntField(objMonitor, udtm_pktSentACKTotal, monitor.pktSentACKTotal); // total number of sent ACK packets
	env->SetIntField(objMonitor, udtm_pktRecvACKTotal, monitor.pktRecvACKTotal); // total number of received ACK packets
	env->SetIntField(objMonitor, udtm_pktSentNAKTotal, monitor.pktSentNAKTotal); // total number of sent NAK packets
	env->SetIntField(objMonitor, udtm_pktRecvNAKTotal, monitor.pktRecvNAKTotal); // total number of received NAK packets
	env->SetLongField(objMonitor, udtm_usSndDurationTotal,
			monitor.usSndDurationTotal); // total time duration when UDT is sending data (idle time exclusive)

	// local measurements
	env->SetLongField(objMonitor, udtm_pktSent, monitor.pktSent); // number of sent data packets, including retransmissions
	env->SetLongField(objMonitor, udtm_pktRecv, monitor.pktRecv); // number of received packets
	env->SetIntField(objMonitor, udtm_pktSndLoss, monitor.pktSndLoss); // number of lost packets (sender side)
	env->SetIntField(objMonitor, udtm_pktRcvLoss, monitor.pktRcvLoss); // number of lost packets (receiverer side)
	env->SetIntField(objMonitor, udtm_pktRetrans, monitor.pktRetrans); // number of retransmitted packets
	env->SetIntField(objMonitor, udtm_pktSentACK, monitor.pktSentACK); // number of sent ACK packets
	env->SetIntField(objMonitor, udtm_pktRecvACK, monitor.pktRecvACK); // number of received ACK packets
	env->SetIntField(objMonitor, udtm_pktSentNAK, monitor.pktSentNAK); // number of sent NAK packets
	env->SetIntField(objMonitor, udtm_pktRecvNAK, monitor.pktRecvNAK); // number of received NAK packets
	env->SetDoubleField(objMonitor, udtm_mbpsSendRate, monitor.mbpsSendRate); // sending rate in Mb/s
	env->SetDoubleField(objMonitor, udtm_mbpsRecvRate, monitor.mbpsRecvRate); // receiving rate in Mb/s
	env->SetLongField(objMonitor, udtm_usSndDuration, monitor.usSndDuration); // busy sending time (i.e., idle time exclusive)

	// instant measurements
	env->SetDoubleField(objMonitor, udtm_usPktSndPeriod, monitor.usPktSndPeriod); // packet sending period, in microseconds
	env->SetIntField(objMonitor, udtm_pktFlowWindow, monitor.pktFlowWindow); // flow window size, in number of packets
	env->SetIntField(objMonitor, udtm_pktCongestionWindow,
			monitor.pktCongestionWindow); // congestion window size, in number of packets
	env->SetIntField(objMonitor, udtm_pktFlightSize, monitor.pktFlightSize); // number of packets on flight
	env->SetDoubleField(objMonitor, udtm_msRTT, monitor.msRTT); // RTT, in milliseconds
	env->SetDoubleField(objMonitor, udtm_mbpsBandwidth, monitor.mbpsBandwidth); // estimated bandwidth, in Mb/s
	env->SetIntField(objMonitor, udtm_byteAvailSndBuf, monitor.byteAvailSndBuf); // available UDT sender buffer size
	env->SetIntField(objMonitor, udtm_byteAvailRcvBuf, monitor.byteAvailRcvBuf); // available UDT receiver buffer size

}

// #########################################################################

void UDT_CopyArrayToSet(jint* array, UDT::UDSET* udSet, const jsize size) {
	pair<UDT::UDSET::iterator, bool> rv;
	for (jint index = 0; index < size; index++) {
		const jint socketID = array[index];
		rv = UD_SET(socketID, udSet);
		assert(rv.second == true);
	}
}

void UDT_CopySetToArray(UDT::UDSET* udSet, jint* array, const jsize size) {
	UDT::UDSET::iterator iterator = udSet->begin();
	for (jint index = 0; index < size; index++) {
		const jint socketID = *iterator;
		array[index] = socketID;
		++iterator;
	}
}

// sizeArray parameters
#define UDT_READ_INDEX		com_barchart_udt_SocketUDT_UDT_READ_INDEX
#define UDT_WRITE_INDEX		com_barchart_udt_SocketUDT_UDT_WRITE_INDEX
#define UDT_EXCEPT_INDEX	com_barchart_udt_SocketUDT_UDT_EXCEPT_INDEX
#define UDT_SIZE_COUNT		com_barchart_udt_SocketUDT_UDT_SIZE_COUNT

// ### array-based implementation ###
//
// note: relies on input parameters consistency checking in java
//
// return value, when NOT exception
// <0 : should not happen
// =0 : timeout, no ready sockets
// >0 : total number or reads, writes, exceptions
//
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_select0(JNIEnv *env,
		jclass clsSocketUDT, //
		const jintArray objReadArray, //
		const jintArray objWriteArray, //
		const jintArray objExceptArray, //
		const jintArray objSizeArray, //
		const jlong millisTimeout) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	// get interest sizes
	jint* sizeArray = NULL;
	sizeArray = (jint*) malloc(sizeof(jint) * UDT_SIZE_COUNT);
	if (sizeArray == NULL) {
		UDT_ThrowExceptionUDT_Message(env, 0,
				"select0 : can not allocate sizeArray");
		return JNI_ERR;
	}
	env->GetIntArrayRegion(objSizeArray, 0, UDT_SIZE_COUNT, sizeArray);

	const jsize readSize = sizeArray[UDT_READ_INDEX];
	const jsize writeSize = sizeArray[UDT_WRITE_INDEX];

	const bool isInterestedInRead = readSize > 0;
	const bool isInterestedInWrite = writeSize > 0;

	jint* readArray = NULL;
	jint* writeArray = NULL;

	// make empty sets
	UDT::UDSET readSet;
	UDT::UDSET writeSet;
	UDT::UDSET exceptSet;

	// interested in read
	if (isInterestedInRead) {
		//		cout << "udt-select0; readSize=" << readSize << EOL;
		readArray = (jint*) malloc(sizeof(jint) * readSize);
		if (readArray == NULL) {
			UDT_ThrowExceptionUDT_Message(env, 0,
					"select0 : can not allocate readArray");
			return JNI_ERR; // XXX free ?
		}
		env->GetIntArrayRegion(objReadArray, 0, readSize, readArray);
		UDT_CopyArrayToSet(readArray, &readSet, readSize);
	}

	// interested in write
	if (isInterestedInWrite) {
		//		cout << "udt-select0; writeSize=" << writeSize << EOL;
		writeArray = (jint*) malloc(sizeof(jint) * writeSize);
		if (writeArray == NULL) {
			UDT_ThrowExceptionUDT_Message(env, 0,
					"select0 : can not allocate writeArray");
			return JNI_ERR; // XXX free ?
		}
		env->GetIntArrayRegion(objWriteArray, 0, writeSize, writeArray);
		UDT_CopyArrayToSet(writeArray, &writeSet, writeSize);
	}

	// convert timeout
	timeval timeValue;
	X_ConvertMillisIntoTimeValue(millisTimeout, &timeValue);

	// do select
	const int rv = UDT::select(0, &readSet, &writeSet, &exceptSet, &timeValue);

	// process timeout & errors
	if (rv <= 0) { // UDT::ERROR is '-1'; UDT_TIMEOUT is '=0';
		free(sizeArray);
		if (isInterestedInRead) {
			free(readArray);
		}
		if (isInterestedInWrite) {
			free(writeArray);
		}
		if (rv == UDT_TIMEOUT) { // timeout
			return UDT_TIMEOUT;
		} else {
			UDT::ERRORINFO errorInfo = UDT::getlasterror();
			UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "select0", &errorInfo);
			return JNI_ERR;
		}
	}

	// return read interest
	if (isInterestedInRead) {
		const jsize readSizeReturn = readSet.size();
		//		cout << "udt-select0; readSizeReturn=" << readSizeReturn << EOL;
		sizeArray[UDT_READ_INDEX] = readSizeReturn;
		if (readSizeReturn > 0) {
			UDT_CopySetToArray(&readSet, readArray, readSizeReturn);
			env->SetIntArrayRegion(objReadArray, 0, readSizeReturn, readArray);
		}
		free(readArray);
	}

	// return write interest
	if (isInterestedInWrite) {
		const jsize writeSizeReturn = writeSet.size();
		//		cout << "udt-select0; writeSizeReturtn=" << writeSizeReturn << EOL;
		sizeArray[UDT_WRITE_INDEX] = writeSizeReturn;
		if (writeSizeReturn > 0) {
			UDT_CopySetToArray(&writeSet, writeArray, writeSizeReturn);
			env->SetIntArrayRegion(objWriteArray, 0, writeSizeReturn,
					writeArray);
		}
		free(writeArray);
	}

	// exceptions report
	const jsize exceptSizeReturn = exceptSet.size();
	sizeArray[UDT_EXCEPT_INDEX] = exceptSizeReturn;
	if (exceptSizeReturn > 0) {
		jint* exceptArray = NULL;
		exceptArray = (jint*) malloc(sizeof(jint) * exceptSizeReturn);
		if (exceptArray == NULL) {
			UDT_ThrowExceptionUDT_Message(env, 0,
					"select0 : can not allocate exceptArray");
			return JNI_ERR; // XXX free ?
		}
		UDT_CopySetToArray(&exceptSet, exceptArray, exceptSizeReturn);
		env->SetIntArrayRegion(objExceptArray, 0, exceptSizeReturn, exceptArray);
		free(exceptArray);
	}

	// return sizes
	env->SetIntArrayRegion(objSizeArray, 0, UDT_SIZE_COUNT, sizeArray);
	free(sizeArray);

	return rv;

}

// ### direct-buffer-based implementation ###
//
// note: relies on input parameters consistency checking in java
//
// return value, when NOT exception
// <0 : should not happen
// =0 : timeout, no ready sockets
// >0 : total number or reads, writes, exceptions
//
JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_select1(JNIEnv* env,
		jclass clsSocketUDT, //
		const jobject objReadBuffer, //
		const jobject objWriteBuffer, //
		const jobject objExceptBuffer, //
		const jobject objSizeBuffer, //
		const jlong millisTimeout) {

	UNUSED(clsSocketUDT);

	// get interest sizes
	jint* sizeArray = static_cast<jint*> (env->GetDirectBufferAddress(
			objSizeBuffer));

	const jsize readSize = sizeArray[UDT_READ_INDEX];
	const jsize writeSize = sizeArray[UDT_WRITE_INDEX];

	const bool isInterestedInRead = readSize > 0;
	const bool isInterestedInWrite = writeSize > 0;

	jint* readArray = NULL;
	jint* writeArray = NULL;

	// make empty sets
	UDT::UDSET readSet;
	UDT::UDSET writeSet;
	UDT::UDSET exceptSet;

	// interested in read
	if (isInterestedInRead) {
		readArray = static_cast<jint*> (env->GetDirectBufferAddress(
				objReadBuffer));
		UDT_CopyArrayToSet(readArray, &readSet, readSize);
	}

	// interested in write
	if (isInterestedInWrite) {
		writeArray = static_cast<jint*> (env->GetDirectBufferAddress(
				objWriteBuffer));
		UDT_CopyArrayToSet(writeArray, &writeSet, writeSize);
	}

	// convert timeout
	timeval timeValue;
	X_ConvertMillisIntoTimeValue(millisTimeout, &timeValue);

	// do select
	const int rv = UDT::select(0, &readSet, &writeSet, &exceptSet, &timeValue);

	// process timeout & errors
	if (rv <= 0) { // UDT::ERROR is '-1'; UDT_TIMEOUT is '=0';
		if (rv == UDT_TIMEOUT) { // timeout
			return UDT_TIMEOUT;
		} else {
			UDT::ERRORINFO errorInfo = UDT::getlasterror();
			UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "select1", &errorInfo);
			return JNI_ERR;
		}
	}

	// return read interest
	if (isInterestedInRead) {
		const jsize readSizeReturn = readSet.size();
		assert(readSizeReturn <= readSize);
		sizeArray[UDT_READ_INDEX] = readSizeReturn;
		if (readSizeReturn > 0) {
			UDT_CopySetToArray(&readSet, readArray, readSizeReturn);
		}
	}

	// return write interest
	if (isInterestedInWrite) {
		const jsize writeSizeReturn = writeSet.size();
		sizeArray[UDT_WRITE_INDEX] = writeSizeReturn;
		assert(writeSizeReturn <= writeSize);
		if (writeSizeReturn > 0) {
			UDT_CopySetToArray(&writeSet, writeArray, writeSizeReturn);
		}
	}

	// exceptions status report
	const jsize exceptSizeReturn = exceptSet.size();
	sizeArray[UDT_EXCEPT_INDEX] = exceptSizeReturn;
	if (exceptSizeReturn > 0) {
		jint* exceptArray = static_cast<jint*> (env->GetDirectBufferAddress(
				objExceptBuffer));
		UDT_CopySetToArray(&exceptSet, exceptArray, exceptSizeReturn);
	}

	return rv;

}

// not used
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_selectEx0(JNIEnv *env,
		jclass clsSocketUDT, jintArray objSelectArray, jintArray objReadArray,
		jintArray objWriteArray, jintArray objExceptionArray, jlong timeout) {

	UNUSED(clsSocketUDT);
	UNUSED(objReadArray);
	UNUSED(objWriteArray);
	UNUSED(objExceptionArray);

	// convert timeout
	int64_t msTimeOut = static_cast<int64_t> (timeout);

	// make and populate input vector

	std::vector<UDTSOCKET> selectFDs;

	jsize selectSize = env->GetArrayLength(objSelectArray);

	jboolean isCopy;

	jint *selectArray = env->GetIntArrayElements(objSelectArray, &isCopy);

	for (jint index = 0; index < selectSize; index++) {

		jint socketID = selectArray[index];

		selectFDs.push_back(socketID);

	}

	// make empty output vectors

	std::vector<UDTSOCKET> readFDs;
	std::vector<UDTSOCKET> writeFDs;
	std::vector<UDTSOCKET> exceptFDs;

	// XXX exceptions from exceptFDs?
	//	int rv = UDT::selectEx(//
	//			selectFDs, &readFDs, &writeFDs, &exceptFDs, msTimeOut);
	const int rv = UDT::selectEx(//
			selectFDs, &readFDs, &writeFDs, NULL, msTimeOut);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "selectEx0", &errorInfo);
		return;
	}

	//
	//	cout << "udt-selectEx0; readFDs=" << readFDs.size() << EOL;
	//	cout << "udt-selectEx0; writeFDs=" << writeFDs.size() << EOL;
	//	cout << "udt-selectEx0; exceptFDs=" << exceptFDs.size() << EOL;

	// update selected operations

	for (jint index = 0; index < selectSize; index++) {

		jobject objSocketUDT = NULL; // XXX

		jint socketID = env->GetIntField(objSocketUDT, udts_SocketID);

		std::vector<UDTSOCKET>::iterator result;

		if (!readFDs.empty()) {
			result = find(readFDs.begin(), readFDs.end(), socketID);
			if (result != readFDs.end()) {
				env->SetBooleanField(objSocketUDT, //
						udts_IsSelectedReadID, JNI_TRUE);
			}
		}

		if (!writeFDs.empty()) {
			result = find(writeFDs.begin(), writeFDs.end(), socketID);
			if (result != writeFDs.end()) {
				env->SetBooleanField(objSocketUDT, //
						udts_IsSelectedWriteID, JNI_TRUE);
			}
		}

		if (!exceptFDs.empty()) {
			result = find(exceptFDs.begin(), exceptFDs.end(), socketID);
			if (result != exceptFDs.end()) {
				env->SetBooleanField(objSocketUDT, //
						udts_IsSelectedExceptionID, JNI_TRUE);
			}
		}

	}

}

// not used
JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_selectEx1(JNIEnv *env,
		jclass self, jobject registeredKeySet, jobject selectedKeySet,
		jlong timeout) {

	UNUSED(self);
	UNUSED(selectedKeySet);

	// convert timeout
	int64_t msTimeOut = static_cast<int64_t> (timeout);

	// TODO make global --- get IDs

	jclass clsKeyUDT = (env)->FindClass("com/barchart/udt/nio/SelectionKeyUDT");
	jfieldID socketIDID = env->GetFieldID(clsKeyUDT, //
			"socketID", "I");

	jobject iteratorRegistered = env->CallObjectMethod(registeredKeySet,
			jdk_clsSet_iteratorID);

	// make and populate input vector

	std::vector<UDTSOCKET> selectFDs;

	int count = 0;
	while (JNI_TRUE == env->CallBooleanMethod(iteratorRegistered,
			jdk_clsIterator_hasNextID)) {
		jobject keyUDT = env->CallObjectMethod(iteratorRegistered,
				jdk_clsIterator_nextID);
		jint socketID = env->GetIntField(keyUDT, socketIDID);
		selectFDs.push_back(socketID);
		count++;
	}

	//	cout << "udt-selectEx1; count=" << count << EOL;

	// make empty output vectors

	std::vector<UDTSOCKET> readFDs;
	std::vector<UDTSOCKET> writeFDs;
	std::vector<UDTSOCKET> exceptFDs;

	// make select
	const int rv = UDT::selectEx(//
			selectFDs, &readFDs, &writeFDs, NULL, msTimeOut);

	// process errors
	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "selectEx1", &errorInfo);
		return;
	}

	// debug stats
	//	cout << "udt-selectEx1; readFDs   = " << readFDs.size() << EOL;
	//	cout << "udt-selectEx1; writeFDs  = " << writeFDs.size() << EOL;
	//	cout << "udt-selectEx1; exceptFDs = " << exceptFDs.size() << EOL;

	//	jobject iteratorSelected =
	//			env->CallObjectMethod(selectedKeySet, iteratorID);

}

JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_getStatus0(JNIEnv *env,
		jobject self) {

	const jint socketID = UDT_GetSocketID(env, self);

	const UDTSTATUS status = UDT::getsockstate(socketID);

	return static_cast<jint> (status);

}

//

JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_epollCreate(JNIEnv *env,
		jclass clsSocketUDT) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	const int rv = UDT::epoll_create();

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "epollCreate", &errorInfo);
		return JNI_ERR;
	}

	return static_cast<jint> (rv);

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_epollRelease(
		JNIEnv *env, jclass clsSocketUDT, const jint pollID) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	const int rv = UDT::epoll_release(pollID);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "epollRelease", &errorInfo);
		return;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_epollAdd(JNIEnv *env,
		jclass clsSocketUDT, const jint pollID, const jint socketID) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	const int rv = UDT::epoll_add_usock(pollID, socketID, NULL);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "epollAdd", &errorInfo);
		return;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_epollRemove(JNIEnv *env,
		jclass clsSocketUDT, const jint pollID, const jint socketID) {

	UNUSED(env);
	UNUSED(clsSocketUDT);

	const int rv = UDT::epoll_remove_usock(pollID, socketID, NULL);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID, "epollRemove",
				&errorInfo);
		return;
	}

}

JNIEXPORT jint JNICALL Java_com_barchart_udt_SocketUDT_epollWait(JNIEnv *env,
		jclass clsSocketUDT, //
		const jint pollID, //
		const jobject objReadBuffer, //
		const jobject objWriteBuffer, //
		const jobject objExceptBuffer, //
		const jobject objSizeBuffer, //
		const jlong millisTimeout) {

	UNUSED(clsSocketUDT);

	// make empty sets
	UDT::UDSET readSet;
	UDT::UDSET writeSet;

	// do select
	const int rv = UDT::epoll_wait(//
			pollID, &readSet, &writeSet, millisTimeout, NULL, NULL);

	// process timeout & errors
	if (rv <= 0) { // UDT::ERROR is '-1'; UDT_TIMEOUT is '=0';
		if (rv == UDT_TIMEOUT) { // timeout
			return UDT_TIMEOUT;
		} else {
			UDT::ERRORINFO errorInfo = UDT::getlasterror();
			UDT_ThrowExceptionUDT_ErrorInfo(env, 0, "epollWait", &errorInfo);
			return JNI_ERR;
		}
	}

	//

	// get interest sizes
	jint* sizeArray = static_cast<jint*> //
			(env->GetDirectBufferAddress(objSizeBuffer));

	{// return read interest
		const jsize readSize = readSet.size();
		if (readSize > 0) {
			sizeArray[UDT_READ_INDEX] = readSize;
			jint* readArray = static_cast<jint*> (//
					env->GetDirectBufferAddress(objReadBuffer));
			UDT_CopySetToArray(&readSet, readArray, readSize);
		}
	}

	{ // return write interest
		const jsize writeSize = writeSet.size();
		if (writeSize > 0) {
			sizeArray[UDT_WRITE_INDEX] = writeSize;
			jint* writeArray = static_cast<jint*> (//
					env->GetDirectBufferAddress(objWriteBuffer));
			UDT_CopySetToArray(&writeSet, writeArray, writeSize);
		}
	}

	{ // return exceptions report TODO
		UDT::UDSET exceptSet;
	}

	return rv;

}

// #########################################3
// start - used for development only


JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testEmptyCall0(
		JNIEnv *env, jobject self) {

	// test cost of JNI call
	UNUSED(env);
	UNUSED(self);

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testIterateArray0(
		JNIEnv *env, jobject self, jobjectArray objArray) {

	// test cost of JNI-to-Java array iteration
	UNUSED(self);

	const jsize size = env->GetArrayLength(objArray);
	for (jint index = 0; index < size; index++) {
		jobject objAny = env->GetObjectArrayElement(objArray, index);
		objAny = NULL;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testIterateSet0(
		JNIEnv *env, jobject self, jobject objSet) {

	// test cost of JNI-to-Java set iteration
	UNUSED(self);

	jobject iterator = env->CallObjectMethod(objSet, jdk_clsSet_iteratorID);

	jint count = 0;

	while (JNI_TRUE == env->CallBooleanMethod(iterator,
			jdk_clsIterator_hasNextID)) {
		jobject objAny =
				env->CallObjectMethod(iterator, jdk_clsIterator_nextID);
		objAny = NULL;
		count++;
	}

}

JNIEXPORT jintArray JNICALL JNICALL Java_com_barchart_udt_SocketUDT_testMakeArray0(
		JNIEnv *env, jobject self, jint size) {

	// test cost of JNI-to-Java make array
	UNUSED(self);

	jintArray array = env->NewIntArray(size);

	return array;

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testGetSetArray0(
		JNIEnv *env, jobject self, jintArray objArray, jboolean isReturn) {

	// test cost of array copy
	UNUSED(self);

	jboolean isCopy;

	jint* data = env->GetIntArrayElements(objArray, &isCopy);

	//	jsize size = env->GetArrayLength(objArray);

	if (isReturn == JNI_TRUE) {
		env->ReleaseIntArrayElements(objArray, data, JNI_UPDATE);
	} else {
		env->ReleaseIntArrayElements(objArray, data, JNI_ABORT);
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testInvalidClose0(
		JNIEnv *env, jobject self, const jint socketID) {

	// test error on close of a closed socket
	UNUSED(self);

	const int rv = UDT::close(socketID);

	if (rv == UDT::ERROR) {
		UDT::ERRORINFO errorInfo = UDT::getlasterror();
		UDT_ThrowExceptionUDT_ErrorInfo(env, socketID,
				"testInvalidClose0 - close failed", &errorInfo);
		return;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testCrashJVM0(
		JNIEnv *env, jobject self) {

	// test crash jvm to debug crash log parser

	printf("native: test crash jvm \n");

	UNUSED(env);
	UNUSED(self);

	jint *array = NULL;

	array[0] = 1;

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testDirectByteBufferAccess0(
		JNIEnv* env, jobject self, jobject bufferObj) {

	// test correctness of direct BYTE buffer access
	UNUSED(self);

	jbyte* byteBuffer = static_cast<jbyte*> (env->GetDirectBufferAddress(
			bufferObj));

	jlong capacity = env->GetDirectBufferCapacity(bufferObj);
	int capacityInt = (int) capacity;

	printf("native: byteBuffer capacity=%d \n", capacityInt);

	byteBuffer[0] = 'A';
	byteBuffer[1] = 'B';
	byteBuffer[2] = 'C';

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testDirectIntBufferAccess0(
		JNIEnv* env, jobject self, jobject bufferObj) {

	// test correctness of direct INT buffer access
	UNUSED(self);

	jint* intBuffer =
			static_cast<jint*> (env->GetDirectBufferAddress(bufferObj));

	jlong capacity = env->GetDirectBufferCapacity(bufferObj);
	int capacityInt = (int) capacity;

	printf("native: intBuffer capacity=%d \n", capacityInt);

	intBuffer[0] = 'A';
	intBuffer[1] = 'B';
	intBuffer[2] = 'C';

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testFillArray0(
		JNIEnv *env, jobject self, jbyteArray arrayObj) {

	// test cost of jni array fill
	UNUSED(self);

	jboolean isCopy;
	jbyte* array = env->GetByteArrayElements(arrayObj, &isCopy);
	jsize size = env->GetArrayLength(arrayObj);

	for (int k = 0; k < size; k++) {
		array[k] = (char) k;
	}

	env->ReleaseByteArrayElements(arrayObj, array, JNI_UPDATE);

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testFillBuffer0(
		JNIEnv *env, jobject self, jobject bufferObj) {

	// test cost of jni direct buffer fill
	UNUSED(self);

	jbyte* buffer =
			static_cast<jbyte*> (env->GetDirectBufferAddress(bufferObj));

	jlong capacity = env->GetDirectBufferCapacity(bufferObj);

	for (int k = 0; k < capacity; k++) {
		buffer[k] = (char) k;
	}

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testSocketStatus0(
		JNIEnv *env, jobject self) {

	// test socket status api

	const jint socketID = UDT_GetSocketID(env, self);

	//	printf("native: test socket status; id=%d \n", socketID);

	const UDTSTATUS status = UDT::getsockstate(socketID);

	printf("native: test socket status; status=%d \n", status);

}

JNIEXPORT void JNICALL Java_com_barchart_udt_SocketUDT_testEpoll0(JNIEnv *env,
		jobject self) {

	const jint socketID = UDT_GetSocketID(env, self);

	printf("native: test epoll; socketID=%d \n", socketID);

	const int epollID = UDT::epoll_create();

	printf("native: test epoll; epollID=%d \n", epollID);

}

// finish - used for development only
// #########################################


} // [extern "C"]
