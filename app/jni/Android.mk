LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
include C:\Users\craigdev\Development\no-camera-opencv\OpenCV-android-sdk/sdk/native/jni/OpenCV.mk
LOCAL_MODULE := opencvsample
LOCAL_SRC_FILES := sample.cpp
include $(BUILD_SHARED_LIBRARY)