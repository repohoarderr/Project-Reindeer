package com.example.elk;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ShapeFactory {
    private ShapeFactory(){
        //hide useless constructor
    }

    public static Shape createShape(List<BasicLine> singleShapeAsLines) {
        int numArcs = 0;
        int numStraightLines = 0;

        for (BasicLine basicLine : singleShapeAsLines) {
            if (basicLine.getSource() instanceof Line2D) {
                numStraightLines++;
            }
            if (basicLine.getSource() instanceof Arc2D){
                numArcs++;
            }
        }

        if (numStraightLines == 3 && numArcs == 3) {
            //TODO: triangle w/ rounded corners
        }

        if (numStraightLines == 4 && numArcs == 4) {
            return parseRoundRectangle(singleShapeAsLines);
        }

        if (numStraightLines == 4 && numArcs == 0) {
            //TODO: temp vals
            return new Rectangle(1, 2, 3, 4);
        }

        //TODO: obvs finish
        return new Polygon();
    }

    private static RoundRectangle2D.Double parseRoundRectangle(List<BasicLine> singleShapeAsLines) {
        //TODO: temp vals
        ArrayList<Line2D> straights = new ArrayList<>();
        ArrayList<Arc2D> curves = new ArrayList<>();

        singleShapeAsLines.forEach(line -> {
            if (line.getSource() instanceof Line2D) {
                straights.add((Line2D) line.getSource());
            }
            if (line.getSource() instanceof Arc2D){
                curves.add((Arc2D) line.getSource());
            }
        });
        boolean isArcWidthConsistent = true;

        //TODO: verify that getWidth() is the correct function
        double arcWidth = curves.get(0).getWidth();
        double TOLERANCE = 0.001;
        for (Arc2D arc : curves) {
            if (Math.abs(arc.getHeight()  - arcWidth) < TOLERANCE){
                isArcWidthConsistent = false;
                break;
            }
        }

        boolean isArcHeightConsistent = true;
        double arcHeight = curves.get(0).getHeight();
        for (Arc2D arc : curves) {
            if (Math.abs(arc.getHeight()  - arcWidth) < TOLERANCE){
                isArcHeightConsistent = false;
                break;
            }
        }

        //TODO: fix temp values
        double xCord = 0;
        double yCord = 0;
        double width = 2;
        double height = 2;

        //TODO: use booleans isArcHeightConsistant and isArcWidthConsistent to create flags later
        return new RoundRectangle2D.Double(xCord, yCord, width, height, arcWidth, arcHeight);
    }
}
