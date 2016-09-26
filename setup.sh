#!/bin/sh

if [ -d phone_data/ ]
then
    echo ".. model exists already ..."
else
    echo ".. Try to download the model ..."
    ./tools/get_model.py
fi

rm phone_data.tar > /dev/null 2>&1
echo "--- Try to push data to sdcard ---"
adb wait-for-device
echo "--- start pushing ---"
adb push phone_data/ /sdcard/

