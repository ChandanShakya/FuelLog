#!/bin/sh

#
# Copyright © 2015-2021 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
#
#   Gradle Wrapper for Gradle 8.1.2
#
#   Important for running:
#
#   (1) You need a JAVA_HOME environment variable set to a JDK installation
#       directory.
#
#   (2) You should verify that the JAVA_HOME points to a compatible JDK version.
#       The minimum compatible JDK version is 8.
#
#   ANY CHANGES TO THE SCRIPTS SHOULD BE REVIEWED ON ALL PLATFORMS.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing / for empty
    [ -h "$app_path" ]
do
    cd "$APP_HOME"
    app_path=$( readlink "$app_path" )
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
APP_HOME=$( cd "${APP_HOME:-./}" > /dev/null && pwd -P ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


#
# Help message
#
print_help () {
    cat << EOF
Set JAVA_HOME to the directory of your JDK installation.

Example:
    export JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre
    export PATH=$JAVA_HOME/bin:$PATH

Then run:
    ./gradlew <task>

For more details see https://docs.gradle.org/8.1.2/userguide/installation.html
EOF
}

#
# Determine the Java command to use to start the JVM.
#
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/bin/java" ] ; then
        JAVACMD=$JAVA_HOME/bin/java
    else
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
      ;;
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)              
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
      ;;
    esac
fi

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$CLASSPATH\"" org.gradle.wrapper.GradleWrapperMain "$@"

exec "$JAVACMD" "$@"
