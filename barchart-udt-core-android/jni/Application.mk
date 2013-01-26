
# FIXME remove: use instead device provided /system/lib/libstlport.so
APP_STL := stlport_shared

# FIXME remove: fix cast in CCC.cpp
APP_CPPFLAGS += -fpermissive

APP_CPPFLAGS += -fPIC -fexceptions -Wall -Wextra -O2 -fno-strict-aliasing
