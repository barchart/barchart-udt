# must start this make in maven basedir
DIR_BASE:=$(PWD)
DIR_INCLUDE:=${DIR_BASE}/include
DIR_MAKE:=${DIR_BASE}/target/DIR_MAKE
#
$(warning DIR_BASE : ${DIR_BASE}")
$(warning DIR_MAKE : ${DIR_MAKE}")
$(warning DIR_INCLUDE : ${DIR_INCLUDE}")

$(shell if [ -d $(DIR_MAKE) ] ; then exit 0; else mkdir $(DIR_MAKE) ; fi ;)

include	${DIR_BASE}/native/makefile-include.mk

DIRS = ${DIR_JNI} # ${DIR_UDT}
TARGETS = all clean

$(TARGETS): % : $(patsubst %, %.%, $(DIRS))

$(foreach TGT, $(TARGETS), $(patsubst %, %.$(TGT), $(DIRS))):
	$(MAKE) --directory $(subst ., , $@) --makefile makefile-barchart.mk \
		DIR_BASE=${DIR_BASE} \
		DIR_INCLUDE=$(DIR_INCLUDE) \
		DIR_MAKE=$(DIR_MAKE) \ 
