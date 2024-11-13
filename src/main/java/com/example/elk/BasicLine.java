package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents either a Line2D or an Arc2D.
 * Used to simplify comparison between these two classes and
 * help condense collections of lines into composite objects such as RoundRectangle2D.Double.
 */
public class BasicLine implements Comparable<BasicLine> {
    private final Point2D startPoint;
    private final Point2D endPoint;
    private final Shape source;
    private boolean draw = true;

    public BasicLine(Line2D src) {
        source = src;
        startPoint = src.getP1();
        endPoint = src.getP2();
    }

    public BasicLine(Arc2D src) {
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
                case PathIterator.SEG_QUADTO:
                    System.out.printf("quad to x1=%f, y1=%f, x2=%f, y2=%f\n",
                            coords[0], coords[1], coords[2], coords[3]);
                    lines.add(new BasicLine(new Arc2D.Double()));//TODO: not sure what values to put here
                    prevPoint = new Point2D.Double(coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.printf("cubic to x1=%f, y1=%f, x2=%f, y2=%f, x3=%f, y3=%f\n",
                            coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    lines.add(new BasicLine(new Arc2D.Double()));//TODO: not sure what values to put here

                    //uncomment this line to approximate curves w/ straight lines
                    //lines.add(new BasicLine(new Line2D.Double(prevPoint, new Point2D.Double(coords[4], coords[5]))));
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

    /**
     * From a list of parallel and in-line lines, return a longer line which connects all lines.
     * ex. the lines ----    ------            ------ would be merged to --------------------------------
     *
     * @param list the lines to be merged
     * @return the merged line, with its draw boolean set to false
     */
    public static BasicLine createMergedLine(List<BasicLine> list) {
        Point2D bestStartPoint = list.getFirst().getStartPoint();
        Point2D bestEndpoint = list.getLast().getEndPoint();
        double maxDistance = bestStartPoint.distance(bestEndpoint);

        for (BasicLine line : list) {
            for (BasicLine lineLine : list) {
                if (line == lineLine) {
                    continue;
                }
                if (line.getStartPoint().distance(lineLine.getStartPoint()) > maxDistance) {
                    bestStartPoint = line.getStartPoint();
                    bestEndpoint = lineLine.getStartPoint();
                    maxDistance = bestStartPoint.distance(bestEndpoint);
                }
                if (line.getEndPoint().distance(lineLine.getStartPoint()) > maxDistance) {
                    bestStartPoint = line.getEndPoint();
                    bestEndpoint = lineLine.getStartPoint();
                    maxDistance = bestStartPoint.distance(bestEndpoint);
                }
                if (line.getStartPoint().distance(lineLine.getEndPoint()) > maxDistance) {
                    bestStartPoint = line.getStartPoint();
                    bestEndpoint = lineLine.getEndPoint();
                    maxDistance = bestStartPoint.distance(bestEndpoint);
                }
                if (line.getEndPoint().distance(lineLine.getEndPoint()) > maxDistance) {
                    bestStartPoint = line.getEndPoint();
                    bestEndpoint = lineLine.getEndPoint();
                    maxDistance = bestStartPoint.distance(bestEndpoint);
                }
            }
        }
        BasicLine result = new BasicLine(new Line2D.Double(bestStartPoint, bestEndpoint));
        result.draw = false;
        return result;
    }

    public Shape getSource() {
        return source;
    }

    public Point2D getStartPoint() {
        return startPoint;
    }

    public Point2D getEndPoint() {
        return endPoint;
    }

    private boolean nearlyEquals(Point2D point1, Point2D point2, double tolerance) {
        return Math.abs(point1.getX() - point2.getX()) < tolerance &&
                Math.abs(point1.getY() - point2.getY()) < tolerance;
    }

    public boolean isLinkedWith(BasicLine other) {
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

    @Override
    public int compareTo(BasicLine o) {
        if (this.getLength() > o.getLength()) {
            return 1;
        } else if (this.getLength() < o.getLength()) {
            return -1;
        }

        return 0;
    }

    public double getLength() {
        if (source instanceof Line2D line) {
            return line.getP1().distance(line.getP2());
        } else if (source instanceof Arc2D.Double arc) {
            double extentRad = Math.toRadians(arc.extent);
            double radius = arc.width / 2;
            double angleRad = Math.abs(extentRad);
            return radius * angleRad;
        }
        return 0;
    }

    /**
     * Return true if two lines are "in line" with each other. In other words, they are two line segments which are part of a larger line.
     * @param listLine the line being compared with "this"
     * @return true if lines are "in line", false if otherwise
     */
    public boolean isInLineWith(BasicLine listLine) {
        if (this.source instanceof Arc2D ||
                listLine.source instanceof Arc2D) {
            throw new UnsupportedOperationException("Arcs do not have a slope");
        }
        //slope of two lines must be the same
        final double TOLERANCE = 0.001;
        if (Math.abs(this.getSlope() - listLine.getSlope()) > TOLERANCE) {
            return false;
        }

        //slope of one line's point to the other line's point must match the slope of the lines themselves
        Line2D thisLineSrc = (Line2D) source;
        Line2D listLineSrc = (Line2D) listLine.source;

        double deltaY = thisLineSrc.getY2() - listLineSrc.getY1();
        double deltaX = thisLineSrc.getX2() - listLineSrc.getX1();

        if (deltaX == 0) {
            //check to see if all lines are straight up and down
            return listLine.getSlope() == Double.POSITIVE_INFINITY;
        }

        return Math.abs((deltaY / deltaX) - listLine.getSlope()) < TOLERANCE;
    }

    public double getSlope() {
        if (this.source instanceof Arc2D) {
            throw new UnsupportedOperationException("Arcs do not have a slope");
        }
        Line2D lineSrc = (Line2D) source;
        double deltaY = lineSrc.getY2() - lineSrc.getY1();
        double deltaX = lineSrc.getX2() - lineSrc.getX1();

        final double TOLERANCE = 0.001;
        if (Math.abs(deltaX) <= TOLERANCE) {//straight line up and down
            return Double.POSITIVE_INFINITY;
        }

        return deltaY / deltaX;
    }

    public boolean doDraw() {
        return draw;
    }
}
