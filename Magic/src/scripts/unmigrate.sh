#!/bin/bash
for f in *.dat.migrated; do 
    mv -- "$f" "${f%.dat.migrated}.dat"
done