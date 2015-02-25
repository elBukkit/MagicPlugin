#!/bin/bash
cd "$( dirname "$0" )"
rm -Rf target
mkdir target
cd target
mkdir survival
cd survival
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../survival/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-RP.zip *
cd ..

mkdir potter 
cd potter
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../potter/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-potter-RP.zip *
cd ..
