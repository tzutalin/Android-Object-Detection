/*
*  Copyright (C) 2015 TzuTaLin
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.tzutalin.vision.visionrecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Identifies and locate the specified objects in a
 * {@link android.graphics.Bitmap} graphic object.
 */
public class ObjectDetector extends CaffeClassifier <List<VisionDetRet>>{
    private static final String TAG = "ObjectDetector";
    private ByteBuffer _handler = null;

    /**
     * Creates a ObjectDetector, configured with its model path, trained weights, etc.
     * These parameters cannot be changed once the object is constructed.
     * @param context Context
     * @param modelPath Caffe's model
     * @param wieghtsPath Caffe's trained wieght
     * @param manefile The file path of the image image
     * @param synsetFile The file path to load label's titles
     */
    public ObjectDetector(Context context, String modelPath, String wieghtsPath, String manefile, String synsetFile) throws IllegalAccessException {
        super(context, modelPath, wieghtsPath, manefile, synsetFile);
        if (new File(mModelPath).exists() == false ||
                new File(mWeightsPath).exists() == false ||
                new File(mSynsetPath).exists() == false ) {
            throw new IllegalAccessException(" Cannot find model");
        }
    }

    @Override
    public void init(int imgWidth, int imgHeight) {
        super.init(imgWidth, imgHeight);
        jniLoadModel(mModelPath, mWeightsPath, mMeanPath, mSynsetPath);
    }

    @Override
    public void deInit() {
        super.deInit();
        jniRelease();
    }
    /**
     * Detect and locate objects according to the given image path
     * @param imgPath image path
     * @return The list of the result {@link VisionDetRet} which objected are detected
     * @throws IllegalArgumentException if the Bitmap dimensions don't match
     *               the dimensions defined at initialization or the given array
     *               is not sized equal to the width and hegiht defined at initialization
     */
    @Override
    public List<VisionDetRet> classifyByPath(String imgPath) {
        // TODO : If file path didn't exist
        List<VisionDetRet> ret = new ArrayList<VisionDetRet>();

       /* BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
        return classify(bitmap);*/
        int numObjs = jniClassifyImgByPath(imgPath);
        for (int i = 0; i != numObjs; i++) {
            VisionDetRet det = new VisionDetRet();
            int success = jniGetDetRet(det, i);
            if (success >= 0)
                ret.add(det);

        }
        return ret;
    }

    /**
     * Detect and locate objects according to the given bitmap
     * @param bitmap bitmap object {@link android.graphics.Bitmap} graphic object.
     * @return The list of the result {@link VisionDetRet} which objected are detected
     * @throws IllegalArgumentException if the Bitmap dimensions don't match
     *               the dimensions defined at initialization or the given array
     *               is not sized equal to the width and hegiht defined at initialization
     */
    @Override
    public List<VisionDetRet> classify(Bitmap bitmap) {
        /*if (bitmap.getWidth() != mImgWidth || bitmap.getHeight() != mImgHeight) {
            throw new IllegalArgumentException(
                    "bitmap size doesn't match initialization");
        }*/
        List<VisionDetRet> ret = new ArrayList<VisionDetRet>();
        storeBitmap(bitmap);
        int numObjs = jniClassifyBitmap(_handler);
        for (int i = 0; i != numObjs; i++) {
            VisionDetRet det = new VisionDetRet();
            int success = jniGetDetRet(det, i);
            android.util.Log.d(TAG , det.getLabel());
            if (success >= 0)
                ret.add(det);
        }
        freeBitmap();
        return ret;
    }

    private void storeBitmap(final Bitmap bitmap) {
        if (_handler != null)
            freeBitmap();
        _handler = jniStoreBitmapData(bitmap);
    }

    private void freeBitmap() {
        if (_handler == null)
            return;
        jniFreeBitmapData(_handler);
        _handler = null;
    }

    protected native int jniLoadModel(String modelPath, String weightsPath, String meanfilePath, String sysetPath);

    protected native int jniRelease();

    protected native int jniClassifyImgByPath(String imgPath);

    private native int jniClassifyBitmap(ByteBuffer handler);

    private native int jniGetDetRet(VisionDetRet det, int index);

    // Bitmap
    private native ByteBuffer jniStoreBitmapData(Bitmap bitmap);

    private native void jniFreeBitmapData(ByteBuffer handler);
}
