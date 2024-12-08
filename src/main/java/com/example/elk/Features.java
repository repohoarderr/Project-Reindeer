package com.example.elk;

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
      }
      else if (feature instanceof Path2D.Double path2d){
        linePool.addAll(BasicLine.path2DToLines(path2d));
      }else {
        newFeatureList.add(JSONShapeFactory.createJSONShapeFromCondensedShape(feature)); //feature is already condensed (ellipse, circle, etc.)
      }
    }

    BasicLine poolLine;
    BasicLine shapeLine;

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
          newFeatureList.add(JSONShapeFactory.createJSONShape(singleShapeAsLines));
          singleShapeAsLines.clear();
        }
      }
      //we've stopped categorizing lines and would be stuck in an infinite loop trying to categorize lines which don't connect, so break
      if (linePool.size() == oldSize){
        break;
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
