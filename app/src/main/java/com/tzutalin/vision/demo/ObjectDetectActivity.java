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

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.provider.BigImageCardProvider;
import com.dexafree.materialList.view.MaterialListView;
import com.tzutalin.vision.visionrecognition.ObjectDetector;
import com.tzutalin.vision.visionrecognition.R;
import com.tzutalin.vision.visionrecognition.VisionClassifierCreator;
import com.tzutalin.vision.visionrecognition.VisionDetRet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetectActivity extends Activity {
    private final static String TAG = "ObjectDetectActivity";
    private ObjectDetector mObjectDet;
    // UI
    MaterialListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_object_detect);
        mListView = (MaterialListView) findViewById(R.id.material_listview);
        final String key = Camera2BasicFragment.KEY_IMGPATH;
        String imgPath = getIntent().getExtras().getString(key);
        if (!new File(imgPath).exists()) {
            Toast.makeText(this, "No file path", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        DetectTask task = new DetectTask();
        task.execute(imgPath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_object_detect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==========================================================
    // Tasks inner class
    // ==========================================================
    private class DetectTask extends AsyncTask<String, Void, List<VisionDetRet>> {
        private ProgressDialog mmDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mmDialog = ProgressDialog.show(ObjectDetectActivity.this, getString(R.string.dialog_wait),getString(R.string.dialog_object_decscription), true);
        }

        @Override
        protected List<VisionDetRet> doInBackground(String... strings) {
            final String filePath = strings[0];
            long startTime;
            long endTime;
            Log.d(TAG, "DetectTask filePath:" + filePath);
            if (mObjectDet == null) {
                try {
                    mObjectDet = VisionClassifierCreator.createObjectDetector(getApplicationContext());
                    // TODO: Get Image's height and width
                    mObjectDet.init(0, 0);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            List<VisionDetRet> ret = new ArrayList<>();
            if (mObjectDet != null) {
                startTime = System.currentTimeMillis();
                Log.d(TAG, "Start objDetect");
                ret.addAll(mObjectDet.classifyByPath(filePath));
                Log.d(TAG, "end objDetect");
                endTime = System.currentTimeMillis();
                final double diffTime = (double) (endTime - startTime) / 1000;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ObjectDetectActivity.this, "Take " + diffTime + " second", Toast.LENGTH_LONG).show();
                    }
                });
            }
            File beDeletedFile = new File(filePath);
            if (beDeletedFile.exists()) {
                beDeletedFile.delete();
            } else {
                Log.d(TAG, "file does not exist " + filePath);
            }

            mObjectDet.deInit();
            return ret;
        }

        @Override
        protected void onPostExecute(List<VisionDetRet> rets) {
            super.onPostExecute(rets);
            if (mmDialog != null) {
                mmDialog.dismiss();
            }
            // TODO: Remvoe it
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            String retImgPath = "/sdcard/temp.jpg";
            Bitmap bitmap = BitmapFactory.decodeFile(retImgPath, options);

            Drawable d = new BitmapDrawable(getResources(), bitmap);
            Card card = new Card.Builder(ObjectDetectActivity.this)
                    .withProvider(BigImageCardProvider.class)
                    .setDrawable(d)
                    .endConfig()
                    .build();
            mListView.add(card);
            for (VisionDetRet item : rets) {
                StringBuilder sb = new StringBuilder();
                sb.append(item.getLabel())
                        .append(", Prob:").append(item.getConfidence())
                        .append(" [")
                        .append(item.getLeft()).append(',')
                        .append(item.getTop()).append(',')
                        .append(item.getRight()).append(',')
                        .append(item.getBottom())
                        .append(']');
                Log.d(TAG, sb.toString());

                if (!item.getLabel().equalsIgnoreCase("background")) {
                    card = new Card.Builder(ObjectDetectActivity.this)
                            .withProvider(BigImageCardProvider.class)
                            .setTitle("Detect Result")
                            .setDescription(sb.toString())
                            .endConfig()
                            .build();
                    mListView.add(card);
                }
            }

            File beDeletedFile = new File(retImgPath);
            if (beDeletedFile.exists()) {
                beDeletedFile.delete();
            }

        }
    }
}
