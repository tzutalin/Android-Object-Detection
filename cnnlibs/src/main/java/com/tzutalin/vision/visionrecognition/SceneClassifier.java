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
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Identifies the current scene in a
 * {@link android.graphics.Bitmap} graphic object.
 */
public class SceneClassifier extends CaffeClassifier<List<VisionDetRet>> {
    private static final String TAG = "SceneClassifier";
    private static final int MODEL_DIM = 224;
    private ByteBuffer _handler;

    /**
     * Creates a SceneClassifier, configured with its model path, trained weights, etc.
     * These parameters cannot be changed once the object is constructed.
     *
     * @param context          Context
     * @param sceneModelPath   Caffe's model
     * @param sceneWieghtsPath Caffe's trained wieght
     * @param sceneManefile    The file path of the image image
     * @param sceneSynsetFile  The file path to load label's titles
     */
    public SceneClassifier(Context context, String sceneModelPath, String sceneWieghtsPath, String sceneManefile, String sceneSynsetFile) throws IllegalAccessException {
        super(context, sceneModelPath, sceneWieghtsPath, sceneManefile, sceneSynsetFile);
        if (!new File(mModelPath).exists() ||
                !new File(mWeightsPath).exists() ||
                !new File(mSynsetPath).exists() ) {
            throw new IllegalAccessException("SceneClassifier cannot find model");
        }
    }

    /**
     * Recognize the scene according to the given image path
     *
     * @param imgPath image path
     * @return The list of the result {@link VisionDetRet} which scenes are recognized
     * @throws IllegalArgumentException if the Bitmap dimensions don't match
     *                                  the dimensions defined at initialization or the given array
     *                                  is not sized equal to the width and hegiht defined at initialization
     */
    @Override
    public List<VisionDetRet> classifyByPath(String imgPath) {
        List<VisionDetRet> ret = new ArrayList<>();

        if (TextUtils.isEmpty(imgPath) || !new File(imgPath).exists()) {
            Log.e(TAG, "classifyByPath. Invalid Input path");
            return ret;
        }

        float[] propArray = jniClassifyImgByPath(imgPath);
        if (propArray != null) {
            Map<String, Float> sortedMap = Utils.sortPrediction(mSynsets, propArray);
            int kSize = 10;
            for (Map.Entry<String, Float> sortedmapEntry : sortedMap.entrySet()) {
                VisionDetRet det = new VisionDetRet(sortedmapEntry.getKey(), sortedmapEntry.getValue(), 0, 0, 0, 0);
                ret.add(det);
                if (kSize == ret.size())
                    break;
            }
        }

        return ret;
    }

    /**
     * Recognize the scene according to the given bitmap
     *
     * @param bitmap bitmap object {@link android.graphics.Bitmap} graphic object.
     * @return The list of the result {@link VisionDetRet} which scenes are recognized
     * @throws IllegalArgumentException if the Bitmap dimensions don't match
     *                                  the dimensions defined at initialization or the given array
     *                                  is not sized equal to the width and hegiht defined at initialization
     */
    @Override
    public List<VisionDetRet> classify(Bitmap bitmap) {
        List<VisionDetRet> ret = new ArrayList<>();

        // Check input
        if (bitmap == null) {
            Log.e(TAG, "classify. Invalid Input bitmap");
            return ret;
        }

        storeBitmap(bitmap);

        float[] propArray = jniClassifyBitmap(_handler);
        if (propArray != null) {
            Map<String, Float> sortedmap = Utils.sortPrediction(mSynsets, propArray);
            int kSize = 10;
            for (Map.Entry<String, Float> sortedMapEntry : sortedmap.entrySet()) {
                VisionDetRet det = new VisionDetRet(sortedMapEntry.getKey(), sortedMapEntry.getValue(), 0, 0, 0, 0);
                ret.add(det);
                if (kSize == ret.size())
                    break;
            }
        }

        freeBitmap();
        return ret;
    }

    @Override
    public void init(int imgWidth, int imgHeight) {
        super.init(imgWidth, imgHeight);
        jniLoadModel(mModelPath, mWeightsPath);
        jniSetInputModelDim(MODEL_DIM, MODEL_DIM);
    }

    @Override
    public void deInit() {
        super.deInit();
        jniRelease();
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

    protected native int jniLoadModel(String modelPath, String weightsPath);

    protected native int jniSetInputModelDim(int width, int height);

    protected native int jniRelease();

    protected native float[] jniClassifyImgByPath(String imgPath);

    private native float[] jniClassifyBitmap(ByteBuffer handler);

    // Bitmap
    private native ByteBuffer jniStoreBitmapData(Bitmap bitmap);

    private native void jniFreeBitmapData(ByteBuffer handler);

    private native Bitmap jniGetBitmapFromStoredBitmapData(ByteBuffer handler);
}
