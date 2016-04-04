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

package com.tzutalin.vision.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.tzutalin.vision.visionrecognition.R;
import com.tzutalin.vision.visionrecognition.SceneClassifier;
import com.tzutalin.vision.visionrecognition.VisionClassifierCreator;
import com.tzutalin.vision.visionrecognition.VisionDetRet;
import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.provider.BigImageCardProvider;
import com.dexafree.materialList.view.MaterialListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SceneRecognitionActivity extends Activity {
    private final static String TAG = "SceneRecognitionActivity";
    // UI
    MaterialListView mListView;
    private SceneClassifier mClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_scene_recognition);
        mListView = (MaterialListView) findViewById(R.id.material_listview);
        final String key = Camera2BasicFragment.KEY_IMGPATH;
        String imgPath = getIntent().getExtras().getString(key);
        if (!new File(imgPath).exists()) {
            Toast.makeText(this, "No file path", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        PredictTask task = new PredictTask();
        task.execute(imgPath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bm = BitmapFactory.decodeFile(imgPath, options);
        Drawable d = new BitmapDrawable(getResources(), bm);

        Card card = new Card.Builder(SceneRecognitionActivity.this)
                .withProvider(BigImageCardProvider.class)
                .setDescription("Input image")
                .setDrawable(d)
                .endConfig()
                .build();
        mListView.add(card);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClassifier != null) {
            mClassifier.deInit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scene_recognition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==========================================================
    // Tasks inner class
    // ==========================================================
    private class PredictTask extends AsyncTask<String, Void, List<VisionDetRet>> {
        private ProgressDialog mmDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mmDialog = ProgressDialog.show(SceneRecognitionActivity.this, getString(R.string.dialog_wait),getString(R.string.dialog_scene_decscription), true);
        }

        @Override
        protected List<VisionDetRet> doInBackground(String... strings) {
            initCaffeMobile();
            long startTime;
            long endTime;
            final String filePath = strings[0];
            List<VisionDetRet> rets = new ArrayList<>();
            Log.d(TAG, "PredictTask filePath:" + filePath);
            if (mClassifier != null) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                Log.d(TAG, "format:" + options.inPreferredConfig);
                Bitmap bitmapImg = BitmapFactory.decodeFile(filePath, options);
                startTime = System.currentTimeMillis();
                rets.addAll(mClassifier.classify(bitmapImg));

                endTime = System.currentTimeMillis();
                final double diffTime = (double) (endTime - startTime) / 1000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SceneRecognitionActivity.this, "Take " + diffTime + " second", Toast.LENGTH_LONG).show();
                    }
                });
            }
            File beDeletedFile = new File(filePath);
            if (beDeletedFile.exists()) {
                beDeletedFile.delete();
            } else {
                Log.d(TAG, "file does not exist " + filePath);
            }
            return rets;
        }

        @Override
        protected void onPostExecute(List<VisionDetRet> rets) {
            super.onPostExecute(rets);
            if (mmDialog != null) {
                mmDialog.dismiss();
            }

            ArrayList<String> items = new ArrayList<>();
            for (VisionDetRet each : rets) {
                items.add("[" + each.getLabel() + "] Prob: " + each.getConfidence());
            }

            int count = 0;
            for (String item : items) {
                count++;
                Card card = new Card.Builder(SceneRecognitionActivity.this)
                        .withProvider(BigImageCardProvider.class)
                        .setTitle("Top " + count)
                        .setDescription(item)
                        .endConfig()
                        .build();
                mListView.add(card);
            }
        }
    }
    // ==========================================================
    // Private methods
    // ==========================================================
    private void initCaffeMobile() {
        if (mClassifier == null) {
            try {
                mClassifier = VisionClassifierCreator.createSceneClassifier(getApplicationContext());
                Log.d(TAG, "Start Load model");
                // TODO : Fix it
                mClassifier.init(224,224);  // init once
                Log.d(TAG, "End Load model");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
