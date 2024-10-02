package com.example.elk;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

/*
 *  This code implements a simple DXF file parser that can read many 2D DXF files containing POLYLINE and SPLINE
 *  outlines such as thoes used for embroidery patterns and input to machines like Silhouette paper cutters.
 *  It's designed to convert POLYLINE and SPLINE sequences into an array of Path2D.Double objects from Java's
 *  geom package.  The parser assumes that DXF file's units are inches, but you can pass the parser a maximum
 *  size value and it will scale down the converted shape so that its maximum dimension fits within this limit.
 *  The code also contains a simple viewer app you can run to try it out on a DXF file.  From the command line
 *  type:
 *          java -jar DXFReader.jar file.dxf
 *
 *  I've tested this code with a variety of simple, 2D DXF files and it's able to read most of them.  However,
 *  the DXF file specification is very complex and I have only implemented a subset of it, so I cannot guarantee
 *  that this code will read all 2D DXF files.  Some instance variables are placeholders for features that have
 *  yet to be implmenented.
 *
 *  I'm publishing this source code under the MIT License (See: https://opensource.org/licenses/MIT)
 *
 *  Copyright 2017 Wayne Holder
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 *  documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 *  to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 *  the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 *  THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class DXFReader {
  private static final boolean DEBUG = false;
  private static final boolean INFO = false;
  private static final boolean ANIMATE = false;
  private static boolean drawText;
  private static boolean drawMText;
  private static boolean drawDimen;
  private final ArrayList<DrawItem> entities = new ArrayList<>();
  private ArrayList<Entity> stack = new ArrayList<>();
  public static final Map<String, Block> blockDict = new TreeMap<>();
  private Entity cEntity = null;
  private Rectangle2D bounds;
  public static double uScale = 0.039370078740157; // default to millimeters as units
  private String units = "millimeters";
  private boolean scaled;

  interface AutoPop {
  }


  DXFReader() {
    this("in");
  }

  DXFReader(String dUnits) {
    if ("in".equals(dUnits)) {
      uScale = 1.0;
      units = "inches";
    } else if ("cm".equals(dUnits)) {
      uScale = 0.39370078740157;
      units = "centimeters";
    }
  }

  private void setUnits(String val) {
    if (val != null) {
      switch (Integer.parseInt(val)) {
        case 0:             // unitless (assume millimeters)
          uScale = 0.039370078740157;
          units = "millimeters";
          break;
        case 1:             // inches
          uScale = 1.0;
          units = "inches";
          break;
        case 2:             // feet
          uScale = 1.0 / 12;
          units = "feet";
          break;
        case 3:             // miles
          uScale = 63360.0;
          units = "miles";
          break;
        case 4:             // millimeters
          uScale = 0.039370078740157;
          units = "millimeters";
          break;
        case 5:             // centimeters
          uScale = 0.39370078740157;
          units = "centimeters";
          break;
        case 6:             // meters
          uScale = 39.370078740157;
          units = "meters";
          break;
        case 7:             // kilometers
          uScale = 39370.078740157;
          units = "kilometers";
          break;
        case 8:             // microinches
          uScale = 0.000001;
          units = "microinches";
          break;
        case 9:             // mils
          uScale = 0.001;
          units = "mils";
          break;
        case 10:            // yards
          uScale = 36.0;
          units = "yards";
          break;
        case 11:            // angstroms
          uScale = 3.9370078740157e-9;
          units = "angstroms";
          break;
        case 12:            // nanometers
          uScale = 3.9370078740157e-8;
          units = "nanometers";
          break;
        case 13:            // microns
          uScale = 3.9370078740157e-5;
          units = "microns";
          break;
        case 14:            // decimeters
          uScale = 3.9370078740157;
          units = "decimeters";
          break;
        case 15:            // decameters
          uScale = 393.70078740157;
          units = "decameters";
          break;
        case 16:            // hectometers
          uScale = 3937.007878740157;
          units = "hectometers";
          break;
        case 17:            // gigameters
          uScale = 39370078740.157;
          units = "gigameters";
          break;
        case 18:            // astronomical units
          uScale = 5.89e+12;
          units = "astronomical units";
          break;
        case 19:            // light years
          uScale = 3.725e+17;
          units = "light years";
          break;
        case 20:            // parsecs
          uScale = 1.215e+18;
          units = "parsecs";
          break;
      }
    }
  }

  // Text code
  public void addX(Path2D.Double path, double cx, double cy, double tenth) {
    path.moveTo(cx - tenth, cy - tenth);
    path.lineTo(cx + tenth, cy + tenth);
    path.moveTo(cx + tenth, cy - tenth);
    path.lineTo(cx - tenth, cy + tenth);
  }

  // Provides a way to disable drawing of certain types
  public static boolean doDraw(DrawItem entity) {
    return (!(entity instanceof Text) || drawText) &&
            (!(entity instanceof MultiLineText) || drawMText) &&
            (!(entity instanceof Dimension) || drawDimen);
  }

  /**
   * Enables drawing og TEXT objects (disabled by default)
   *
   * @param enable true to enable
   */
  public void enableText(boolean enable) {
    drawText = enable;
  }

  /**
   * Enables drawing og MTEXT objects (disabled by default)
   *
   * @param enable true to enable
   */
  public void enableMText(boolean enable) {
    drawMText = enable;
  }

  /**
   * Enables drawing og DIMENSION objects (disabled by default)
   *
   * @param enable true to enable
   */
  public void enableDimen(boolean enable) {
    drawDimen = enable;
  }

  // TODO: implement when I understand how this is supposed to work...
  class Hatch extends DrawItem implements AutoPop {
    Hatch(String type) {
      super(type);
    }
  }

  /*
   * Note: code for "DIMENSION" is incomplete
   */

  class Circle extends DrawItem implements AutoPop {
    Ellipse2D.Double circle = new Ellipse2D.Double();
    private double cx, cy, radius;

    Circle(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                  // Center Point X1
          cx = Double.parseDouble(value) * uScale;
          break;
        case 20:                                  // Center Point Y2
          cy = Double.parseDouble(value) * uScale;
          break;
        case 40:                                  // Radius
          radius = Double.parseDouble(value) * uScale;
          break;
      }
    }

    @Override
    Shape getShape() {
      return circle;
    }

    @Override
    void close() {
      circle.setFrame(cx - radius, cy - radius, radius * 2, radius * 2);
    }
  }

  /**
   * Crude implementation of ELLIPSE
   */
  class Ellipse extends DrawItem implements AutoPop {
    RectangularShape ellipse;
    private Shape shape;
    private double cx, cy, mx, my, ratio, start, end;

    Ellipse(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                  // Center Point X1
          cx = Double.parseDouble(value) * uScale;
          break;
        case 11:                                  // Endpoint of major axis X
          mx = Double.parseDouble(value) * uScale;
          break;
        case 20:                                  // Center Point Y2
          cy = Double.parseDouble(value) * uScale;
          break;
        case 21:                                  // Endpoint of major axis Y
          my = Double.parseDouble(value) * uScale;
          break;
        case 40:                                  // Ratio of minor axis to major axis
          ratio = Double.parseDouble(value);
          break;
        case 41:                                  // Start parameter (this value is 0.0 for a full ellipse)
          start = Double.parseDouble(value);
          break;
        case 42:                                  // End parameter (this value is 2pi for a full ellipse)
          end = Double.parseDouble(value);
          break;
      }
    }

    @Override
    Shape getShape() {
      if (false) {
        // Test code
        Path2D.Double path = new Path2D.Double();
        // Draw center point
        addX(path, cx, cy, .2 * uScale);
        // Draw Endpoint of major axis
        addX(path, cx + mx, cy + my, .1 * uScale);
        // Add ellipse
        path.append(shape, false);
        return path;
      } else {
        return shape;
      }
    }

    @Override
    void close() {
      if (start != 0 || end != 0) {
        ellipse = new Arc2D.Double();
        double startAngle = Math.toDegrees(start);
        double endAngle = Math.toDegrees(end);
        // Make angle negative so it runs clockwise when using Arc2D.Double
        ((Arc2D.Double) ellipse).setAngleStart(-startAngle);
        double extent = startAngle - (endAngle < startAngle ? endAngle + 360 : endAngle);
        ((Arc2D.Double) ellipse).setAngleExtent(extent);
      } else {
        ellipse = new Ellipse2D.Double();
      }
      double hoff = Math.abs(Math.sqrt(mx * mx + my * my));
      double voff = Math.abs(hoff * ratio);
      ellipse.setFrame(-hoff, -voff, hoff * 2, voff * 2);
      double angle = Math.atan2(my, mx);
      AffineTransform at = new AffineTransform();
      at.translate(cx, cy);
      at.rotate(angle);
      shape = at.createTransformedShape(ellipse);
    }
  }

  class Arc extends DrawItem implements AutoPop {
    Arc2D.Double arc = new Arc2D.Double(Arc2D.OPEN);
    private double cx, cy, startAngle, endAngle, radius;

    Arc(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                  // Center Point X1
          cx = Double.parseDouble(value) * uScale;
          break;
        case 20:                                  // Center Point Y2
          cy = Double.parseDouble(value) * uScale;
          break;
        case 40:                                  // Radius
          radius = Double.parseDouble(value) * uScale;
          break;
        case 50:                                  // Start Angle
          startAngle = Double.parseDouble(value);
          break;
        case 51:                                  // End Angle
          endAngle = Double.parseDouble(value);
          break;
      }
    }

    @Override
    Shape getShape() {
      return arc;
    }

    @Override
    void close() {
      arc.setFrame(cx - radius, cy - radius, radius * 2, radius * 2);
      // Make angle negative so it runs clockwise when using Arc2D.Double
      arc.setAngleStart(-startAngle);
      double extent = startAngle - (endAngle < startAngle ? endAngle + 360 : endAngle);
      arc.setAngleExtent(extent);
    }
  }

  class Line extends DrawItem implements AutoPop {
    Line2D.Double line;
    private double xStart, yStart, xEnd, yEnd;

    Line(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                              // Line Point X1
          xStart = Double.parseDouble(value) * uScale;
          break;
        case 20:                              // Line Point Y2
          yStart = Double.parseDouble(value) * uScale;
          break;
        case 11:                              // Line Point X2
          xEnd = Double.parseDouble(value) * uScale;
          break;
        case 21:                              // Line Point Y2
          yEnd = Double.parseDouble(value) * uScale;
          break;
      }
    }

    @Override
    void close() {
      line = new Line2D.Double(xStart, yStart, xEnd, yEnd);
    }

    @Override
    Shape getShape() {
      return line;
    }
  }

  class Polyline extends DrawItem {
    private Path2D.Double path;
    private List<Vertex> points;
    private double firstX, firstY, lastX, lastY;
    private boolean firstPoint = true;
    private boolean close;

    Polyline(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      if (gCode == 70) {
        int flags = Integer.parseInt(value);
        close = (flags & 1) != 0;
      }
    }

    @Override
    void addChild(Entity child) {
      if (child instanceof Vertex) {
        if (points == null) {
          points = new ArrayList<>();
        }
        points.add((Vertex) child);
      }
    }

    @Override
    Shape getShape() {
      return path;
    }

    @Override
    void close() {
      path = new Path2D.Double();
      double bulge = 0.0;
      for (Vertex vertex : points) {
        if (firstPoint) {
          firstPoint = false;
          path.moveTo(firstX = lastX = vertex.xx, firstY = lastY = vertex.yy);
        } else {
          if (bulge != 0) {
            path.append(getArcBulge(lastX, lastY, vertex.xx, vertex.yy, bulge), true);
            lastX = vertex.xx;
            lastY = vertex.yy;
          } else {
            path.lineTo(lastX = vertex.xx, lastY = vertex.yy);
          }
        }
        bulge = vertex.bulge;
      }
      if (close) {
        if (bulge != 0) {
          path.append(getArcBulge(lastX, lastY, firstX, firstY, bulge), true);
        } else {
          path.closePath();
        }
      }
    }
  }

  class Vertex extends Entity {
    double xx, yy, bulge;

    Vertex(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                    // Vertex X
          xx = Double.parseDouble(value) * uScale;
          break;
        case 20:                                    // Vertex Y
          yy = Double.parseDouble(value) * uScale;
          break;
        case 42:                                    // Vertex Bulge factor
          bulge = Double.parseDouble(value);
          break;
      }
    }
  }

  class LwPolyline extends DrawItem implements AutoPop {
    Path2D.Double path;
    List<LSegment> segments = new ArrayList<>();
    LSegment cSeg;
    private double xCp, yCp;
    private boolean hasXcp, hasYcp;
    private boolean close;

    class LSegment {
      private final double dx;
      private final double dy;
      private double bulge;

      LSegment(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
      }
    }

    LwPolyline(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                      // Control Point X
          xCp = Double.parseDouble(value) * uScale;
          hasXcp = true;
          break;
        case 20:                                      // Control Point Y
          yCp = Double.parseDouble(value) * uScale;
          hasYcp = true;
          break;
        case 70:                                      // Flags
          int flags = Integer.parseInt(value);
          close = (flags & 0x01) != 0;
          break;
        case 42:                                      // Bulge factor  (positive = right, negative = left)
          cSeg.bulge = Double.parseDouble(value);
          break;
        case 90:                                      // Number of Vertices
          int vertices = Integer.parseInt(value);
          break;
      }
      if (hasXcp && hasYcp) {
        hasXcp = hasYcp = false;
        segments.add(cSeg = new LSegment(xCp, yCp));
      }
    }

    @Override
    Shape getShape() {
      return path;
    }

    @Override
    void close() {
      path = new Path2D.Double();
      boolean first = true;
      double lastX = 0, lastY = 0, firstX = 0, firstY = 0;
      double bulge = 0;
      for (LSegment seg : segments) {
        if (bulge != 0) {
          path.append(getArcBulge(lastX, lastY, lastX = seg.dx, lastY = seg.dy, bulge), true);
        } else {
          if (first) {
            path.moveTo(firstX = lastX = seg.dx, firstY = lastY = seg.dy);
            first = false;
          } else {
            path.lineTo(lastX = seg.dx, lastY = seg.dy);
          }
        }
        bulge = seg.bulge;
      }
      if (close) {
        if (bulge != 0) {
          path.append(getArcBulge(lastX, lastY, firstX, firstY, bulge), true);
        } else {
          path.lineTo(firstX, firstY);
        }
      }
    }
  }

  class Spline extends DrawItem implements AutoPop {
    Path2D.Double path = new Path2D.Double();
    List<Point2D.Double> cPoints = new ArrayList<>();
    private double xCp, yCp;
    private boolean hasXcp, hasYcp;
    private boolean closed;
    private int numCPs;
    private int degree;

    Spline(String type) {
      super(type);
    }

    @Override
    void addParm(int gCode, String value) {
      switch (gCode) {
        case 10:                                    // Control Point X
          xCp = Double.parseDouble(value) * uScale;
          hasXcp = true;
          break;
        case 20:                                    // Control Point Y
          yCp = Double.parseDouble(value) * uScale;
          hasYcp = true;
          break;
        case 70:                                    // Flags (bitfield)
          // bit 0: Closed spline, bit 1:  Periodic spline, bit 2: Rational spline, bit 3: Planar, bit 4: Linear (planar bit is also set)
          // Examples:
          //    10 = Closed, Periodic, Planar Spline
          //
          int flags = Integer.parseInt(value);
          closed = (flags & 0x01) != 0;
          break;
        case 71:                                    // Degree of the spline curve
          degree = Integer.parseInt(value);
          break;
        case 73:                                    // Number of Control Points
          numCPs = Integer.parseInt(value);
          break;
      }
      if (hasXcp && hasYcp) {
        cPoints.add(new Point2D.Double(xCp, yCp));
        hasXcp = hasYcp = false;
        if (cPoints.size() == numCPs) {
          if (degree == 3) {
            Point2D.Double[] points = cPoints.toArray(new Point2D.Double[0]);
            path.moveTo(points[0].x, points[0].y);
            for (int ii = 1; ii < points.length; ii += 3) {
              path.curveTo(points[ii].x, points[ii].y, points[ii + 1].x, points[ii + 1].y, points[ii + 2].x, points[ii + 2].y);
            }
          }
        } else if (degree == 2) {
          Point2D.Double[] points = cPoints.toArray(new Point2D.Double[0]);
          path.moveTo(points[0].x, points[0].y);
          for (int ii = 1; ii < points.length; ii += 2) {
            path.quadTo(points[ii].x, points[ii].y, points[ii + 1].x, points[ii + 1].y);
          }
        }
      }
    }

    @Override
    Shape getShape() {
      if (closed) {
        path.closePath();
        closed = false;
      }
      return path;
    }
  }


  /**
   * See: http://darrenirvine.blogspot.com/2015/08/polylines-radius-bulge-turnaround.html
   *
   * @param sx    Starting x for Arc
   * @param sy    Starting y for Arc
   * @param ex    Ending x for Arc
   * @param ey    Ending y for Arc
   * @param bulge bulge factor (bulge > 0 = clockwise, else counterclockwise)
   * @return Arc2D.Double object
   */
  private Arc2D.Double getArcBulge(double sx, double sy, double ex, double ey, double bulge) {
    Point2D.Double p1 = new Point2D.Double(sx, sy);
    Point2D.Double p2 = new Point2D.Double(ex, ey);
    Point2D.Double mp = new Point2D.Double((p2.x + p1.x) / 2, (p2.y + p1.y) / 2);
    Point2D.Double bp = new Point2D.Double(mp.x - (p1.y - mp.y) * bulge, mp.y + (p1.x - mp.x) * bulge);
    double u = p1.distance(p2);
    double b = (2 * mp.distance(bp)) / u;
    double radius = u * ((1 + b * b) / (4 * b));
    double dx = mp.x - bp.x;
    double dy = mp.y - bp.y;
    double mag = Math.sqrt(dx * dx + dy * dy);
    Point2D.Double cp = new Point2D.Double(bp.x + radius * (dx / mag), bp.y + radius * (dy / mag));
    double startAngle = 180 - Math.toDegrees(Math.atan2(cp.y - p1.y, cp.x - p1.x));
    double opp = u / 2;
    double extent = Math.toDegrees(Math.asin(opp / radius)) * 2;
    double extentAngle = bulge >= 0 ? -extent : extent;
    Point2D.Double ul = new Point2D.Double(cp.x - radius, cp.y - radius);
    return new Arc2D.Double(ul.x, ul.y, radius * 2, radius * 2, startAngle, extentAngle, Arc2D.OPEN);
  }

  private void push() {
    stack.add(cEntity);
  }

  private void pop() {
    if (cEntity != null) {
      cEntity.close();
    }
    cEntity = stack.remove(stack.size() - 1);
  }

  private void addChildToTop(Entity child) {
    if (stack.size() > 0) {
      Entity top = stack.get(stack.size() - 1);
      if (top != null) {
        top.addChild(child);
      }
    }
  }

  private void addEntity(DrawItem entity) {
    if (cEntity instanceof Block) {
      Block block = (Block) cEntity;
      if (entity instanceof Insert && (block.flags & 2) != 0) {
        push();
        entities.add(entity);
        cEntity = entity;
      } else {
        push();
        block.addEntity(entity);
        cEntity = entity;
      }
    } else {
      push();
      entities.add(entity);
      cEntity = entity;
    }
  }

  private void debugPrint(String value) {
    for (int ii = 0; ii < stack.size(); ii++) {
      System.out.print("  ");
    }
    System.out.println(value);
  }

  public Shape[] parseFile(File file, double maxSize, double minSize) throws IOException {
    stack = new ArrayList<>();
    cEntity = null;
    Scanner lines = new Scanner(new FileInputStream(file));
    while (lines.hasNextLine()) {
      String line = lines.nextLine().trim();
      String value = lines.nextLine().trim();
      int gCode = Integer.parseInt(line);
      if (gCode == 0) {                             // Entity type
        if (cEntity instanceof AutoPop) {
          pop();
        }
        if (DEBUG) {
          debugPrint(value);
        }
        switch (value) {
          case "SECTION":
            cEntity = new Section(value);
            break;
          case "ENDSEC":
            if (cEntity instanceof Section) {
              Section section = (Section) cEntity;
              if ("HEADER".equals(section.sType)) {
                Map<Integer, String> attrs = section.attributes.get("$INSUNITS");
                if (attrs != null) {
                  String units = attrs.get(70);
                  setUnits(units);
                }
                attrs = section.attributes.get("$LUNITS");
                if (attrs != null) {
                  String units = attrs.get(70);
                  setUnits(units);
                }
              }
            }
            cEntity = null;
            stack.clear();
            break;
          case "TABLE":
            push();
            cEntity = new Entity(value);
            break;
          case "ENDTAB":
            pop();
            break;
          case "BLOCK":
            push();
            cEntity = new Block(value);
            break;
          case "ENDBLK":
            pop();
            while ("BLOCK".equals(cEntity.type)) {
              pop();
            }
            break;
          case "SPLINE":
            addEntity(new Spline(value));
            break;
          case "INSERT":
            addEntity(new Insert(value));
            break;
          case "TEXT":
            addEntity(new Text(value));
            break;
          case "MTEXT":
            addEntity(new MultiLineText(value));
            break;
          case "HATCH":
            addEntity(new Hatch(value));
            break;
          case "CIRCLE":
            addEntity(new Circle(value));
            break;
          case "ELLIPSE":
            addEntity(new Ellipse(value));
            break;
          case "ARC":
            addEntity(new Arc(value));
            break;
          case "LINE":
            addEntity(new Line(value));
            break;
          case "DIMENSION":
            addEntity(new Dimension(value));
            break;
          case "POLYLINE":
            addEntity(new Polyline(value));
            break;
          case "LWPOLYLINE":
            addEntity(new LwPolyline(value));
            break;
          case "VERTEX":
            if (cEntity != null && !"VERTEX".equals(cEntity.type)) {
              push();
            }
            addChildToTop(cEntity = new Vertex(value));
            break;
          case "SEQEND":
            while (stack.size() > 0 && !"BLOCK".equals(cEntity.type)) {
              pop();
            }
            break;
        }
      } else {
        if (cEntity != null) {
          if (DEBUG) {
            debugPrint(gCode + ": " + value);
          }
          cEntity.addParm(gCode, value);
        }
      }
    }
    ArrayList<Shape> shapes = new ArrayList<>();
    for (DrawItem entity : entities) {
      if (doDraw(entity)) {
        Shape shape = entity.getShape();
        if (shape != null) {
          shapes.add(shape);
        }
      }
    }
    Features feature = new Features();
    feature.setFeatureList(shapes);
    Shape[] sOut = new Shape[shapes.size()];
    if (!shapes.isEmpty()) {
      for (Shape shape : shapes) {
        bounds = bounds == null ? shape.getBounds2D() : bounds.createUnion(shape.getBounds2D());
      }
      double scale = 1;
      double maxAxis = Math.max(bounds.getWidth(), bounds.getHeight());
      // Limit size to maxSize inches on max dimension
      //TODO: remove scaling things? Might just be for the window
      if (maxSize > 0 && maxAxis > maxSize) {
        scale = maxSize / maxAxis;
        scaled = true;
      }
      // If minSize specified, scale up max dimension to match
      if (minSize > 0 && maxAxis < minSize) {
        scale = minSize / maxAxis;
        scaled = true;
      }
      // Scale, as needed, and flip Y axis
      AffineTransform at = new AffineTransform();
      at.scale(scale, -scale);
      at.translate(-bounds.getMinX(), -bounds.getHeight() - bounds.getMinY());
      for (int ii = 0; ii < shapes.size(); ii++) {
        sOut[ii] = at.createTransformedShape(shapes.get(ii));
      }
    }
    return sOut;
  }

  /*
   * Simple DXF Viewer to test the Parser
   */

  static class DXFViewer extends JPanel implements Runnable {
    private final DecimalFormat df = new DecimalFormat("#0.0#");
    private final double SCREEN_PPI = Toolkit.getDefaultToolkit().getScreenResolution();
    private final Shape[] shapes;
    private final double border = 0.125;
    private final DXFReader dxf;
    private Rectangle2D bounds;

    DXFViewer(String fileName, double maxSize, double minSize) throws IOException {
      dxf = new DXFReader();
      shapes = dxf.parseFile(new File(fileName), maxSize, minSize);
      if (shapes.length > 0) {
        // Create a bounding box that's the union of all shapes in the shapes array
        for (Shape shape : shapes) {
          bounds = bounds == null ? shape.getBounds2D() : bounds.createUnion(shape.getBounds2D());
        }
        if (bounds != null) {
          int wid = (int) Math.round((bounds.getWidth() + border * 2) * SCREEN_PPI);
          int hyt = (int) Math.round((bounds.getHeight() + border * 4) * SCREEN_PPI);   // Hmm.. why * 4 needed?
          setSize(new java.awt.Dimension(Math.max(wid, 640), Math.max(hyt, 400)));
        }
        JFrame frame = new JFrame();
        frame.setTitle(fileName);
        frame.setSize(getSize());
        frame.add(this, BorderLayout.CENTER);
        frame.setResizable(true);
        frame.setVisible(true);
        if (ANIMATE) {
          (new Thread(this)).start();
        }
      } else {
        throw new IllegalStateException("No shapes found in file: " + fileName);
      }
    }

    int frame = 0;

    public void run() {
      while (true) {
        try {
          Thread.sleep(500);
          frame++;
          repaint();
        } catch (InterruptedException ex) {
          ex.printStackTrace();
          return;
        }
      }
    }

    public void paint(Graphics g) {
      java.awt.Dimension d = getSize();
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setBackground(getBackground());
      g2.clearRect(0, 0, d.width, d.height);
      AffineTransform atScale = new AffineTransform();
      atScale.translate(border * SCREEN_PPI, border * SCREEN_PPI);
      atScale.scale(SCREEN_PPI, SCREEN_PPI);
      g2.setColor(Color.black);
      if (ANIMATE) {
        if (shapes != null) {
          int count = 0;
          for (Shape shape : shapes) {
            if (count++ >= frame) {
              break;
            }
            g2.draw(atScale.createTransformedShape(shape));
          }
        }
      } else {
        for (Shape shape : shapes) {
          g2.draw(atScale.createTransformedShape(shape));
        }
      }
      if (INFO) {
        int yOff = 30;
        g2.setFont(new Font("Monaco", Font.PLAIN, 12));
        g2.drawString("Paths:      " + shapes.length, 20, yOff);
        yOff += 15;
        g2.drawString("Location:   " + df.format(dxf.bounds.getX()) + " x " + df.format(dxf.bounds.getY()), 20, yOff);
        yOff += 15;
        g2.drawString("Original:   " + df.format(dxf.bounds.getWidth()) + " x " + df.format(dxf.bounds.getHeight()) + " inches", 20, yOff);
        yOff += 15;
        g2.drawString("Orig Units: " + dxf.units, 20, yOff);
        yOff += 15;
        if (dxf.scaled) {
          g2.drawString("Scaled To: " + df.format(bounds.getWidth()) + " x " + df.format(bounds.getHeight()) + " inches", 20, yOff);
        }
      }
    }
  }

  public static void main(String[] args)  {
    //viewing code (opens up swing viewer)
//    if (args.length < 1) { //if no arguments set, open the file picker
////            System.out.println("Usage: java -jar DXFReader.jar <dxf file>");
//      JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());
//      fileChooser.showOpenDialog(null);
//      try {
//        new DXFViewer(fileChooser.getSelectedFile().toString(), 14, 3);
//      }catch (NullPointerException e){
//        System.exit(0);
//      }
//    } else {
//      new DXFViewer(args[0], 14, 3);
//    }

    JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView());
    fileChooser.addChoosableFileFilter(new FileFilter() {
      public String getDescription() {
        return "DXF Files (*.dxf)";
      }

      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        } else {
          return f.getName().toLowerCase().endsWith(".dxf");
        }
      }
    });
    fileChooser.showOpenDialog(null);
    File file = fileChooser.getSelectedFile();

    TestClass.runThings(file);
  }
}
