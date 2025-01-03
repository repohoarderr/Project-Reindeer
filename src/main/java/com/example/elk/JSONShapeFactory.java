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

    public static JSONShape createJSONShape(List<BasicLine> sourceLines) {
        ArrayList<BasicLine> drawLines = new ArrayList<>(sourceLines);
        ArrayList<BasicLine> shapeComponentLines = new ArrayList<>(sourceLines);

        //if sourceLines is a single arc in the shape of a circle, return a circle object
        if (sourceLines.size() == 1 && sourceLines.get(0).getSource() instanceof Arc2D arc2D
                && Math.abs(arc2D.getAngleExtent()) % 360 == 0){
                return JSONShapeFactory.createJSONShape(
                        new Ellipse2D.Double(arc2D.getX(), arc2D.getY(), arc2D.getWidth(), arc2D.getHeight()));
            }


        //if lines aren't all connected, then we need to "connect" them together
        //this happens if we are trying to parse a shape which has had lines taken out
        //ex. a triangle with a radius notch will have the radius notch lines removed so we can recognize the triangle portion

        //if lines have a gap between them because a notch has been removed, fill those gaps
        if (!BasicLine.isOneLinkedShape(sourceLines)){
            List<List<BasicLine>> linesToCombine = sourceLines.stream()
                    .filter(line -> line.getSource() instanceof Line2D)
                    .collect(Collectors.groupingBy(BasicLine::hashCode))
                    .values()
                    .stream()
                    .filter(lineList -> lineList.size() > 1)
                    .toList();
            for (List<BasicLine> list : linesToCombine){
                shapeComponentLines.removeAll(list);

                //note that createMergedLine returns a BasicLine with its draw boolean set to false
                shapeComponentLines.add(BasicLine.createMergedLine(list));
            }
        }

        JSONShape shape = parseSimpleShape(shapeComponentLines, drawLines);
        if (shape != null) return shape;

        return parseSubFeaturesOrFreehand(sourceLines, drawLines);
    }

    private static JSONShape parseSimpleShape(List<BasicLine> shapeComponentLines, ArrayList<BasicLine> drawLines) {
        //tally number of arcs and lines
        int numCurves = (int) shapeComponentLines.stream().filter(line->
                line.getSource() instanceof Arc2D ||
                line.getSource() instanceof QuadCurve2D ||
                line.getSource() instanceof CubicCurve2D).count();
        int numStraightLines = (int) shapeComponentLines.stream().filter(line-> line.getSource() instanceof Line2D).count();

        if (numStraightLines == 2 && numCurves == 2) {
            return new JSONCustomShape(ShapeType.OBLONG, drawLines);
        }

        if (numStraightLines == 3 && numCurves == 3) {
            return new JSONCustomShape(ShapeType.ROUND_TRIANGLE, drawLines);
        }

        if (numStraightLines == 3 && numCurves == 0) {
            return new JSONCustomShape(ShapeType.TRIANGLE, drawLines);
        }

        if (numStraightLines == 4 && numCurves == 4) {
            //check parallel lines to see if we have a trapezoid or a rectangle
            List<List<BasicLine>> parallels = findParallelLines(shapeComponentLines);

            if (parallels.size() == 2){
                //TODO: throw in check for parallelograms here
                return new JSONShape(parseRoundRectangle(shapeComponentLines), drawLines);
            }
            else if (parallels.size() == 1){
                return new JSONCustomShape(ShapeType.ROUND_TRAPEZOID, drawLines);
            }
        }

        if (numStraightLines == 4 && numCurves == 0) {
            return new JSONShape(new Rectangle2D.Double(calculateXCoord(shapeComponentLines), calculateYCoord(shapeComponentLines),
                    calculateWidth(shapeComponentLines), calculateHeight(shapeComponentLines)), drawLines);
        }

        return null;
    }

    private static JSONShape parseSubFeaturesOrFreehand(List<BasicLine> shapeComponentLines, ArrayList<BasicLine> drawLines) {
        //check to see if shape has notch features, chamfered corners, etc.
        //TODO: idea for checking for chamfered corners
        //check to see if there are two sets of two lines which are parallel with each other --> these are the sides of the non-chamfered rectangle
        //additional lines are chamfered corners?

        List<JSONShape> likelyRadiusNotches = findLikelyRadiusNotches(shapeComponentLines);
        //TODO: similar processes for likelyNotches, likelyChamferedCorners, etc

        List<BasicLine> nonFeatureLines = new ArrayList<>(shapeComponentLines); //list of lines which don't draw a specific feature
        for (JSONShape subFeature : likelyRadiusNotches){
            nonFeatureLines.removeAll(subFeature.lines);
        }

        JSONShape newShape;
        //if we've removed some lines, check again to see if we can make our base shape
        if (nonFeatureLines.size() < shapeComponentLines.size()){
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
                if (list.stream().allMatch(line -> line.isLinkedWith(arc))){//if all lines connect to arc
                    List<BasicLine> radiusNotchLines = new ArrayList<>(list);
                    radiusNotchLines.add(arc);
                    likelyRadiusNotches.add(new JSONCustomShape(ShapeType.RADIUS_NOTCH,radiusNotchLines));
                }
            }
        }
        return likelyRadiusNotches;
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


        equalLengthLines.removeIf(list -> list.size() < 2);
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
        double maxY = Double.NEGATIVE_INFINITY;
        double minY = Double.MAX_VALUE;

        for (BasicLine line : singleShapeAsLines) {
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            maxY = Math.max(maxY, lineBounds.getMaxY());
            minY = Math.min(minY, lineBounds.getMinY());
        }

        return maxY - minY;
    }

    private static double calculateWidth(List<BasicLine> singleShapeAsLines) {
        //TODO: provides incorrect width if rectangle is rotated?
        // Do we want longest side or distance between leftmost and rightmost points?
        double maxX = Double.NEGATIVE_INFINITY;
        double minX = Double.MAX_VALUE;

        for (BasicLine line : singleShapeAsLines) {
            Rectangle2D lineBounds = line.getSource().getBounds2D();
            maxX = Math.max(maxX, lineBounds.getMaxX());
            minX = Math.min(minX, lineBounds.getMinX());
        }
        return maxX - minX;
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

    /**
     * Create a JSONShape from an object which is already fully parsed by the .dxf reader, such as a circle or ellipse.
     * @param feature the feature
     * @return the JSONShape created from the passed in feature
     */
    public static JSONShape createJSONShape(Shape feature) {
        return new JSONShape(feature);
    }
}
