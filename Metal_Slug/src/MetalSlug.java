/* ******************************
 *
 * File: MetalSlug.java
 *
 * Main driver for game
 *
 * *************************** */

import java.awt.Color;

public class MetalSlug{
	public static void main(String[] args){
		
		// Initialize EZ and framerate
		EZ.initialize(1500,600);
		EZ.setFrameRate(180);
		
		// Initialize map, control screen, and BGM
		Initializer map = new Initializer("map", 5250, 300);
		Initializer control = new Initializer("control", 750, 300);
		control.playSound();
		boolean pauseflag = true;
		
		// Scoreboard and victory sound
		EZText scoreboard = EZ.addText(105, 35, "SCORE: 0", Color.white, 30);
		EZSound victory1 = EZ.addSound("Sounds/Victory.wav");
		EZSound victory2 = EZ.addSound("Sounds/Victory1.wav");
		
		// Maximum amount of bullets and grenades player can have on map at one time
		final int BULLETS = 15;
		final int GRENADES = 15;
		
		// Number of units and number of projectiles (1 projectile per enemy)
		final int DEATHCOUNTER = 42;
		final int MAXPROJECTILES = 42;
		final int MAXUNITS = 42;
		
		// Counter for player grenades/bullets to cycle through each enemy
		int UNITS_AND_PROJECTILES = 42;

		// Indexes for current state of the player's bullet and grenade
		boolean foundBullet = false, foundGrenade = false;
		int vaccantBulletIndex = 0;
		int vaccantGrenadeIndex = 0;

		// Player action
		char action;
		
		// New player and animate effects
		Player player = new Player(150, 500);
		player.animationInit();
		
		// Make enemy units, player projectiles and enemy projectiles
		Enemy units[] = new Enemy[MAXUNITS];
		Projectile bullets[] = new Projectile[BULLETS];
		Projectile grenades[] = new Projectile[GRENADES];
		Projectile enemyProjectiles[] = new Projectile[MAXPROJECTILES];

		// Creating enemies and their projectiles(enters screen at 1500)
		units[0] = new Enemy(1700, 500, "scientist", 1500, 600);
		units[1] = new Enemy(6800, 150, "helicopter", 1500, 600);
		units[2] = new Enemy(3500, 200, "UFO", 1500, 600);
		units[3] = new Enemy(2200, 500, "Tank", 1500, 600);
		units[4] = new Enemy(3726, 500, "Tank", 1500, 600);
		units[5] = new Enemy(3826, 500, "Tank", 1500, 600);
		units[6] = new Enemy(3926, 500, "scientist", 1500, 600);
		units[7] = new Enemy(8726, 100, "UFO", 1500, 600);
		units[8] = new Enemy(8826, 100, "UFO", 1500, 600);
		units[9] = new Enemy(8926, 100, "UFO", 1500, 600);
		units[10] = new Enemy(12726, 500, "zombie", 1500, 600);
		units[11] = new Enemy(12826, 500, "zombie", 1500, 600);
		units[12] = new Enemy(12926, 500, "zombie", 1500, 600);
		units[13] = new Enemy(16377, 500, "mecharobot", 1500, 600);
		units[14] = new Enemy(16376, 500, "mecharobot", 1500, 600);
		units[15] = new Enemy(16226, 300, "helicopter", 1500, 600);
		units[16] = new Enemy(16226, 150, "helicopter", 1500, 600);
		units[17] = new Enemy(20576, 50, "UFO", 1500, 600);
		units[18] = new Enemy(20475, 500, "mecharobot", 1500, 600);
		units[19] = new Enemy(20376, 100, "helicopter", 1500, 600);
		units[20] = new Enemy(20176, 250, "airship", 1500, 600);
		units[21] = new Enemy(20376, 400, "helicopter", 1500, 600);
		units[22] = new Enemy(20576, 200, "UFO", 1500, 600);
		units[23] = new Enemy(20476, 500, "Tank", 1500, 600);
		units[24] = new Enemy(20576, 350, "UFO", 1500, 600);
		units[25] = new Enemy(20176, 250, "airship", 1500, 600);
		units[26] = new Enemy(4576, 150, "UFO", 1500, 600);
		units[27] = new Enemy(4726, 500, "Tank", 1500, 600);
		units[28] = new Enemy(4926, 500, "scientist", 1500, 600);
		units[29] = new Enemy(3626, 500, "scientist", 1500, 600);
		units[30] = new Enemy(13377, 500, "mecharobot", 1500, 600);
		units[31] = new Enemy(14377, 500, "mecharobot", 1500, 600);
		units[32] = new Enemy(14577, 500, "mecharobot", 1500, 600);
		units[33] = new Enemy(13226, 350, "helicopter", 1500, 600);
		units[34] = new Enemy(13626, 370, "helicopter", 1500, 600);
		units[35] = new Enemy(12226, 100, "helicopter", 1500, 600);
		units[36] = new Enemy(5576, 50, "UFO", 1500, 600);
		units[37] = new Enemy(5676, 150, "UFO", 1500, 600);
		units[38] = new Enemy(5776, 250, "UFO", 1500, 600);
		units[39] = new Enemy(5576, 350, "UFO", 1500, 600);
		units[40] = new Enemy(6576, 450, "UFO", 1500, 600);
		units[41] = new Enemy(5876, 550, "UFO", 1500, 600);
		
		//Projectiles for enemies
		//-10 so initial bullet is off screen, X controls delay of when bullet first appears
		enemyProjectiles[0] = new Projectile(0, -10, "scientistBullet");
		enemyProjectiles[1] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[2] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[3] = new Projectile(0, -10, "tankBullet");
		enemyProjectiles[4] = new Projectile(0, -10, "tankBullet");
		enemyProjectiles[5] = new Projectile(0, -10, "tankBullet");
		enemyProjectiles[6] = new Projectile(0, -10, "scientistBullet");
		enemyProjectiles[7] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[8] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[9] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[10] = new Projectile(0, -10, "zombieBullet");
		enemyProjectiles[11] = new Projectile(0, -10, "zombieBullet");
		enemyProjectiles[12] = new Projectile(0, -10, "zombieBullet");
		enemyProjectiles[13] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[14] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[15] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[16] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[17] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[18] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[19] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[20] = new Projectile(0, -10, "airshipBullet");
		enemyProjectiles[21] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[22] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[23] = new Projectile(0, -10, "tankBullet");
		enemyProjectiles[24] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[25] = new Projectile(0, -10, "airshipBullet");
		enemyProjectiles[26] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[27] = new Projectile(0, -10, "tankBullet");
		enemyProjectiles[28] = new Projectile(0, -10, "scientistBullet");
		enemyProjectiles[29] = new Projectile(0, -10, "scientistBullet");
		enemyProjectiles[30] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[31] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[32] = new Projectile(0, -10, "mechaBullet");
		enemyProjectiles[33] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[34] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[35] = new Projectile(0, -10, "helicopterBullet");
		enemyProjectiles[36] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[37] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[38] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[39] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[40] = new Projectile(0, -10, "UFOBullet");
		enemyProjectiles[41] = new Projectile(0, -10, "UFOBullet");
		
		// Initialize enemies, player's bullets and grenades
		for(int i = 0; i < UNITS_AND_PROJECTILES; i++)
		{
			units[i].unitsInit();
			enemyProjectiles[i].projectileInit();
		}
		for(int i = 0; i < BULLETS; i++)
		{
			bullets[i] = new Projectile(-200,-200, "playerbullet");
			bullets[i].projectileInit();
			bullets[i].animationInit();
		}
		for(int i = 0; i < GRENADES; i++)
		{
			grenades[i] = new Projectile(-200,-200, "playergrenade");
			grenades[i].projectileInit();
			grenades[i].animationInit();
		}
		
		/* Player controls
		    w - look up
		    a - left
		    s - crouch
		    d - move right
		    p - random movement
		    l - shoot grenade
		    k - shoot bullet
		    j - knife
		    spacebar - jump
		 */

		// Main game loop
		while(true)
		{  
			// Move background map and update score
			map.translateObject(.5, 300);
			scoreboard.setMsg("SCORE: "+ units[0].getPlayerScore());
			
			// Pause game with 'p'
			if(EZInteraction.isKeyDown('p'))
			{
				pauseflag = true;
				control.pullToFront();
			}
			while (pauseflag == true)
			{
				control.show();
				EZ.refreshScreen();
				// Unpause
				if(EZInteraction.wasKeyPressed('o'))
				{
					pauseflag = false;
					control.hide();
					control.stopSound();
				}
			}
			
			// Get key pressed by player
			action = player.processPlayer();

			// Process bullets and find array of bullet not currently in use (these keys are any shooting key)
			if(action == 'k' || action == 'm' || action == 'o' || action == 'u' || action == 'z')
			{
				int i = 0;
				while(foundBullet == false && (i < BULLETS))
				{
					// If found vaccant bullet
					if(bullets[i].beingUsed() == false)
					{
						bullets[i].switchState();
						foundBullet = true;
						vaccantBulletIndex = i;
					}
					// Check next bullet
					else
						i++;
				}
				// All in use
				if(i == BULLETS)
					foundBullet = false;
			}
			
			// k is standing shoot, m is crouch shoot, o is jump shoot, u is land shoot, z is look up shoot
			if(foundBullet == true)
			{				
				if(action == 'k' || action == 'o' || action == 'u')
					bullets[vaccantBulletIndex].translateObject(player.getXpos() + 50, player.getYpos());
				if(action == 'm')
					bullets[vaccantBulletIndex].translateObject(player.getXpos() + 50, player.getYpos() + 25);
				if(action == 'z')
					bullets[vaccantBulletIndex].translateObjectUp(player.getXpos(), player.getYpos());
				foundBullet = false;
			}
			
			// Process grenades and find array of grenades not currently in use
			if(action == 'l' || action == 'n' || action == 'i' || action == 't' || action == 'x')
			{
				int i = 0;
				while(foundGrenade == false && (i < GRENADES))
				{
					// If found vaccant grenade
					if(grenades[i].beingUsed() == false)
					{
						grenades[i].switchState();
						foundGrenade = true;
						vaccantGrenadeIndex = i;
					}
					// Check next grenade
					else
						i++;
				}
				// All in use
				if(i == GRENADES)
					foundGrenade = false;
			}
			
			// l is standing grenade, n is crouch grenade, i is jump grenade, t is grenade
			if(foundGrenade == true)
			{
				if(action == 'l' || action == 'i' || action == 't')
					grenades[vaccantGrenadeIndex].translateObject(player.getXpos() + 50, player.getYpos());
				if(action == 'n')
					grenades[vaccantGrenadeIndex].translateObject(player.getXpos() + 50, player.getYpos() + 25);
				if(action == 'x')
					grenades[vaccantGrenadeIndex].translateObjectUp(player.getXpos(), player.getYpos());
				foundGrenade = false;	
			}
			
			// Process player's projectiles
			for(int i = 0; i < UNITS_AND_PROJECTILES; i++)
			{
				for(int j = 0; j < BULLETS; j++)
					if(bullets[j].beingUsed() == true)
						bullets[j].processProjectile(units[i].getXCenter(), units[i].getYCenter());
				for(int j = 0; j < GRENADES; j++)
					if(grenades[j].beingUsed() == true)
						grenades[j].processProjectile(units[i].getXCenter(), units[i].getYCenter());		
			}
			
			// Controls movement for enemy units
			for(int i = 0; i < UNITS_AND_PROJECTILES; i++)
				units[i].move();
			
			// Check if player's projectiles collide with enemy units
			for(int j = 0; j < UNITS_AND_PROJECTILES; j++)
			{
				for(int i = 0; i < BULLETS; i++)
					if(bullets[i].beingUsed() == true)
						if(bullets[i].isPointInElement(units[j].getXCenter(), units[j].getYCenter()) && units[j].getAliveOrDead() == true)
							units[j].collision();
				for(int i = 0; i < GRENADES; i++)
					if(grenades[i].beingUsed() == true)
						if(grenades[i].isPointInElement(units[j].getXCenter(), units[j].getYCenter()) && units[j].getAliveOrDead() == true)
							units[j].collision();
			}
			
			// Move enemy projectiles across map, resets if out of map
			for(int i = 0; i < UNITS_AND_PROJECTILES; i++)
				enemyProjectiles[i].processEnemyProjectile(units[i].getXCenter(), units[i].getYCenter(), units[i].getHealth());
			for(int i = 0; i < UNITS_AND_PROJECTILES; i++)
				if(enemyProjectiles[i].isPointInElement(player.getXpos(), player.getYpos()))
						player.collision();
			
			// Win
			if(units[25].returnDeathcounter() == DEATHCOUNTER)
			{
				// Hide other objects, show only victory
				System.out.println("Win");
				player.hidePlayer();
				player.translateVictoryAnimation(player.getXpos(), player.getYpos());
				victory1.play();
				victory2.play();
				// Hide player projectiles
				for(int i = 0; i < BULLETS; i++)
					if(bullets[i].beingUsed() == true)
						bullets[i].hide();
				for(int i = 0; i < GRENADES; i++)
					if(grenades[i].beingUsed() == true)
						grenades[i].hide();
				while(true)
					player.victoryAnimation();
			}
			
			//System.out.println("X is " + EZInteraction.getXMouse() + " Y is " + EZInteraction.getYMouse());
			//System.out.println("grenades max is " + GRENADES);
			//System.out.println(units[0].getHealth());
			//System.out.println("UNITS_is " + UNITS_AND_PROJECTILES);
			//System.out.println("Current map is " + map.getCurrentMap());
			//System.out.println("X is " + player.getXpos() + " Y is " + player.getYpos());
			//System.out.println(units[0].returnDeathcounter());
			EZ.refreshScreen();
		}
	}
}