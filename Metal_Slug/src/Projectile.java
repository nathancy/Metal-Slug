/* ******************************
 *
 * File: Projectile.java
 *
 * Class for controlling all
 * projectiles for enemies and
 * player.
 *
 * *************************** */

import java.awt.event.KeyEvent;
import java.awt.Color;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;

public class Projectile {

  private double posx;                                                     // Current x position
  private double posy;                                                     // Current y position

  private EZImage projectileGrenade[] = new EZImage[GRENADEEXPLOSIONPICS]; // Grenade explosion animation
  private EZImage projectileBullet[] = new EZImage[BULLETPICS];            // Bullet explosion animation
  private EZImage grenade;
  private EZImage bullet;
  private EZSound bulletsound;
  private EZSound grenadesound;
  //SIMPLE ANIMATION, 7, 3 for little more animated, Use simple animation for no lag
  private static final int GRENADEEXPLOSIONPICS = 2;
  private static final int BULLETPICS = 2;

	/*
	//FULL ANIMATION
	private static final int GRENADEEXPLOSIONPICS = 19;
	private static final int ROCKETPICS = 2;
	*/

  private static final int ENEMYPROJECTILESPEED = 2;                       // Speed of enemy projectiles
  private static final double PLAYERSHOOTSPEED = .15;                      // Player shoot speed
  private static final double PLAYERGRENADESPEED = .10;                    // Player grenade speed
  private static final double PROJECTILEROTATE = .125;                     // Rotation of player projectiles
  private static final float SCALINGFACTOR = 2.5f;                         // Scale objects to same factor
  private static final int DELAY = 1;                                      // Animation delay
  private String type;                                                     // The unit type
  private static final int MAPXSIZE = 1450;
  private boolean using;                                                   // If projectile is currently on map
  private boolean projectileup;

  // Master for animations
  public Projectile(int x, int y) {
    posx = x;
    posy = y;
    type = "master";
  }

  // Constructs projectile depending on unit type
  public Projectile(int x, int y, String letter) {
    posx = x;
    posy = y;

    if (letter == "playergrenade") {
      grenade = EZ.addImage("Grenade.png", x, y);
      grenade.hide();
      type = "playergrenade";
      using = false;
      projectileup = false;
      grenadesound = EZ.addSound("Sounds/Shoot1.wav");

    }
    if (letter == "playerbullet") {
      bullet = EZ.addImage("Attack2.png", x, y);
      bullet.hide();
      type = "playerbullet";
      using = false;
      projectileup = false;
      bulletsound = EZ.addSound("Sounds/Shoot3.wav");
    }
    if (letter == "scientistBullet") {
      bullet = EZ.addImage("EnemyScientist/ScientistBullet.png", x, y);
      bullet.hide();
      type = "scientistBullet";
    }
    if (letter == "helicopterBullet") {
      bullet = EZ.addImage("EnemyHelicopter/HelicopterBullet.png", x, y);
      bullet.hide();
      type = "helicopterBullet";
    }
    if (letter == "UFOBullet") {
      bullet = EZ.addImage("EnemyUFO/UFOBullet.png", x, y);
      bullet.hide();
      type = "UFOBullet";
    }
    if (letter == "tankBullet") {
      bullet = EZ.addImage("EnemyTank/TankBullet.png", x, y);
      bullet.hide();
      type = "tankBullet";
    }
    if (letter == "zombieBullet") {
      bullet = EZ.addImage("EnemyZombieMacro/ZombieAttack.png", x, y);
      bullet.hide();
      type = "zombieBullet";
    }
    if (letter == "mechaBullet") {
      bullet = EZ.addImage("EnemyMechaRobot/MechaRobotAttack.png", x, y);
      bullet.hide();
      type = "mechaBullet";
    }
    if (letter == "airshipBullet") {
      bullet = EZ.addImage("EnemyAirship/AirshipAttack.png", x, y);
      bullet.hide();
      type = "airshipBullet";
    }
  }

  // Returns true if projectile is currently in play, false otherwise
  public boolean beingUsed() {
    return using;
  }

  // Returns x position
  public double getXpos() {
    return posx;
  }

  // Returns y position
  public double getYpos() {
    return posy;
  }

  // Returns the type of the unit
  public String returnType() {
    return type;
  }

  // Translates the player's bullets and grenades
  public void translateObject(int x, int y) {
    if (type == "playerbullet") {
      posx = x;
      posy = y;
      bullet.translateTo(posx, posy);
      projectileup = false;
      bulletsound.play();
    }

    if (type == "playergrenade") {
      posx = x;
      posy = y;
      grenade.translateTo(posx, posy);
      projectileup = false;
      grenadesound.play();
    }
  }

