package com.example.elk;

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

    public BasicLine(Point2D startPoint, Point2D endPoint){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Point2D getStartPoint(){
        return startPoint;
    }

    public Point2D getEndPoint(){
        return endPoint;
    }

    public boolean isLinkedWith(BasicLine other){
        return this.startPoint.equals(other.startPoint) ||
                this.startPoint.equals(other.endPoint) ||
                this.endPoint.equals(other.startPoint) ||
                this.endPoint.equals(other.endPoint);
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
