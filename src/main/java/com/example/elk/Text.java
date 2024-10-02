package com.example.elk;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import static com.example.elk.DXFReader.uScale;

/**
 * Crude implementation of TEXT using GlyphVector to create vector outlines of text
 * Note: this code should use, or support vector fonts such as those by Hershey
 */
class Text extends DrawItem implements DXFReader.AutoPop {
    private final Canvas canvas = new Canvas();
    private double ix, iy, ix2, iy2, textHeight, rotation;
    private int hAdjust, vAdjust;
    private String text;

    Text(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 1:                                       // Text string
                // Process Control Codes and Special Chars
                // https://forums.autodesk.com/t5/autocad-forum/text-commands-eg-u/td-p/1977654
                StringBuilder buf = new StringBuilder();
                for (int ii = 0; ii < value.length(); ii++) {
                    char cc = value.charAt(ii);
                    if (cc == '%') {
                        cc = value.charAt(ii + 2);
                        ii += 2;
                        if (Character.isDigit(cc)) {
                            int code = 0;
                            while (Character.isDigit(cc = value.charAt(ii))) {
                                code = (code * 10) + (cc - '0');
                                ii++;
                            }
                            // todo: how to convert value of "code" into special character
                            buf.append("\uFFFD");                 // Insert Unicode "unknown character" symbol
                            ii--;
                        } else {
                            switch (cc) {
                                case 'u':                             // Toggles underscoring on and off
                                    // Ignored
                                    break;
                                case 'd':                             // Draws degrees symbol (°)
                                    buf.append("\u00B0");
                                    break;
                                case 'p':                             // Draws plus/minus tolerance symbol (±)
                                    buf.append("\u00B1");
                                    break;
                                case 'c':                             // Draws circle diameter dimensioning symbol (Ø)
                                    buf.append("\u00D8");
                                    break;
                                case 'o':                             // Toggles overscoring on and off
                                    // Ignored
                                    break;
                            }
                        }
                    } else {
                        buf.append(cc);
                    }
                }
                text = buf.toString();
                break;
            case 10:                                      // Insertion X
                ix = Double.parseDouble(value) * uScale;
                break;
            case 11:                                      // Second alignment point X
                ix2 = Double.parseDouble(value) * uScale;
                break;
            case 20:                                      // Insertion Y
                iy = Double.parseDouble(value) * uScale;
                break;
            case 21:                                      // Second alignment point Y
                iy2 = Double.parseDouble(value) * uScale;
                break;
            case 40:                                      // Nominal (initial) text height
                textHeight = Double.parseDouble(value) * uScale;
                break;
            case 50:                                      // Rotation angle in degrees
                rotation = Double.parseDouble(value);
                break;
            case 71:                                      // Text generation flags (optional, default = 0):
                // Not implemented
                // 2 = Text is backward (mirrored in X)
                // 4 = Text is upside down (mirrored in Y)
                break;
            case 72:                                      // Horizontal text justification type (optional, default = 0) integer codes
                //0 = Left; 1= Center; 2 = Right
                //3 = Aligned (if vertical alignment = 0)
                //4 = Middle (if vertical alignment = 0)
                //5 = Fit (if vertical alignment = 0)
                hAdjust = Integer.parseInt(value);
                break;
            case 73:                                      // Vertical text justification type (optional, default = 0): integer codes
                // 0 = Baseline; 1 = Bottom; 2 = Middle; 3 = Top
                vAdjust = Integer.parseInt(value);
                break;
        }
    }

    @Override
    Shape getShape() {
        // Note: I had to scale up font size by 10x to make it render properly
        float points = (float) textHeight * 10f;
        Font font = (new Font("Helvetica", Font.PLAIN, 72)).deriveFont(points);
        HashMap<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        attrs.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        attrs.put(TextAttribute.TRACKING, 0.1);
        font = font.deriveFont(attrs);
        GlyphVector gv = font.createGlyphVector(canvas.getFontMetrics(font).getFontRenderContext(), text);
        // Step 1 - Convert GlyphVector to Shape
        AffineTransform at1 = new AffineTransform();
        Shape shape = at1.createTransformedShape(gv.getOutline());
        Rectangle2D bnds = shape.getBounds2D();
        // Step 2 - Translate shape according to vAdjust and hAdjust values
        AffineTransform at2 = new AffineTransform();
        // TODO: test all attachment point cases
        if (vAdjust == 3 && hAdjust == 0) {                             // Top left
            at2.translate(0, bnds.getHeight());
        } else if (vAdjust == 3 && hAdjust == 1) {                      // Top center
            at2.translate(-bnds.getWidth() / 2, bnds.getHeight());
        } else if (vAdjust == 3 && hAdjust == 2) {                      // Top right
            at2.translate(-bnds.getWidth(), bnds.getHeight());
        } else if (vAdjust == 2 && hAdjust == 0) {                      // Middle left
            at2.translate(0, bnds.getHeight() / 2);
        } else if (vAdjust == 2 && hAdjust == 1) {                      // Middle center
            at2.translate(-bnds.getWidth() / 2, bnds.getHeight() / 2);
        } else if (vAdjust == 2 && hAdjust == 2) {                      // Middle right
            at2.translate(-bnds.getWidth(), bnds.getHeight() / 2);
        } else if (vAdjust == 1 && hAdjust == 0) {                      // Bottom left (natural position)
            at2.translate(0, 0);
        } else if (vAdjust == 1 && hAdjust == 1) {                      // Bottom center
            at2.translate(-bnds.getWidth() / 2, 0);
        } else if (vAdjust == 1 && hAdjust == 2) {                      // Bottom right
            at2.translate(-bnds.getWidth(), 0);
        }
        shape = at2.createTransformedShape(shape);
        // Step 3 - Rotate and Scale shape
        AffineTransform at3 = new AffineTransform();
        at3.rotate(Math.toRadians(rotation));
        at3.scale(.1, -.1);
        shape = at3.createTransformedShape(shape);
        // Step 4 - Translate shape to final position
        AffineTransform at4 = new AffineTransform();
        if (hAdjust != 0 || vAdjust != 0) {
            at4.translate(ix2, iy2);
        } else {
            at4.translate(ix, iy);
        }
        shape = at4.createTransformedShape(shape);
        return shape;
    }
}
