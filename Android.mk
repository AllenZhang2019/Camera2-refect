#ifneq ($(MTK_BSP_PACKAGE) , yes)
ifneq ($(filter 2 3, $(MTK_CAMERA_APP_VERSION)),)
LOCAL_ROOT_PATH:= $(call my-dir)

include $(LOCAL_ROOT_PATH)/common/src/com/mediatek/camera/common/jpeg/jni_jpegdecoder/Android.mk
include $(LOCAL_ROOT_PATH)/common/src/com/mediatek/camera/common/jpeg/encoder/jni_jpegencoder/Android.mk
include $(LOCAL_ROOT_PATH)/common/Android.mk
include $(LOCAL_ROOT_PATH)/host/Android.mk
include $(LOCAL_ROOT_PATH)/portability/Android.mk
include $(LOCAL_ROOT_PATH)/tests/Android.mk
include $(LOCAL_ROOT_PATH)/feature/setting/matrixdisplay/matrixdisplay_ext/Android.mk
include $(LOCAL_ROOT_PATH)/testscat/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/pluginroot/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/panorama/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/pip/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/vsdof/Android.mk
#include $(LOCAL_ROOT_PATH)/feature/mode/slowmotion/Android.mk
endif
#endif
