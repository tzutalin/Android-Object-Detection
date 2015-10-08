#!/bin/sh

if [ -d phone_data/ ]
then
    echo "model exists"
else
    ./tools/get_model.py
fi

adb push phone_data/ /sdcard/
rm phone_data.tar

