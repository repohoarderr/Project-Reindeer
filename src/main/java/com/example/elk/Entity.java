package com.example.elk;

import java.awt.*;

class Entity {
    final String type;

    Entity(String type) {
        this.type = type;
    }

    // Override these methods is subclasses, as needed
    void addParm(int gCode, String value) {
    }

    void addChild(Entity child) {
    }

    void close() {
    }
}

class DrawItem extends Entity {

    DrawItem(String type) {
        super(type);
    }

    Shape getShape() {
        return null;
    }
}
