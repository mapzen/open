LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
 
LOCAL_MODULE    := vtm-jni
LOCAL_C_INCLUDES := . 
 
LOCAL_CFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -Wall -std=c99 -O2 -ffast-math
LOCAL_CPPFLAGS := $(LOCAL_C_INCLUDES:%=-I%) -O2 -Wall -D__ANDROID__ -Wall -std=c99 -O2 -ffast-math
LOCAL_LDLIBS := -lm -llog
LOCAL_ARM_MODE  := arm
 
LOCAL_SRC_FILES := tessellate/tessellate.c\
	tessellate/dict.c\
	tessellate/normal.c\
	tessellate/TessellateJni.c\
	tessellate/priorityq.c\
	tessellate/geom.c\
	tessellate/mesh.c\
	tessellate/render.c\
	tessellate/memalloc.c\
	tessellate/tess.c\
	tessellate/sweep.c\
	tessellate/tessmono.c\
	gl/utils.c
 
include $(BUILD_SHARED_LIBRARY)
