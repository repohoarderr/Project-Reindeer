import React from 'react';
import { Stage, Layer, Line, Arc, Circle } from 'react-konva';

const VisualizeShapes = ({ shapesData }) => {
    const renderShapes = () => {
        return shapesData.map((shape, index) => {
            if (shape.type === 'Line2D') {
                return (
                    <Line
                        key={index}
                        points={[shape.startX * 100, shape.startY * 100, shape.endX * 100, shape.endY * 100]}
                        stroke="black"
                        strokeWidth={2}
                    />
                );
            } else if (shape.type === 'Arc2D') {
                return (
                    <Arc
                        key={index}
                        x={shape.startX * 100}
                        y={shape.startY * 100}
                        angle={shape.arcType === 0 ? 180 : 360}
                        innerRadius={shape.radius * 100}
                        outerRadius={shape.radius * 100}
                        rotation={shape.rotation}
                        stroke="red"
                        strokeWidth={2}
                    />
                );
            } else if (shape.type === 'circle') {
                // Drawing a circle
                return (
                    <Circle
                        key={index}
                        x={shape.centerX * 100}  // Scale the centerX position
                        y={shape.centerY * 100}  // Scale the centerY position
                        radius={shape.radius * 100}  // Scale the radius
                        stroke="blue"  // Circle outline color
                        strokeWidth={2}
                    />
                );
            }
            return null;
        });
    };

    return (
        <Stage width={window.innerWidth} height={window.innerHeight}>
            <Layer>
                {renderShapes()}
            </Layer>
        </Stage>
    );
};

export default VisualizeShapes;
