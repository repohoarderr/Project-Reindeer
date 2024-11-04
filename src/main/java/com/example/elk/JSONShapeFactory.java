package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import static java.lang.Double.isInfinite;

public class JSONShapeFactory {
    private JSONShapeFactory() {
        //hide useless constructor
    }

    public static JSONShape createJSONShape(List<BasicLine> singleShapeAsLines) {
        int numArcs = 0;
        int numStraightLines = 0;

        for (BasicLine basicLine : singleShapeAsLines) {
            if (basicLine.getSource() instanceof Line2D) {
                numStraightLines++;
            }
            if (basicLine.getSource() instanceof Arc2D) {
                numArcs++;
            }
        }

        if (numStraightLines == 2 && numArcs == 2) {
            return new JSONCustomShape(ShapeType.OBLONG, singleShapeAsLines);
        }

        //TODO: idea for checking for chamfered corners
        //check to see if there are two sets of two lines which are parallel with each other --> these are the sides of the non-chamfered rectangle
        //additional lines are chamfered corners?

        if (numStraightLines == 3 && numArcs == 3) {
            return new JSONCustomShape(ShapeType.ROUND_TRIANGLE, singleShapeAsLines);
        }

        if (numStraightLines == 3 && numArcs == 0) {
            return new JSONCustomShape(ShapeType.TRIANGLE, singleShapeAsLines);
        }

        if (numStraightLines == 4 && numArcs == 4) {
            //check parallel lines to see if we have a trapezoid or a rectangle
            ArrayList<ArrayList<Line2D>> parallels = findParallelLines(singleShapeAsLines);

            if (parallels.size() == 2){
                return new JSONShape(parseRoundRectangle(singleShapeAsLines), singleShapeAsLines);

            }
            else{
                return new JSONCustomShape(ShapeType.ROUND_TRAPEZOID, singleShapeAsLines);
            }
        }

        if (numStraightLines == 4 && numArcs == 0) {
            return new JSONShape(new Rectangle2D.Double(calculateXCoord(singleShapeAsLines), calculateYCoord(singleShapeAsLines),
                    calculateWidth(singleShapeAsLines), calculateHeight(singleShapeAsLines)), singleShapeAsLines);
        }

        //TODO: obvs finish
        Collections.sort(singleShapeAsLines);
        return new JSONCustomShape(ShapeType.FREEHAND, singleShapeAsLines);
    }

    private static RoundRectangle2D.Double parseRoundRectangle(List<BasicLine> singleShapeAsLines) {
        //find arc width. If arcs have differing width, take the largest one
        Optional<Arc2D> arc = singleShapeAsLines.stream()
                .map(BasicLine::getSource)
                .filter(Arc2D.class::isInstance)
                .map(Arc2D.class::cast)
                .reduce((arc1, arc2) -> arc1.getWidth() > arc2.getWidth() ? arc1 : arc2);

        double arcWidth = arc.map(RectangularShape::getWidth).orElse(0.0);

        //find arc height. If arcs have differing height, take the largest one
        Optional<Arc2D> secondArc = singleShapeAsLines.stream()
                .map(BasicLine::getSource)
                .filter(Arc2D.class::isInstance)
                .map(Arc2D.class::cast)
                .reduce((arc1, arc2) -> arc1.getHeight() > arc2.getHeight() ? arc1 : arc2);

        double arcHeight = secondArc.map(RectangularShape::getWidth).orElse(0.0);

        //if arc radius differs across arcs, this will be noted in the JSONShape constructor

        double xCord = calculateXCoord(singleShapeAsLines);
        double yCord = calculateYCoord(singleShapeAsLines);

        double width = calculateWidth(singleShapeAsLines);
        double height = calculateHeight(singleShapeAsLines);

        return new RoundRectangle2D.Double(xCord, yCord, width, height, arcWidth, arcHeight);
    }

    public static double calculateYCoord(List<BasicLine> singleShapeAsLines) {
        double yCoord = 0;
        for (BasicLine line : singleShapeAsLines) {
            yCoord += line.getSource().getBounds2D().getCenterY();
        }
        return yCoord / singleShapeAsLines.size();
    }

    public static double calculateXCoord(List<BasicLine> singleShapeAsLines) {
        double xCoord = 0;
        for (BasicLine line : singleShapeAsLines) {
            xCoord += line.getSource().getBounds2D().getCenterX();
        }
        return xCoord / singleShapeAsLines.size();
    }

    public static double calculateHeight(List<BasicLine> singleShapeAsLines) {
        double maxY = 0;
        double minY = Double.MAX_VALUE;

        for (BasicLine line : singleShapeAsLines) {
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            if (lineBounds.getMaxY() > maxY) {
                maxY = lineBounds.getMaxY();
            }
            if (lineBounds.getMinY() < minY) {
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

        for (BasicLine line : singleShapeAsLines) {
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            if (lineBounds.getMaxX() > maxX) {
                maxX = lineBounds.getMaxX();
            }
            if (lineBounds.getMinX() < minX) {
                minX = lineBounds.getMinX();
            }
        }
        return maxX - minX;
    }

    public static JSONShape createJSONShapeFromCondensedShape(Shape feature) {
        return new JSONShape(feature);
    }

    public static ArrayList<ArrayList<Line2D>> findParallelLines(List<BasicLine> singleShapeAsLines) {

        ArrayList<ArrayList<Line2D>> parallels = new ArrayList<>();

        double straightSlope;
        double parallelLineSlope;

        //create deep copy
        ArrayList<Line2D> straightsCopy = new ArrayList<>(singleShapeAsLines.stream()
                .filter(line -> line.getSource() instanceof Line2D.Double)
                .map(line -> (Line2D) line.getSource())
                .toList());

        while (!straightsCopy.isEmpty()) {
            goHere:
            for (int i = 0; i < straightsCopy.size(); ++i) {
                Line2D straight = straightsCopy.get(i);

                straightSlope = (straight.getY2() - straight.getY1()) / (straight.getX2() - straight.getX1());
                for (ArrayList<Line2D> list : parallels) {
                    if (list.isEmpty()) {
                        list.add(straight);
                        straightsCopy.remove(straight);
                        break goHere;
                    }

                    Line2D listLine = list.get(0);
                    parallelLineSlope = (listLine.getY2() - listLine.getY1()) / (listLine.getX2() - listLine.getX1());

                    boolean bothAreInfinite = isInfinite(parallelLineSlope) && isInfinite(straightSlope); //account for vertical lines (infinite slope)
                    final double TOLERANCE = 0.001;
                    if (bothAreInfinite || Math.abs(straightSlope - parallelLineSlope) < TOLERANCE) {
                        list.add(straight);
                        straightsCopy.remove(straight);
                        break goHere;
                    }
                }
                parallels.add(new ArrayList<>()); //add new list of parallels if line doesn't match anywhere else
            }
        }
        return parallels;
    }
}
