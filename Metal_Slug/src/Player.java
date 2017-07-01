/* ******************************
 *
 * File: Player.java
 *
 * Class for controlling all actions
 * involving the player such as
 * collision, animation, and states.
 *
 * *************************** */


import java.awt.event.KeyEvent;
import java.awt.Color;
import java.io.*;
import java.lang.*;

public class Player {

  // Player states
  private EZImage Stand;
  private EZImage Shoot;
  private EZImage Crouch;
  private EZImage Jump;
  private EZImage Land;
  private EZImage Up;
  private EZImage Knife;
  private EZImage Grenade;
  private EZImage Death1;
  private EZImage Death2;
  private EZImage Victory;
  private EZImage Left;
  private int health;

  // Holds animation pictures for each state
  private EZImage playerShooting[] = new EZImage[SHOOTINGPICS];
  private EZImage playerWalking[] = new EZImage[WALKINGPICS];
  private EZImage playerKnife[] = new EZImage[KNIFEPICS];
  private EZImage playerTurn[] = new EZImage[TURNPICS];
  private EZImage playerUp[] = new EZImage[UPPICS];
  private EZImage playerReload[] = new EZImage[RELOADPICS];
  private EZImage playerIdle[] = new EZImage[IDLEPICS];
  private EZImage playerGrenade[] = new EZImage[GRENADEPICS];
  private EZImage playerCrouchGrenade[] = new EZImage[CROUCHGRENADEPICS];
  private EZImage playerCrouchShoot[] = new EZImage[CROUCHSHOOTPICS];
  private EZImage playerJumpUp[] = new EZImage[JUMPUPPICS];
  private EZImage playerVictory[] = new EZImage[VICTORYPICS];

  // Player coordinates, current state, and sounds
  private int direction;
  private int posx = 0;
  private int posy = 0;
  private int playerState;
  private EZSound reload;
  private EZSound gettinghit;

  private static final int MAPXLENGTH = 1475;

  // State values
  private static final int STAND = 1;
  private static final int SHOOT = 2;
  private static final int CROUCH = 3;
  private static final int JUMP = 4;
  private static final int LAND = 5;
  private static final int UP = 6;
  private static final int KNIFE = 7;
  private static final int GRENADE = 8;
  private static final int DEATH1 = 9;
  private static final int DEATH2 = 10;
  private static final int VICTORY = 11;
  private static final int LEFT = 101;
  private static final int RIGHT = 100;

  // Speed of jump, land, movement, and delay
  private static final float MOVE_SPEED = 3.75f;
  private static final float JUMP_SPEED = 4;
  private static final float LAND_SPEED = 4;
  private static final int JUMPLENGTH = 160;
  private static final int DELAY = 3;
  private static final int HEALTH = 800;

  // SMOOTH MOVEMENT, use 180 frame rate, 150 or 120

  private static final int SHOOTINGPICS = 0;
  private static final int WALKINGPICS = 0;
  private static final int KNIFEPICS = 0;
  private static final int TURNPICS = 0;
  private static final int UPPICS = 0;
  private static final int RELOADPICS = 18;
  private static final int RELOADTIME = 150;
  private static final int IDLEPICS = 0;
  private static final int GRENADEPICS = 0;
  private static final int CROUCHGRENADEPICS = 0;
  private static final int CROUCHSHOOTPICS = 0;
  private static final int JUMPUPPICS = 0;
  private static final int VICTORYPICS = 6;

  //USE THESE SETTINGS FOR REALISTIC MOVEMENT, FULL ANIMATION FOR EACH STATE
  //use 90 or 120 frame rate(EXPERIMENTAL), may need to adjust projectile speeds and map translation

	/*
	private static final int SHOOTINGPICS = 7;
	private static final int WALKINGPICS = 14;
	private static final int KNIFEPICS = 7;
	private static final int TURNPICS = 0;
	private static final int UPPICS = 7;
	private static final int RELOADPICS = 18;
	private static final int RELOADTIME = 20;
	private static final int IDLEPICS = 10;
	private static final int GRENADEPICS = 6;
	private static final int CROUCHGRENADEPICS = 11;
	private static final int CROUCHSHOOTPICS = 16;
	private static final int JUMPUPPICS = 5;
	private static final int VICTORYPICS = 6;
	*/

  // Counters for jump/land
  private int reloadCounter = 0;
  private float jumpCounter = 0;
  private static final float SCALINGFACTOR = 2.5f;

