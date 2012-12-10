# compiler
GCC:=gcc
GCCFLAGS:=

LIB_NAME1:=UDT
LIB_NAME2:=UDT-wrapper
LIB_NAME:=SocketUDT

GCCLIBS:= -l stdc++ #
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
GCC:=C:/MinGW/bin/gcc.exe
GCCFLAGS:= -Wl,--kill-at -Wl,--add-stdcall-alias #
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
endif
ifeq "$(CPU_ARCH)" "x86_64"
CPU:=x86
BITS:=64
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

ARCV_NAME1:=$(LIB_NAME1)-$(SIGNATURE).a
ARCV_NAME2:=$(LIB_NAME2)-$(SIGNATURE).a

PATH_NAME1:="../sourceforge-udt/build-$(SIGNATURE)/$(ARCV_NAME1)"
PATH_NAME2:="../sourceforge-udt-wrapper/build-$(SIGNATURE)/$(ARCV_NAME2)"

LIBS_TO_LINK:=$(PATH_NAME1) $(PATH_NAME2) 

OUT_DIR:="./src/main/resources"

TARGET_NAME:=$(PREFIX)$(LIB_NAME)-$(SIGNATURE).$(LIBEXT)

TARGET_PATH:=$(OUT_DIR)/$(TARGET_NAME)

.DEFAULT_GOAL:=info

info:
	@echo
	@echo "<info>"
	@echo "PWD='$(PWD)'"
	@echo "OS_NAME='$(OS_NAME)'CPU_ARCH='$(CPU_ARCH)' BITS='$(BITS)'"
	@echo "PREFIX='$(PREFIX)' SIGNATURE='$(SIGNATURE)' LIBEXT='$(LIBEXT)'"
	@echo "TARGET_NAME=$(TARGET_NAME)"

all:
	@echo
	@$(MAKE) clean
	@echo "<all>"
# compile
	$(GCC) $(GCCFLAGS) -shared \
	$(LIBPATH) \
	-Wl,-soname,$(TARGET_NAME) -o $(TARGET_PATH) \
 	-Wl,--whole-archive $(LIBS_TO_LINK) -Wl,--no-whole-archive \
 	-Wall -fmessage-length=0 -fPIC -D_REENTRANT -static-libgcc $(GCCLIBS)
# subverstion status upgate
	$(shell touch $(TARGET_PATH))
	
clean:
	@echo
	@echo "<clean>"
	rm -f $(TARGET_PATH)
	 	
