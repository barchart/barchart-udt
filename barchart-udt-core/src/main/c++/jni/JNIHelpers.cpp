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
 * Developers: Andrei Pozolotin, CCob
 *
 * =================================================================================
 */

#include "jni.h"
#include "JNIHelpers.h"

// ### JNI

#define JNI_UPDATE 0 // jni object release with copy
//

// ### JDK references

// JDK classes
jclass jdk_clsBoolean; // java.lang.Boolean
jclass jdk_clsInteger; // java.lang.Integer
jclass jdk_clsLong; // java.lang.Long
jclass jdk_clsInet4Address; // java.net.Inet4Address
jclass jdk_clsInet6Address; // java.net.Inet6Address
jclass jdk_clsInetSocketAddress; // java.net.InetSocketAddress
jclass jdk_clsSocketException; // java.net.SocketException
jclass jdk_clsSet; // java.util.Set
jclass jdk_clsIterator; // java.util.Iterator

// JDK fields
jfieldID isa_InetAddressID; // java.net.InetSocketAddress#addr
jfieldID isa_PortID; // java.net.InetSocketAddress#port
jfieldID ia_AddressID; // java.net.InetAddress#address

// JDK methods
jmethodID jdk_clsBoolean_initID; // new Boolean(boolean x)
jmethodID jdk_clsInteger_initID; // new Integer(int x)
jmethodID jdk_clsLong_initID; // new Long(long x)
jmethodID jdk_clsInet4Address_initID; // new InetAddress()
jmethodID jdk_clsInetSocketAddress_initID; // new InetSocketAddress(InetAddress x)
jmethodID jdk_clsSet_iteratorID; // Iterator set.iterator()
jmethodID jdk_clsSet_addID; // boolean set.add(Object)
jmethodID jdk_clsSet_containsID; // boolean set.contains(Object)
jmethodID jdk_clsIterator_hasNextID; // boolean iterator.hasNext()
jmethodID jdk_clsIterator_nextID; // Object iterator.next()

// UDT methods
jfieldID udt_clsCCC_fld_nativeHandleID;


void X_InitClassReference(JNIEnv *env, jclass *classReference,
		const char *className) {
	CHK_NUL_RET_RET(env, "env");
	CHK_NUL_RET_RET(className, "className");
	CHK_NUL_RET_RET(classReference, "classReference");
	jclass klaz = env->FindClass(className);
	CHK_NUL_RET_RET(klaz, "klaz");
	*classReference = static_cast<jclass>(env->NewGlobalRef((jobject) klaz));
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

// NOTE: ipv4 only
int X_ConvertInetSocketAddressToSockaddr(JNIEnv* env,
		jobject objInetSocketAddress, sockaddr* sockAddr) {

	CHK_NUL_RET_ERR(env, "env");
	CHK_NUL_RET_ERR(sockAddr, "sockAddr");
	CHK_NUL_RET_ERR(objInetSocketAddress, "objInetSocketAddress");

	jobject objInetAddress = env->GetObjectField(objInetSocketAddress,
			isa_InetAddressID);
	CHK_NUL_RET_ERR(objInetAddress, "objInetAddress");

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
	CHK_NUL_RET_ERR(sockAddr, "sockAddr");
	CHK_NUL_RET_ERR(objInetSocketAddress, "objInetSocketAddress");

	jobject objInetAddress = env->GetObjectField(objInetSocketAddress,
			isa_InetAddressID);
	CHK_NUL_RET_ERR(objInetAddress, "objInetAddress");

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
	CHK_NUL_RET_NUL(jdk_clsInet4Address, "jdk_clsInet4Address");

	jobject objInetAddress = env->NewObject(jdk_clsInet4Address,
			jdk_clsInet4Address_initID);

	CHK_NUL_RET_NUL(objInetAddress, "objInetAddress");

	env->SetIntField(objInetAddress, ia_AddressID, address);

	return objInetAddress;

}

// NOTE: ipv4 only
jobject X_NewInetSocketAddress(JNIEnv* env, sockaddr* sockAddr) {

	CHK_NUL_RET_NUL(env, "env");
	CHK_NUL_RET_NUL(sockAddr, "sockAddr");

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;
	jint address = ntohl(sockAddrIn->sin_addr.s_addr);
	jint port = ntohs(sockAddrIn->sin_port);

	jobject objInetAddress = X_NewInetAddress(env, address);
	CHK_NUL_RET_NUL(objInetAddress, "objInetAddress");

	jobject objInetSocketAddress = env->NewObject(jdk_clsInetSocketAddress,
			jdk_clsInetSocketAddress_initID, objInetAddress, port);

	CHK_NUL_RET_NUL(objInetSocketAddress, "objInetSocketAddress");

	return objInetSocketAddress;

}

bool X_IsSockaddrEqualsInetSocketAddress(JNIEnv* env, sockaddr* sockAddr,
		jobject socketAddress) {

	CHK_NUL_RET_FLS(env, "env");
	CHK_NUL_RET_FLS(sockAddr, "sockAddr");
	CHK_NUL_RET_FLS(socketAddress, "socketAddress");

	sockaddr_in* sockAddrIn = (sockaddr_in*) sockAddr;

	jint address1 = ntohl(sockAddrIn->sin_addr.s_addr);
	jint port1 = ntohs(sockAddrIn->sin_port);

	jobject objInetAddress = env->GetObjectField(socketAddress,
			isa_InetAddressID);

	CHK_NUL_RET_FLS(objInetAddress, "objInetAddress");

	jint address2 = env->GetIntField(objInetAddress, ia_AddressID);
	jint port2 = env->GetIntField(socketAddress, isa_PortID);

	if (address1 == address2 && port1 == port2) {
		return true;
	}

	return false;

}
