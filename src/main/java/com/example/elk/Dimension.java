package com.example.elk;

import java.awt.*;
import java.awt.geom.Path2D;

import static com.example.elk.DXFReader.blockDict;
import static com.example.elk.DXFReader.uScale;

class Dimension extends DrawItem implements DXFReader.AutoPop {
    private String blockHandle, blockName;
    private double ax, ay, mx, my;
    private int type, orientation;

    Dimension(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 2:                                     // Name of Block to with Dimension graphics
                blockName = value;
                break;
            case 5:                                     // Handle of Block to with Dimension graphics
                blockHandle = value;
                break;
            case 10:                                    // Definition Point X
                ax = Double.parseDouble(value) * uScale;
                break;
            case 20:                                    // Definition Point Y
                ay = Double.parseDouble(value) * uScale;
                break;
            case 11:                                    // Mid Point X
                mx = Double.parseDouble(value) * uScale;
                break;
            case 21:                                    // Mid Point Y
                my = Double.parseDouble(value) * uScale;
                break;
            case 70:                                    // Dimension type (0-6 plus bits at 32,64,128)
                type = Integer.parseInt(value);
                break;
            case 71:                                    // Attachment orientation (1-9) for 1=UL, 2=UC, 3=UR, etc
                orientation = Integer.parseInt(value);
                break;
        }
    }

    @Override
    Shape getShape() {
        Block block = blockDict.get(blockName);
        if (block != null && block.entities.size() > 0) {
            Path2D.Double path = new Path2D.Double();
            for (DrawItem entity : block.entities) {
                Shape shape = entity.getShape();
                if (shape != null) {
                    path.append(shape, false);
                }
            }
            return path;
        }
        return null;
    }
}
