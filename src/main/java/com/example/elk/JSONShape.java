package com.example.elk;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JSONShape {
    //required parameters
    private ShapeType type;
    private List<BasicLine> lines;
    private int id;

    //optional parameters
    private Shape source; //if a shape has a java.awt version (such as circle or rectangle, but not trapezoid or freehand), store it here
    private boolean kissCut = false;

    private boolean multipleRadius = false;//used for shapes where different corners may have different angle radii
    private List<Double> cornerRadii = new ArrayList<>();

    static int UNIQUE_ID = 0;

    private JSONShape(){
        //hide useless constructor
    }

    public JSONShape(Shape shape, List<BasicLine> visualLines) {
        source = shape;
        lines = visualLines;
        id = UNIQUE_ID;
        UNIQUE_ID++;
    }

    public JSONShape(Shape feature) {//used for features where the draw data and table data is the same (circles)
        source = feature;
        id = UNIQUE_ID;
        UNIQUE_ID++;
    }

    public JSONObject writeJSON(){
        JSONObject jsonWriter = new JSONObject();
        //shape was parsed from a group of lines
        if (lines != null){
            //output full shape data (table data)
            jsonWriter.put("table", Features.shapeToJSON(source, id)); //todo: won't work for trapezoids, etc.

            //output individual line data (drawing data)
            //TODO: figure out why polygons don't have line data
            for (BasicLine line : lines){
                jsonWriter.put("drawing", Features.shapeToJSON(line.getSource(), id));
            }
        }
        //shape was already condensed when parsed from file (circle, ellipse, etc.)
        else{
            //table data and drawing data are the same
            jsonWriter.put("table", Features.shapeToJSON(source, id));
            jsonWriter.put("drawing", Features.shapeToJSON(source, id));
        }

        return jsonWriter;
    }

    public void enableKissCut(){
        kissCut = true;
    }

    Optional<Shape> getShape(){
        return Optional.of(source);
    }
}
