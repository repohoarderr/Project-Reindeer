package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Double.isInfinite;

public class JSONShapeFactory {
    private JSONShapeFactory() {
        //hide useless constructor
    }

    public static JSONShape createJSONShape(List<BasicLine> singleShapeAsLines) {
        //if lines aren't all connected, then we need to "connect" them together
        //this happens if we are trying to parse a shape which has had lines taken out
        //ex. a triangle with a radius notch will have the radius notch lines removed so we can recognize the triangle portion

        ArrayList<BasicLine> drawLines = new ArrayList<>(singleShapeAsLines);

        //if lines have a gap between them because a notch has been removed, fill those gaps
        if (!BasicLine.isOneLinkedShape(singleShapeAsLines)){
            ArrayList<ArrayList<BasicLine>> linesToCombine = findLinesToCombine(singleShapeAsLines);
            for (ArrayList<BasicLine> list : linesToCombine){
                singleShapeAsLines.removeAll(list);

                //note that createMergedLine returns a BasicLine with its draw boolean set to false
                singleShapeAsLines.add(BasicLine.createMergedLine(list));
            }
        }

        //tally number of arcs and lines
        int numArcs = (int) singleShapeAsLines.stream().filter(line-> line.getSource() instanceof Arc2D).count();
        int numStraightLines = (int) singleShapeAsLines.stream().filter(line-> line.getSource() instanceof Line2D).count();

        if (numStraightLines == 2 && numArcs == 2) {
            return new JSONCustomShape(ShapeType.OBLONG, drawLines);
        }

        if (numStraightLines == 3 && numArcs == 3) {
            return new JSONCustomShape(ShapeType.ROUND_TRIANGLE, drawLines);
        }

        if (numStraightLines == 3 && numArcs == 0) {
            return new JSONCustomShape(ShapeType.TRIANGLE, drawLines);
        }

        if (numStraightLines == 4 && numArcs == 4) {
            //check parallel lines to see if we have a trapezoid or a rectangle
            List<List<BasicLine>> parallels = findParallelLines(singleShapeAsLines);

            if (parallels.size() == 2){
                //TODO: throw in check for parallelograms here
                return new JSONShape(parseRoundRectangle(singleShapeAsLines), drawLines);
            }
            else{
                return new JSONCustomShape(ShapeType.ROUND_TRAPEZOID, drawLines);
            }
        }

        if (numStraightLines == 4 && numArcs == 0) {
            return new JSONShape(new Rectangle2D.Double(calculateXCoord(singleShapeAsLines), calculateYCoord(singleShapeAsLines),
                    calculateWidth(singleShapeAsLines), calculateHeight(singleShapeAsLines)), drawLines);
        }

        return parseSubFeaturesOrFreehand(singleShapeAsLines, drawLines);
    }

    private static JSONShape parseSubFeaturesOrFreehand(List<BasicLine> singleShapeAsLines, ArrayList<BasicLine> drawLines) {
        //check to see if shape has notch features, chamfered corners, etc.
        //TODO: idea for checking for chamfered corners
        //check to see if there are two sets of two lines which are parallel with each other --> these are the sides of the non-chamfered rectangle
        //additional lines are chamfered corners?

        List<JSONShape> likelyRadiusNotches = findLikelyRadiusNotches(singleShapeAsLines);
        //TODO: similar processes for likelyNotches, likelyChamferedCorners, etc

        List<BasicLine> nonFeatureLines = new ArrayList<>(singleShapeAsLines); //list of lines which don't draw a specific feature
        for (JSONShape subFeature : likelyRadiusNotches){
            nonFeatureLines.removeAll(subFeature.lines);
        }

        JSONShape newShape;
        //if we've removed some lines, check again to see if we can make our base shape
        if (nonFeatureLines.size() < singleShapeAsLines.size()){
            newShape = JSONShapeFactory.createJSONShape(nonFeatureLines);
            //return newShape with additional subfeatures if they exist
            for (JSONShape subFeature : likelyRadiusNotches){
                newShape.addSubFeature(subFeature);
            }

            return newShape;
        }
        else{
            //if we haven't removed lines this time around, probably a freehand shape
            return new JSONCustomShape(ShapeType.FREEHAND, drawLines);
        }
    }

