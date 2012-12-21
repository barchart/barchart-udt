/**
 * =================================================================================
 *
 * BSD LICENCE (http://en.wikipedia.org/wiki/BSD_licenses)
 *
 * Copyright (C) 2009-2012, Barchart, Inc. (http://www.barchart.com/)
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
 * Developers: Andrei Pozolotin, CCob
 *
 * =================================================================================
 */

#ifndef JNIHELPERS_H_
#define JNIHELPERS_H_

#include "udt.h"

#include <cassert>
#include <climits>
#include <cstring>
#include <vector>
#include <algorithm>

#include "jni.h"

// ### JNI

#define JNI_UPDATE 0 // jni object release with copy
//

// ### JDK references

// JDK classes
extern jclass jdk_clsBoolean; // java.lang.Boolean
extern jclass jdk_clsInteger; // java.lang.Integer
extern jclass jdk_clsLong; // java.lang.Long
extern jclass jdk_clsInet4Address; // java.net.Inet4Address
extern jclass jdk_clsInet6Address; // java.net.Inet6Address
extern jclass jdk_clsInetSocketAddress; // java.net.InetSocketAddress
extern jclass jdk_clsSocketException; // java.net.SocketException
extern jclass jdk_clsSet; // java.util.Set
extern jclass jdk_clsIterator; // java.util.Iterator

// JDK fields
extern jfieldID isa_InetAddressID; // java.net.InetSocketAddress#addr
extern jfieldID isa_PortID; // java.net.InetSocketAddress#port
extern jfieldID ia_AddressID; // java.net.InetAddress#address

// JDK methods
extern jmethodID jdk_clsBoolean_initID; // new Boolean(boolean x)
extern jmethodID jdk_clsInteger_initID; // new Integer(int x)
extern jmethodID jdk_clsLong_initID; // new Long(long x)
extern jmethodID jdk_clsInet4Address_initID; // new InetAddress()
extern jmethodID jdk_clsInetSocketAddress_initID; // new InetSocketAddress(InetAddress x)
extern jmethodID jdk_clsSet_iteratorID; // Iterator set.iterator()
extern jmethodID jdk_clsSet_addID; // boolean set.add(Object)
extern jmethodID jdk_clsSet_containsID; // boolean set.contains(Object)
extern jmethodID jdk_clsIterator_hasNextID; // boolean iterator.hasNext()
extern jmethodID jdk_clsIterator_nextID; // Object iterator.next()

// UDT methods
extern jfieldID udt_clsCCC_fld_nativeHandleID;


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

extern "C"{

void X_InitClassReference(JNIEnv *env, jclass *classReference,
		const char *className);

jobject X_NewBoolean(JNIEnv *env, bool value);

jobject X_NewInteger(JNIEnv *env, int value);

jobject X_NewLong(JNIEnv* env, int64_t value);

int X_InitSockAddr(sockaddr* sockAddr);

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
		jobject objInetSocketAddress, sockaddr* sockAddr);

// NOTE: only ipv4
jobject X_NewInetAddress(JNIEnv* env, jint address);

// NOTE: ipv4 only
jobject X_NewInetSocketAddress(JNIEnv* env, sockaddr* sockAddr);
bool X_IsSockaddrEqualsInetSocketAddress(JNIEnv* env, sockaddr* sockAddr,
		jobject socketAddress);


void UDT_ThrowExceptionUDT_Message(JNIEnv* env, const jint socketID,
		const char *comment);

}//extern "C"




#endif /* JNIHELPERS_H_ */
