package com.example.elk;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class JSONShapeFactory {
    private JSONShapeFactory(){
        //hide useless constructor
    }

    public static JSONShape createJSONShape(List<BasicLine> singleShapeAsLines) {
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
            return new JSONShape(parseRoundRectangle(singleShapeAsLines), singleShapeAsLines);
        }

        if (numStraightLines == 4 && numArcs == 0) {
            return new JSONShape(new Rectangle2D.Double(calculateXCoord(singleShapeAsLines), calulateYCoord(singleShapeAsLines),
                    calculateWidth(singleShapeAsLines), calculateHeight(singleShapeAsLines)), singleShapeAsLines);
        }

        //TODO: obvs finish
        return new JSONShape(new Polygon(), singleShapeAsLines);
    }

    //TODO: need to verify that we are parsing a rectangle and not a trapezoid
    private static RoundRectangle2D.Double parseRoundRectangle(List<BasicLine> singleShapeAsLines) {

        //find arc width. If arcs have differing width, take the largest one
        double arcWidth = singleShapeAsLines.stream()
                .map(BasicLine::getSource)
                .filter(source -> source instanceof Arc2D)
                .map(line -> (Arc2D) line)
                .reduce((arc1, arc2) -> arc1.getWidth() > arc2.getHeight() ? arc1 : arc2)
                .get().getWidth();

        //find arc height. If arcs have differing height, take the largest one
        double arcHeight = singleShapeAsLines.stream()
                .map(BasicLine::getSource)
                .filter(source -> source instanceof Arc2D)
                .map(line -> (Arc2D) line)
                .reduce((arc1, arc2) -> arc1.getHeight() > arc2.getHeight() ? arc1 : arc2)
                .get().getHeight();

        //if arc radius differs across arcs, this will be noted in the JSONShape constructor

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

    public static JSONShape createJSONShapeFromCondensedShape(Shape feature) {
        return new JSONShape(feature);
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
