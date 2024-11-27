package com.example.elk;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper for line objects, particularly Line2D, Arc2D, QuadCurve2D, and CubicCurve2D.
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

    public BasicLine(QuadCurve2D src){
        source = src;
        startPoint = src.getP1();
        endPoint = src.getP2();
    }

    public BasicLine(CubicCurve2D src){
        source = src;
        startPoint = src.getP1();
        endPoint = src.getP2();
    }

    public static List<BasicLine> path2DToLines(Path2D.Double path2d) {
        ArrayList<BasicLine> lines = new ArrayList<>();
        PathIterator pathIterator = path2d.getPathIterator(new AffineTransform());

        double[] coords = new double[6]; //pass coords into currentSegment() to fill coords w/ data
        Point2D prevPoint = null;

        //adapted from https://stackoverflow.com/questions/47728519/getting-the-coordinate-pairs-of-a-path2d-object-in-java
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    prevPoint = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    Point2D newPoint = new Point2D.Double(coords[0], coords[1]);
                    lines.add(new BasicLine(new Line2D.Double(prevPoint, newPoint)));
                    prevPoint = newPoint;
                    break;
                case PathIterator.SEG_QUADTO:
                    QuadCurve2D quadCurve2D = new QuadCurve2D.Double(
                            prevPoint.getX(), prevPoint.getY(), coords[0], coords[1],
                            coords[2], coords[3]);
                    lines.add(new BasicLine(quadCurve2D));

                    prevPoint = quadCurve2D.getP2();
                    break;
                case PathIterator.SEG_CUBICTO:
                    CubicCurve2D cubicCurve2D = new CubicCurve2D.Double(
                            prevPoint.getX(), prevPoint.getY(),
                            coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]
                    );

                    lines.add(new BasicLine(cubicCurve2D));
                    prevPoint = cubicCurve2D.getP2();
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
            pathIterator.next();
        }
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

        if (list.stream().anyMatch(line -> !(line.getSource() instanceof Line2D))) {
            throw new UnsupportedOperationException("Only straight lines can be merged by createMergedLine.");
        }

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

    /**
     * BasicLines with the same hash code should be merged.
     * For straight lines, these are lines with the same slope and y-intercept which are likely broken up by a perimeter feature.
     * For arcs, these are lines which are part of the same overall ellipse, which are likely segmented by the software which exported them.
     * @return the hash code of the BasicLine
     */
    @Override
    public int hashCode(){
        if (source instanceof Arc2D arc){
            Arc2D arcCopy = new Arc2D.Double(arc.getX(), arc.getY(),
                    arc.getWidth(), arc.getHeight(),
                    arc.getAngleStart(), 360, Arc2D.OPEN);

            //need to apply rounding to the hash anyway due to lack of precision in x and y values
            //ex. arcs with center x at 0.248 and 0.25 should have the same hash if all other values are equal
            return (int) (arcCopy.getWidth() * 10000019 +
                    arcCopy.getHeight() * 10006721 +
                    arcCopy.getBounds2D().getCenterX() * 10010111 +
                    arcCopy.getBounds2D().getCenterY() * 10000379);
        }
        else if (source instanceof Line2D line){
            // Get the coordinates of the line's two endpoints
            double x1 = line.getX1();
            double y1 = line.getY1();
            double x2 = line.getX2();
            double y2 = line.getY2();

            // Compute the slope m
            double slope;
            if (x2 == x1) {
                // Vertical line: assign an infinite slope
                slope = Double.POSITIVE_INFINITY;
            } else {
                slope = (y2 - y1) / (x2 - x1);
            }

            // Compute the y-intercept b
            double intercept = y1 - slope * x1;

            // Return a combined hash code for the slope and intercept
            return Objects.hash(slope, intercept);
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
        else if (source instanceof QuadCurve2D quadCurve2D){
            //adapted from https://gamedev.stackexchange.com/questions/6009/bezier-curve-arc-length
            Point2D a = quadCurve2D.getP1();
            Point2D b = quadCurve2D.getP2();
            Point2D c = quadCurve2D.getCtrlPt();

            Point2D v = new Point2D.Double( 2*(b.getX() - a.getX()), 2*(b.getY() - a.getY()));
            Point2D w = new Point2D.Double(c.getX() - (2*b.getX()) + a.getX(), c.getY() - (2*b.getY()) + a.getY());

            double uu = 4*(w.getX()*w.getX() + w.getY()*w.getY());

            if(uu < 0.00001)
            {
                return (float) Math.sqrt((c.getX() - a.getX())*(c.getX() - a.getX()) + (c.getY() - a.getY())*(c.getY() - a.getY()));
            }

            double vv = 4*(v.getX()*w.getX() + v.getY()*w.getY());
            double ww = v.getX()*v.getX() + v.getY()*v.getY();

            double t1 = (2*Math.sqrt(uu*(uu + vv + ww)));
            double t2 = 2*uu+vv;
            double t3 = vv*vv - 4*uu*ww;
            double t4 = (2*Math.sqrt(uu*ww));

            //return (float) ((t1*t2 - t3*Math.log(t2+t1) -(vv*t4 - t3*Math.log(vv+t4))) / (8*Math.pow(uu, 1.5)));
            return 0; //TODO: not tested yet
        }
        else if (source instanceof CubicCurve2D cubicCurve2D){
            double length = 0.0;
            int segments = 1000;
            double prevX = cubicCurve2D.getX1();
            double prevY = cubicCurve2D.getY1();

            for (int i = 1; i <= segments; i++) {
                double t = i / (double) segments;
                double x = getCubicBezierX(cubicCurve2D, t);
                double y = getCubicBezierY(cubicCurve2D, t);

                // Calculate distance from the previous point to the current point
                double dx = x - prevX;
                double dy = y - prevY;
                length += Math.sqrt(dx * dx + dy * dy);

                prevX = x;
                prevY = y;
            }

//            return 0; TODO: not tested
            return length;
        }
        return 0;
    }

    private static double getCubicBezierX(CubicCurve2D curve, double t) {
        double x1 = curve.getX1();
        double x2 = curve.getCtrlX1();
        double x3 = curve.getCtrlX2();
        double x4 = curve.getX2();
        return Math.pow(1 - t, 3) * x1 + 3 * Math.pow(1 - t, 2) * t * x2 + 3 * (1 - t) * Math.pow(t, 2) * x3 + Math.pow(t, 3) * x4;
    }

    private static double getCubicBezierY(CubicCurve2D curve, double t) {
        double y1 = curve.getY1();
        double y2 = curve.getCtrlY1();
        double y3 = curve.getCtrlY2();
        double y4 = curve.getY2();
        return Math.pow(1 - t, 3) * y1 + 3 * Math.pow(1 - t, 2) * t * y2 + 3 * (1 - t) * Math.pow(t, 2) * y3 + Math.pow(t, 3) * y4;
    }

    public double getSlope() {
        if (!(this.source instanceof Line2D)) {
            throw new UnsupportedOperationException("This line does not have a slope");
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
