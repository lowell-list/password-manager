#!/bin/bash

javac -Xlint:deprecation -d out src/*.java
cd out
jar cfe ../PasswordManager.jar PasswordManager *.class
cd ..
