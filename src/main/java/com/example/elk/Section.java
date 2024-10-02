package com.example.elk;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

class Section extends Entity {
    final Map<String, Map<Integer, String>> attributes = new TreeMap<>();
    private Map<Integer, String> attValues;
    String sType;

    Section(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        if (gCode == 2 && sType == null) {
            sType = value;
        } else if (gCode == 9) {
            attValues = new HashMap<>();
            attributes.put(value, attValues);
        } else if (attValues != null) {
            attValues.put(gCode, value);
        }
    }
}
