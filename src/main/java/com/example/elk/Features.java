package com.example.elk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
//import org.json.simple.JSONObject;

public class Features {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private ArrayList<Shape> featureList;
    public static ArrayList<Shape> staticFeatureList;

    public Features() {
        featureList = null;
    }

    public void setFeatureList(ArrayList<Shape> shapeArrayList) { // set feature list variable to the shapes list in DXFReader
        featureList = shapeArrayList;
    }

    public void printFeatures() {
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

    public static JSONObject featureJSON(Shape shape) {
        JSONObject jsonWriter = new JSONObject();
        if (shape instanceof Line2D.Double line2D) {
            jsonWriter.put("length", Math.sqrt((Math.pow(line2D.x2 - line2D.x1, 2)) + Math.pow(line2D.y2 - line2D.y1, 2)));
            jsonWriter.put("startX", line2D.x1);
            jsonWriter.put("startY", line2D.y1);
            jsonWriter.put("endX", line2D.x2);
            jsonWriter.put("endY", line2D.y2);
            jsonWriter.put("type", "Line2D");
        }
        else if (shape instanceof Arc2D.Double arc2D) {
//TODO Figure out how to send rounded triangle.
            double angleRad = Math.toRadians(Math.abs((arc2D.getAngleStart() - arc2D.getAngleExtent())));
            double radius = (arc2D.width * arc2D.width) / (8 * (arc2D.height)) + (arc2D.height / 2);
            double arcLength = radius * angleRad;
            jsonWriter.put("length", arcLength);
            jsonWriter.put("startX", arc2D.getStartPoint().getX());
            jsonWriter.put("startY", arc2D.getStartPoint().getY());
            jsonWriter.put("endX", arc2D.getEndPoint().getX());
            jsonWriter.put("endY", arc2D.getEndPoint().getY());
            jsonWriter.put("centerX", arc2D.getCenterX());
            jsonWriter.put("centerY", arc2D.getCenterY());
            jsonWriter.put("radius", radius);
            jsonWriter.put("arcType", arc2D.getArcType());
            jsonWriter.put("rotation", (arc2D.getAngleStart()));
            jsonWriter.put("angle", -arc2D.extent);
            jsonWriter.put("type", "Arc2D");
        }
        else if (shape instanceof Ellipse2D.Double ellipse2D) {
            double a = ellipse2D.height / 2;
            double b = ellipse2D.width / 2;
            double circum = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2)) / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
            jsonWriter.put("circumference", circum);
            if (ellipse2D.getWidth() == ellipse2D.getHeight()) {
                jsonWriter.put("radius", ((Ellipse2D.Double) shape).getHeight() / 2);
                if (((Ellipse2D.Double) shape).getHeight() >= 1) {
                    jsonWriter.put("type", "punch");
                } else {
                    jsonWriter.put("type", "circle");
                }
            } else {
                jsonWriter.put("width", ellipse2D.getWidth());
                jsonWriter.put("height", ellipse2D.getHeight());
                jsonWriter.put("type", "ellipse");
            }
            double area = Math.PI * a * b;
            jsonWriter.put("area", area);
            double centerX = ellipse2D.getCenterX();
            double centerY = ellipse2D.getCenterY();
            jsonWriter.put("centerX", centerX);
            jsonWriter.put("centerY", centerY);
        }
        else {
            String fullClassName = shape.getClass().getName();

            //remove "java.awt."
            String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            jsonWriter.put("type", shortenedClassName);
        }
    return jsonWriter;
}

public void condenseFeatureList() {
    ArrayList<Shape> newFeatureList = new ArrayList<>();

    //all lines which aren't complete shapes (ellipses, circles, etc. are excluded)
    ArrayList<BasicLine> linePool = new ArrayList<>();

    //used to build one shape out of several lines
    ArrayList<BasicLine> singleShapeAsLines = new ArrayList<>();

    //add all lines to line pool
    for (Shape feature : featureList) {
        if (feature instanceof Arc2D) {
            linePool.add(new BasicLine((Arc2D) feature));
        } else if (feature instanceof Line2D) {
            linePool.add(new BasicLine((Line2D) feature));
        } else {
            newFeatureList.add(feature); //feature is already condensed (ellipse, circle, etc.)
        }
    }

    BasicLine poolLine;
    BasicLine shapeLine;

    //may cause infinite looping if there are lines which don't connect to anything
    //TODO: while (!linePool.isEmpty() && linePool.size() changed from last iteration?)
    while (!linePool.isEmpty()) {
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
                newFeatureList.add(ShapeFactory.createShape(singleShapeAsLines));
                singleShapeAsLines.clear();
            }
        }
    }

    featureList = newFeatureList;
}

public String getFeatureListAsString() {
    String temp = "";
    for (Shape item : featureList) {
        temp += item.toString() + "\n";
    }
    return temp;
}

public Shape[] getFeatures() {
    Shape[] features = new Shape[featureList.size()];
    for (int i = 0; i < featureList.size(); ++i) {
        features[i] = featureList.get(i);
    }

    return features;
}
}
