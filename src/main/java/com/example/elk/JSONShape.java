package com.example.elk;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

public class JSONShape {
    //required parameters
    private ShapeType type;
    private List<BasicLine> lines;
    private int id;

    //optional parameters
    private Shape source; //if a shape has a java.awt version (such as circle or rectangle, but not trapezoid or freehand), store it here
    private boolean multipleRadius;//used for shapes where different corners may have different angle radii
    private boolean kissCut;
    private List<Double> cornerRadii;

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

    Optional<Shape> getShape(){
        return Optional.of(source);
    }
}
