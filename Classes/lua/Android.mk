# File: Android.mk
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := lua5.4
LOCAL_SRC_FILES := lapi.c \
                   lcode.c \
                   lctype.c \
                   ldebug.c \
                   ldo.c \
                   ldump.c \
                   lfunc.c \
                   lgc.c \
                   llex.c \
                   lmem.c \
                   lobject.c \
                   lopcodes.c \
                   lparser.c \
                   lstate.c \
                   lstring.c \
                   ltable.c \
                   ltm.c \
                   lundump.c \
                   lvm.c \
                   lzio.c \
                   lauxlib.c \
                   lbaselib.c \
                   lcorolib.c \
                   ldblib.c \
                   liolib.c \
                   lmathlib.c \
                   loadlib.c \
                   loslib.c \
                   lstrlib.c \
                   ltablib.c \
                   lutf8lib.c \
                   linit.c
LOCAL_CFLAGS    := -DLUA_USE_POSIX -O2 -Wall -D"getlocaledecpoint()='.'" -Wno-psabi -fexceptions -DLUA_COMPAT_5_3
#LOCAL_CPP_EXTENSION := .c
include $(BUILD_STATIC_LIBRARY)

