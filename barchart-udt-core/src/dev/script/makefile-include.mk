#
DIR_JNI:=${DIR_BASE}/native/jni
DIR_UDT:=${DIR_BASE}/native/udt/src
DIR_OUT:=${DIR_BASE}/src/main/resources


# compiler
GCC:=g++    #
#
COMP_FLAGS+= -c   -fPIC   -pthread -D_REENTRANT   #   
COMP_FLAGS+= -O2 -finline-functions -fno-strict-aliasing -fno-omit-frame-pointer   #
COMP_FLAGS+= -Wall -Werror -Wno-unused -Wno-parentheses  -fmessage-length=0   #
COMP_FLAGS+= -I"${DIR_INCLUDE}" #    
COMP_FLAGS+= -I"${DIR_UDT}" #    
#
LINK_FLAGS:= -shared   -fPIC   -pthread -D_REENTRANT   -static-libgcc    #

LIB_NAME:=SocketUDT

GCCLIBS:=	# -l stdc++
LIBPATH:=

# INTEL x86

# windows
ifeq "$(OS)" "Windows_NT"
OS_NAME:=windows
CPU_ARCH:=$(PROCESSOR_ARCHITECTURE)
ifeq "$(CPU_ARCH)" "x86"
CPU:=x86
BITS:=32
LIBPATH+= -L "C:/MinGW/lib" -L "C:/MinGW/lib/gcc/mingw32/3.4.5" #
GCCLIBS+= -l gcc -l ws2_32 # -l crtdll  # -l mingw32 # 
GCC:=C:/MinGW/bin/gcc.exe	#
COMP_FLAGS+= -Wl,--kill-at -Wl,--add-stdcall-alias #
endif
ifeq "$(CPU_ARCH)" "AMD64"
CPU:=x86
BITS:=64
$(error $(CPU_ARCH) is not yet supported)
endif
endif

# linux
ifeq "$(shell uname -s)" "Linux"
OS_NAME:=linux
CPU_ARCH:=$(shell uname -m)
ifeq "$(CPU_ARCH)" "i386"
CPU:=x86
BITS:=32
$(error $(CPU_ARCH) is not yet supported)
endif
ifeq "$(CPU_ARCH)" "x86_64"
CPU:=x86
BITS:=64
COMP_FLAGS+= -DLINUX -DAMD64 -DARCH=AMD64 -D_GNU_SOURCE #
COMP_FLAGS+= -I"${DIR_INCLUDE}/linux" #    
endif
endif

# $(error 99, test $(OS_NAME))


# result of detect
ifeq "$(OS_NAME)" ""
$(error 1, can not determine OS_NAME)
endif
ifeq "$(BITS)" ""
$(error 2, can not determine BITS)
endif


# prefix/suffix
ifeq "$(OS_NAME)" "linux"
PREFIX:=lib
LIBEXT:=so
endif
ifeq "$(OS_NAME)" "windows"
PREFIX:=
LIBEXT:=dll
endif

SIGNATURE:=$(OS_NAME)-$(CPU)-$(BITS)
	
TARGET_NAME:=$(PREFIX)$(LIB_NAME)-$(SIGNATURE).$(LIBEXT)
TARGET_PATH:=$(DIR_OUT)/$(TARGET_NAME)

.DEFAULT_GOAL:=info


#OBJ_UDT = md5 common window list buffer packet channel queue ccc cache core api
#OBJ_JNI = com_barchart_udt_SocketUDT

info:
	@echo
	@echo "[info]"
	@echo "PWD='$(PWD)'"
	@echo "OS_NAME='$(OS_NAME)' CPU_ARCH='$(CPU_ARCH)' BITS='$(BITS)'"
	@echo "PREFIX='$(PREFIX)' SIGNATURE='$(SIGNATURE)' LIBEXT='$(LIBEXT)'"
	@echo "TARGET_NAME=$(TARGET_NAME)"

#all-udt:	$(patsubst %, udt-%, $(OBJ_UDT))

#$(OBJ_UDT)	:	
#	@echo "target: $@"
#	$(shell if [ -d $(DIR_TMP) ] ; then exit 0; else mkdir $(DIR_TMP) ; fi ;)
#	$(GCC) $(COMP_FLAGS) "$(DIR_UDT)/$@.cpp" -o "$(DIR_TMP)/$@.o"
	
#$(OBJ_JNI)	:
#	@echo "target: $@"
#	$(GCC) $(COMP_FLAGS) "$(DIR_JNI)/$@.cpp" -o "$(DIR_TMP)/$@.o"
	
#all-udt	:	$(OBJ_UDT)  	
#all-jni	:	$(OBJ_JNI)  	

#all:	#all-udt all-jni
#	@echo
#	@echo "<all>"
#	$(GCC) $(LINK_FLAGS) \
#	"$(DIR_TMP)/%.o" \
#	-Wl,-soname,$(TARGET_NAME) -o $(TARGET_PATH)
	

#
#	$(GCC) $(COMP_FLAGS) -shared \
#	$(LIBPATH) \
#	-Wl,-soname,$(TARGET_NAME) -o $(TARGET_PATH) \
# 	-Wl,--whole-archive $(LIBS_TO_LINK) -Wl,--no-whole-archive \
# 	-Wall -fmessage-length=0 -fPIC -D_REENTRANT -static-libgcc $(GCCLIBS)
#
#	$(GCC) $(COMP_FLAGS)  \


# subverstion status upgate
#	$(shell touch $(TARGET_PATH))
	
#clean:
#	@echo
#	@echo "<clean>"
#	rm -f $(TARGET_PATH)
#	rm -f $(DIR_TMP)/**
 	
 