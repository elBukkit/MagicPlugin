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
zip -r -X ../Magic-RP-6-1.zip *
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
zip -r -X ../Magic-potter-RP-6.zip *
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
zip -r -X ../Magic-bending-RP-6.zip *
cd ..

echo "** BUILDING STARS **"

mkdir stars
cd stars
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../stars/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-stars-RP-6.zip *
cd ..

echo "** BUILDING FFA **"

mkdir ffa
cd ffa
mkdir assets
cd assets
cp -R ../../../common/assets/* .
cd ..
cp -R ../../bending/* .
cp -R ../../potter/* .
cp -R ../../stars/* .
cp -R ../../survival/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-ffa-RP-6.zip *
cd ..
