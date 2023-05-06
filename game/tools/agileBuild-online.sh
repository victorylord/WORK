#!/bin/bash
export JAVA_HOME=/home/scmtools/buildkit/java/jdk-1.8-8u20
export MAVEN_HOME=/home/scmtools/buildkit/maven/apache-maven-3.3.9
export PATH=${JAVA_HOME}/bin:${MAVEN_HOME}/bin:$PATH
mkdir -p output
mvn -U clean install -online -Dmaven.test.skip=true || { echo "maven install failed, aborting"; exit 1; }
cp web/target/web-1.0.0-SNAPSHOT.jar output/

