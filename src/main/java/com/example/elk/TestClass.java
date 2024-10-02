package com.example.elk;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TestClass {

    public static void runThings(File file) {
        Shape[] shapesList;
        try {
            shapesList = new DXFReader().parseFile(file, 14, 3);
            for (Shape s : shapesList){
                System.out.println(s);
            }
        } catch (IOException e) {
            System.out.println("Error opening file :(");
        }
    }
}