    private static List<JSONShape> findLikelyRadiusNotches(List<BasicLine> singleShapeAsLines) {
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
                }
                if (connected){
                    List<BasicLine> radiusNotchLines = new ArrayList<>(list);
                    radiusNotchLines.add(arc);
                    likelyRadiusNotches.add(new JSONCustomShape(ShapeType.RADIUS_NOTCH,radiusNotchLines));
                }
            }
        }
        return likelyRadiusNotches;
    }

    private static ArrayList<ArrayList<BasicLine>> findLinesToCombine(List<BasicLine> singleShapeAsLines) {
        List<List<BasicLine>> parallelLines = findParallelLines(singleShapeAsLines);
        ArrayList<BasicLine> allLines = new ArrayList<>();
        parallelLines.forEach(allLines::addAll);

        ArrayList<ArrayList<BasicLine>> linesToCombine = new ArrayList<>();

        //start w/ list of parallel lines
        //add lines to the same list if the slope between the lines matches the slope of the lines themselves
        //this means that the lines are "in line" with each other
        //(they are two line segments which are part of the same line)
        toHere:
        for (BasicLine line : allLines){
            if (linesToCombine.isEmpty()){
                linesToCombine.add(new ArrayList<>());
            }

            for (ArrayList<BasicLine> list : linesToCombine){
                if (list.isEmpty()){
                    list.add(line);
                    continue toHere;
                }
                if (line.isInLineWith(list.getFirst())){
                    list.add(line);
                    continue toHere;
                }
            }
            //if we never added the line to a list, we need to create a new bucket for it
            linesToCombine.add(new ArrayList<>());
            linesToCombine.getLast().add(line);
        }
        linesToCombine.removeIf(list -> list.size() < 2);
        return linesToCombine;
    }

    private static boolean isAngleExtentSufficient(Arc2D arc, double targetExtent) {
        double extent = arc.getAngleExtent();
        if (extent < 0){
            extent *= -1;
        }
        return extent > targetExtent;
    }

    private static List<List<BasicLine>> getEqualLengthLines(List<BasicLine> lines) {
        Collection<List<BasicLine>> equalLengthLines = lines.stream()
                .filter(line -> line.getSource() instanceof Line2D)
                .collect(Collectors.groupingBy(line -> {
                    final double TOLERANCE = 0.001;
                    return Math.round(line.getLength() / TOLERANCE) * TOLERANCE;
                }))
                .values();


        equalLengthLines.removeIf(list -> list.size() == 1);
        return equalLengthLines.stream().toList();
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

    /**
     * Create a JSONShape from an object which is already fully parsed by the .dxf reader, such as a circle or ellipse.
     * @param feature the feature
     * @return the JSONShape created from the passed in feature
     */
    public static JSONShape createJSONShapeFromCondensedShape(Shape feature) {
        return new JSONShape(feature);
    }

    public static List<List<BasicLine>> findParallelLines(List<BasicLine> singleShapeAsLines) {

        //create deep copy
        ArrayList<BasicLine> straightsCopy = new ArrayList<>(singleShapeAsLines.stream()
                .filter(line -> line.getSource() instanceof Line2D.Double)
                .toList());

        return straightsCopy.stream()
                .collect(Collectors.groupingBy(line -> {
                    //group lines by slope. Need to round because we're comparing doubles, and create a special case for infinite slopes
                    double slope = line.getSlope();
                    if (isInfinite(slope)){
                        return Math.abs(slope);
                    }
                    final double TOLERANCE = 0.01;
                    return Math.round(slope / TOLERANCE) * TOLERANCE;
                }))
                .values()
                .stream().filter(list -> list.size() > 1)//don't include "groups" of one line
                .toList();
    }
}
