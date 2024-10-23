package com.example.elk;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents either a Line2D or an Arc2D.
 * Used to simplify comparison between these two classes and
 * help condense collections of lines into composite objects such as RoundRectangle2D.Double.
 */
public class BasicLine
{
    private Point2D startPoint;
    private Point2D endPoint;
    private Shape source; //either a Line2D or Arc2D

    public BasicLine(Line2D src){
        source = src;
        startPoint = src.getP1();
        endPoint = src.getP2();
    }
    public BasicLine(Arc2D src){
        source = src;
        startPoint = src.getStartPoint();
        endPoint = src.getEndPoint();
    }

    public Shape getSource(){
        return source;
    }

    public Point2D getStartPoint(){
        return startPoint;
    }

    public Point2D getEndPoint(){
        return endPoint;
    }

    public boolean nearlyEquals(Point2D point1, Point2D point2, double tolerance){
        return Math.abs(point1.getX() - point2.getX()) < tolerance &&
                Math.abs(point1.getY() - point2.getY()) < tolerance;
    }

    public boolean isLinkedWith(BasicLine other){
//        return this.startPoint.equals(other.startPoint) ||
//                this.startPoint.equals(other.endPoint) ||
//                this.endPoint.equals(other.startPoint) ||
//                this.endPoint.equals(other.endPoint);

        final double TOLERANCE = 0.001;
        return this != other &&
                (nearlyEquals(this.startPoint, other.startPoint, TOLERANCE) ||
                nearlyEquals(this.startPoint, other.endPoint, TOLERANCE) ||
                nearlyEquals(this.endPoint, other.startPoint, TOLERANCE) ||
                nearlyEquals(this.endPoint, other.endPoint, TOLERANCE));
    }
    public static boolean isOneLinkedShape(List<BasicLine> compositeShapeComponents) {
        //cannot be true with fewer than three lines
        if (compositeShapeComponents.size() < 3) {
            return false;
        }

        //every line needs to be connected to only two other lines
        for (BasicLine line : compositeShapeComponents) {
            ArrayList<BasicLine> matches = new ArrayList<>();
            for (BasicLine innerLine : compositeShapeComponents) {
                if (line.isLinkedWith(innerLine)) {
                    matches.add(line);
                }
            }

            if (matches.size() != 2) {
                return false;
            }
        }
        return true;
    }
}
