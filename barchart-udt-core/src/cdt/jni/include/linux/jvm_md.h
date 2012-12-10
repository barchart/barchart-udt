/*
 * @(#)jvm_md.h	1.20 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#ifndef _JAVASOFT_JVM_MD_H_
#define _JAVASOFT_JVM_MD_H_

/*
 * This file is currently collecting system-specific dregs for the
 * JNI conversion, which should be sorted out later.
 */

#include <dirent.h>		/* For DIR */
#include <sys/param.h>		/* For MAXPATHLEN */
#include <unistd.h>		/* For F_OK, R_OK, W_OK */

#define JNI_ONLOAD_SYMBOLS   {"JNI_OnLoad"}
#define JNI_ONUNLOAD_SYMBOLS {"JNI_OnUnload"}

#define JNI_LIB_PREFIX "lib"
#define JNI_LIB_SUFFIX ".so"

#define JVM_MAXPATHLEN MAXPATHLEN

#define JVM_R_OK    R_OK
#define JVM_W_OK    W_OK
#define JVM_X_OK    X_OK
#define JVM_F_OK    F_OK

/*
 * File I/O
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/signal.h>

/* O Flags */

#define JVM_O_RDONLY     O_RDONLY
#define JVM_O_WRONLY     O_WRONLY
#define JVM_O_RDWR       O_RDWR
#define JVM_O_O_APPEND   O_APPEND
#define JVM_O_EXCL       O_EXCL
#define JVM_O_CREAT      O_CREAT
#define JVM_O_DELETE     0x10000

/* Signals */

#define JVM_SIGINT     SIGINT
#define JVM_SIGTERM    SIGTERM


#endif /* !_JAVASOFT_JVM_MD_H_ */
