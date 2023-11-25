#!/bin/bash

rm -rf out
mkdir out
javac -cp ".:lib/gson-2.10.1.jar" -Xlint:deprecation -d out src/*.java
cd out
jar xf ../lib/gson-2.10.1.jar
jar cfe ../PasswordManager.jar PasswordManager *.class com/google/gson
cd ..
