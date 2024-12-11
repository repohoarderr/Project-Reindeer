package com.example.elk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class JSONCustomShape extends JSONShape {
    ShapeType shapeType;

    private JSONCustomShape(){
        //hide useless constructor
    }

    public JSONCustomShape(ShapeType type, List<BasicLine> visualLines){
        this.shapeType = type;
        this.lines.addAll(visualLines);

        this.multipleRadius = isMultipleRadius();

        this.centerX = JSONShapeFactory.calculateXCoord(lines);
        this.centerY = JSONShapeFactory.calculateYCoord(lines);
    }

    public JSONObject writeTableData() { //Add the shape to the feature list in JSON format
        JSONObject jsonWriter = new JSONObject();
        switch(this.getShapeType()){
            case TRIANGLE -> {
                jsonWriter.put("type", "triangle");
                jsonWriter.put("class", "F1C");
            }
            case ROUND_TRIANGLE -> {
                jsonWriter.put("multipleRadius", this.multipleRadius);
                jsonWriter.put("type", "roundTriangle");
                jsonWriter.put("class", "F1C");
            }
            case TRAPEZOID -> {
                jsonWriter.put("type", "trapezoid");
                jsonWriter.put("class", "F1A");
            }
            case ROUND_TRAPEZOID -> {
                jsonWriter.put("type", "roundTrapezoid");
                jsonWriter.put("class", "F1A");
            }
            case OBLONG ->{
                jsonWriter.put("multipleRadius", this.multipleRadius);
                jsonWriter.put("type", "oblong");
                jsonWriter.put("class", "F1B");

            }
            case RADIUS_NOTCH ->{
                jsonWriter.put("type", "radiusNotch");
                jsonWriter.put("class", "F17");
            }
            case MITERED_NOTCH ->{
                jsonWriter.put("type", "miteredNotch");
            }
            case CORNER_NOTCH ->{
                jsonWriter.put("type", "cornerNotch");
            }
            case CHAMFERED_CORNER ->{
                jsonWriter.put("type", "chamferedCorner");
            }
            case FREEHAND -> {
                jsonWriter.put("type", "freehand");
                jsonWriter.put("class", "freehand");
            }
        }

        jsonWriter.put("perimeter", getPerimeter());
        jsonWriter.put("centerX", this.getCenterX());
        jsonWriter.put("centerY", this.getCenterY());
        jsonWriter.put("id", id);
        return jsonWriter;
    }

    @Override
    public JSONObject writeJSONShape(){
        JSONObject jsonWriter = new JSONObject();
        JSONArray arr = new JSONArray();

        //output full shape data (table data)
        jsonWriter.put("table", writeTableData());

        //output individual line data (drawing data)
        for (BasicLine line : lines){
            if (line.doDraw()){
                arr.add(writeDrawData(line.getSource(), id));
            }
        }
        jsonWriter.put("drawing", arr);

        return jsonWriter;
    }

    public ShapeType getShapeType(){
        return shapeType;
    }
}