  // Constructor for player
  public Player(int x, int y) {
    posx = x;
    posy = y;

    // Stand is default facing right
    Stand = EZ.addImage("PlayerStanding/0.png", posx, posy);
    Shoot = EZ.addImage("PlayerShooting/7.png", posx, posy);
    Crouch = EZ.addImage("Player/PlayerCrouch.png", posx, posy + 15);
    Jump = EZ.addImage("Player/PlayerJump.png", posx, posy);
    Land = EZ.addImage("Player/PlayerLand.png", posx, posy);
    Up = EZ.addImage("Player/PlayerUp.png", posx, posy);
    Left = EZ.addImage("Player/PlayerLeft.png", posx, posy);
    Knife = EZ.addImage("Player/PlayerKnife.png", posx, posy);
    Grenade = EZ.addImage("Player/PlayerGrenade.png", posx, posy);
    Death1 = EZ.addImage("Player/PlayerDeath1.png", posx, posy);
    Death2 = EZ.addImage("Player/PlayerDeath2.png", posx, posy);
    Victory = EZ.addImage("Player/PlayerVictory.png", posx, posy);

    direction = RIGHT;
    playerState = STAND;
    hidePlayer();
    Stand.show();
    health = HEALTH;
    reload = EZ.addSound("Sounds/Reload.wav");
    gettinghit = EZ.addSound("Sounds/Gettinghit.wav");
  }

