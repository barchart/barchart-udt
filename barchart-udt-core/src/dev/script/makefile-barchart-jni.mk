#

include	../makefile-include.mk

OBJS = com_barchart_udt_SocketUDT.o

%.o	: %.cpp %.h
	$(GCC) $(COMP_FLAGS) $< -o $@ 

all	:	$(OBJS)
