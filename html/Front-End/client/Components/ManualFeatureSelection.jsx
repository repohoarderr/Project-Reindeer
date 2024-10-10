import React from "react";

export default function ManualFeatureSelection() {
    const selectFeature = (event) => {
        switch (event.target.innerText) {//button text for now, will need to change once buttons become icons
            case "1A":
                console.log("Good job clicking the first button!!!")
                break;
            case "1B":
                break;
            case "1C":
                break;
            case "2A":
                break;
            case "3":
                break;
            case "4":
                break;
            case "6":
                break;
            case "7":
                break;
            case "8":
                break;
            case "9":
                break;
            case "11":
                break;
            case "12":
                break;
            case "13":
                break;
            case "14":
                break;
            case "15":
                break;
            case "17":
                break;
            case "S1":
                break;
            case "S2":
                break;
            case "16":
                break;
            default:
                console.log("what did you click???") //TODO: actual error stuff here later
        }
    }

    return (
        <div className="manual-feature-selection">
            <button className="feature-button" onClick={selectFeature}>1A</button>
            <button className="feature-button" onClick={selectFeature}>1B</button>
            <button className="feature-button" onClick={selectFeature}>1C</button>
            <button className="feature-button" onClick={selectFeature}>2A</button>
            <button className="feature-button" onClick={selectFeature}>3</button>
            <button className="feature-button" onClick={selectFeature}>4</button>
            <button className="feature-button" onClick={selectFeature}>6</button>
            <button className="feature-button" onClick={selectFeature}>7</button>
            <button className="feature-button" onClick={selectFeature}>8</button>
            <button className="feature-button" onClick={selectFeature}>9</button>
            <button className="feature-button" onClick={selectFeature}>11</button>
            <button className="feature-button" onClick={selectFeature}>12</button>
            <button className="feature-button" onClick={selectFeature}>13</button>
            <button className="feature-button" onClick={selectFeature}>14</button>
            <button className="feature-button" onClick={selectFeature}>15</button>
            <button className="feature-button" onClick={selectFeature}>17</button>
            <button className="feature-button" onClick={selectFeature}>S1</button>
            <button className="feature-button" onClick={selectFeature}>S2</button>
            <button className="feature-button" onClick={selectFeature}>16</button>
        </div>

    );
}