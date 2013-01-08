#!/bin/bash
#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#-----------------------------------------------------------------------------
#
#	${projectBuildStamp}
#
#-----------------------------------------------------------------------------
# TESTED: redhat(chkconfig)
#
# The following lines are used by the 'chkconfig' init manager.
# 	They should remain commented.
#
# chkconfig:	2 3 4 5		20 80
# description:	slave-agent
#-----------------------------------------------------------------------------
# TESTED: debian(update-rc.d)
#
# The following lines are used by the LSB-compliant init managers.
# 	They should remain commented.
# 	http://refspecs.freestandards.org/LSB_3.1.0/LSB-Core-generic/LSB-Core-generic/facilname.html
#
### BEGIN INIT INFO
# Provides:          slave-agent
# Required-Start:    $network $local_fs $remote_fs $syslog
# Required-Stop:     $network $local_fs $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       slave-agent
# Short-Description: slave-agent
### END INIT INFO
#-----------------------------------------------------------------------------

# use this instead of "echo"
log(){
# one argument to echo
	logger "### slave-agent: $1"
 	echo   "### slave-agent: $1"
	echo   "### slave-agent: $1" >> slave-agent.log
}

# Get the real fully qualified path to this script
SCRIPT=$(realpath $0)

# Resolve the any sym links;
REAL_PATH=$(dirname  $SCRIPT)

log "REAL_PATH=$REAL_PATH"

JAVA_PID_FILE="$REAL_PATH/java.pid"

# jenkins working directory defined by script location
cd "$REAL_PATH"

# wrapper service link:
# 	debian, redhat
WRAPPER_SVC="/etc/init.d/slave-agent"

do_install(){
    ln --symbolic --force "$SCRIPT" "$WRAPPER_SVC"
    if [ $? -eq 0 ] ; then
	log "INSTALL: added: $WRAPPER_SVC"
    else
        log "INSTALL: error: failed to add: $WRAPPER_SVC"
        exit 1
    fi
    chmod 775 "$WRAPPER_SVC"
    chown --silent --recursive "$RUN_AS_USER":root "$REAL_PATH"
}

do_uninstall(){
	rm --force "$WRAPPER_SVC"
    if [ $? -eq 0 ] ; then
	log "UNINSTALL: removed: $WRAPPER_SVC"
    else
        log "UNINSTALL: error: failed to remove: $WRAPPER_SVC"
        exit 1
    fi
}

# correction for the usual "think different" in macosx launchd
do_think(){

    do_status

    do_restart

    log "thinking different"

    sleep 10000

}

do_start(){

	do_status

	log "START: Jenkins Slave STARTING..."

	URL="https://jenkins.barchart.com/jnlpJars/slave.jar"

	while [ true ] ; do
		curl --insecure --silent --url "$URL" --output "slave.jar" && break
		log "download failure URL=$URL [$?]"
		sleep 1
	done

	log "download success URL=$URL"

	JAVA_USER="jenkins"
	JAVA_EXEC="java -jar slave.jar -jnlpUrl file:slave-agent.jnlp -noCertificateCheck -slaveLog slave.log" 

	su "$JAVA_USER" -c "$JAVA_EXEC" > "$JAVA_USER.log" 2>&1 &

	JAVA_PID="$!"

	echo $JAVA_PID > $JAVA_PID_FILE

	disown $JAVA_PID

	log "START: Jenkins Slave STARTED; JAVA_PID=$JAVA_PID"

}

do_stop(){

	do_status

	log "STOP: Jenkins Slave STOPPING..."
	
	kill $(cat $JAVA_PID_FILE)
	
	rm -f $JAVA_PID_FILE
	
	log "STOP: Jenkins Slave STOPPED"

}

do_status(){
	
	if [ -f "$JAVA_PID_FILE" ]; then
		log "STATUS: Jenkins Slave is RUNNING"
	else
		log "STATUS: Jenkins Slave is STOPPED"
	fi

}

do_restart(){
	do_stop
	do_start
}


#
#
#
case "$1" in

	install)
		do_install
		;;
	
	uninstall)
		do_uninstall
		;;
	
	start)
		do_restart
		;;
	
	stop)
		do_stop
		;;
	
	status)
		do_status
		;;
	
	restart)
		do_restart
		;;
	
	think)
	    do_think
	    ;;
	
	*)
		log $"Usage: $0 {install|uninstall|start|stop|status|restart}"
		exit 1

esac

exit 0
