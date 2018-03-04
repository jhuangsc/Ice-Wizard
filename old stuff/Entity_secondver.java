/**
	ENTITY - for Ice Wizard game
		by Jonathan Huang - Apr 2012
*/
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;

class Entity {

	/// classification constants
	static final int OBJ_PLAYER = 0, OBJ_PROJECTILE = 1, OBJ_NPC = 2, OBJ_OBJ = 3;
	static final int PLAYER = 10;
	static final int PROJECTILE_SNOWFLAKE = 100, PROJECTILE_ICESHARD = 101, PROJECTILE_FIREBALL = 102;
	static final int ENEMY_FIREBALL = 200;
	static final int ENEMY_BOSS_GOLEM = 300, ENEMY_BOSS_CHAOS = 301, ENEMY_BOSS_ORDER = 302;
	
	/// meta variables
	Game parent;
	Entity owner;
	AI ai;
	ArrayList<Vector> velocities;
	ArrayList<Entity> projectiles;
	ArrayList<Effect> effects;
	boolean garbage = false;
	boolean firing = false, dead = false;
	Timer t125ms;

	/// physics variables
	double radius, mass, baseSpeed, angleFacing; // radius in pixels, mass in kg
	double x, y;
	boolean ignoresFriction = false;
	
	/// game variables
	int type, id;
	double hp, hpmax, mp, mpmax, shield, shieldmax;
	int charge, chargemax;	
	
	/// effects flags
	boolean canMove = true, canAttack = true, aiActive = true, invulnerable = false;
	int extraBullets = 0;
	double damageDealtMult = 1.0, damageTakenMult = 1.0, baseSpeedMult = 1.0;
	
	public Entity(double X, double Y, int TYPE, int ID, Game PARENT, Entity OWNER) {
		this.parent = PARENT;
		this.owner = OWNER;
		this.x = X;
		this.y = Y;
		
		velocities = new ArrayList<Vector>();
		projectiles = new ArrayList<Entity>();
		effects = new ArrayList<Effect>();
		this.baseSpeed = 100.0;
		this.angleFacing = 0.0;
		
		this.type = TYPE;
		this.id = ID;
		
		if (type == OBJ_PLAYER) {
			ai = new AI(this);
			this.hp = 100;
			this.hpmax = 100;
			this.mp = 100;
			this.mpmax = 100;
			this.shield = 100.0;
			this.shieldmax = 100.0;
			this.charge = 100;
			this.chargemax = 100;
			this.mass = 20.0;
			this.radius = 16.0;
		} else if (type == OBJ_PROJECTILE) {
			this.hp = 10;
			this.hpmax = 10;
			this.mp = 0;
			this.mpmax = 0;
			this.shield = 0.0;
			this.shieldmax = 0.0;
			this.charge = 0;
			this.chargemax = 0;
			this.mass = 0.5;
			this.ignoresFriction = true;
			
			switch(id) {
				case PROJECTILE_SNOWFLAKE:	this.radius = 3.0; baseSpeed = 300.0; break;
				case PROJECTILE_ICESHARD:	this.radius = 6.0; baseSpeed = 600.0; break;
				case PROJECTILE_FIREBALL:	this.radius = 4.5; baseSpeed = 100.0; break;
			}
		} else if (type == OBJ_NPC) {
			ai = new AI(this);
			switch(id) {
				case ENEMY_FIREBALL: 	
					this.hp = 200.0; this.hpmax = 200.0; 
					this.shield = 100.0; this.shieldmax = 100.0;
					this.mass = 10.0; this.radius = 8.0; break;
				case ENEMY_BOSS_GOLEM: 	
					this.hp = 5000.0; this.hpmax = 5000.0; 
					this.shield = 3000.0; this.shieldmax = 3000.0;
					this.mass = 800.0; this.radius = 80.0; break;
				case ENEMY_BOSS_CHAOS: 	
					this.hp = 7500.0; this.hpmax = 7500.0; 
					this.shield = 1200.0; this.shieldmax = 1200.0;
					this.mass = 1000.0; this.radius = 100.0; break;
				case ENEMY_BOSS_ORDER: 	
					this.hp = 9000.0; this.hpmax = 9000.0; 
					this.shield = 1500.0; this.shieldmax = 1500.0;
					this.mass = 1200.0; this.radius = 120.0; break;
			}
			this.mp = 100;
			this.mpmax = 100;
			this.charge = 100;
			this.chargemax = 100;
		} else if (type == OBJ_OBJ) {
			this.hp = 1000.0;
			this.hpmax = 1000.0;
			this.mp = 0.0;
			this.mpmax = 0.0;
			this.shield = 0.0;
			this.shieldmax = 0.0;
			this.mass = 10.0;
			this.radius = 20.0;
		} 
	}
	
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Update AI, effect durations, ability cooldowns
		/// Uses 100ms timer in Game: called every 100ms
		public void update() {
			try {
				ai.act();
			} catch (NullPointerException e) { }
			
			regenerateShield();
			regenerateHP();
			regenerateMP();
			
			/// update effect durations, where applicable
			for (int i = 0; i < effects.size(); i++) {
				if (effects.get(i).expired()) clearEffects();
				else effects.get(i).update();
			}
			
			/// update ability cooldowns
			for (int i = 0; i < ai.abilities.size(); i++) {
				ai.abilities.get(i).update();
			}
		}
	
