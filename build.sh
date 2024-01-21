#!/bin/bash

# Replace {GIT_COMMIT_HASH} with the current commit hash in the manifest
cp ./src/META-INF/MANIFEST.MF.template ./src/META-INF/MANIFEST.MF
commit_hash=$(git rev-parse --short HEAD)
sed -i '' "s/{GIT_COMMIT_HASH}/$commit_hash/g" ./src/META-INF/MANIFEST.MF

# Build the jar
rm -rf out
mkdir out
javac -cp ".:lib/gson-2.10.1.jar" -Xlint:deprecation -d out src/*.java
cd out
jar xf ../lib/gson-2.10.1.jar
jar cmf ../src/META-INF/MANIFEST.MF ../PasswordManager.jar *.class com/google/gson
cd ..

# Clean up
rm ./src/META-INF/MANIFEST.MF