/* ******************************
 *
 * File: Enemy.java
 *
 * Class for controlling all enemy
 * functions and features.
 *
 * *************************** */

import java.awt.Color;
import java.lang.String;
import java.io.*;
import java.util.*;

public class Enemy {

  // Current x position, y position, destination to move unit to, range it can move in
  private int posx;
  private int posy;
  private int destx;
  private int desty;
  private int rangex;
  private int rangey;
  private int NORTH = 1, SOUTH = 2, EAST = 3, WEST = 4;

  private boolean flag;
  private final int MAPXLENGTH = 1500;                  // Max X length
  private final int MAPYLENGTH = 600;                   // Max Y length
  private final int HALFX = MAPXLENGTH / 2;
  private final int HALFY = MAPYLENGTH / 2;
  private int AIR_HP = 10;                              // Health of air units
  private int GROUND_HP = 15;                           // Health of ground units
  private int DEATHHP = -1;                             // Death animation health
  private int direction;

  private int ENEMYMOVESPEED = 1;                       // Move speed can only be 1 or 2
  private EZSound sound;                                // Enemy death sound
  private EZImage picture;                              // Enemy alive picture
  private EZImage death;                                // Enemy death picture
  private String type;                                  // Enemy type
  private static int deathcounter = 0;

  private boolean alive_or_dead;                        // Status of enemy unit
  private int health;                                   // Enemy health
  private static final float SCALINGFACTOR = 2.5f;
  private static int playerscore[][] = new int[1][2];   // Holds player's score

  // Constructor to create enemy units
  public Enemy(int x, int y, String character, int rx, int ry) {
    if (character == "scientist") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyScientist/Scientist.png", posx, posy);
      death = EZ.addImage("EnemyScientist/ScientistDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "scientist";
      health = GROUND_HP;
      sound = EZ.addSound("Sounds/Scientist.wav");
    }
    if (character == "helicopter") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyHelicopter/EnemyHelicopter.png", posx, posy);
      death = EZ.addImage("EnemyHelicopter/HelicopterDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "helicopter";
      health = AIR_HP;
      sound = EZ.addSound("Sounds/Helicopter.wav");
    }
    if (character == "UFO") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyUFO/EnemyUFO.png", posx, posy);
      death = EZ.addImage("EnemyUFO/UFODeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "UFO";
      health = AIR_HP;
      sound = EZ.addSound("Sounds/UFO.wav");
    }
    if (character == "Tank") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyTank/EnemyTank.png", posx, posy);
      death = EZ.addImage("EnemyTank/TankDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "Tank";
      health = GROUND_HP;
      sound = EZ.addSound("Sounds/Tank.wav");
    }
    if (character == "zombie") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyZombieMacro/Zombie.png", posx, posy);
      death = EZ.addImage("EnemyZombieMacro/ZombieDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "zombie";
      health = GROUND_HP;
      sound = EZ.addSound("Sounds/Zombie.wav");
    }
    if (character == "mecharobot") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyMechaRobot/EnemyMechaRobot.png", posx, posy);
      death = EZ.addImage("EnemyMechaRobot/MechaRobotDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "mecharobot";
      health = 20;
      sound = EZ.addSound("Sounds/Mecha.wav");
    }
    if (character == "airship") {
      posx = x;
      posy = y;
      picture = EZ.addImage("EnemyAirShip/EnemyAirship.png", posx, posy);
      death = EZ.addImage("EnemyAirShip/AirshipDeath.png", posx, posy);
      flag = true;
      rangex = rx;
      rangey = ry;
      alive_or_dead = true;
      death.pushToBack();
      death.hide();
      setRandomDirection();
      type = "airship";
      health = AIR_HP;
      sound = EZ.addSound("Sounds/Airship.wav");
    }
  }

  // Scales size of enemy pictures
  public void unitsInit() {
    if (type == "scientist") {
      picture.scaleTo(SCALINGFACTOR);
      death.scaleTo(SCALINGFACTOR);
    }
    if (type == "helicopter") {
      picture.scaleTo(SCALINGFACTOR);
      death.scaleTo(SCALINGFACTOR);
    }
    if (type == "UFO") {
      picture.scaleTo(2);
      death.scaleTo(SCALINGFACTOR);
    }
    if (type == "Tank") {
      picture.scaleTo(2);
      death.scaleTo(2);
    }
    if (type == "zombie") {
      picture.scaleTo(SCALINGFACTOR);
      death.scaleTo(2);
    }
    if (type == "mecharobot") {
      picture.scaleTo(2);
      death.scaleTo(2);
    }
    if (type == "airship") {
      picture.scaleTo(2);
      death.scaleTo(2);
    }
  }

  // Return current x position
  public int getXCenter() {
    return posx;
  }

  // Return current y position
  public int getYCenter() {
    return posy;
  }

  // Return state of enemy unit
  public boolean getAliveOrDead() {
    return alive_or_dead;
  }

