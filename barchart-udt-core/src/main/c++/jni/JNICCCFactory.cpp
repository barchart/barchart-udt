/*
 * JNICCCFactory.cpp
 *
 *  Created on: 18 Dec 2012
 *      Author: ceri.coburn
 */

#include "JNICCCFactory.h"
#include "JNICCC.h"
#include "JNIHelpers.h"

jclass 		JNICCCFactory::_cls_FactoryInterfaceUDT = NULL;
jmethodID 	JNICCCFactory::_udt_clsFactoryInterfaceUDT_create = NULL;
jmethodID 	JNICCCFactory::_udt_clsFactoryInterfaceUDT_cloneFactory = NULL;

bool JNICCCFactory::initJNITypes(JNIEnv* env){

	if(_cls_FactoryInterfaceUDT != NULL)
		return true;

	X_InitClassReference(env,&_cls_FactoryInterfaceUDT,"com/barchart/udt/FactoryInterfaceUDT");

	if(_cls_FactoryInterfaceUDT==NULL)
		return false;

	_udt_clsFactoryInterfaceUDT_cloneFactory = env->GetMethodID(_cls_FactoryInterfaceUDT, "cloneFactory", "()Lcom/barchart/udt/FactoryInterfaceUDT;");
	_udt_clsFactoryInterfaceUDT_create = env->GetMethodID(_cls_FactoryInterfaceUDT, "create", "()Lcom/barchart/udt/CCC;");

	return true;
}

JNIEnv* JNICCCFactory::AttachToJVM()
{
	JNIEnv* env = NULL;

	jint result = _javaVM->GetEnv(reinterpret_cast<void**>(&env),JNI_VERSION_1_4);

	if(result == JNI_EDETACHED)
		_javaVM->AttachCurrentThread(reinterpret_cast<void**>(&env),NULL);

	return env;
}


JNICCCFactory::JNICCCFactory(JNIEnv* env, jobject factoryUDT) {

	initJNITypes(env);

	if(factoryUDT==NULL)
		;//TODO create new exception

	env->GetJavaVM(&_javaVM);

	//We need to create a global JNI ref since
	//we are storing the jobject as a member variable
	//for use later on
	_factoryUDT = env->NewGlobalRef(factoryUDT);
}

JNICCCFactory::~JNICCCFactory(){

	JNIEnv* env = AttachToJVM();

	if(_factoryUDT != NULL)
		env->DeleteLocalRef(_factoryUDT);
}

CCC* JNICCCFactory::create(){

	JNIEnv* env = AttachToJVM();

	jobject objCCC = env->CallObjectMethod(_factoryUDT,_udt_clsFactoryInterfaceUDT_create);

	if(objCCC==NULL && !env->ExceptionCheck() ){
		UDT_ThrowExceptionUDT_Message(env, 0, "failed to allocate CCC class via com.barchart.udt.FactoryInterfaceUDT");
	}

	return reinterpret_cast<CCC*> ((intptr_t) env->GetLongField(objCCC,udt_clsCCC_fld_nativeHandleID));
}

CCCVirtualFactory* JNICCCFactory::clone(){

	JNIEnv* env = AttachToJVM();

	jobject objFactoryUDT = env->CallObjectMethod(_factoryUDT,_udt_clsFactoryInterfaceUDT_cloneFactory);

	return new JNICCCFactory(env,objFactoryUDT);
}

