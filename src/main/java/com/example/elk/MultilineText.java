package com.example.elk;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.elk.DXFReader.uScale;

/**
 * Crude implementation of MTEXT (Multi-line Text) using GlyphVector to create vector outline of text
 * Note: the MTEXT spec is very complex and assumes the ability to decode embedded format codes, use vector fonts
 * such as those by Hershey, and other features I have not implemented.
 * https://knowledge.safe.com/articles/38908/autocad-workflows-reading-and-writing-text-mtext-f.html
 * <p>
 * Example Text with Format Codes: https://adndevblog.typepad.com/autocad/2017/09/dissecting-mtext-format-codes.html
 * \A1;3'-1"
 * \A1;6'-10{\H0.750000x;\S1/2;}"
 * \A1;PROVIDE 20 MIN. DOOR\PW/ SELF CLOSING HINGES
 * {\Farchquik.shx|c0;MIN. 22"x 30" ATTIC ACCESS}
 * "HEATILATOR" 42" GAS BURNING DIRECT VENT FIREPLACE, OR EQUAL
 * BOLLARD,\PFOR W.H.\PPROTECTION
 */

class MultiLineText extends DrawItem implements DXFReader.AutoPop {
    private final Canvas canvas = new Canvas();
    private String text;
    private double ix, iy, textHeight, refWidth, xRot, yRot;
    private int attachPoint;

    MultiLineText(String type) {
        super(type);
    }

    @Override
    void addParm(int gCode, String value) {
        switch (gCode) {
            case 1:                                         // Text string
                // Process Format Codes (most are ignored)
                List<String> lines = new ArrayList<>();
                StringBuilder buf = new StringBuilder();
                for (int jj = 0; jj < value.length(); jj++) {
                    char cc = value.charAt(jj);
                    if (cc == '\\') {
                        cc = value.charAt(++jj);
                        switch (cc) {
                            case 'A':                               // Alignment
                            case 'C':                               // Color
                            case 'F':                               // Font file name
                            case 'H':                               // Text height
                            case 'Q':                               // Slanting (obliquing) text by angle
                            case 'S':                               // Stacking Fractions
                            case 'T':                               // Tracking, char.spacing - e.g. \T2;
                            case 'W':                               // Text width
                                int tdx = value.indexOf(";", jj);
                                String val = value.substring(jj + 1, tdx);
                                jj = tdx;
                                if (cc == 'S') {                      // Stacking Fractions (1/2, 1/3, etc)
                                    if ("1/2".equals(val)) {
                                        buf.append("\u00BD");             // Unicode for 1/2
                                    } else if ("1/3".equals(val)) {
                                        buf.append("\u2153");           // Unicode for 1/3
                                    } else if ("1/4".equals(val)) {
                                        buf.append("\u00BC");             // Unicode for 1/4
                                    } else if ("2/3".equals(val)) {
                                        buf.append("\u2154");             // Unicode for 2/3
                                    } else if ("3/4".equals(val)) {
                                        buf.append("\u00BE");             // Unicode for 3/4
                                    } else {
                                        String[] parts = val.split("/");
                                        if (parts.length == 2) {
                                            buf.append(parts[0]);
                                            buf.append("\u2044");
                                            buf.append(parts[1]);
                                        }
                                    }
                                }
                                break;
                            case 'P':                               // New paragraph (new line)
                                lines.add(buf.toString());
                                buf.setLength(0);
                                break;
                            case '\\':                              // Escape character - e.g. \\ = "\", \{ = "{"
                                buf.append(value.charAt(++jj));
                                break;
                        }
                    } else if (cc == '{') {
                        // Begin area influenced by special code
                    } else if (cc == '}') {
                        // End area influenced by special code
                    } else {
                        buf.append(cc);
                    }
                }
                lines.add(buf.toString());
                // Skip handling all but first line of text
                text = lines.get(0);
                if (text.length() > 30 && refWidth > 0) {
                    // KLudge until code to handle "refWidth" is added
                    text = text.substring(0, 30) + "...";
                }
                break;
            case 7:                                       // Text style name (STANDARD if not provided) (optional)
                break;
            case 10:                                      // Insertion X
                ix = Double.parseDouble(value) * uScale;
                break;
            case 11:                                      // X Rotation Unit Vector
                xRot = Double.parseDouble(value);
                break;
            case 20:                                      // Insertion Y
                iy = Double.parseDouble(value) * uScale;
                break;
            case 21:                                      // Y Rotation Unit Vector
                yRot = Double.parseDouble(value);
                break;
            case 40:                                      // Nominal (initial) text height
                textHeight = Double.parseDouble(value) * uScale;
                break;
            case 41:                                      // Reference rectangle width
                refWidth = Double.parseDouble(value) * uScale;
                break;
            case 71:                                      // Attachment point
                attachPoint = Integer.parseInt(value);
                break;
            case 72:                                      // Drawing direction: 1 = Left to right; 3 = Top to bottom; 5 = By style
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
        // Step 2 - Translate shape according to Attachment Point value
        AffineTransform at2 = new AffineTransform();
        // TODO: test all attachment point cases
        switch (attachPoint) {
            case 1:                                 // Top left
                at2.translate(0, bnds.getHeight());
                break;
            case 2:                                 // Top center
                at2.translate(-bnds.getWidth() / 2, bnds.getHeight());
                break;
            case 3:                                 // Top right
                at2.translate(-bnds.getWidth(), bnds.getHeight());
                break;
            case 4:                                 // Middle left
                at2.translate(0, bnds.getHeight() / 2);
                break;
            case 5:                                 // Middle center
                at2.translate(-bnds.getWidth() / 2, bnds.getHeight() / 2);
                break;
            case 6:                                 // Middle right
                at2.translate(-bnds.getWidth(), bnds.getHeight() / 2);
                break;
            case 7:                                 // Bottom left (natural position)
                at2.translate(0, 0);
                break;
            case 8:                                 // Bottom center
                at2.translate(-bnds.getWidth() / 2, 0);
                break;
            case 9:                                 // Bottom right
                at2.translate(-bnds.getWidth(), 0);
                break;
        }
        shape = at2.createTransformedShape(shape);
        // Step 3 - Rotate and Scale shape
        AffineTransform at3 = new AffineTransform();
        double rotation = Math.atan2(yRot, xRot);
        at3.rotate(rotation);
        at3.scale(.1, -.1);
        shape = at3.createTransformedShape(shape);
        // Step 4 - Translate shape to final position
        AffineTransform at4 = new AffineTransform();
        at4.translate(ix, iy);
        shape = at4.createTransformedShape(shape);
        return shape;
    }
}
