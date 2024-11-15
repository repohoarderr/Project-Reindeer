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
    protected List<JSONShape> subFeatures = new ArrayList<>(); //hold list of notches, chamfered corners, etc
    protected int id;

    private Shape source; //if a shape has a java.awt counterpart (such as circle or rectangle, but not trapezoid or freehand), store it here

    protected boolean multipleRadius = false;//used for shapes where different corners may have different angle radii

    protected static int idCounter = 0; //increment this counter every time a shape is created

    protected double centerX;
    protected double centerY;

    protected JSONShape(){
        //hide useless constructor
    }

    public JSONShape(Shape shape, List<BasicLine> visualLines) {
        source = shape;
        lines.addAll(visualLines);

        this.multipleRadius = isMultipleRadius();

        this.centerX = JSONShapeFactory.calculateXCoord(visualLines);
        this.centerY = JSONShapeFactory.calculateYCoord(visualLines);

        id = idCounter;
        idCounter++;
    }

    public JSONShape(Shape feature) {//used for features where the draw data and table data is the same (circles)
        source = feature;

        centerX = feature.getBounds2D().getCenterX();
        centerY = feature.getBounds2D().getCenterY();

        id = idCounter;
        idCounter++;
    }

    /**
     * Add the sub feature, usually a notch or other incision, to the shape.
     * @param feature The feature being added to the shape.
     * @return The set of lines in this which aren't part of any sub feature.
     */
    public List<BasicLine> addSubFeature(JSONShape feature){
        feature.setId(this.id);
        this.subFeatures.add(feature);

        List<BasicLine> subFeatureLines = new ArrayList<>();
        for (JSONShape shape : subFeatures){
            subFeatureLines.addAll(shape.lines);
        }
        List<BasicLine> linesWithoutFeatures = new ArrayList<>(this.lines);
        linesWithoutFeatures.removeAll(subFeatureLines);

        return linesWithoutFeatures;
    }

    private void setId(int id) {
        this.id = id;
        if (this.subFeatures != null && !this.subFeatures.isEmpty()){
            for (JSONShape shape : subFeatures){
                shape.setId(id);
            }
        }
    }

    protected boolean isMultipleRadius() {
        //populate curves list
        ArrayList<Arc2D> curves = new ArrayList<>();
        lines.forEach(line -> {
            if (line.getSource() instanceof Arc2D arc2D){
                curves.add(arc2D);
            }
        });

        if (curves.isEmpty()){
            return false;
        }

        //check if arc angle extent, arc width, and arc height are consistent
        double arcExtent = curves.get(0).getAngleExtent();
        double arcWidth = curves.get(0).getWidth();
        double arcHeight = curves.get(0).getHeight();

        final double TOLERANCE = 0.01;
        for (Arc2D arc : curves) {
            if (Math.abs(arc.getAngleExtent()  - arcExtent) > TOLERANCE ||
                    Math.abs(arc.getWidth()  - arcWidth) > TOLERANCE ||
                    Math.abs(arc.getHeight()  - arcHeight) > TOLERANCE){
                return true;
            }
        }

       return false;
    }

    /**
     * Write the entire shape into JSON.
     * This includes the table data of the overall shape and the drawing data of its component lines.
     * @return the table and drawing data of one JSONShape
     */
    public JSONObject writeJSONShape(){
        JSONObject jsonWriter = new JSONObject();
        JSONArray arr = new JSONArray();
        //if shape was parsed from a group of lines
        if (!lines.isEmpty()){
            //output full shape data (table data)
            jsonWriter.put("table", writeTableData());

            //output individual line data (drawing data)
            arr = new JSONArray();
            for (BasicLine line : lines){
                if (line.doDraw()){
                    arr.add(writeDrawData(line.getSource(), id));
                }
            }
            jsonWriter.put("drawing", arr);
        }
        //shape was already condensed when parsed from file (circle, ellipse, etc.)
        else{
            //table data and drawing data are the same
            jsonWriter.put("table", writeDrawData(source, id));
            arr.add(writeDrawData(source, id));
            jsonWriter.put("drawing", arr);
        }

        return jsonWriter;
    }

    public List<JSONObject> writeJSONSubfeatures() {
        ArrayList<JSONObject> objects = new ArrayList<>();
        for (JSONShape subFeature : subFeatures){
            objects.add(subFeature.writeJSONShape());
        }

        return objects;
    }

    public Optional<Shape> getShape(){
        return Optional.of(source);
    }

    public static JSONObject writeDrawData(Shape shape, int id) {
        JSONObject jsonWriter = new JSONObject();
        switch (shape) {
            case Line2D.Double line2D -> { //if the shape is a line
                jsonWriter.put("length", Math.sqrt((Math.pow(line2D.x2 - line2D.x1, 2)) + Math.pow(line2D.y2 - line2D.y1, 2)));
                jsonWriter.put("startX", line2D.x1);
                jsonWriter.put("startY", line2D.y1);
                jsonWriter.put("endX", line2D.x2);
                jsonWriter.put("endY", line2D.y2);
                jsonWriter.put("type", "line2D");
            }
            case Arc2D.Double arc2D -> { //if the shape is an arc2d
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
            case Ellipse2D.Double ellipse2D -> { //if the shape is an ellipse
                double a = ellipse2D.height / 2;
                double b = ellipse2D.width / 2;
                double circum = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2)) / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
                jsonWriter.put("circumference", circum);
                jsonWriter.put("perimeter", circum);

                if (ellipse2D.getWidth() == ellipse2D.getHeight()) { //check to see if the shape is a circle or oval
                    jsonWriter.put("radius", ellipse2D.getHeight() / 2);
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
            default -> { // default to this if the shape does not fall under any category
                String fullClassName = shape.getClass().getName();

                //remove "java.awt."
                String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

                //set first char to lowercase
                shortenedClassName = Character.toLowerCase(shortenedClassName.charAt(0)) + shortenedClassName.substring(1);
                jsonWriter.put("type", shortenedClassName);
            }
        }

        jsonWriter.put("id", id);
        return jsonWriter;
    }

    public JSONObject writeTableData() {
        JSONObject jsonWriter = new JSONObject();
        switch (source) {
            case Rectangle2D.Double rect -> { //if the shape is a rectangle
                jsonWriter.put("width", rect.width);
                jsonWriter.put("height", rect.height);
                jsonWriter.put("centerX", rect.getCenterX());
                jsonWriter.put("centerY", rect.getCenterY());
                jsonWriter.put("area", rect.width * rect.height);
                jsonWriter.put("type", "rectangle");
            }
            case RoundRectangle2D.Double roundRect -> { //if the shape is a rectangle with radius corners
                jsonWriter.put("width", roundRect.width);
                jsonWriter.put("height", roundRect.height);
                jsonWriter.put("centerX", roundRect.getCenterX());
                jsonWriter.put("centerY", roundRect.getCenterY());
                jsonWriter.put("area", roundRect.width * roundRect.height);
                jsonWriter.put("cornerRadius", roundRect.getArcHeight());
                jsonWriter.put("multipleRadius", this.multipleRadius);
                jsonWriter.put("type", "roundRectangle");
            }
            default -> { // default to this if the shape does not fall under any category
                String fullClassName = source != null ? source.getClass().getName() : "unknown";

                //remove "java.awt."
                String shortenedClassName = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);

                //set first char to lowercase
                shortenedClassName = Character.toLowerCase(shortenedClassName.charAt(0)) + shortenedClassName.substring(1);
                jsonWriter.put("type", shortenedClassName);
            }
        }

        jsonWriter.put("perimeter", getPerimeter());
        jsonWriter.put("id", id);
        return jsonWriter;
    }

    public double getCenterX(){
        return centerX;
    }

    public double getCenterY(){
        return centerY;
    }

    public double getPerimeter(){
        double mainShapePerimeter = 0;
        if (source instanceof Ellipse2D.Double ellipse2d){
            double a = ellipse2d.height / 2;
            double b = ellipse2d.width / 2;
            mainShapePerimeter = Math.PI * (a + b) * (3 * (Math.pow(a - b, 2))
                    / (Math.pow(a + b, 2)) * (Math.sqrt(-3 * (Math.pow(a - b, 2) / Math.pow(a + b, 2)) + 4) + 10) + 1);
        }
        else{
            mainShapePerimeter =  lines.stream()
                    .map(BasicLine::getLength)
                    .reduce(Double::sum)
                    .orElse(0.0);
        }

        double subFeaturesPerimeter = subFeatures.stream()
                .map(JSONShape::getPerimeter)
                .reduce(Double::sum)
                .orElse(0.0);

        return mainShapePerimeter + subFeaturesPerimeter;
    }
}