  // Move player's projectiles up if shooting/greande up
  public void translateObjectUp(int x, int y) {
    if (type == "playerbullet" && projectileup == false) {
      posx = x;
      posy = y;
      bullet.translateTo(posx, posy);
      projectileup = true;
      bulletsound.play();
    }

    if (type == "playergrenade" && projectileup == false) {
      posx = x;
      posy = y;
      grenade.translateTo(posx, posy);
      projectileup = true;
      grenadesound.play();
    }
  }

  // Hide player projectiles
  public void hide() {
    if (type == "playerbullet")
      bullet.hide();
    if (type == "playergrenade")
      grenade.hide();
  }

  // Checks if player projectiles are shooting up
  public boolean returnUpOrNot() {
    return projectileup;
  }

  // Reset enemy projectiles if off map or if hits player
  public void resetEnemyProjectile(int x, int y) {
    if (type == "scientistBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 55, posy -= 20);
    }
    if (type == "helicopterBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 150, posy += 40);
    }
    if (type == "UFOBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx, posy);
    }
    if (type == "tankBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 55, posy -= 30);
    }
    if (type == "zombieBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 55, posy);
    }
    if (type == "mechaBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 160, posy += 5);
    }
    if (type == "airshipBullet") {
      posx = x;
      posy = y;
      bullet.hide();
      bullet.translateTo(posx -= 250, posy += 50);
    }
  }

