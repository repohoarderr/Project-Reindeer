package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Features {
    //Debug console colors
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private List<Shape> featureList;
    private List<JSONShape> jsonFeatureList;

    public Features() {
        featureList = null;
    }

    public void setFeatureList(List<Shape> shapeList) { // set feature list variable to the shapes list in DXFReader
        featureList = shapeList;
        jsonFeatureList = condenseFeatureList(shapeList);
    }


    public void printFeatures() { //Debug to console
        for (Shape feature : featureList) {
            if (feature instanceof Line2D) {
                System.out.print(ANSI_GREEN + "Line2D" + ANSI_RESET);
                System.out.println("\n\t" + ANSI_BLUE + "Length: " + ANSI_RESET + Math.sqrt((Math.pow(((Line2D.Double) feature).x2 - ((Line2D.Double) feature).x1, 2)) + Math.pow(((Line2D.Double) feature).y2 - ((Line2D.Double) feature).y1, 2)));
            } else if (feature instanceof Arc2D) {
                System.out.print(ANSI_GREEN + "Arc2D" + ANSI_RESET);
                double angleRad = Math.toRadians(Math.abs((((Arc2D.Double) feature).getAngleStart() - ((Arc2D.Double) feature).getAngleExtent())));
                double radius = ((Arc2D.Double) feature).width / 2 * Math.sin(angleRad / 2);
                double arcLength = radius * angleRad;
                System.out.println("\n\t" + ANSI_BLUE + "Radius: " + ANSI_RESET + radius + ANSI_BLUE + "\n\tArc Length: " + ANSI_RESET + arcLength);
            } else if (feature instanceof Ellipse2D) {
                System.out.print(ANSI_GREEN + "Ellipse2D" + ANSI_RESET);
                double a = ((Ellipse2D.Double) feature).height / 2;
                double b = ((Ellipse2D.Double) feature).width / 2;

                double circum = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2)) / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
                System.out.print(ANSI_BLUE + "\n\tCircumference: " + ANSI_RESET + circum);

                double area = Math.PI * a * b;

                System.out.println(ANSI_BLUE + "\n\tArea: " + ANSI_RESET + area);
            } else {
                String fullClassName = feature.getClass().getName();

                //remove "java.awt."
                String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                System.out.println(ANSI_GREEN + shortenedClassName + ANSI_RESET);
                System.out.println("\t No further information.");
            }
        }
    }

    public List<JSONShape> condenseFeatureList(List<Shape> features) {
        List<JSONShape> newFeatureList = new ArrayList<>();

        //all lines which aren't complete shapes (ellipses, circles, etc. are excluded)
        ArrayList<BasicLine> linePool = new ArrayList<>();

        //add all lines to line pool
        for (Shape feature : features) {
            switch (feature) {
                case Arc2D arc2d -> linePool.add(new BasicLine(arc2d));
                case Line2D line2d -> linePool.add(new BasicLine(line2d));
                case Path2D.Double path2d -> linePool.addAll(BasicLine.path2DToLines(path2d));
                default -> newFeatureList.add(JSONShapeFactory.createJSONShape(feature)); //feature is already condensed (ellipse, circle, etc.)
            }
        }

        BasicLine poolLine;
        BasicLine shapeLine;

        //build one shape out of several lines
        List<BasicLine> singleShapeAsLines = new ArrayList<>();

        while (!linePool.isEmpty()) {
            int oldSize = linePool.size();
            for (int i = 0; i < linePool.size(); i++) {
                poolLine = linePool.get(i);

                //if singleShapeAsLines is empty, we won't have anything to compare poolLine to
                if (singleShapeAsLines.isEmpty()) {
                    singleShapeAsLines.add(poolLine);
                    linePool.remove(poolLine);
                }

                for (int ii = 0; ii < singleShapeAsLines.size(); ii++) {
                    shapeLine = singleShapeAsLines.get(ii);
                    if (poolLine.equals(shapeLine)) {
                        continue;
                    }
                    if (poolLine.isLinkedWith(shapeLine)) {
                        singleShapeAsLines.add(poolLine);
                        linePool.remove(poolLine);
                        break;//Java doesn't like iterating through an ArrayList while modifying it
                    }
                }

                if (BasicLine.isOneLinkedShape(singleShapeAsLines)) {
                    singleShapeAsLines = condenseArcs(singleShapeAsLines);
                    newFeatureList.add(JSONShapeFactory.createJSONShape(singleShapeAsLines));
                    singleShapeAsLines.clear();
                }
            }
            if (linePool.size() == oldSize) {
                if (singleShapeAsLines.size() == 1){
                    //we've stopped categorizing lines and would be stuck in an infinite loop trying to categorize lines which don't connect, so break
                    break;
                }
                singleShapeAsLines = condenseArcs(singleShapeAsLines);
                newFeatureList.add(JSONShapeFactory.createJSONShape(singleShapeAsLines));
                singleShapeAsLines.clear();
            }
        }

        //add uncategorized lines so they can still be drawn
        linePool.forEach(line -> newFeatureList.add(new JSONShape(line.getSource())));
        if (!singleShapeAsLines.isEmpty()){
            singleShapeAsLines = condenseArcs(singleShapeAsLines);
            newFeatureList.add(JSONShapeFactory.createJSONShape(singleShapeAsLines));
        }
        return newFeatureList;
    }

    /**
     * Arcs may be segmented into smaller sub-arcs. This method connects those arcs back into larger arcs.
     *
     * @param lines
     * @return list of lines w/ smaller arcs condensed into larger arcs
     */
    private List<BasicLine> condenseArcs(List<BasicLine> lines) {
        List<BasicLine> returned = new ArrayList<>(lines.stream()
                .filter(line -> line.getSource() instanceof Line2D.Double ||
                        line.getSource() instanceof QuadCurve2D.Double ||
                        line.getSource() instanceof CubicCurve2D.Double)
                .toList());

        List<BasicLine> arcs = lines.stream()
                .filter(line -> line.getSource() instanceof Arc2D.Double)
                .collect(Collectors.groupingBy(BasicLine::hashCode))
                .values()
                .stream().map(list -> list.stream().reduce((bigLine, smallLine) ->{
                    Arc2D bigArc = (Arc2D) bigLine.getSource();
                    Arc2D smallArc = (Arc2D) smallLine.getSource();

                    double x = bigArc.getX();
                    double y = bigArc.getY();
                    double width = bigArc.getWidth();
                    double height = bigArc.getHeight();

                    final double TOLERANCE = 0.01;
                    boolean startAtSmallArcStart = Math.abs(smallArc.getAngleStart() + smallArc.getAngleExtent() - bigArc.getAngleStart()) % 360 < TOLERANCE;

                    double angleStart = startAtSmallArcStart ? smallArc.getAngleStart() : bigArc.getAngleStart();
                    double angleExtent = bigArc.getAngleExtent() + smallArc.getAngleExtent();

                    return new BasicLine(new Arc2D.Double(x, y, width, height, angleStart, angleExtent, Arc2D.OPEN));
                }).orElse(null)).toList();

        returned.addAll(arcs);
        return returned;

    }

    public Shape[] getFeatures() {
        Shape[] features = new Shape[featureList.size()];
        for (int i = 0; i < featureList.size(); ++i) {
            features[i] = featureList.get(i);
        }

        return features;
    }

    public JSONShape[] getJSONFeatures() {
        JSONShape[] jsonFeatures = new JSONShape[jsonFeatureList.size()];
        for (int i = 0; i < jsonFeatureList.size(); ++i) {
            jsonFeatures[i] = jsonFeatureList.get(i);
        }

        return jsonFeatures;
    }
}