		/// Checks if object has a velocity with given ID 
		/// Mainly used for base movement speed
		public boolean getHasVelocityID(String ID) {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(ID)) return true;
			} return false;
		}
		
		public boolean isGarbage() {
			return garbage;
		}
		
		public boolean isDead() {
			return dead;
		}
		
		public boolean alive() {
			return !dead;
		}
		
		public void clearEffects() {
			for (int i = 0; i < effects.size(); i++) {
				if (effects.get(i).expired()) {
					effects.remove(i);
					clearEffects();
					break;
				}
			}
		}
	
	//###################################################
	//## SPEED/VELOCITY METHODS #########################
	//###################################################
	
		/// Modify position based on current net speed
		/// Also check for collision with walls and other entities
		public void move() {
			if (this.type > OBJ_OBJ) return;
			
			double dy = getNetYVelocity()/parent.fps;
			double dx = getNetXVelocity()/parent.fps;
			
			int bounceX = 0;
			int bounceY = 0;
			
			/// if projectile was going to hit a wall
				/// damage the wall 
			for (int i = 0; i < parent.walls.size(); i++) {
				if (this.type != OBJ_PROJECTILE) break;
				
				if (this.getCollision(parent.walls.get(i))) 
					while (this.alive()) 
						dealDamageTo(parent.walls.get(i));
			}
			
			/// if other entity was going to hit a wall
				/// prevent it from passing through the wall
			for (int i = 0; i < parent.walls.size(); i++) {
				if (this.type == OBJ_PROJECTILE) break;
				Wall w = parent.walls.get(i);
				
				if (this.getCollision(w)) {
					int theta = (int)Math.toDegrees(Math.atan2( (w.cx - x), (w.cy - y) ));
					
					/// weird angles in java: 0 up, 90 left, 180/-180 down, -90 right
					if (theta == -135) { // from bottom right, lock X left, Y up
						bounceX = 1;
						bounceY = 1;
					} else if (theta == 135) { // from bottom left, lock X right, Y up
						bounceX = -1;
						bounceY = 1;
					} else if (theta == 45) { // from top left, lock X right, Y down
						bounceX = -1;
						bounceY = -1;
					} else if (theta == -45) { // from top right, lock X left, Y down
						bounceX = 1;
						bounceY = -1;
					} else if (theta > -135 && theta < -45) { // on right side: lock X left, normal Y
						bounceX = 1;
					} else if (theta < -135 || theta > 135) { // on bottom side: lock Y up, normal X
						bounceY = 1;
					} else if (theta > 45 && theta < 135) { // on left side: lock X right, normal Y
						bounceX = -1;
					} else if (theta > -45 && theta < 45) { // on top side: lock Y down, normal X
						bounceY = -1;
					} else {
						System.out.println("Whoa, collision error! Invalid angle detected.");
					}
					
					if (bounceX == -1) this.x = w.xLeft - this.radius;
					else if (bounceX == 1) this.x = w.xRight + this.radius;
					
					if (bounceY == -1) this.y = w.yTop - this.radius;
					else if (bounceY == 1) this.y = w.yBottom + this.radius;
					
				}
			}
			
			/// check collision with other entities
			for (int i = 0; i < parent.mobs.size(); i++) {
				if (this.type != OBJ_PLAYER && this.type != OBJ_PROJECTILE) break;
				Entity e = parent.mobs.get(i);
				
				if (getCollision(e))
					if (this.type == OBJ_PROJECTILE) 
						dealDamageTo(e);
					else
						this.touched(e);
			}
			
			/// update movement within bounds if no walls or enemies were hit
			if (bounceX == 0) this.x += dx;
			if (bounceY == 0) this.y += dy;
			
			/// if entity was going to go offscreen
				/// despawn if it is a projectile
				/// else keep it within the boundaries
			if (this.y < 0.0) {
				if (type == OBJ_PROJECTILE) dead();
				else this.y = 0.0;
			}
			if (this.y > parent.sy) {
				if (type == OBJ_PROJECTILE) dead();
				else this.y = parent.sy;
			}
			if (this.x < 0.0) { 
				if (type == OBJ_PROJECTILE) dead();
				else this.x = 0.0;
			}
			if (this.x > parent.sx) {
				if (type == OBJ_PROJECTILE) dead();
				else this.x = parent.sx;
			}
		}
		
		public void moveTowards(double nx, double ny) {
			if (this.type > OBJ_OBJ || this.type == OBJ_PLAYER) return;
			
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Vector.MOBMOVEMENT)) velocities.remove(i);
					
			// get angle to target location
			double ntheta = Math.atan2( (ny - y), (nx - x) );
			
			velocities.add(new Vector(Vector.MOBMOVEMENT,baseSpeed,ntheta,-1,parent));
		} 
		
		public void moveTowardsPlayer() {
			if (this.type == OBJ_PLAYER) return;
			
			moveTowards(parent.player.x, parent.player.y);
		}
		
		public void applyVelocity(double magnitude, double theta) {
			velocities.add(new Vector(Vector.VELOCITY,magnitude,theta,-1,parent));
		}
		
		public void applyVelocity(double magnitude, double theta, int durMS) {
			velocities.add(new Vector(Vector.VELOCITY,magnitude,theta,durMS,parent));
		}
		
		public double getNetXVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude * (v.id.equals("XBASESPEED") || v.id.equals("MOBMOVEMENT")? baseSpeedMult : 1.0) * Math.cos(v.direction);
			}
			return s;
		}
		
		public double getNetYVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude * (v.id.equals("YBASESPEED") || v.id.equals("MOBMOVEMENT")? baseSpeedMult : 1.0) * Math.sin(v.direction);
			}
			return s;
		}
		
		public double getVelocityMagnitude() {
			return Math.sqrt( Math.pow(getNetXVelocity(),2) + Math.pow(getNetYVelocity(),2) );
		}
		
		public double getVelocityAngle() {
			return Math.atan2( getNetYVelocity(), getNetXVelocity() );
			//return -1.0*Math.atan2(getNetXVelocity(), getNetYVelocity()) + Math.PI/2.0;
		}
		
		public void applyBaseXSpeed(int mult) {
			if (getHasVelocityID(Vector.XBASESPEED)) return;
			velocities.add(new Vector(Vector.XBASESPEED,baseSpeed,0.0+(Math.PI*mult),-1,parent));
		}
		
		public void removeBaseXSpeed() {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(Vector.XBASESPEED))
					velocities.remove(i);
			}
		}
		
		public void applyBaseYSpeed(int mult) {
			if (getHasVelocityID(Vector.YBASESPEED)) return;
			velocities.add(new Vector(Vector.YBASESPEED,baseSpeed,(Math.PI/2.0)+(Math.PI*mult),-1,parent));
		}
		
		public void removeBaseYSpeed() {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(Vector.YBASESPEED))
					velocities.remove(i);
			}
		}
	
	//###################################################
	//## PLAYER MOVEMENT METHODS ########################
	//###################################################
	
		public void moveUp() {
			applyBaseYSpeed(1);
		}
		
		public void moveDown() {
			applyBaseYSpeed(0);
		}
		
		public void moveLeft() {
			applyBaseXSpeed(1);
		}
		
		public void moveRight() {
			applyBaseXSpeed(0);
		}
		
		public void stop() {
			removeBaseYSpeed();
			removeBaseXSpeed();
		}
		
		public void stopX() {
			removeBaseXSpeed();
		}
		
		public void stopY() {
			removeBaseYSpeed();
		}
		
	//###################################################
	//## ANGLE METHODS ##################################
	//###################################################
	
		/// Takes angle in radians
		public void rotate(double theta) {
			this.angleFacing += theta;
		}
		
		/// Takes angle in radians
		public void setDirection(double theta) {
			this.angleFacing = theta;
		}
	
	//###################################################
	//## PHYSICS METHODS ################################
	//###################################################
	
		/// Check for collision based on distance between centre point and radii lengths
			public boolean getCollision(Entity obj) {
				double distance = Math.sqrt( Math.pow((this.x - obj.x),2) + Math.pow((this.y - obj.y),2) );
				if (distance - this.radius - obj.radius <= 0.0) return true;
				return false;
			}
			
		/// Same thing with walls but just check if within wall's bounds
			public boolean getCollision(Wall w) {
				if (this.x + this.radius > w.xLeft && this.x - this.radius < w.xRight && 
					this.y + this.radius > w.yTop && this.y - this.radius < w.yBottom) return true;
				return false;
			}
			
			public double getAngleTo(Entity e) {
				return Math.atan2( (this.y - e.y), (this.x - e.x) );
			}
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	public void spawnProjectile(int ID, double angleFacing, double tx, double ty) {
		Entity e = new Entity(this.x, this.y, OBJ_PROJECTILE, ID, parent, this);
		projectiles.add(e);
		projectiles.get(projectiles.size()-1).setDirection(angleFacing);
		projectiles.get(projectiles.size()-1).moveTowards(tx, ty);
	}
	
	public void touched(Entity e) {
		if (this.type == OBJ_PLAYER) {
			if (!this.hasEffect(Effect.FLINCHED)) {
				receiveDamage(e.damageDealt());
				applyVelocity(e.damageDealt(), getAngleTo(e), 100);
				e.applyVelocity(e.damageDealt(), getAngleTo(e)+Math.PI, 100);
			}
		} else if (this.type != OBJ_PROJECTILE) {
			if (!this.hasEffect(Effect.PACIFIED)) {
				dealDamageTo(e);
				applyVelocity(e.damageDealt(), getAngleTo(e), 100);
				e.applyVelocity(e.damageDealt(), getAngleTo(e)+Math.PI, 100);
			}
		}
	}
	
	public void dead() {
		dead = true;
		try {
			ai.stop();
		} catch (NullPointerException e) { }
		if (this.type == OBJ_PROJECTILE) garbage = true;
	}
	
	public double damageDealt() {
		double d = 0.0;
		switch(this.id) {
			case PROJECTILE_SNOWFLAKE:	d = 10.0; break;
			case PROJECTILE_ICESHARD:	d = 100.0; break;
			case PROJECTILE_FIREBALL:	d = 20.0; break;
			case ENEMY_FIREBALL: 		d = 20.0; break;
			case ENEMY_BOSS_GOLEM:		d = 50.0; break;
			case ENEMY_BOSS_CHAOS:		d = 40.0; break;
			case ENEMY_BOSS_ORDER:		d = 60.0; break;
		}
		try {
			return d*owner.damageDealtMult;
		} catch (NullPointerException e) {
			return d*damageDealtMult;
		}
	}
	
	public void dealDamageTo(Entity e) {
	
		if (hasEffect(Effect.PACIFIED)) return;
		
		try { 
			if (e == this.owner) return;
		} catch (NullPointerException exc) {}
		
		e.receiveDamage(damageDealt());
		
		if (this.type == OBJ_NPC) addEffect(Effect.PACIFIED);
		if (this.type == OBJ_PLAYER) buildCharge();
		if (this.type == OBJ_PROJECTILE) this.receiveDamage(10);	/// Projectiles take 10 damage for each enemy they hit
	}
	
	public void dealDamageTo(Wall w) {
	
		if (hasEffect(Effect.PACIFIED)) return;
		
		w.receiveDamage(damageDealt());
		// no charge built from hitting walls
		if (this.type == OBJ_PROJECTILE) this.receiveDamage(10);
	}
	
	public void receiveDamage(double dmg) {
		if (hasEffect(Effect.FLINCHED)) return;
		dmg *= damageTakenMult;
		
		shield -= dmg;
		if (shield < 0.0) {
			hp += shield;
			shield = 0.0;
		}
		
		if (hp <= 0.0) hp = 0.0;
		else if (hp > hpmax) hp = hpmax;
		
		if (this.type == OBJ_PLAYER) addEffect(Effect.FLINCHED);
		
		if (hp <= 0.0) dead();
	}
	
	public void spendMP(double m) {
		mp -= m;
		if (mp < 0.0) mp = 0.0;
		
		/// remove any effects that require mana
		removeMPToggles();
	}
	
	public void releaseCharge() {
		charge = 0;
	}
	
	public double getShieldPercent() {
		return Math.round(10000*(this.shield/this.shieldmax))/100.0;
	}
	
	public double getHPPercent() {
		return Math.round(10000*(this.hp/this.hpmax))/100.0;
	}
	
	public double getMPPercent() {
		return Math.round(10000*(this.mp/this.mpmax))/100.0;
	}
	
	public double getChargePercent() {
		return Math.round(10000*(1.0*this.charge/this.chargemax))/100.0;
	}
	
	public void regenerateShield() {
		if (this.shieldmax == 0.0) return;
		
		shield += 1.0;
		if (shield > shieldmax) shield = shieldmax;
	}
	
	public void regenerateHP() {
		if (this.type == OBJ_PLAYER) return;
		
		hp += 0.5;
		if (hp > hpmax) hp = hpmax;
	}
	
	public void regenerateMP() {
		mp += 2.0;
		if (mp > mpmax) mp = mpmax;
	}
	
	public void buildCharge() {
		charge += 1;
		if (charge > chargemax) charge = chargemax;
	}
	
	public void rechargeShield(double s) {
		shield += s;
		if (shield > shieldmax) shield = shieldmax;
	}
	
	public void recoverHP(double h) {
		hp += h;
		if (hp > hpmax) hp = hpmax;
	}
	
	public void recoverMP(double m) {
		mp += m;
		if (mp > mpmax) mp = mpmax;
	}
	
	//###################################################
	//## EFFECTS AND ABILITIES ##########################
	//###################################################
	
	public void addEffect(int effectID) {
		effects.add(new Effect(effectID, this));
	}
	
	public void removeEffect(int effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id == effectID) {
				effects.get(i).removeMods();
				effects.remove(i);
				return;
			}
		}
	}
	
	public void removeMPToggles() {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).requiresMP()) {
				effects.get(i).removeMods();
				effects.remove(i);
				removeMPToggles();
			}
		}
	}
	
	public boolean hasEffect(int effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id == effectID) return true;
		} return false;
	}
	
	public void addAbility(int abilityID) {
		if (hasAbility(abilityID)) return;
		ai.abilities.add(new Ability(abilityID,this));
	}
	
	/// Version of useAbility for non-targeted abilities
	public void useAbility(int abilityID) {
		if (!hasAbility(abilityID)) return;
		ai.useAbility(abilityID, this.x, this.y);
	}
	
	/// useAbility for targeted abilities
	public void useAbility(int abilityID, double tx, double ty) {
		if (!hasAbility(abilityID)) return;
		ai.useAbility(abilityID, tx, ty);
	}
	
	public boolean hasAbility(int abilityID) {
		for (int i = 0; i < ai.abilities.size(); i++) {
			if (ai.abilities.get(i).id == abilityID) return true;
		} return false;
	}
	
	
	
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
	
		if (id == PLAYER) {
			if (hasEffect(Effect.FLINCHED)) g.setColor(new Color(200,200,255));
			else g.setColor(new Color(255,255,255));
			drawUberflake(g, this.x, this.y, this.radius);
			//drawKochflake(g, this.x, this.y, 100.0);//this.radius);
		} else if (id == PROJECTILE_SNOWFLAKE) {
			drawSnowflake(g, this.x, this.y, this.radius);
		} else if (id == PROJECTILE_ICESHARD) {
			drawIceShard(g, this.x, this.y);
		} else if (id == PROJECTILE_FIREBALL) {
			
		} else if (id == ENEMY_FIREBALL) {
			g.setColor(new Color(255,(int)(200*(1 - this.getHPPercent()/100.0)),(int)(200*(1 - this.getHPPercent()/100.0)) ));
			g.fillOval((int)(this.x - this.radius), (int)(this.y - this.radius), (int)(this.radius*2), (int)(this.radius*2));
		} else if (id == ENEMY_BOSS_GOLEM) {
			drawGolem(g, this.x, this.y, this.radius);
		} else if (id == ENEMY_BOSS_CHAOS) {
			drawHexBoss(g, this.x, this.y, this.radius, bossConstJagged);
		} else if (id == ENEMY_BOSS_ORDER) {
			drawHexBoss(g, this.x, this.y, this.radius, bossConstSmooth);
		}
