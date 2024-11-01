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

        this.assignMultipleRadius();
    }

    public static JSONObject writeJSONComponent(JSONCustomShape customShape, int id) { //Add the shape to the feature list in JSON format
        JSONObject jsonWriter = new JSONObject();
        switch(customShape.getShapeType()){
            case TRIANGLE -> {
            }
            case ROUND_TRIANGLE -> {
            }
            case TRAPEZOID -> {
            }
            case ROUND_TRAPEZOID -> {
            }
            case FREEHAND -> {
                jsonWriter.put("centerX", customShape.getCenterX());
                jsonWriter.put("centerY", customShape.getCenterY());
                jsonWriter.put("type", "freehand");
            }
        }

        jsonWriter.put("id", id);
        return jsonWriter;
    }

    @Override
    public JSONObject writeJSONShape(){
        JSONObject jsonWriter = new JSONObject();
        JSONArray arr = new JSONArray();

        //output full shape data (table data)
        jsonWriter.put("table", writeJSONComponent(this, id));

        //output individual line data (drawing data)
        for (BasicLine line : lines){
            arr.add(writeJSONComponent(line.getSource(), id));
        }
        jsonWriter.put("drawing", arr);

        return jsonWriter;
    }

    public ShapeType getShapeType(){
        return shapeType;
    }
}
