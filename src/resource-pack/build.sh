#!/bin/bash
cd "$( dirname "$0" )"
rm -Rf target
mkdir target
cd target

# New resource packs

echo "** BUILDING SURVIVAL **"

mkdir survival
cd survival
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../survival/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-RP-5.zip *
cd ..

echo "** BUILDING POTTER **"

mkdir potter 
cd potter
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../potter/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-potter-5-RP.zip *
cd ..

echo "** BUILDING BENDING **"

mkdir bending
cd bending
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../bending/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-bending-5-RP.zip *
cd ..

# 4.0 Configs include outdated models/textures

echo "** BUILDING 4.0 SURVIVAL **"

mkdir survival-4
cd survival-4
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cp -R ../../../4.0/common/assets/* .
cd ..
cp -R ../../survival/* .
cp -R ../../4.0/survival/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-RP.zip *
cd ..

echo "** BUILDING 4.0 POTTER **"
mkdir potter-4
cd potter-4
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cp -R ../../../4.0/common/assets/* .
cd ..
cp -R ../../potter/* .
cp -R ../../4.0/potter/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-potter-RP.zip *
cd ..

echo "** BUILDING 4.0 BENDING **"
mkdir bending-4
cd bending-4
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cp -R ../../../4.0/common/assets/* .
cd ..
cp -R ../../bending/* .
cp -R ../../4.0/bending/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-bending-RP.zip *
cd ..