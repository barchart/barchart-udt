#!/bin/bash

#
# ARM-RPI build environment
#
# https://github.com/barchart/barchart-udt/issues/2
#

#
# location of this script
#
SCRIPT=$(realpath $0)
REAL_PATH=$(dirname  $SCRIPT)

#
# kernel from
# http://xecdesign.com/downloads/linux-qemu/kernel-qemu
#
KERNEL="$REAL_PATH/2013-01-03-kernel-qemu.bin"

#
# system image with java, jenkins, maven, git 
#
SYSTEM="$REAL_PATH/2012-12-16-wheezy-raspbian.img"

#
# QEMU emulator version 1.0.50 (Debian 1.0.50-2012.03-0ubuntu2.1)
#
QEMU_HOME="/usr/bin"

#
# QEMU emulator version 1.3.0
# custom v 1.3.0 
# http://wiki.qemu.org/Download 
#
#QEMU_HOME="/usr/local/bin"

#
# available memory
#
RAM="256"

"$QEMU_HOME/qemu-system-arm" \
	-M versatilepb \
	-cpu arm1176 \
	-kernel $KERNEL \
	-hda $SYSTEM \
	-m $RAM \
	-redir tcp:22222::22 \
	-no-reboot \
	-serial stdio \
	-curses \
	-append "root=/dev/sda2 panic=1" \
	&
