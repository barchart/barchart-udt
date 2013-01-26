
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := barchart-udt-core-android

LOCAL_CFLAGS    := -DLINUX

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

FILE_LIST := $(wildcard $(LOCAL_PATH)/include/*.cpp)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_SHARED_LIBRARY)

include $(ANDROID_MAVEN_PLUGIN_MAKEFILE)
