import React, { useState } from "react";

export default function ManualFeatureSelection() {
  //Ensure the state and toggle function are correctly initialized
  const [isVisible, setIsVisible] = useState(false);
  const [isManualVisible, setIsManualVisible] = useState(false); // State for user's manual

    //Function to toggle panel visibility
    const togglePanel = () => {
        setIsVisible(!isVisible);
    };

    //Function to toggle the user's manual visibility
    const toggleManual = () => {
        setIsManualVisible(!isManualVisible);
    };


  //Button click handler
  const selectFeature = (event) => {
    switch (event.target.innerText) {
      case "1A":
        console.log("Good job clicking the first button!!!");
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
        console.log("what did you click???"); // TODO: Add error handling logic here.
    }
  };

  return (
    <div className="fade-out-panel-container">

      {/* Toggle button placed at the top-right corner */}
      <button className="toggle-button" onClick={togglePanel}>
      <i className="fas fa-shapes"></i> {/* Use the Shapes icon */}
      </button>

      <div className={`fade-out-panel ${isVisible ? "visible" : "hidden"}`}>

        {/* Feature buttons */}
        <button className="feature-button">
        <img src="/icons/Grp1.png" alt="1A thumbnail" className="thumbnail" />
        </button>

        <button className="feature-button">
        <img src="/icons/Grp2A.png" alt="1B thumbnail" className="thumbnail" />
        </button>

        <button className="feature-button">
        <img src="/icons/Grp3.png" alt="1C thumbnail" className="thumbnail" />
        </button>

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

      {/* User's manual book icon at the bottom-right corner */}
      <button className="manual-button" onClick={toggleManual}>
        <i className="fas fa-book"></i>
      </button>

      {/* User's manual dialog box with mock text */}
      {isManualVisible && (
        <div className="manual-dialog">
          <p>
            Welcome to the User Manual! This is a guide to help you navigate through the app.
            There was something special about this little creature. Donna couldn't quite pinpoint what it was, but she knew with all her heart that it was true. It wasn't a matter of if she was going to try and save it, but a matter of how she was going to save it. She went back to the car to get a blanket and when she returned the creature was gone.
There was a time in his life when her rudeness would have set him over the edge. He would have raised his voice and demanded to speak to the manager. That was no longer the case. He barely reacted at all, letting the rudeness melt away without saying a word back to her. He had been around long enough to know where rudeness came from and how unhappy the person must be to act in that way. All he could do was feel pity and be happy that he didn't feel the way she did to lash out like that.
The red glow of tail lights indicating another long drive home from work after an even longer 24-hour shift at the hospital. The shift hadn’t been horrible but the constant stream of patients entering the ER meant there was no downtime. She had some of the “regulars” in tonight with new ailments they were sure were going to kill them. It’s amazing what a couple of Tylenol and a physical exam from the doctor did to eliminate their pain, nausea, headache, or whatever other mild symptoms they had. Sometimes she wondered if all they really needed was some interaction with others and a bit of the individual attention they received from the nurses.
She patiently waited for his number to be called. She had no desire to be there, but her mom had insisted that she go. She's resisted at first, but over time she realized it was simply easier to appease her and go. Mom tended to be that way. She would keep insisting until you wore down and did what she wanted. So, here she sat, patiently waiting for her number to be called. There was something special about this little creature. Donna couldn't quite pinpoint what it was, but she knew with all her heart that it was true. It wasn't a matter of if she was going to try and save it, but a matter of how she was going to save it. She went back to the car to get a blanket and when she returned the creature was gone.
There was a time in his life when her rudeness would have set him over the edge. He would have raised his voice and demanded to speak to the manager. That was no longer the case. He barely reacted at all, letting the rudeness melt away without saying a word back to her. He had been around long enough to know where rudeness came from and how unhappy the person must be to act in that way. All he could do was feel pity and be happy that he didn't feel the way she did to lash out like that.
The red glow of tail lights indicating another long drive home from work after an even longer 24-hour shift at the hospital. The shift hadn’t been horrible but the constant stream of patients entering the ER meant there was no downtime. She had some of the “regulars” in tonight with new ailments they were sure were going to kill them. It’s amazing what a couple of Tylenol and a physical exam from the doctor did to eliminate their pain, nausea, headache, or whatever other mild symptoms they had. Sometimes she wondered if all they really needed was some interaction with others and a bit of the individual attention they received from the nurses.
She patiently waited for his number to be called. She had no desire to be there, but her mom had insisted that she go. She's resisted at first, but over time she realized it was simply easier to appease her and go. Mom tended to be that way. She would keep insisting until you wore down and did what she wanted. So, here she sat, patiently waiting for her number to be called. There was something special about this little creature. Donna couldn't quite pinpoint what it was, but she knew with all her heart that it was true. It wasn't a matter of if she was going to try and save it, but a matter of how she was going to save it. She went back to the car to get a blanket and when she returned the creature was gone.
There was a time in his life when her rudeness would have set him over the edge. He would have raised his voice and demanded to speak to the manager. That was no longer the case. He barely reacted at all, letting the rudeness melt away without saying a word back to her. He had been around long enough to know where rudeness came from and how unhappy the person must be to act in that way. All he could do was feel pity and be happy that he didn't feel the way she did to lash out like that.
The red glow of tail lights indicating another long drive home from work after an even longer 24-hour shift at the hospital. The shift hadn’t been horrible but the constant stream of patients entering the ER meant there was no downtime. She had some of the “regulars” in tonight with new ailments they were sure were going to kill them. It’s amazing what a couple of Tylenol and a physical exam from the doctor did to eliminate their pain, nausea, headache, or whatever other mild symptoms they had. Sometimes she wondered if all they really needed was some interaction with others and a bit of the individual attention they received from the nurses.
She patiently waited for his number to be called. She had no desire to be there, but her mom had insisted that she go. She's resisted at first, but over time she realized it was simply easier to appease her and go. Mom tended to be that way. She would keep insisting until you wore down and did what she wanted. So, here she sat, patiently waiting for her number to be called.
          </p>
        </div>
      )}


    </div>
  );
}
