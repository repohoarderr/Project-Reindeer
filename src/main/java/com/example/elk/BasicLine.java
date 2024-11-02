package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collection;
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
    private Shape source; //either a Line2D, Arc2D, or Path2D

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


    public static List<BasicLine> path2DToLines(Path2D.Double path2d) {
        ArrayList<BasicLine> lines = new ArrayList<>();
        PathIterator pathIterator = path2d.getPathIterator(new AffineTransform());

        double[] coords = new double[6]; //pass coords into currentSegment() to fill coords w/ data
        Point2D prevPoint = null;
        Point2D startPoint;

        //adapted from https://stackoverflow.com/questions/47728519/getting-the-coordinate-pairs-of-a-path2d-object-in-java
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    System.out.printf("move to x1=%f, y1=%f\n",
                            coords[0], coords[1]);
                    startPoint = new Point2D.Double(coords[0], coords[1]);
                    prevPoint = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.printf("line to x1=%f, y1=%f\n",
                            coords[0], coords[1]);
                    lines.add(new BasicLine(new Line2D.Double(prevPoint, new Point2D.Double(coords[0], coords[1]))));
                    prevPoint = new Point2D.Double(coords[0], coords[1]);
                    break;
//                case PathIterator.SEG_QUADTO: //TODO: not sure if we need support for this
//                    System.out.printf("quad to x1=%f, y1=%f, x2=%f, y2=%f\n",
//                            coords[0], coords[1], coords[2], coords[3]);
//                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.printf("cubic to x1=%f, y1=%f, x2=%f, y2=%f, x3=%f, y3=%f\n",
                            coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    lines.add(new BasicLine(new Arc2D.Double()));//TODO: not sure what values to put here
                    prevPoint = new Point2D.Double(coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.printf("close\n");
                    break;
            }
            pathIterator.next();
        }
        System.out.println();
        System.out.println();
        return lines;
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
