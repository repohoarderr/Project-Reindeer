package com.example.elk;

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

//  public com.example.mathiasdiepricingtool.Features(ArrayList<Shape> shapeList){
//    featureList = (Shape[]) shapeList.toArray();
//    System.out.print(featureList.toString());
//  }

    public void setFeatureList(ArrayList<Shape> shapeArrayList) { // set feature list variable to the shapes list in DXFReader
        featureList = shapeArrayList;
        String out = "";
        for (Shape feature : featureList) {
            System.out.println(feature);
            String className = feature.getClass().getName().substring(feature.getClass().getName().lastIndexOf(".") + 1, feature.getClass().getName().lastIndexOf("$"));
            switch (className) {
                case "Line2D" -> {
                    System.out.print(ANSI_GREEN + className + ANSI_RESET);
                    System.out.println("\n\t" + ANSI_BLUE + "Length: " + ANSI_RESET + Math.sqrt((Math.pow(((Line2D.Double) feature).x2 - ((Line2D.Double) feature).x1, 2)) + Math.pow(((Line2D.Double) feature).y2 - ((Line2D.Double) feature).y1, 2)));
                    out += "\n\t" + ANSI_BLUE + "Length: " + ANSI_RESET + Math.sqrt((Math.pow(((Line2D.Double) feature).x2 - ((Line2D.Double) feature).x1, 2)) + Math.pow(((Line2D.Double) feature).y2 - ((Line2D.Double) feature).y1, 2));
                }
                case "Arc2D" -> {
                    System.out.print(ANSI_GREEN + className + ANSI_RESET);
                    double angleRad = Math.toRadians(Math.abs((((Arc2D.Double) feature).getAngleStart() - ((Arc2D.Double) feature).getAngleExtent())));
                    double radius = ((Arc2D.Double) feature).width / 2 * Math.sin(angleRad / 2);
                    double arcLength = radius * angleRad;
                    System.out.println("\n\t" + ANSI_BLUE + "Radius: " + ANSI_RESET + radius + ANSI_BLUE + "\n\tArc Length: " + ANSI_RESET + arcLength);
                    out += "\n\t" + ANSI_BLUE + "Radius: " + ANSI_RESET + radius + ANSI_BLUE + "\n\tArc Length: " + ANSI_RESET + arcLength;
                }
                case "Ellipse2D" -> {
                    System.out.print(ANSI_GREEN + className + ANSI_RESET);
                    double a = ((Ellipse2D.Double) feature).height / 2;
                    double b = ((Ellipse2D.Double) feature).width / 2;

                    double circum = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2)) / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
                    System.out.print(ANSI_BLUE + "\n\tCircumference: " + ANSI_RESET + circum);
                    out += ANSI_BLUE + "\n\tCircumference: " + ANSI_RESET + circum;

                    double area;
                    if (a == b) {
                        area = Math.PI * Math.pow(a, 2);
                    } else {
                        area = Math.PI * a * b;
                    }
                    System.out.println(ANSI_BLUE + "\n\tArea: " + ANSI_RESET + area);
                }
                default -> {
                    System.out.println(className);
                    out += className;
                }
            }
        }
        condenseFeatureList();
    }

    private void condenseFeatureList() {
        ArrayList<Shape> newFeatureList = new ArrayList<>();
        ArrayList<BasicLine> linePool = new ArrayList<>();

        ArrayList<BasicLine> singleShapeAsLines = new ArrayList<>();

        //add all lines to line pool
        for (Shape feature : featureList) {
            if (feature instanceof Arc2D){
                linePool.add(new BasicLine((Arc2D) feature));
            }
            else if (feature instanceof Line2D){
                linePool.add(new BasicLine((Line2D) feature));
            }
            else{
                newFeatureList.add(feature); //feature is already condensed (ellipse, circle, etc.)
            }
        }

        BasicLine poolLine;
        BasicLine shapeLine;
        while(!linePool.isEmpty()){
            for (int i = 0; i < linePool.size(); i++) {
                poolLine = linePool.get(i);

                if (singleShapeAsLines.isEmpty()){
                    singleShapeAsLines.add(poolLine);
                    linePool.remove(poolLine);
                    i--;
                    continue;
                }

                for (int ii = 0; ii < singleShapeAsLines.size(); ii++) {
                    shapeLine = singleShapeAsLines.get(ii);
                    if (poolLine.equals(shapeLine)){
                        continue;
                    }
                    if (poolLine.isLinkedWith(shapeLine)){
                        singleShapeAsLines.add(poolLine);
                        linePool.remove(poolLine);
                        break;
                    }
                }

                if (BasicLine.isOneLinkedShape(singleShapeAsLines)){
                    //TODO: very much a temp value
                    newFeatureList.add(ShapeFactory.createShape(singleShapeAsLines));
                    singleShapeAsLines.clear();
                }
            }
        }

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

