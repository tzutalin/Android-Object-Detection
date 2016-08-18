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
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Create an instance using default instances for vision recognition and detection
 */
public final class VisionClassifierCreator {
    private final static String SCENE_MODEL_PATH = "/sdcard/phone_data/vision_scene/mit/deploy_places205_mem.protxt";
    private final static String SCENE_WIEGHTS_PATH = "/sdcard/phone_data/vision_scene/mit/googlelet_places205_train_iter_2400000.caffemodel";
    private final static String SCENE_MEAN_FILE = null;
    private final static String SCENE_SYNSET_FILE = "/sdcard/phone_data/vision_scene/mit/mit_category_table";

    private final static String DETECT_MODEL_PATH = "/sdcard/phone_data/fastrcnn/deploy.prototxt";
    private final static String DETECT_WIEGHTS_PATH = "/sdcard/phone_data/fastrcnn/caffenet_fast_rcnn_iter_40000.caffemodel";
    private final static String DETECT_MEAN_FILE = "/sdcard/phone_data/fastrcnn/imagenet_mean.binaryproto";
    private final static String DETECT_SYNSET_FILE = "/sdcard/phone_data/fastrcnn/fastrcnn_synset";

    private VisionClassifierCreator() throws InstantiationException {
        throw new InstantiationException("This class is not for instantiation");
    }

    /**
     * Create an instance using a default {@link SceneClassifier} instance
     * @return {@link SceneClassifier instance
     */
    @NonNull
    public static SceneClassifier createSceneClassifier(@NonNull Context context) throws IllegalAccessException {
        return new SceneClassifier(context, SCENE_MODEL_PATH, SCENE_WIEGHTS_PATH, SCENE_MEAN_FILE, SCENE_SYNSET_FILE);
    }

    /**
     * Create an instance using a default {@link ObjectDetector} instance
     * @return {@link ObjectDetector} instance
     */
    @NonNull
    public static ObjectDetector createObjectDetector(@NonNull Context context) throws IllegalAccessException {
        return new ObjectDetector(context, DETECT_MODEL_PATH, DETECT_WIEGHTS_PATH, DETECT_MEAN_FILE, DETECT_SYNSET_FILE);
    }
}
