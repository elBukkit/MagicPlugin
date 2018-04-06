#!/bin/bash
cd "$( dirname "$0" )"
rm -Rf target
mkdir target
cd target

# New resource packs

echo "** BUILDING DEFAULT **"

mkdir default
cd default
cp -R ../../default/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-RP-8.zip *
cd ..

echo "** BUILDING DEFAULT-Skulls **"

mkdir default-skulls
cd default-skulls
mkdir assets
cd assets
cp -R ../../../skulls/assets/* .
cd ..
cp -R ../../default/* .
rm -R assets/minecraft/textures/items/spells
rm -R assets/minecraft/textures/items/brushes
rm -R assets/minecraft/models/item/spells
rm -R assets/minecraft/models/item/spells_disabled
rm -R assets/minecraft/models/item/brushes
rm assets/minecraft/models/item/diamond_axe.json
rm assets/minecraft/models/item/diamond_hoe.json
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-skulls-RP-8.zip *
cd ..

echo "** BUILDING SKULLS **"

mkdir skulls
cd skulls
mkdir assets
cp -R ../../skulls/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../flat-skulls.zip *
cd ..

echo "** BUILDING PAINTERLY **"

mkdir painterly
cd painterly
cp -R ../../default/* .
cp -R ../../painterly/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-painterly-RP-8.zip *
cd ..

echo "** BUILDING POTTER **"

mkdir potter 
cd potter
cp -R ../../default/* .
cp -R ../../potter/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-potter-RP-8.zip *
cd ..

echo "** BUILDING WAR **"

mkdir war
cd war
cp -R ../../war/* .

sed -e '$ d' ../../war/assets/minecraft/sounds.json > assets/minecraft/sounds.json
echo , >> assets/minecraft/sounds.json
tail -n +2 ../../war/assets/minecraft/sound-overrides.json >> assets/minecraft/sounds.json
rm assets/minecraft/sound-overrides.json
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-war-RP-8.zip *
cd ..

echo "** BUILDING STARS **"

mkdir stars
cd stars
cp -R ../../default/* .
cp -R ../../stars/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-stars-RP-8.zip *
cd ..

echo "** BUILDING ALL **"

mkdir all
cd all
cp -R ../../default/* .
cp -R ../../war/assets/minecraft/sounds/* assets/minecraft/sounds/
cp -R ../../war/assets/minecraft/models/item/* assets/minecraft/models/item/
cp -R ../../war/assets/minecraft/textures/misc assets/minecraft/textures/
cp -R ../../war/assets/minecraft/textures/items/custom/* assets/minecraft/textures/items/custom/
sed -e '$ d' ../../default/assets/minecraft/sounds.json > assets/minecraft/sounds.json
echo , >> assets/minecraft/sounds.json
tail -n +2 ../../war/assets/minecraft/sounds.json >> assets/minecraft/sounds.json
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-all-RP-8.zip *
cd ..

echo "** BUILDING AJ **"

mkdir aj
cd aj
cp -R ../../default/* .
cp -R ../../aj/* .
find . -name ".DS_Store" -type f -delete
zip -r -X ../Magic-aj.zip *
cd ..
