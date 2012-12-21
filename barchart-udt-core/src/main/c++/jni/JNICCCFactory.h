/*
 * JNICCCFactory.h
 *
 *  Created on: 18 Dec 2012
 *      Author: ceri.coburn
 */

#ifndef JNICCCFACTORY_H_
#define JNICCCFACTORY_H_

#include "ccc.h"
#include <jni.h>

class JNICCCFactory: public CCCVirtualFactory {

private:

	static jclass  _cls_FactoryInterfaceUDT;
	static jmethodID _udt_clsFactoryInterfaceUDT_create;
	static jmethodID _udt_clsFactoryInterfaceUDT_cloneFactory;

	JavaVM* _javaVM;
	jobject _factoryUDT;

	static bool initJNITypes(JNIEnv* env);

	JNIEnv* AttachToJVM();

public:
	JNICCCFactory(JNIEnv* env, jobject factoryUDT);
	virtual ~JNICCCFactory();

	CCC* create();

	CCCVirtualFactory* clone();
};

#endif /* JNICCCFACTORY_H_ */