//		else if (id == ENEMY_BOSS_SLICK) {
//			drawHexBoss(g, this.x, this.y, this.radius, bossConstWood);
	//	}
		drawHPBar(g);
	}
	
	public void drawBars(Graphics g) {
		drawShieldBar(g);
		drawHPBar(g);
		drawMPBar(g);
	}
	
	public void drawShieldBar(Graphics g) {
		if (this.type == OBJ_PROJECTILE) return;
		if (this.shieldmax == 0.0) return;
		if (getShieldPercent() == 100.0 && getHPPercent() == 100.0) return;
		
		int len = (this.radius < 60? 50 : 100);
		
		int xTL = (int)(this.x - len/2);
		int yTL = (int)(this.y - this.radius - 15 - (this.radius > 60? 10 : 0));
		g.setColor(new Color(0,0,200));
		g.fillRect(xTL, yTL, (int)(len*getShieldPercent()/100.0), 4);
	}
	
	public void drawHPBar(Graphics g) {
		if (this.type == OBJ_PROJECTILE) return;
		if (getHPPercent() == 100.0 && getShieldPercent() == 100.0) return;
		
		int len = (this.radius < 60? 50 : 100);
		
		int xTL = (int)(this.x - len/2);
		int yTL = (int)(this.y - this.radius - 10 - (this.radius > 60? 10 : 0));
		g.setColor(new Color(127,0,0));
		g.fillRect(xTL, yTL, len, 4);
		g.setColor(new Color(0,127,0));
		g.fillRect(xTL, yTL, (int)(len*getHPPercent()/100.0), 4);
	}
	
	public void drawMPBar(Graphics g) {
		if (this.type == OBJ_PROJECTILE) return;
		if (this.mpmax == 0.0) return;
		if (getMPPercent() == 100.0) return;
		
		int len = (this.radius < 60? 50 : 100);
		
		int xTL = (int)(this.x - len/2);
		int yTL = (int)(this.y - this.radius - 5 - (this.radius > 60? 10 : 0));
		g.setColor(new Color(160,0,200));
		g.fillRect(xTL, yTL, (int)(len*getMPPercent()/100.0), 4);
	}
		
	/// BIG snowflake (or, potentially big)
	
		public void drawUberflake(Graphics g, double X, double Y, double len1) {
			if (len1 <= 2) return;
			drawSnowflake(g,X,Y,len1);
			drawUberflake(g,X,Y,len1/3.0);
		}

		public void drawSnowflake(Graphics g, double X, double Y, double len) {
			if (len < 3) return;
			
			double r = 0.0 + this.angleFacing;;
				
			for (double theta = r; theta < r + 2.0*Math.PI; theta += 2.0*Math.PI/6.0) {
				double x2 = X + (len * Math.cos(theta));
				double y2 = Y + (len * Math.sin(theta));
				g.drawLine((int)X, (int)Y, (int)x2, (int)y2);
				drawSnowflake(g, x2, y2, len/3.0);
			}
		}
	
	/// Just a triangle
	
		public void drawIceShard(Graphics g, double X, double Y) {
			int x1, y1, x2, y2, x3, y3;
			x1 = (int)(X + radius*2.0*Math.cos(angleFacing));
			y1 = (int)(Y + radius*2.0*Math.sin(angleFacing));
			x2 = (int)(X + radius*Math.cos(angleFacing+(2.0*Math.PI/3.0)));
			y2 = (int)(Y + radius*Math.sin(angleFacing+(2.0*Math.PI/3.0)));
			x3 = (int)(X + radius*Math.cos(angleFacing+(4.0*Math.PI/3.0)));
			y3 = (int)(Y + radius*Math.sin(angleFacing+(4.0*Math.PI/3.0)));
			int[] xar = {x1, x2, x3};
			int[] yar = {y1, y2, y3};
			g.setColor(new Color(240,240,255));
			g.fillPolygon( xar, yar, 3);
			//g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
			//g.drawLine((int)x2,(int)y2,(int)x3,(int)y3);
			//g.drawLine((int)x3,(int)y3,(int)x1,(int)y1);
		}
	
	/*// Koch snowflake
	
		public void drawKochflake(Graphics g, double X, double Y, double len) {
			if (len <= 1) return;
			double theta = -2.0*Math.PI/3.0;
			
			for (int i = 0; i < 6; i++) {
				double x1 = X + len*Math.cos(theta), y1 = Y + len*Math.sin(theta);
				theta += Math.PI/3.0;
				drawKochSide(g, x1, y1, X + len*Math.cos(theta), Y + len*Math.sin(theta), theta, len);
			}
			
		}
		
		public void drawKochSide(Graphics g, double x1, double y1, double x2, double y2, double len) {
			if (len <= 1) return;
			g.setColor(new Color(255,0,0));
			g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
			double xm2, ym2, xm3, ym3, xn, yn;
			
			xm2 = x1 + (len/3.0)*Math.cos(theta);
			ym2 = y1 + (len/3.0)*Math.sin(theta);
			theta += Math.PI/3.0;
			xm3 = xm2 + (len/3.0)*Math.cos(theta);
			ym3 = ym2 + (len/3.0)*Math.sin(theta);
			xn = Math.min(xm2,xm3) + (xm3 - xm2);
			yn = Math.min(ym2,ym3) + (ym3 - ym2);
			g.setColor(new Color(255,255,255));
			g.drawLine((int)x1, (int)y1, (int)xm2, (int)ym2);
			g.drawLine((int)xm3, (int)ym3, (int)x2, (int)y2);
			drawKochSide(g,xm2,ym2,xm3,ym3,theta,len/3.0);
		}
	*/
	/// Recursively drawn squares
	public void drawGolem(Graphics g, double cx, double cy, double side) {
		if (side < 2.0) return;
		g.setColor(new Color(255,255,255));
		g.fillRect((int)(cx - side/2.0), (int)(cy - side/2.0), (int)(side), (int)(side));
		g.setColor(new Color(255,0,0));
		g.drawRect((int)(cx - side/2.0), (int)(cy - side/2.0), (int)(side), (int)(side));
		drawGolem(g, cx - side/2.0, cy - side/2.0, side/2.2);
		drawGolem(g, cx + side/2.0, cy - side/2.0, side/2.2);
		drawGolem(g, cx - side/2.0, cy + side/2.0, side/2.2);
		drawGolem(g, cx + side/2.0, cy + side/2.0, side/2.2);
	}
	
	/// Bosses that were the result of failed attempts at coding the Koch snowflake
	/// they looked pretty cool nonetheless so I kept the code
		static final int bossConstWood = 1, bossConstJagged = 2, bossConstSmooth = 3;
		
		public void drawHexBoss(Graphics g, double X, double Y, double len, int bossConst) {
			if (len <= 1) return;
			double theta = -2.0*Math.PI/3.0;
			for (int i = 0; i < 6; i++) {
				double x1 = X + len*Math.cos(theta), y1 = Y + len*Math.sin(theta);
				theta += Math.PI/3.0;
				drawHexBossSide(g, x1, y1, X + len*Math.cos(theta), Y + len*Math.sin(theta), theta, len, bossConst);
			}
		}
		public void drawHexBossSide(Graphics g, double x1, double y1, double x2, double y2, double theta, double len, int bossConst) {
			if (len <= 1) return;
			g.setColor(new Color(255,0,0));
			g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
			double xm2, ym2, xm3, ym3, xn, yn;
			
			xm2 = x1 + (bossConst==bossConstWood? -1.0 : 1.0)*(len/3.0)*Math.cos(theta);
			ym2 = y1 + (bossConst==bossConstWood? -1.0 : 1.0)*(len/3.0)*Math.sin(theta);
			theta += Math.PI/3.0;
			xm3 = (bossConst==bossConstSmooth? xm2 : x2) + (bossConst==bossConstJagged? -1.0 : 1.0)*(len/3.0)*Math.cos(theta);
			ym3 = (bossConst==bossConstSmooth? ym2 : y2) + (bossConst==bossConstJagged? -1.0 : 1.0)*(len/3.0)*Math.sin(theta);
			xn = Math.min(xm2,xm3) + (xm3 - xm2);
			yn = Math.min(ym2,ym3) + (ym3 - ym2);
			g.setColor(new Color(128,0,0));
			g.drawLine((int)x1, (int)y1, (int)xm2, (int)ym2);
			g.drawLine((int)xm3, (int)ym3, (int)x2, (int)y2);
			drawHexBossSide(g,xm2,ym2,xm3,ym3,theta,len/3.0,bossConst);
		}
}