#!/bin/bash
export JAVA_HOME=$ORACLEJDK_17_0_1_HOME
export MAVEN_HOME=$MAVEN_3_6_3_HOME
export PATH=$MAVEN_HOME/bin:$ORACLEJDK_17_0_1_BIN:$PATH
mkdir -p output
mvn -U clean install -Pdev -Dmaven.test.skip=true || { echo "maven install failed, aborting"; exit 1; }
cp web/target/web-1.0.0-SNAPSHOT.jar output/