  // Controls movement and status of enemy projectiles
  public void processEnemyProjectile(int x, int y, int health) {
    // If within Map
    if (x < 1500) {
      if (type == "scientistBullet") {
        if (health > 0)
          translateScientistBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "helicopterBullet") {
        if (health > 0)
          translateHelicopterBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "UFOBullet") {
        if (health > 0)
          translateUFOBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "tankBullet") {
        if (health > 0)
          translateTankBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "zombieBullet") {
        if (health > 0)
          translateZombieBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "mechaBullet") {
        if (health > 0)
          translateMechaBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
      if (type == "airshipBullet") {
        if (health > 0)
          translateAirshipBullet(x, y);
        else {
          bullet.translateTo(-10, 0);
          bullet.hide();
        }
      }
    }
  }

  // Controls particular enemy's projectile
  // Could have made all into one function for all translate bullets but wanted to be able to specifically change one if needed to
  public void translateAirshipBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateMechaBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateZombieBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateTankBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateUFOBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateHelicopterBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  public void translateScientistBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx -= ENEMYPROJECTILESPEED, posy);
    if (bullet.getXCenter() <= 0) {
      resetEnemyProjectile(x, y);
    }
  }

  // Controls player's grenade
  public void translateGrenade(int x, int y) {
    grenade.show();
    grenade.pullToFront();
    grenade.rotateBy(PROJECTILEROTATE);
    grenade.translateTo(posx += PLAYERGRENADESPEED, posy);

    // If hit object or out of map
    if (grenade.getXCenter() > MAPXSIZE || grenade.isPointInElement(x, y)) {
      grenade.hide();
      grenadeExplosionAnimation(grenade.getXCenter(), grenade.getYCenter());
      grenade.translateTo(0, 0);
      using = false;
    }
  }

  // Controls player's bullet if shooting horizontally
  public void translateBullet(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx += PLAYERSHOOTSPEED, posy);
    if (bullet.getXCenter() > MAPXSIZE || bullet.isPointInElement(x, y)) {
      bullet.hide();
      bulletExplosionAnimation(bullet.getXCenter(), bullet.getYCenter());
      bullet.translateTo(0, 0);
      using = false;
    }
  }

  // Controls player's bullet if shooting up
  public void translateBulletUp(int x, int y) {
    bullet.show();
    bullet.pullToFront();
    bullet.translateTo(posx, posy -= PLAYERSHOOTSPEED);
    bullet.rotateTo(-90);

    if (bullet.getYCenter() <= 10 || bullet.isPointInElement(x, y)) {
      bullet.hide();
      bulletExplosionAnimation(bullet.getXCenter(), bullet.getYCenter());
      bullet.translateTo(0, 0);
      using = false;
      projectileup = false;
      bullet.rotateTo(0);
    }
  }

  // Controls player's grenade if shooting up
  public void translateGrenadeUp(int x, int y) {
    grenade.show();
    grenade.pullToFront();
    grenade.rotateBy(PROJECTILEROTATE);
    grenade.translateTo(posx, posy -= PLAYERGRENADESPEED);

    // If hit object or out of map
    if (grenade.getYCenter() <= 10 || grenade.isPointInElement(x, y)) {
      grenade.hide();
      grenadeExplosionAnimation(grenade.getXCenter(), grenade.getYCenter());
      grenade.translateTo(0, 0);
      using = false;
      projectileup = false;
    }
  }

  // Processes the type of player's projectile
  public void processProjectile(int x, int y) {
    if (type == "playerbullet" && projectileup == false) {
      translateBullet(x, y);
    }
    if (type == "playerbullet" && projectileup == true) {
      translateBulletUp(x, y);
    }
    if (type == "playergrenade" && projectileup == false) {
      translateGrenade(x, y);
    }
    if (type == "playergrenade" && projectileup == true) {
      translateGrenadeUp(x, y);
    }
  }

  // Switch state of projectile to being used or vacant
  public void switchState() {
    if (using == true) {
      using = false;
    } else {
      using = true;
    }
  }

  // Scales size of player's projectiles and enemy's projectiles
  public void projectileInit() {
    if (type == "playergrenade")
      grenade.scaleTo(SCALINGFACTOR);
    if (type == "playerbullet")
      bullet.scaleTo(.9);
    if (type == "scientistBullet")
      bullet.scaleTo(SCALINGFACTOR);
    if (type == "helicopterBullet")
      bullet.scaleTo(.9);
    if (type == "UFOBullet")
      bullet.scaleTo(SCALINGFACTOR);
    if (type == "tankBullet")
      bullet.scaleTo(2);
    if (type == "zombieBullet")
      bullet.scaleTo(2);
    if (type == "mechaBullet")
      bullet.scaleTo(1.8);
    if (type == "airshipBullet")
      bullet.scaleTo(1.8);
  }

  // Initializes animation of player's grenades and bullets
  public void animationInit() {
    if (type == "playergrenade") {
      for (int i = 0; i < GRENADEEXPLOSIONPICS; i++) {
        projectileGrenade[i] = EZ.addImage("ProjectileGrenadeExplosion/" + i + ".png", -10, 0);
        projectileGrenade[i].hide();
        projectileGrenade[i].scaleTo(SCALINGFACTOR);
      }
    }
    if (type == "playerbullet") {
      for (int i = 0; i < BULLETPICS; i++) {
        projectileBullet[i] = EZ.addImage("ProjectileBulletExplosion/" + i + ".png", -10, 0);
        projectileBullet[i].hide();
        projectileBullet[i].scaleTo(SCALINGFACTOR + 1);
      }
    }
  }

  // Check if player's projectiles are touching in specified coordinates. true if is, false otherwise
  public boolean isPointInElement(int x, int y) {
    if (type == "playerbullet") {
      if (bullet.isPointInElement(x, y))
        return true;
      else
        return false;
    }
    if (type == "playergrenade") {
      if (grenade.isPointInElement(x, y))
        return true;
      else
        return false;
    }
    if (type == "scientistBullet" || type == "helicopterBullet" || type == "UFOBullet" || type == "tankBullet" || type == "zombieBullet" || type == "mechaBullet" || type == "airshipBullet") {
      if (bullet.isPointInElement(x, y))
        return true;
      else
        return false;
    }
    //Note: will never get here unless type is spelled wrong
    return false;
  }

  // Controls player's grenade animation
  public void grenadeExplosionAnimation(int posx, int posy) {
    translateGrenadeExplosionAnimation(posx, posy);
    for (int i = 0; i < GRENADEEXPLOSIONPICS; i++) {
      projectileGrenade[i].show();
      //EZ.refreshScreen();
      for (int counter = 0; counter < DELAY; counter++) {
        EZ.refreshScreen();
      }
      projectileGrenade[i].hide();
    }
  }

  // Controls player's bullet animation
  public void bulletExplosionAnimation(int posx, int posy) {
    translateBulletExplosionAnimation(posx, posy);
    for (int i = 0; i < BULLETPICS; i++) {
      projectileBullet[i].show();
      //EZ.refreshScreen();
      for (int counter = 0; counter < DELAY; counter++) {
        EZ.refreshScreen();
      }
      projectileBullet[i].hide();
    }
  }

  // Translate the player's bullet explosion animation pictures to where the bullet hit or if out of map
  public void translateBulletExplosionAnimation(int posx, int posy) {
    for (int i = 0; i < BULLETPICS; i++) {
      projectileBullet[i].translateTo(posx, posy);
    }
  }

  // Translate the player's grenade explosion animation pictures to where the grenade hit or if out of map
  public void translateGrenadeExplosionAnimation(int posx, int posy) {
    for (int i = 0; i < GRENADEEXPLOSIONPICS; i++) {
      projectileGrenade[i].translateTo(posx, posy);
    }
  }
}
