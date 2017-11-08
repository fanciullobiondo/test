#!/bin/bash
mkdir -p $HOME/databases
mvn '-Dexec.args=-classpath %classpath launcher.Main 8090 '$HOME'/databases' -Dexec.executable=/usr/java/jdk1.8.0/bin/java -Dexec.classpathScope=runtime exec:exec