  // Scale the pictures for each state and load in animation pictures
  public void animationInit() {
    Stand.scaleTo(SCALINGFACTOR);
    Shoot.scaleTo(SCALINGFACTOR);
    Crouch.scaleTo(SCALINGFACTOR);
    Jump.scaleTo(SCALINGFACTOR);
    Land.scaleTo(SCALINGFACTOR);
    Up.scaleTo(SCALINGFACTOR);
    Knife.scaleTo(SCALINGFACTOR);
    Grenade.scaleTo(SCALINGFACTOR);
    Death1.scaleTo(SCALINGFACTOR);
    Death2.scaleTo(SCALINGFACTOR);
    Victory.scaleTo(SCALINGFACTOR);
    Left.scaleTo(SCALINGFACTOR);

    // Load in animation pictures
    for (int i = 0; i < SHOOTINGPICS; i++) {
      playerShooting[i] = EZ.addImage("PlayerShooting/" + i + ".png", posx, posy);
      playerShooting[i].hide();
      playerShooting[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < WALKINGPICS; i++) {
      playerWalking[i] = EZ.addImage("PlayerWalking/" + i + ".png", posx, posy);
      playerWalking[i].hide();
      playerWalking[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < KNIFEPICS; i++) {
      playerKnife[i] = EZ.addImage("PlayerKnife/" + i + ".png", posx, posy);
      playerKnife[i].hide();
      playerKnife[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < TURNPICS; i++) {
      playerTurn[i] = EZ.addImage("PlayerDirection/" + i + ".png", posx, posy);
      playerTurn[i].hide();
      playerTurn[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < UPPICS; i++) {
      playerUp[i] = EZ.addImage("PlayerUp/" + i + ".png", posx, posy);
      playerUp[i].hide();
      playerUp[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < RELOADPICS; i++) {
      playerReload[i] = EZ.addImage("PlayerReload/" + i + ".png", posx, posy);
      playerReload[i].hide();
      playerReload[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < IDLEPICS; i++) {
      playerIdle[i] = EZ.addImage("PlayerIdle/" + i + ".png", posx, posy);
      playerIdle[i].hide();
      playerIdle[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < GRENADEPICS; i++) {
      playerGrenade[i] = EZ.addImage("PlayerGrenade/" + i + ".png", posx, posy);
      playerGrenade[i].hide();
      playerGrenade[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < CROUCHGRENADEPICS; i++) {
      playerCrouchGrenade[i] = EZ.addImage("PlayerCrouchGrenade/" + i + ".png", posx, posy);
      playerCrouchGrenade[i].hide();
      playerCrouchGrenade[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < CROUCHSHOOTPICS; i++) {
      playerCrouchShoot[i] = EZ.addImage("PlayerCrouchShoot/" + i + ".png", posx, posy);
      playerCrouchShoot[i].hide();
      playerCrouchShoot[i].scaleTo(SCALINGFACTOR);
    }
    for (int i = 0; i < VICTORYPICS; i++) {
      playerVictory[i] = EZ.addImage("PlayerVictory/" + i + ".png", -20, 0);
      playerVictory[i].hide();
      playerVictory[i].scaleTo(SCALINGFACTOR);
    }
  }

  // All animations for each state. Enable 2nd refreshScreen depending on frame rate
  // Controls the shoot animation
  public void shootingAnimation(int posx, int posy) {
    translateShootingAnimation(posx, posy);
    for (int i = 0; i < SHOOTINGPICS; i++) {
      playerShooting[i].show();
      EZ.refreshScreen();
      playerShooting[i].hide();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
    }
  }

  // Controls victory animation
  public void victoryAnimation() {
    for (int i = 0; i < VICTORYPICS; i++) {
      playerVictory[i].show();
      for (int j = 0; j < 20; j++) {
        EZ.refreshScreen();
      }
      playerVictory[i].hide();
    }
  }

  // Controls walking animation
  public void walkingAnimation() {
    for (int i = 0; i < WALKINGPICS; i++) {
      posx += MOVE_SPEED;
      translateWalkingAnimation(getXpos(), getYpos());
      playerWalking[i].show();
      EZ.refreshScreen();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerWalking[i].hide();
    }
  }

  // Controls knife animation
  public void knifeAnimation(int posx, int posy) {
    translateKnifeAnimation(posx, posy);
    for (int i = 0; i < KNIFEPICS; i++) {
      playerKnife[i].show();
      //EZ.refreshScreen();
      for (int counter = 0; counter < DELAY; counter++) {
        EZ.refreshScreen();
      }
      playerKnife[i].hide();
    }
  }

  // Controls when player turns from left/right or right/left
  public void turnAnimation(char side) {
    translateTurnAnimation(getXpos(), getYpos());
    if (side == 'd' && direction == LEFT) {
      for (int i = TURNPICS - 1; i < 0; i--) {
        playerTurn[i].show();
        EZ.refreshScreen();
				/*
				for(int counter = 0; counter < DELAY; counter++)
				{
					EZ.refreshScreen();
				}
				*/
        playerTurn[i].hide();
      }
    }
    if (side == 'a' && direction == RIGHT) {
      for (int i = 0; i < TURNPICS; i++) {
        playerTurn[i].show();
        EZ.refreshScreen();
				/*
				for(int counter = 0; counter < DELAY; counter++)
				{
					EZ.refreshScreen();
				}
				*/
        playerTurn[i].hide();
      }
    }
  }

  // Controls when player looks up
  public void UpAnimation(int posx, int posy) {
    translateUpAnimation(posx - 10, posy - 16);
    for (int i = 0; i < UPPICS; i++) {
      playerUp[i].show();
      EZ.refreshScreen();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerUp[i].hide();
    }
  }

  // Controls reload animation
  public void reloadAnimation(int posx, int posy) {
    translateReloadAnimation(posx, posy);
    for (int i = 0; i < RELOADPICS; i++) {
      playerReload[i].show();
      EZ.refreshScreen();
      for (int counter = 0; counter < DELAY; counter++) {
        EZ.refreshScreen();
      }
      playerReload[i].hide();
    }
  }

  // Controls idle animation, currently not used
  public void idleAnimation(int posx, int posy) {
    translateIdleAnimation(posx, posy);
    for (int i = 0; i < IDLEPICS; i++) {
      playerIdle[i].show();
      EZ.refreshScreen();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerIdle[i].hide();
    }
  }

  // Controls animation when throw grenade
  public void grenadeAnimation(int posx, int posy) {
    translateGrenadeAnimation(posx, posy);
    for (int i = 0; i < GRENADEPICS; i++) {
      playerGrenade[i].show();
      EZ.refreshScreen();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerGrenade[i].hide();
    }
  }

  // Controls animation when throwing grenade in crouch position
  public void crouchGrenadeAnimation(int posx, int posy) {
    translateCrouchGrenadeAnimation(posx - 10, posy + 10);
    for (int i = 0; i < CROUCHGRENADEPICS; i++) {
      playerCrouchGrenade[i].show();
      EZ.refreshScreen();
			/*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerCrouchGrenade[i].hide();
    }
  }

  // Controls animation when shooting in crouch position
  public void crouchShootAnimation(int posx, int posy) {
    translateCrouchShootAnimation(posx - 10, posy + 10);
    for (int i = 0; i < CROUCHSHOOTPICS; i++) {
      playerCrouchShoot[i].show();
      EZ.refreshScreen();
		    /*
			for(int counter = 0; counter < DELAY; counter++)
			{
				EZ.refreshScreen();
			}
			*/
      playerCrouchShoot[i].hide();
    }
  }

  // Translate animation pictures depending on action to current player's position
  public void translateVictoryAnimation(int posx, int posy) {
    for (int i = 0; i < VICTORYPICS; i++) {
      playerVictory[i].translateTo(posx, posy);
    }
  }

  public void translateCrouchShootAnimation(int posx, int posy) {
    for (int i = 0; i < CROUCHSHOOTPICS; i++) {
      playerCrouchShoot[i].translateTo(posx, posy);
    }
  }

  public void translateCrouchGrenadeAnimation(int posx, int posy) {
    for (int i = 0; i < CROUCHGRENADEPICS; i++) {
      playerCrouchGrenade[i].translateTo(posx, posy);
    }
  }

  public void translateGrenadeAnimation(int posx, int posy) {
    for (int i = 0; i < GRENADEPICS; i++) {
      playerGrenade[i].translateTo(posx, posy);
    }
  }

  public void translateIdleAnimation(int posx, int posy) {
    for (int i = 0; i < IDLEPICS; i++) {
      playerIdle[i].translateTo(posx, posy);
    }
  }

  public void translateReloadAnimation(int posx, int posy) {
    for (int i = 0; i < RELOADPICS; i++) {
      playerReload[i].translateTo(posx, posy);
    }
  }

  public void translateUpAnimation(int posx, int posy) {
    for (int i = 0; i < UPPICS; i++) {
      playerUp[i].translateTo(posx, posy);
    }
  }

  public void translateTurnAnimation(int posx, int posy) {
    for (int i = 0; i < TURNPICS; i++) {
      playerTurn[i].translateTo(posx, posy);
    }
  }

  public void translateKnifeAnimation(int posx, int posy) {
    for (int i = 0; i < KNIFEPICS; i++) {
      playerKnife[i].translateTo(posx, posy);
    }
  }

  public void translateWalkingAnimation(int posx, int posy) {
    for (int i = 0; i < WALKINGPICS; i++) {
      playerWalking[i].translateTo(posx, posy);
    }
  }

  public void translateShootingAnimation(int posx, int posy) {
    for (int i = 0; i < SHOOTINGPICS; i++) {
      playerShooting[i].translateTo(posx, posy);
    }
  }

  // Check if touching in specified coordinates. true if is, false otherwise
  public boolean isPointInElement(int x, int y) {
    if (Stand.isPointInElement(x, y) || Jump.isPointInElement(x, y))
      return true;
    else
      return false;
  }

  // Health for player
  public void collision() {
    health--;
    gettinghit.play();
    //If dead
    if (health <= 0) {
      //Currently player can't die
    }
  }

  // Returns player health
  public int getHealth() {
    return health;
  }

  // Returns current x position
  public int getXpos() {
    return posx;
  }

  // Returns current y position
  public int getYpos() {
    return posy;
  }

  // Returns reload number
  public int getReloadCounter() {
    return reloadCounter;
  }

  // Controls when to reload
  public void reload() {
    // Reload
    if (reloadCounter == RELOADTIME) {
      hidePlayer();
      reloadAnimation(getXpos(), getYpos());
      Stand.show();
      reloadCounter = 0;
      reload.play();
    }
  }

  // Hide all player states
  public void hidePlayer() {
    Stand.hide();
    Shoot.hide();
    Crouch.hide();
    Jump.hide();
    Land.hide();
    Up.hide();
    Knife.hide();
    Grenade.hide();
    Death1.hide();
    Death2.hide();
    Victory.hide();
    Left.hide();
  }

  // Translate player state pictures to desired coordinates
  public void translatePlayer(int x, int y) {
    Stand.translateTo(x, y);
    Shoot.translateTo(x, y);
    Crouch.translateTo(x, y + 15);
    Jump.translateTo(x, y);
    Land.translateTo(x, y);
    Up.translateTo(x, y);
    Knife.translateTo(x, y);
    Grenade.translateTo(x, y);
    Death1.translateTo(x, y);
    Death2.translateTo(x, y);
    Victory.translateTo(x, y);
    Left.translateTo(x, y);
  }

  // Returns the player state
  public int currentState() {
    return playerState;
  }

  // Controls all actions and controls of player
  public char processPlayer() {
    switch (playerState) {
      case STAND:

        hidePlayer();
        Stand.show();

        // Shoot
        if (EZInteraction.wasKeyPressed('k')) {
          hidePlayer();
          shootingAnimation(getXpos(), getYpos());
          reload();
          Shoot.show();
          reloadCounter++;
          return 'k';
        }
        // Knife
        else if (EZInteraction.isKeyDown('j')) {
          hidePlayer();
          knifeAnimation(getXpos(), getYpos());
          reload();
          Knife.show();
          reloadCounter++;
          return 'q';
        }
        // Jump
        else if (EZInteraction.wasKeyPressed(KeyEvent.VK_SPACE)) {
          playerState = JUMP;
          jumpCounter = 0;
          hidePlayer();
          Jump.show();
        }
        // Crouch
        else if (EZInteraction.isKeyDown('s')) {
          playerState = CROUCH;
          hidePlayer();
          Crouch.show();
        }
        // Looking up
        else if (EZInteraction.isKeyDown('w')) {
          playerState = UP;
          hidePlayer();
          Up.show();
        }
        // Grenade
        else if (EZInteraction.wasKeyPressed('l')) {
          hidePlayer();
          grenadeAnimation(getXpos(), getYpos());
          Stand.show();
          reload();
          reloadCounter++;
          return 'l';
        }
        // Move right
        else if (EZInteraction.isKeyDown('d')) {
          if (posx <= MAPXLENGTH)
            posx += MOVE_SPEED;
          hidePlayer();
          //turnAnimation('d');
          walkingAnimation();
          translatePlayer(posx, posy);
          Stand.show();
          //direction = RIGHT;
        }
        // Move left
        else if (EZInteraction.isKeyDown('a')) {
          if (posx >= 50)
            posx -= MOVE_SPEED;
          hidePlayer();
          //turnAnimation('a');
          translatePlayer(posx, posy);
          Left.show();
          //direction = LEFT;
        }

        //NOT SURE WHAT THIS IS, idle animation but have to change key to use it, currently not used
			/*
			else if(EZInteraction.isKeyDown('p'))
			{
				hidePlayer();
				idleAnimation(getXpos(), getYpos());
				Stand.show();
			}
			*/
        break;

      case UP:
        // If looking up key is held down
        if (EZInteraction.isKeyDown('w')) {
          hidePlayer();
          //UpAnimation(getXpos(), getYpos());
          Up.show();

          // Move left
          if (EZInteraction.isKeyDown('a')) {
            if (posx >= 50)
              posx -= MOVE_SPEED;
            translatePlayer(posx, posy);
          }
          // Move right
          if (EZInteraction.isKeyDown('d')) {
            if (posx <= MAPXLENGTH)
              posx += MOVE_SPEED;
            translatePlayer(posx, posy);
          }
          // If shoot, return key for shooting up
          if (EZInteraction.wasKeyPressed('k')) {
            return 'z';
          }
          // If grenade, return key for grenade up
          if (EZInteraction.wasKeyPressed('l')) {
            return 'x';
          }
        }

        // If release up key
        else {
          playerState = STAND;
          hidePlayer();
          Stand.show();
        }
        break;

      case CROUCH:

        // If holding crouch key down
        if (EZInteraction.isKeyDown('s')) {
          hidePlayer();
          Crouch.show();

          // Shooting in crouch, return key for crouch shoot
          if (EZInteraction.wasKeyPressed('k')) {
            hidePlayer();
            crouchShootAnimation(getXpos(), getYpos());
            //reload();
            Crouch.show();
            //reloadCounter++;
            return 'm';
          }

          // Grenade in crouch, return key for grenade shoot
          if (EZInteraction.wasKeyPressed('l')) {
            hidePlayer();
            crouchGrenadeAnimation(getXpos(), getYpos());
            //reload();
            Crouch.show();
            //reloadCounter++;
            return 'n';
          }
        }
        // If release crouch key
        else {
          playerState = STAND;
          hidePlayer();
          Stand.show();
        }
        break;

      case JUMP:

        // Jump up
        jumpCounter += JUMP_SPEED;
        // Jump up until jump height
        if (jumpCounter > JUMPLENGTH) {
          playerState = LAND;
          hidePlayer();
          Land.show();
        }
        // If reach height, then land
        else {
          posy -= LAND_SPEED;
          translatePlayer(posx, posy);
          // Shooting while jumping, return key for jump shoot
          if (EZInteraction.wasKeyPressed('k')) {
            reloadCounter++;
            return 'o';
          }
          // Grenade while jumping, return key for jump shoot
          if (EZInteraction.wasKeyPressed('l')) {
            reloadCounter++;
            return 'i';
          }
        }
        break;

      case LAND:

        // Bring player down
        jumpCounter -= JUMP_SPEED;
        //Land until reach ground
        if (jumpCounter <= 0) {
          playerState = STAND;
          hidePlayer();
          Stand.show();
        }
        // If reach ground
        else {
          posy += LAND_SPEED;
          translatePlayer(posx, posy);
          // Shoot while landing, return key for land shoot
          if (EZInteraction.wasKeyPressed('k')) {
            reloadCounter++;
            return 'u';
          }
          // Grenade while jumping, return key for land shoot
          if (EZInteraction.wasKeyPressed('l')) {
            reloadCounter++;
            return 't';
          }
        }
    }

    // Dummy key, should never reach here
    return 'h';
  }
}
