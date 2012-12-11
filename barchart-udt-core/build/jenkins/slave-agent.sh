#
# Copyright (C) 2009-2012 Barchart, Inc. <http://www.barchart.com/>
#
# All rights reserved. Licensed under the OSI BSD License.
#
# http://www.opensource.org/licenses/bsd-license.php
#

#/bin/sh


#-----------------------------------------------------------------------------
#
#	${mavenStamp}
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
# Required-Start:    $local_fs $remote_fs $syslog
# Required-Stop:     $local_fs $remote_fs $syslog
# Should-Start:      $network $named $time
# Should-Stop:       $network $named $time
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: slave-agent
# Description:       slave-agent
### END INIT INFO
#-----------------------------------------------------------------------------

# use this instead of "echo"
log(){
# one argument to echo
# 	echo "### slave-agent: $1"
# 	echo "### slave-agent: $(date)" >> slave-agent.log
	logger "### slave-agent: $1"
	echo   "### slave-agent: $1" >> slave-agent.log
}

# Resolve the true real path and name of this script without any sym links;
#REAL_PATH="$(dirname  "$(readlink -f -n $0)")"
#REAL_NAME="$(basename "$(readlink -f -n $0)")"

THIS_PATH=$(cd ${0%/*} && echo $PWD/${0##*/})

REAL_PATH=$(dirname $THIS_PATH)
REAL_NAME=$(basename $THIS_PATH)

log "REAL_PATH=$REAL_PATH"


# jenkins working directory defined by script location
cd "$REAL_PATH"


# Get the real fully qualified path to this script
SCRIPT="$REAL_PATH/$REAL_NAME"

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
#################################

# ensure network is available

do_ping(){
	log "network ping test"
	ping 8.8.8.8 -c 1 -i 0.2 -t 100 -W 1 && echo "YES" || echo "NO"
}

IS_ONLINE=do_ping
MAX_CHECKS=30
CHECKS=0

while [ $IS_ONLINE == "NO" ]; do
    IS_ONLINE=do_ping
    CHECKS=$[ $CHECKS + 1 ]
    if [ $CHECKS -gt $MAX_CHECKS ]; then
        break
    fi
done

if [ $IS_ONLINE == "NO" ]; then
	log "network is not available"
    exit 1
fi


#################################


# correction for the usual "think different" in macosx launchd
think(){

    status

    log "waiting system startup"

    sleep 30

    start

    log "waiting system shutdown"

    sleep 3000

}


start(){

  status

  log "START: Jenkins Slave STARTING..."

	URL="https://jenkins.barchart.com/jnlpJars/slave.jar"
	
	SLAVE_LOG="$REAL_PATH/slave.log"
	SLAVE_JAR="$REAL_PATH/slave.jar"
	SLAVE_JNLP="$REAL_PATH/slave-agent.jnlp"

	wget --no-check-certificate "$URL" -O "$SLAVE_JAR"

	JAVA_CMD="java -jar $SLAVE_JAR -jnlpUrl file://$SLAVE_JNLP -noCertificateCheck -slaveLog $SLAVE_LOG" 

	$JAVA_CMD > jenkins.log 2>&1 &

	JAVA_PID=$!

  disown $JAVA_PID

  log "START: Jenkins Slave STARTED; JAVA_PID=$JAVA_PID"

}

stop(){

  status

  log "STOP: Jenkins Slave STOPPING..."

  # TODO kill by pid

  PROC_ID=$( ps -ef | grep jenkins | grep slave | grep -v grep | awk '{ print $2 }' )

  kill $PROC_ID

  log "STOP: Jenkins Slave STOPPED"

}

status(){
	# TODO status by pid
  PROC_COUNT=$( ps aux | grep jenkins | grep slave | grep -v grep | wc -l )
  if [ $PROC_COUNT -gt 0 ]; then
    log "STATUS: Jenkins Slave is RUNNING"
  else
    log "STATUS: Jenkins Slave is STOPPED"
  fi
}

restart(){
  stop
  start
}

# See how we were called.
case "$1" in

install)
  do_install
 ;;

uninstall)
  do_uninstall
 ;;

start)
 start
 ;;

stop)
 stop
 ;;

status)
 status
 ;;

restart)
 restart
 ;;

think)
    think
    ;;

*)
 log $"Usage: $0 {install|uninstall|start|stop|status|restart}"
 exit 1

esac

log "done"

exit 0

