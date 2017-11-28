#!/bin/bash
mkdir -p $HOME/footballgame
mkdir -p $HOME/footballgame/.databases
mvn '-Dexec.args=-classpath %classpath launcher.Main 8090' -Dexec.executable=$JAVA_HOME/bin/java -Dexec.classpathScope=runtime clean install exec:exec
