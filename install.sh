#!/bin/bash
mkdir -p $HOME/databases
mvn '-Dexec.args=-classpath %classpath launcher.Main 8090 '$HOME'/databases' -Dexec.executable=$JAVA_HOME/bin/java -Dexec.classpathScope=runtime clean install exec:exec