  // Controls if player's projectiles hit enemy units
  public void collision() {
    if (type == "scientist") {
      health -= 2;
      playerscore[0][0] += 10;

      // Dead
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      // Take away death pictures
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "helicopter") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "UFO") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= 0 && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "Tank") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "zombie") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "mecharobot") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
    if (type == "airship") {
      health -= 2;
      playerscore[0][0] += 10;
      if (health <= 0) {
        translateFirstDeathPictures();
        deadTriggers();
      }
      if (health <= DEATHHP && alive_or_dead == true)
        translateSecondDeathPictures();
    }
  }

  // Translates alive enemy pictures
  private void translateFirstDeathPictures() {
    picture.hide();
    picture.translateTo(0, 0);
  }

  // Translate enemy death pictures
  private void translateSecondDeathPictures() {
    posx = 0;
    posy = 0;
    alive_or_dead = false;
    death.hide();
    death.translateTo(posx, posy);
    deathcounter++;
  }

  // Returns players score
  public int getPlayerScore() {
    return playerscore[0][0];
  }

  // Activates triggers when unit is hit
  private void deadTriggers() {
    death.pullToFront();
    death.show();
    sound.play();
  }

  // Returns current health of enemy
  public int getHealth() {
    return health;
  }

  // Move enemies around map
  public void move() {
    // Enemy still alive
    if (health > 0) {
      // Ground units
      if (type == "Tank" || type == "scientist" || type == "zombie" || type == "mecharobot") {
        if (posx > destx) moveLeft(ENEMYMOVESPEED);
        if (posx < destx) moveRight(ENEMYMOVESPEED);

        // Enemy reached destination
        if ((posx == destx))// || (posx == destx + ENEMYMOVEEQUALIZER) )
        {
          setRandomDirection();
        }
      }
      // Air units
      else {
        if (posx > destx) moveLeft(ENEMYMOVESPEED);
        if (posx < destx) moveRight(ENEMYMOVESPEED);
        if (posy > desty) moveUp(ENEMYMOVESPEED);
        if (posy < desty) moveDown(ENEMYMOVESPEED);

        // Enemy reached destination
        if ((posx == destx) && (posy == desty))//|| ((posy == desty) && (posx == destx + ENEMYMOVEEQUALIZER)) || ((posx == destx) && (posy == desty + ENEMYMOVEEQUALIZER)) || ((posx == destx + ENEMYMOVEEQUALIZER) && (posy == desty + ENEMYMOVEEQUALIZER)))
        {
          setRandomDirection();
        }
      }
    }
  }

  // Set random destination and set direction of object (N, S, E, W)
  public void setRandomDirection() {
    Random randomGenerator = new Random();

    int ranx = randomGenerator.nextInt(rangex);
    int rany = randomGenerator.nextInt(rangey);

    // Flying units
    if (type == "helicopter" || type == "UFO" || type == "airship") {
      while (ranx <= HALFX + 200 || rany < 50 || rany > 580) {
        ranx = randomGenerator.nextInt(rangex);
        rany = randomGenerator.nextInt(rangey);
      }
    }
    // Ground units, only move in x axis
    if (type == "Tank" || type == "scientist" || type == "zombie" || type == "mecharobot") {
      rany = 500;
      while (ranx <= HALFX + 200 || rany != 500) {
        ranx = randomGenerator.nextInt(rangex);
      }
    }

    setDestination(ranx, rany);

    // Quadrant 1
    if (ranx > HALFX && ranx <= MAPXLENGTH && rany >= 0 && rany <= HALFY)
      direction = EAST;
    // Quadrant 2
    if (ranx >= 0 && ranx <= HALFX && rany >= 0 && rany <= HALFY)
      direction = NORTH;
    // Quadrant 3
    if (ranx >= 0 && ranx <= HALFX && rany > HALFY && rany <= MAPYLENGTH)
      direction = WEST;
    // Quadrant 4
    if (ranx > HALFX && ranx <= MAPXLENGTH && rany > HALFY && rany <= MAPYLENGTH)
      direction = SOUTH;
  }

  // Set destination
  public void setDestination(int x, int y) {
    destx = x;
    desty = y;
  }

  // Move left
  public void moveLeft(int step) {
    posx = posx - step;
    setImagePosition(posx, posy);
  }

  // Move right
  public void moveRight(int step) {
    posx = posx + step;
    setImagePosition(posx, posy);
  }

  // Move up
  public void moveUp(int step) {
    posy = posy - step;
    setImagePosition(posx, posy);
  }

  // Move down
  public void moveDown(int step) {
    posy = posy + step;
    setImagePosition(posx, posy);
  }

  // Set object to specific position.
  private void setImagePosition(int posx, int posy) {
    if (flag) {
      picture.translateTo(posx, posy);
      death.translateTo(posx, posy);
    }
  }

  // Return dead timer
  public int returnDeathcounter() {
    return deathcounter;
  }

  // Switch flag value
  public void changeFlag() {
    if (flag == true)
      flag = false;
    else
      flag = true;
  }
}
