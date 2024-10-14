package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
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

  public Features(){
    featureList = null;
  }

//  public com.example.mathiasdiepricingtool.Features(ArrayList<Shape> shapeList){
//    featureList = (Shape[]) shapeList.toArray();
//    System.out.print(featureList.toString());
//  }

  public void setFeatureList(ArrayList<Shape> shapeArrayList){ // set feature list variable to the shapes list in DXFReader
    featureList = shapeArrayList;
    String out = "";
    for(Shape feature : featureList){
      System.out.println(feature);
      String className = feature.getClass().getName().substring(feature.getClass().getName().lastIndexOf(".") + 1, feature.getClass().getName().lastIndexOf("$"));
      switch(className){
        case "Line2D" -> {
          System.out.print(ANSI_GREEN + className + ANSI_RESET);
          System.out.println("\n\t" + ANSI_BLUE + "Length: " + ANSI_RESET + Math.sqrt((Math.pow(((Line2D.Double) feature).x2 - ((Line2D.Double) feature).x1, 2)) + Math.pow(((Line2D.Double) feature).y2 - ((Line2D.Double) feature).y1, 2)));
          out += "\n\t" + ANSI_BLUE + "Length: " + ANSI_RESET + Math.sqrt((Math.pow(((Line2D.Double) feature).x2 - ((Line2D.Double) feature).x1, 2)) + Math.pow(((Line2D.Double) feature).y2 - ((Line2D.Double) feature).y1, 2));
        }case "Arc2D" -> {
          System.out.print(ANSI_GREEN + className + ANSI_RESET);
          double angleRad = Math.toRadians(Math.abs((((Arc2D.Double) feature).getAngleStart() - ((Arc2D.Double) feature).getAngleExtent())));
          double radius = ((Arc2D.Double) feature).width/2*Math.sin(angleRad/2);
          double arcLength = radius * angleRad;
          System.out.println("\n\t" + ANSI_BLUE + "Radius: " + ANSI_RESET + radius + ANSI_BLUE + "\n\tArc Length: " + ANSI_RESET + arcLength);
          out += "\n\t" + ANSI_BLUE + "Radius: " + ANSI_RESET + radius + ANSI_BLUE + "\n\tArc Length: " + ANSI_RESET + arcLength;
        }case "Ellipse2D" -> {
          System.out.print(ANSI_GREEN + className + ANSI_RESET);
          double a = ((Ellipse2D.Double) feature).height/2;
          double b = ((Ellipse2D.Double) feature).width/2;

          double circum = Math.PI*(a+b)*(3*(Math.pow(a-b,2))/(Math.pow(a+b,2))*(Math.sqrt(-3*(Math.pow(a-b,2)/Math.pow(a+b,2))+4)+10)+1);
          System.out.print(ANSI_BLUE + "\n\tCircumference: " + ANSI_RESET + circum );
          out += ANSI_BLUE + "\n\tCircumference: " + ANSI_RESET + circum;

          double area;
          if(a==b){
            area = Math.PI * Math.pow(a,2);
          }else{
            area = Math.PI * a * b;
          }
          System.out.println(ANSI_BLUE + "\n\tArea: " + ANSI_RESET + area );
        } default -> {
          System.out.println(className);
          out += className;
        }
      }
    }
    condenseFeatureList();
  }

  /**
   * Connect arcs and lines together into rounded rectangles, triangles, etc.
   */
  private void condenseFeatureList() {
    //list of arcs/lines which have start/end points which overlap
    ArrayList<Shape> newFeatureList = new ArrayList<>();

    ArrayList<BasicLine> linePool = new ArrayList<>(); //all lines/arcs

    //temp list for building a single shape (each line connects to the next)
    ArrayList<BasicLine> singleShapeAsLines = new ArrayList<>();


    //add lines to linePool
    for (Shape shape : featureList) {
      //ignore shapes which are not arcs or lines, as those are already condensed
      BasicLine tempLine;

      //typecast to necessary object
      if (shape instanceof Arc2D) {
        tempLine = new BasicLine(((Arc2D) shape).getStartPoint(), ((Arc2D) shape).getEndPoint());
      } else if (shape instanceof Line2D) {
        tempLine = new BasicLine(((Line2D) shape).getP1(), ((Line2D) shape).getP2());
      } else {
        newFeatureList.add(shape);
        continue;
      }
      linePool.add(tempLine);
    }

      //loop through components to see if shape's start/end points align with current pool

      //keep looping over collection until the number of lines which link together is 1 (essentially empty)

      //add lines from pool to singleShapeAsLines, which represents a collection of lines which all connect to each other





//    do{
//      singleShapeAsLines.clear();
//      for (BasicLine poolLine : linePool){
//        for (BasicLine tempLine : linePool){
//          if (tempLine.isLinkedWith(poolLine)) {
//          singleShapeAsLines.add(tempLine);
//
//          //submit the composite components as a complete shape if all components connect to each other in a loop
//          if (BasicLine.isOneLinkedShape(singleShapeAsLines)) {
//            //TODO: assume that we are building a rectangle for now, will need to support several other shapes later
//            newFeatureList.add(new RoundRectangle2D.Double(10, 20, 30, 40, 50, 60));
//            break;
//          }
//        }
//      }
//    }
//    while (singleShapeAsLines.size() > 1);
    featureList = newFeatureList;
  }
  //public String getFeatureListAsString(){
//    String temp = "";
//    for(Shape item : featureList){
//      temp += item.toString() + "\n";
//    }
//    return temp;
//  }
  }

