package com.example.elk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents JSON shapes which have a java.awt.Shape counterpart.
 */
public class JSONShape {
    //required parameters
    protected List<BasicLine> lines = new ArrayList<>();
    protected int id;

    private Shape source; //if a shape has a java.awt counterpart (such as circle or rectangle, but not trapezoid or freehand), store it here

    protected boolean multipleRadius = false;//used for shapes where different corners may have different angle radii

    protected static int UNIQUE_ID = 0;

    protected double centerX;
    protected double centerY;

    protected JSONShape(){
        //hide useless constructor
    }

    public JSONShape(Shape shape, List<BasicLine> visualLines) {
        source = shape;
        lines.addAll(visualLines);

        assignMultipleRadius();

        this.centerX = JSONShapeFactory.calculateXCoord(visualLines);
        this.centerY = JSONShapeFactory.calculateYCoord(visualLines);

        id = UNIQUE_ID;
        UNIQUE_ID++;
    }

    public JSONShape(Shape feature) {//used for features where the draw data and table data is the same (circles)
        source = feature;

        centerX = feature.getBounds2D().getCenterX();
        centerY = feature.getBounds2D().getCenterY();

        id = UNIQUE_ID;
        UNIQUE_ID++;
    }

    protected void assignMultipleRadius() {
        //populate curves list
        ArrayList<Arc2D> curves = new ArrayList<>();
        lines.forEach(line -> {
            if (line.getSource() instanceof Arc2D){
                curves.add((Arc2D) line.getSource());
            }
        });

        if (curves.isEmpty()){
            return;
        }

        //check if arc widths/heights are consistent
        boolean isArcWidthConsistent = true;
        boolean isArcHeightConsistent = true;

        //TODO: verify that getWidth() is the correct function
        double arcWidth = curves.get(0).getWidth();
        double arcHeight = curves.get(0).getHeight();
        final double TOLERANCE = 0.01;
        for (Arc2D arc : curves) {
            if (Math.abs(arc.getWidth()  - arcWidth) < TOLERANCE){
                isArcWidthConsistent = false;
            }
            if (Math.abs(arc.getHeight()  - arcHeight) < TOLERANCE){
                isArcHeightConsistent = false;
            }
        }

        if (!isArcHeightConsistent || !isArcWidthConsistent){
            multipleRadius = true;
        }
    }

    /**
     * Write the entire shape into JSON.
     * This includes the table data of the overall shape and the drawing data of its component lines.
     * @return the table and drawing data of one JSONShape
     */
    public JSONObject writeJSONShape(){
        JSONObject jsonWriter = new JSONObject();
        JSONArray arr = new JSONArray();
        //shape was parsed from a group of lines
        if (!lines.isEmpty()){
            //output full shape data (table data)
            jsonWriter.put("table", writeJSONComponent(source, id)); //todo: won't work for trapezoids, etc.

            //output individual line data (drawing data)
            arr = new JSONArray();
            for (BasicLine line : lines){
                arr.add(writeJSONComponent(line.getSource(), id));
            }
            jsonWriter.put("drawing", arr);
        }
        //shape was already condensed when parsed from file (circle, ellipse, etc.)
        else{
            //table data and drawing data are the same
            jsonWriter.put("table", writeJSONComponent(source, id));
            arr.add(writeJSONComponent(source, id));
            jsonWriter.put("drawing", arr);
        }

        return jsonWriter;
    }

    public Optional<Shape> getShape(){
        return Optional.of(source);
    }

    /**
     * Write the JSON data for a single component within a full shape.
     * @param shape the shape being parsed into JSON
     * @param id the id of the component. A full shape shares an id with all of its line components.
     * @return the JSON data as a JSONObject
     */
    public static JSONObject writeJSONComponent(Shape shape, int id) { //Add the shape to the feature list in JSON format
        JSONObject jsonWriter = new JSONObject();
        if (shape instanceof Line2D.Double line2D) { //if the shape is a line
            jsonWriter.put("length", Math.sqrt((Math.pow(line2D.x2 - line2D.x1, 2)) + Math.pow(line2D.y2 - line2D.y1, 2)));
            jsonWriter.put("startX", line2D.x1);
            jsonWriter.put("startY", line2D.y1);
            jsonWriter.put("endX", line2D.x2);
            jsonWriter.put("endY", line2D.y2);
            jsonWriter.put("type", "line2D");
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

            jsonWriter.put("type", "arc2D");
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
            jsonWriter.put("type", "rectangle");
        } else if (shape instanceof RoundRectangle2D.Double roundRect) { //if the shape is a rectangle with radius corners
            jsonWriter.put("width", roundRect.width);
            jsonWriter.put("height", roundRect.height);
            jsonWriter.put("centerX", roundRect.getCenterX());
            jsonWriter.put("centerY", roundRect.getCenterY());
            jsonWriter.put("area", roundRect.width * roundRect.height);
            jsonWriter.put("cornerRadius", roundRect.getArcHeight());
            jsonWriter.put("type", "roundRectangle");
        } else { // default to this if the shape does not fall under any category
            String fullClassName = shape.getClass().getName();

            //remove "java.awt."
            String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

            //set first char to lowercase
            shortenedClassName = Character.toLowerCase(shortenedClassName.charAt(0)) + shortenedClassName.substring(1);
            jsonWriter.put("type", shortenedClassName);
        }

        jsonWriter.put("id", id);
        return jsonWriter;
    }

    public double getCenterX(){
        return centerX;
    }

    public double getCenterY(){
        return centerY;
    }
}
