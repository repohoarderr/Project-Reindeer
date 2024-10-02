package com.example.elk;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import static com.example.elk.DXFReader.*;

class Insert extends DrawItem implements DXFReader.AutoPop {
    private String blockHandle, blockName;
    private double ix, iy, xScale = 1.0, yScale = 1.0, zScale = 1.0, rotation;

    Insert(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 2:                                     // Name of Block to insert
                blockName = value;
                break;
            case 5:                                     // Handle of Block to insert
                blockHandle = value;
                break;
            case 10:                                    // Insertion X
                ix = Double.parseDouble(value) * uScale;
                break;
            case 20:                                    // Insertion Y
                iy = Double.parseDouble(value) * uScale;
                break;
            case 41:                                    // X scaling
                xScale = Double.parseDouble(value);
                break;
            case 42:                                    // Y scaling
                yScale = Double.parseDouble(value);
                break;
            case 43:                                    // Z Scaling (affects x coord and rotation)
                zScale = Double.parseDouble(value);
                break;
            case 50:                                    // Rotation angle (degrees)
                rotation = Double.parseDouble(value);
                break;
        }
    }

    @Override
    Shape getShape() {
        Block block = blockDict.get(blockName);
        if (block != null && block.entities.size() > 0) {
            Path2D.Double path = new Path2D.Double();
            AffineTransform at1 = null;
            if (block.baseX != 0 || block.baseY != 0) {
                // TODO: make this work...
                at1 = new AffineTransform();
                at1.translate(block.baseX, block.baseY);
            }
            AffineTransform at2 = new AffineTransform();
            if (zScale < 0) {
                // Fixes "DXF Files that do not Render Properly/Floor plan.dxf" test file
                at2.translate(-ix, iy);
                at2.scale(-xScale, yScale);
            } else {
                at2.translate(ix, iy);
                at2.scale(xScale, yScale);
            }
            at2.rotate(Math.toRadians(xScale < 0 ? -rotation : rotation));
            for (DrawItem entity : block.entities) {
                if (doDraw(entity)) {
                    Shape shape = entity.getShape();
                    if (shape != null) {
                        if (at1 != null) {
                            // TODO: make this work...
                            shape = at1.createTransformedShape(shape);
                        }
                        shape = at2.createTransformedShape(shape);
                        path.append(shape, false);
                    }
                }
            }
            return path;
        }
        return null;
    }
}