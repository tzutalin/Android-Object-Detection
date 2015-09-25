package com.tzutalin.vision.visionrecognition;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by darrenl on 2015/9/10.
 */
public class Utils {

    public static Map<String, Float> sortPrediction(String[] synsets, float[] propArray) {
        HashMap<String, Float> map = new HashMap<>();
        if (propArray != null) {
            for (int i = 0; i != synsets.length; i++) {
                map.put(synsets[i], propArray[i]);
            }
        }

        return sortByValue(map);

    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
