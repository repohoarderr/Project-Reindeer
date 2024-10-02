package com.example.elk;

import java.util.ArrayList;
import java.util.List;

import static com.example.elk.DXFReader.blockDict;
import static com.example.elk.DXFReader.uScale;

class Block extends Entity {
    final List<DrawItem> entities = new ArrayList<>();
    double baseX;
    double baseY;
    int flags;

    Block(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 2:                                       // Block name
                blockDict.put(value, this);
                break;
            case 5:                                       // Block handle
                break;
            case 10:                                      // Base Point X
                baseX = Double.parseDouble(value) * uScale;
                break;
            case 20:                                      // Base Point Y
                baseY = Double.parseDouble(value) * uScale;
                break;
            case 70:                                      // Flags
                flags = Integer.parseInt(value);
                break;
        }
    }

    void addEntity(DrawItem entity) {
        entities.add(entity);
    }
}
