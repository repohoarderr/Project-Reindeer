package com.example.elk;

import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

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
  private ArrayList<Shape> featureList;
  private ArrayList<JSONShape> jsonFeatureList;
  public Features() {
    featureList = null;
  }

  public void setFeatureList(ArrayList<Shape> shapeList) { // set feature list variable to the shapes list in DXFReader
    featureList = shapeList;
    jsonFeatureList = condenseFeatureList(shapeList); //TODO: some magic here idk
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

  public static JSONObject shapeToJSON(Shape shape){
    return shapeToJSON(shape, -1);
  }

  public static JSONObject shapeToJSON(Shape shape, int id) { //Add the shape to the feature list in JSON format
    JSONObject jsonWriter = new JSONObject();
    if (shape instanceof Line2D.Double line2D) { //if the shape is a line
      jsonWriter.put("length", Math.sqrt((Math.pow(line2D.x2 - line2D.x1, 2)) + Math.pow(line2D.y2 - line2D.y1, 2)));
      jsonWriter.put("startX", line2D.x1);
      jsonWriter.put("startY", line2D.y1);
      jsonWriter.put("endX", line2D.x2);
      jsonWriter.put("endY", line2D.y2);
      jsonWriter.put("type", "Line2D");
    } else if (shape instanceof Arc2D.Double arc2D) { //if the shape is an arc2d
      double startAngleRad = Math.toRadians(arc2D.getAngleStart());
      double extentRad = Math.toRadians(arc2D.extent);
      double endAngleRad = startAngleRad + extentRad;

      double radius = arc2D.width / 2;
      double angleRad = Math.abs(extentRad);
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
      jsonWriter.put("rotation", startAngleRad);  // Keep the start angle (rotation) the same
      jsonWriter.put("angle", endAngleRad);  // Now send the end angle as an absolute value

      jsonWriter.put("type", "Arc2D");
    }
    // ***********************************************************************************
    // starting parsing full shapes
    else if (shape instanceof Ellipse2D.Double ellipse2D) { //if the shape is an ellipse
      double a = ellipse2D.height / 2;
      double b = ellipse2D.width / 2;
      double circum = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2)) / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
      jsonWriter.put("circumference", circum);
      if (ellipse2D.getWidth() == ellipse2D.getHeight()) { //check to see if the shape is a circle or oval
        jsonWriter.put("radius", ((Ellipse2D.Double) shape).getHeight() / 2);
        if (((Ellipse2D.Double) shape).getHeight() <= 0.5) {
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
    else if (shape instanceof Rectangle2D.Double rect) { //if the shape is a rectangle
      jsonWriter.put("width", rect.width);
      jsonWriter.put("height", rect.height);
      jsonWriter.put("centerX", rect.getCenterX());
      jsonWriter.put("centerY", rect.getCenterY());
      jsonWriter.put("area", rect.width * rect.height);
      jsonWriter.put("type", "Rectangle");
    } else if (shape instanceof RoundRectangle2D.Double roundRect) { //if the shape is a rectangle with radius corners
      jsonWriter.put("width", roundRect.width);
      jsonWriter.put("height", roundRect.height);
      jsonWriter.put("centerX", roundRect.getCenterX());
      jsonWriter.put("centerY", roundRect.getCenterY());
      jsonWriter.put("area", roundRect.width * roundRect.height);
      jsonWriter.put("cornerRadius", roundRect.getArcHeight());
      jsonWriter.put("type", "RoundRectangle");
    } else { // default to this if the shape does not fall under any category
      String fullClassName = shape.getClass().getName();

      //remove "java.awt."
      String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
      jsonWriter.put("type", shortenedClassName);
    }

    jsonWriter.put("id", id);
    return jsonWriter;
  }

  public ArrayList<JSONShape> condenseFeatureList(ArrayList<Shape> features) {
    ArrayList<JSONShape> newFeatureList = new ArrayList<>();

    //all lines which aren't complete shapes (ellipses, circles, etc. are excluded)
    ArrayList<BasicLine> linePool = new ArrayList<>();

    //used to build one shape out of several lines
    ArrayList<BasicLine> singleShapeAsLines = new ArrayList<>();

    //add all lines to line pool
    for (Shape feature : features) {
      if (feature instanceof Arc2D arc2d) {
        linePool.add(new BasicLine(arc2d));
      } else if (feature instanceof Line2D line2d) {
        linePool.add(new BasicLine(line2d));
      } else {
        newFeatureList.add(JSONShapeFactory.createJSONShapeFromCondensedShape(feature)); //feature is already condensed (ellipse, circle, etc.)
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
          newFeatureList.add(JSONShapeFactory.createJSONShape(singleShapeAsLines));
          singleShapeAsLines.clear();
        }
      }
    }

    return newFeatureList;
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
