/* ******************************
 *
 * File: Initializer.java
 *
 * Class for controlling map generation,
 * background music, and options screen.
 *
 * *************************** */

import java.awt.Color;

public class Initializer {

  // Map controls
  private double mapx;               // Current X position of map
  private double mapy;               // Current Y position of map
  private EZImage map;               // Map picture
  private String type;               // Separator to distinguish initializer
  private EZSound finalwave;         // Sounds
  private EZSound boss;
  private EZSound BGM1;
  private EZSound missionstart;
  private EZSound BGM2;
  private boolean beginningflag;     // Flags for various positions
  private boolean missionstartflag;
  private boolean alarmflag;         // Final wave flags
  private boolean finalwaveflag;

  // Pictures for Options screen
  private EZImage title;             // Player controls
  private EZImage A;                 // Left
  private EZImage W;                 // Look up
  private EZImage S;                 // Crouch
  private EZImage D;                 // Right
  private EZImage Space;             // Jump
  private EZImage J;                 // Knife
  private EZImage K;                 // Shoot bullets
  private EZImage L;                 // Shoot grenades

  private EZText instructions;
  private EZText attack;
  private EZText shoot;
  private EZText knife;
  private EZText grenade;
  private EZText jump;
  private EZText left;
  private EZText right;
  private EZText up;
  private EZText crouch;
  private EZText P;
  private EZText move;
  private EZText O;
  private EZSound optionsound;

  // Initialize the map
  public Initializer(String string, double x, double y) {
    if (string == "map") {
      mapx = x;
      mapy = y;
      map = EZ.addImage("Map1.png", 5250, 300);
      finalwaveflag = true;
      finalwave = EZ.addSound("Sounds/Finalwave.wav");
      boss = EZ.addSound("Sounds/Boss.wav");
      BGM1 = EZ.addSound("Sounds/BGM2.wav");
      missionstart = EZ.addSound("Sounds/Mission1.wav");
      BGM2 = EZ.addSound("Sounds/BGM1.wav");
      beginningflag = true;
      alarmflag = true;
      missionstartflag = true;

      type = "map";
    }
    if (string == "control") {
      instructions = EZ.addText(750, 50, "Instructions", Color.white, 50);
      attack = EZ.addText(1075, 150, "Attack", Color.white, 50);
      shoot = EZ.addText(1073, 380, "Shoot", Color.white, 35);
      knife = EZ.addText(940, 380, "Knife", Color.white, 35);
      grenade = EZ.addText(1210, 380, "Grenade", Color.white, 35);
      jump = EZ.addText(1080, 550, "Jump", Color.white, 35);
      left = EZ.addText(250, 420, "Left", Color.white, 35);
      right = EZ.addText(750, 420, "Right", Color.white, 35);
      up = EZ.addText(500, 220, "Up", Color.white, 35);
      crouch = EZ.addText(500, 500, "Crouch", Color.white, 35);
      P = EZ.addText(1300, 40, "Press 'P' to pause", Color.white, 35);
      move = EZ.addText(500, 150, "Controls", Color.white, 50);
      O = EZ.addText(1300, 80, "Press 'O' to resume", Color.white, 35);

      title = EZ.addImage("Title.gif", 740, 230);
      A = EZ.addImage("Controls/A.png", 365, 420);
      W = EZ.addImage("Controls/W.png", 500, 300);
      S = EZ.addImage("Controls/S.png", 500, 420);
      D = EZ.addImage("Controls/D.png", 635, 420);
      Space = EZ.addImage("Controls/spacebar.png", 1080, 480);
      J = EZ.addImage("Controls/J.png", 940, 300);
      K = EZ.addImage("Controls/K.png", 1075, 300);
      L = EZ.addImage("Controls/L.png", 1210, 300);
      optionsound = EZ.addSound("Sounds/Optionsscreen.wav");

      type = "control";
    }
  }

  // Start options screen sound
  public void playSound() {
    if (type == "control") {
      optionsound.loop();
    }
  }

  // Stop options screen sound
  public void stopSound() {
    if (type == "control") {
      optionsound.pause();
    }
  }

  // Show options screen pictures
  public void show() {
    if (type == "control") {
      A.show();
      W.show();
      S.show();
      D.show();
      Space.show();
      J.show();
      L.show();
      K.show();
      instructions.show();
      attack.show();
      shoot.show();
      knife.show();
      grenade.show();
      jump.show();
      left.show();
      right.show();
      up.show();
      crouch.show();
      P.show();
      title.show();
      move.show();
      O.show();
    }
  }

  // Hide options screen pictures
  public void hide() {
    if (type == "control") {
      A.hide();
      W.hide();
      S.hide();
      D.hide();
      Space.hide();
      J.hide();
      L.hide();
      K.hide();
      instructions.hide();
      attack.hide();
      shoot.hide();
      knife.hide();
      grenade.hide();
      jump.hide();
      left.hide();
      right.hide();
      up.hide();
      crouch.hide();
      P.hide();
      title.hide();
      O.hide();
      move.hide();
    }
  }

  // Bring options screen pictures to the front
  public void pullToFront() {
    if (type == "control") {
      A.pullToFront();
      W.pullToFront();
      S.pullToFront();
      D.pullToFront();
      Space.pullToFront();
      J.pullToFront();
      K.pullToFront();
      L.pullToFront();
      instructions.pullToFront();
      attack.pullToFront();
      shoot.pullToFront();
      knife.pullToFront();
      grenade.pullToFront();
      jump.pullToFront();
      left.pullToFront();
      right.pullToFront();
      up.pullToFront();
      crouch.pullToFront();
      P.pullToFront();
      title.pullToFront();
      O.pullToFront();
      move.pullToFront();
    }
  }

  // Translate map
  public void translateObject(double posx, double posy) {
    if (type == "map") {
      // BGM sound
      if (mapx == 5249.0 && beginningflag == true) {
        BGM1.loop();
        beginningflag = false;
      }
      // Mission start sound
      if (mapx == 5175.0 && missionstartflag == true) {
        missionstart.play();
        missionstartflag = false;
      }
      // To get this number, x of map - EZ.intialize x -10
      // If reach end of map
      if (mapx >= -3740)
        map.translateTo(mapx -= posx, posy);
      // Final wave sounds
      if (mapx < -3740 && finalwaveflag == true) {
        finalwave.play();
        boss.play();
        BGM1.stop();
        BGM2.loop();
        finalwaveflag = false;
      }
      // More final wave sounds
      if (mapx < -3750 && alarmflag == true) {
        finalwave.play();
        alarmflag = false;
      }
    }
  }

  // Return current map position
  public double getCurrentMap() {
    return mapx;
  }
}
