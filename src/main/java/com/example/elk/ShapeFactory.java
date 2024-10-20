package com.example.elk;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.isInfinite;

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
            return new Rectangle2D.Double(calculateXCoord(singleShapeAsLines), calulateYCoord(singleShapeAsLines),
                    calculateWidth(singleShapeAsLines), calculateHeight(singleShapeAsLines));
        }

        //TODO: obvs finish
        return new Polygon();
    }

    //TODO: need to verify that we are parsing a rectangle and not a trapezoid
    private static RoundRectangle2D.Double parseRoundRectangle(List<BasicLine> singleShapeAsLines) {
        ArrayList<Arc2D> curves = new ArrayList<>();

        //populate straights & curves lists
        singleShapeAsLines.forEach(line -> {
            if (line.getSource() instanceof Arc2D){
                curves.add((Arc2D) line.getSource());
            }
        });


        //check if arc widths/heights are consistent
        boolean isArcWidthConsistent = true;
        boolean isArcHeightConsistent = true;

        //TODO: verify that getWidth() is the correct function
        double arcWidth = curves.get(0).getWidth();
        double arcHeight = curves.get(0).getHeight();
        final double TOLERANCE = 0.01;
        for (Arc2D arc : curves) {
            if (Math.abs(arc.getWidth()  - arcWidth) < TOLERANCE){
                isArcWidthConsistent = false;
            }
            if (Math.abs(arc.getHeight()  - arcHeight) < TOLERANCE){
                isArcHeightConsistent = false;
            }
        }

        double xCord = calculateXCoord(singleShapeAsLines);
        double yCord = calulateYCoord(singleShapeAsLines);

        double width = calculateWidth(singleShapeAsLines);
        double height = calculateHeight(singleShapeAsLines);

        //TODO: use booleans isArcHeightConsistant and isArcWidthConsistent to create flags later
        return new RoundRectangle2D.Double(xCord, yCord, width, height, arcWidth, arcHeight);
    }

    private static double calulateYCoord(List<BasicLine> singleShapeAsLines) {
        double yCoord = 0;
        for (BasicLine line : singleShapeAsLines){
            yCoord += line.getSource().getBounds2D().getCenterY();
        }
        return yCoord / singleShapeAsLines.size();
    }

    private static double calculateXCoord(List<BasicLine> singleShapeAsLines) {
        double xCoord = 0;
        for (BasicLine line : singleShapeAsLines){
            xCoord += line.getSource().getBounds2D().getCenterX();
        }
        return xCoord / singleShapeAsLines.size();
    }

    private static double calculateHeight(List<BasicLine> singleShapeAsLines) {
        double maxY = 0;
        double minY = Double.MAX_VALUE;

        for (BasicLine line : singleShapeAsLines){
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            if (lineBounds.getMaxY() > maxY){
                maxY = lineBounds.getMaxY();
            }
            if (lineBounds.getMinY() < minY){
                minY = lineBounds.getMinY();
            }
        }

        return maxY - minY;
    }

    private static double calculateWidth(List<BasicLine> singleShapeAsLines) {
        //TODO: provides incorrect width if rectangle is rotated?
        // Do we want longest side or distance between leftmost and rightmost points?
        double maxX = 0;
        double minX = Double.MAX_VALUE;

        for (BasicLine line : singleShapeAsLines){
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            if (lineBounds.getMaxX() > maxX){
                maxX = lineBounds.getMaxX();
            }
            if (lineBounds.getMinX() < minX){
                minX = lineBounds.getMinX();
            }
        }
        return maxX - minX;
    }

    //leaving this code here because it will be useful later
    //TODO: use this code to tell the difference between rectangles and trapezoids (if necessary)
//    public void findParallelLinesCode(){
//
//        ArrayList<ArrayList<Line2D>> parallels = new ArrayList<>();
//
//        double straightSlope;
//        double parallelLineSlope;
//
//        ArrayList<Line2D> straightsCopy = new ArrayList<>();
//        //create deep copy
//        straightsCopy.addAll(straights);
//
//        while(!straightsCopy.isEmpty()) {
//            goHere:
//            for (int i = 0; i < straightsCopy.size(); ++i) {
//                Line2D straight = straightsCopy.get(i);
//
//                straightSlope = (straight.getY2() - straight.getY1()) / (straight.getX2() - straight.getX1());
//                for (ArrayList<Line2D> list : parallels) {
//                    if (list.isEmpty()) {
//                        list.add(straight);
//                        straightsCopy.remove(straight);
//                        break goHere;
//                    }
//
//                    Line2D listLine = list.get(0);
//                    parallelLineSlope = (listLine.getY2() - listLine.getY1()) / (listLine.getX2() - listLine.getX1());
//
//                    boolean bothAreInfinite = isInfinite(parallelLineSlope) && isInfinite(straightSlope); //account for vertical lines (infinite slope)
//                    if (bothAreInfinite || Math.abs(straightSlope - parallelLineSlope) < TOLERANCE) {
//                        list.add(straight);
//                        straightsCopy.remove(straight);
//                        break goHere;
//                    }
//                }
//                parallels.add(new ArrayList<>()); //add new list of parallels if line doesn't match anywhere else
//            }
//        }
//    }
}
