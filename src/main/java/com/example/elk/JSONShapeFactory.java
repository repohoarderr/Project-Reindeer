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
        //check to see if shape has notch features, chamfered corners, etc.
        //TODO: idea for checking for chamfered corners
        //check to see if there are two sets of two lines which are parallel with each other --> these are the sides of the non-chamfered rectangle
        //additional lines are chamfered corners?

        //create copy of singleShapeAsLines (shapeLinesCopy)
        //check to see if shapeLinesCopy has radius notch
        //if it does, remove those lines from copy
        //check to see if remaining lines meet parameters of a JSONShape
        //if they do:
            //create the shape, add the notch features to it
        //if they don't:
            //repeat the process until the shape can be parsed or until shapeLinesCopy is empty --> return freehand

        List<List<BasicLine>> equalLengthLines = getEqualLengthLines(singleShapeAsLines);
        List<JSONShape> likelyRadiusNotches = new ArrayList<>();

        for (List<BasicLine> list : equalLengthLines){
            if (list.size() != 2){
                //TODO: what happens when list.size() != 2? We don't know how to deal with this yet
                continue;
            }
            //if the two lines have an arc connecting them,
            //and that arc has an angle extent >= 180 degrees,
            //it is likely a radius notch

            //loop through arcs which have an angle extent > 180
            for (BasicLine arc : singleShapeAsLines.stream()
                    .filter(shape -> shape.getSource() instanceof Arc2D)
                    .filter(shape -> isAngleExtentSufficient((Arc2D)shape.getSource(), 180))
                    .toList()) {
                boolean connected = true;
                for (BasicLine line : list){
                    if (!arc.isLinkedWith(line)){
                        connected = false;
                    }
                    else{
                        System.out.println("at least these are connected");
                    }
                }
                if (connected){
                    List<BasicLine> radiusNotchLines = new ArrayList<>(list);
                    radiusNotchLines.add(arc);
                    likelyRadiusNotches.add(new JSONCustomShape(ShapeType.RADIUS_NOTCH,radiusNotchLines));
                }
            }
        }

        return new JSONCustomShape(ShapeType.FREEHAND, singleShapeAsLines);
    }

    private static boolean isAngleExtentSufficient(Arc2D arc, double targetExtent) {
        double extent = arc.getAngleExtent();
        if (extent < 0){
            extent *= -1;
        }
        return extent > targetExtent;
    }

    private static List<List<BasicLine>> getEqualLengthLines(List<BasicLine> lines) {
        List<List<BasicLine>> equalLengthLines = new ArrayList<>();
        goHere:
        for (BasicLine line : lines){
            if (!(line.getSource() instanceof Line2D)){
                continue;
            }
            if (equalLengthLines.isEmpty()){
                equalLengthLines.add(new ArrayList<>());
                equalLengthLines.get(0).add(line);
                continue;
            }

            final double TOLERANCE = 0.001;
            for (List<BasicLine> list : equalLengthLines){
                if (Math.abs(line.getLength() - list.get(0).getLength()) < TOLERANCE){
                    list.add(line);
                    continue goHere;
                }
            }
            equalLengthLines.add(new ArrayList<>());
            equalLengthLines.get(equalLengthLines.size() - 1).add(line);
        }

        equalLengthLines.removeIf(list -> list.size() == 1);
        return equalLengthLines;
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
