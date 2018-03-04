/**
	ENTITY - for Ice Wizard game
		by Jonathan Huang - Apr 2012
*/
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

class Entity {

	/// classification constants
	static final int OBJ_PLAYER = 0, OBJ_PROJECTILE = 1, OBJ_NPC = 2, OBJ_OBJ = 3;
	static final int FACTION_PLAYER = 0, FACTION_ALLIES = 0, FACTION_ENEMIES = 1, FACTION_NEUTRAL = 2;
	
	/// meta variables
	Game parent;
	Entity owner;
	AI ai;
	BufferedImage img;
	ArrayList<Vector> velocities;
	ArrayList<Entity> projectiles;
	ArrayList<Effect> effects;
	boolean garbage = false;
	boolean firing = false, dead = false;
	Timer t125ms;

	/// physics variables
	double radius = 10.0, mass = 0.1, baseSpeed = 100.0, angleFacing; // radius in pixels, mass in kg
	double x, y;
	
	/// game variables
	String name = "MissingName", id;
	int type, faction = FACTION_NEUTRAL;
	double tx = 0.0, ty = 0.0;
	double hp = 100.0, hpmax = 100.0, mp = 0.0, mpmax = 0.0, shield = 0.0, shieldmax = 0.0;
	double hpregen = 0.0, mpregen = 0.0, shieldregen = 0.0;
	double baseDamage = 0.0;
	double charge = 0, chargemax = 0;	
	
	/// effects values/flags
	boolean invulnerable = false;
	int extraBullets = 0;
	double damageDealtMult = 1.0, damageTakenMult = 1.0, baseSpeedMult = 1.0;
	
	public Entity(String ID, double X, double Y, Game PARENT, Entity OWNER) {
		this.parent = PARENT;
		this.owner = OWNER;
		this.id = ID;
		this.x = X;
		this.y = Y;
		
		velocities = new ArrayList<Vector>();
		projectiles = new ArrayList<Entity>();
		effects = new ArrayList<Effect>();
		this.angleFacing = 0.0;
		
		initStats();
	}
	
	public void initStats() {
		if (id.startsWith("PLAYER")) type = OBJ_PLAYER;
		else if (id.startsWith("PROJECTILE")) type = OBJ_PROJECTILE;
		else if (id.startsWith("MOB") || id.startsWith("BOSS")) type = OBJ_NPC;
		else if (id.startsWith("ITEM") || id.startsWith("OBJECT")) type = OBJ_OBJ;
		else type = OBJ_OBJ;
	
		switch(type) {
			case OBJ_PLAYER: ai = new AI(this); break;
			case OBJ_PROJECTILE: break;
			case OBJ_NPC: ai = new AI(this); break;
			case OBJ_OBJ: break;
		}
		
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("data\\entities\\"+id+".dat"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) throw new IOException();
				String tag = ln.substring(0,ln.indexOf("="));
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					name = val;
				} else if (tag.equals("image")) {
					img = parent.images.get(val);
				} else if (tag.equals("faction")) {
					try {
						faction = (val.equals("PLAYER")? FACTION_PLAYER :
								   val.equals("ALLIES")? FACTION_ALLIES :
								   val.equals("ENEMIES")? FACTION_ENEMIES :
								   val.equals("NEUTRAL")? FACTION_ENEMIES :
								   val.equals("PROJECTILE")? owner.faction : owner.faction);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field FACTION");
					} catch (NullPointerException e) {
						System.out.println("Error while creating entity "+id+": Invalid or projectile faction and has no owner");
					}
				} else if (tag.equals("health")) {
					try {
						hpmax = Double.parseDouble(val);
						hp = hpmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HP");
					}
				} else if (tag.equals("mana")) {
					try {
						mpmax = Double.parseDouble(val);
						mp = mpmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field MP");
					}
						
				} else if (tag.equals("shield")) {
					try {
						shieldmax = Double.parseDouble(val);
						shield = shieldmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field SHIELD");
					}
						
				} else if (tag.equals("hpregen")) {
					try {
						hpregen = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HPREGEN");
					}
						
				} else if (tag.equals("mpregen")) {
					try {
						mpregen = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field MPREGEN");
					}
						
				} else if (tag.equals("shieldregen")) {
					try {
						shieldregen = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field SHIELDREGEN");
					}
						
				} else if (tag.equals("charge")) {
					try {
						chargemax = Double.parseDouble(val);
						charge = 0;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field CHARGE");
					}
						
				} else if (tag.equals("basedamage")) {
					try {
						baseDamage = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field BASEDAMAGE");
					}
						
				} else if (tag.equals("basespeed")) {
					try {
						baseSpeed = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field BASESPEED");
					}
						
				} else if (tag.equals("radius")) {
					try {
						radius = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field RADIUS");
					}
						
				} else if (tag.equals("mass")) {
					try {
						mass = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field MASS");
					}
						
				} else if (tag.equals("flags")) {
					// placeholder
				} else if (tag.equals("ability")) {
					addAbility(val);
				} else if (tag.equals("passive")) {
					// temp: placeholder for an Effect with duration -1 that will be applied on spawn
				}
				
			} file.close();
			
		} catch (IOException e) {
			System.out.println("Error: data\\entities\\"+id+".dat could not be found");
		}
	}
	
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Update AI, effect durations, ability cooldowns
		/// Uses 10ms timer in Game: called every 10ms
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
					this.touched(e);
			}
			if (this.type != OBJ_PLAYER)
				if (getCollision(parent.player))
					this.touched(parent.player);
			
			/// update movement within bounds if no walls or enemies were hit
			if (bounceX == 0) this.x += dx;
			if (bounceY == 0) this.y += dy;
			
			/// if entity was going to go offscreen
				/// despawn if it is a projectile
				/// else keep it within the boundaries
			if (y < radius) {
				if (type == OBJ_PROJECTILE) dead();
				else y = radius;
			}
			if (y > parent.sy - radius) {
				if (type == OBJ_PROJECTILE) dead();
				else y = parent.sy - radius;
			}
			if (x < radius) { 
				if (type == OBJ_PROJECTILE) dead();
				else x = radius;
			}
			if (x > parent.sx - radius) {
				if (type == OBJ_PROJECTILE) dead();
				else x = parent.sx - radius;
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
		
		public void moveTowards(double ntheta) {
			if (this.type > OBJ_OBJ || this.type == OBJ_PLAYER) return;
			
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Vector.MOBMOVEMENT)) velocities.remove(i);
			
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
				s += v.magnitude*Math.cos(v.direction)*(v.id.equals(Vector.XBASESPEED) || v.id.equals(Vector.MOBMOVEMENT)? baseSpeedMult : 1.0);
			}
			return s;
		}
		
		public double getNetYVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude * (v.id.equals(Vector.YBASESPEED) || v.id.equals(Vector.MOBMOVEMENT)? baseSpeedMult : 1.0) * Math.sin(v.direction);
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
			return Math.atan2( (e.y - this.y), (e.x - this.x) );
		}
		
		public double getAngleTo(double tx, double ty) {
			return Math.atan2( (ty - this.y), (tx - this.x) );
		}
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	
	public void touched(Entity e) {
		if (this.faction == e.faction) return;
		
		if (this.type == OBJ_PLAYER) {
			if (!this.hasEffect("FLINCHED")) {
				receiveDamage(e.damageDealt());
				if (e.type != OBJ_PROJECTILE && e.damageTakenMult > 0.0) {
					applyVelocity(e.damageDealt()/this.mass, getAngleTo(e), 100);
					e.applyVelocity(damageDealt()/e.mass, getAngleTo(e)+Math.PI, 100);
				}
			}
		} else if (this.type != OBJ_PROJECTILE) {
			if (!this.hasEffect("PACIFIED")) {
				dealDamageTo(e);
				if (e.type != OBJ_PROJECTILE && e.damageTakenMult > 0.0) {
					applyVelocity(e.damageDealt()/this.mass, getAngleTo(e), 100);
					e.applyVelocity(damageDealt()/e.mass, getAngleTo(e)+Math.PI, 100);
				}
			}
		} else if (this.type == OBJ_PROJECTILE) {
			dealDamageTo(e);
			if (e.damageTakenMult > 0.0) e.applyVelocity(damageDealt()/e.mass, getAngleTo(e)+Math.PI, 100);
		}
	}
	
	public void dead() {
		if (hasEffect("IMMORTAL")) return;
		dead = true;
		try {
			ai.stop();
		} catch (NullPointerException e) { }
		if (this.type == OBJ_PROJECTILE) garbage = true;
	}
	
	public double damageDealt() {
		try {
			return baseDamage*owner.damageDealtMult;
		} catch (NullPointerException e) {
			return baseDamage*damageDealtMult;
		}
	}
	
	public void dealDamageTo(Entity e) {
		if (hasEffect("PACIFIED")) return;
		
		try { 
			if (e == this.owner) return;
		} catch (NullPointerException exc) {}
		
		if (this.type == OBJ_NPC) addEffect("PACIFIED");
		if (this.type == OBJ_PROJECTILE && e.damageTakenMult > 0.0) this.receiveDamage(10);	/// Projectiles take 10 damage for each enemy they hit
		
		e.receiveDamage(damageDealt());
		
		try {
			if (owner.type == OBJ_PLAYER) owner.buildCharge(damageDealt());
		} catch (NullPointerException exc) { }
	}
	
	public void dealDamageTo(Wall w) {
		if (hasEffect("PACIFIED")) return;
		
		w.receiveDamage(damageDealt());
		// no charge built from hitting walls
		if (this.type == OBJ_PROJECTILE) this.receiveDamage(10);
	}
	
	public void receiveDamage(double dmg) {
		if (dead) return;
		if (hasEffect("FLINCHED")) return;
		dmg *= damageTakenMult;
		
		shield -= dmg;
		if (shield < 0.0) {
			hp += shield;
			shield = 0.0;
		}
		
		if (hp <= 0.0) hp = 0.0;
		else if (hp > hpmax) hp = hpmax;
		
		if (this.type == OBJ_PLAYER && dmg > 0.0) addEffect("FLINCHED");
		
		if (hp <= 0.0 && !hasEffect("IMMORTAL")) dead();
	}
	
	public void spendHP(double h) {
		hp -= h;
		if (hp < 0.0) dead();
	}
	
	public void spendMP(double m) {
		mp -= m;
		if (mp < 0.0) mp = 0.0;
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
		if (dead) return;
		if (this.shieldmax == 0.0) return;
		
		shield += shieldregen; /// 100x this per second
		if (shield > shieldmax) shield = shieldmax;
	}
	
	public void regenerateHP() {
		if (dead) return;
		if (this.type == OBJ_PLAYER) return;
		
		hp += hpregen; 
		if (hp > hpmax) hp = hpmax;
	}
	
	public void regenerateMP() {
		if (dead) return;
		
		mp += mpregen; 
		if (mp > mpmax) mp = mpmax;
	}
	
	public void buildCharge(double d) {
		charge += d/100.0;
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
	
	public void addEffect(String effectID) {
		effects.add(new Effect(effectID, this));
	}
	
	public void removeEffect(String effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id.equals(effectID)) {
				effects.get(i).removeMods();
				effects.remove(i);
				return;
			}
		}
	}
	
	public boolean hasEffect(String effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id.equals(effectID)) return true;
		} return false;
	}
	
	public void addAbility(String abilityID) {
		if (hasAbility(abilityID)) return;
		ai.abilities.add(new Ability(abilityID,this));
	}
	
	/// Version of useAbility for non-targeted abilities
	public void useAbility(String abilityID) {
		if (!hasAbility(abilityID) || dead) return;
		ai.useAbility(abilityID, this.x, this.y);
	}
	
	/// useAbility for targeted abilities
	public void useAbility(String abilityID, double tx, double ty) {
		if (!hasAbility(abilityID)) return;
		ai.useAbility(abilityID, tx, ty);
	}
	
	public boolean hasAbility(String abilityID) {
		for (int i = 0; i < ai.abilities.size(); i++) {
			if (ai.abilities.get(i).id.equals(abilityID)) return true;
		} return false;
	}
	
	public void spawnProjectile(String id, double angleFacing) {
		Entity e = new Entity(id, this.x, this.y, parent, this);
		e.faction = this.faction;
		int i = parent.projectiles.size();
		parent.projectiles.add(e);
		parent.projectiles.get(i).setDirection(angleFacing);
		parent.projectiles.get(i).moveTowards(angleFacing);
	}
	
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
	
		try {
			/// use an AffineTransform - changes occur in reverse order
			AffineTransform t = new AffineTransform();
			
			/// translate image to entity's position
			//t.translate((int)(x - img.getWidth()/2), (int)(y - img.getHeight()/2));
			t.translate(x, y);
			
			/// rotate
			t.rotate(angleFacing);
			
			/// translate so it rotates around the center of the image
			t.translate(-img.getWidth()/2, -img.getHeight()/2);
			
			/// draw this entity using the transformations
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(img,t,null);
		
		} catch (NullPointerException exc) {
			System.out.println("Error: Entity "+id+" is missing its image");
		}
	}
	
	public static final int BAR_MP = 0, BAR_HP = 1, BAR_SHIELD = 2, BAR_CHARGE = 3;
	
	/// generic drawBars method for drawing bars over enemies, other objects and walls
	public void drawBars(Graphics g) {
		drawBar(g, BAR_SHIELD);
		drawBar(g, BAR_HP);
		drawBar(g, BAR_MP);
		/// don't draw charge bar for mobs
	}
	
	public void drawBar(Graphics g, int type) {
		int len = (int)(radius < 60? 50 : radius < 200? 100 : 200);
		int ymod = 10 + type*5;
		drawBar(g, type, (x-len/2.0), (y - radius - ymod - (radius>60? 10 : 0)), len, false);
	}
	
	/// more specific drawBar method - use this instead of drawBar(Graphics,int) for player UI
	public void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean player) {
		if (this.type == OBJ_PROJECTILE) return;
		int height = (player? 10 : 5);
		switch(type) {
			case BAR_MP: 
				if ((mpmax == 0.0 || getMPPercent() == 100.0) && !player) return; 
				else {
					g.setColor(new Color(210,50,255,180));
					g.fillRect((int)TLX, (int)TLY, (int)(len*getMPPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_HP: 
				if ((hpmax == 0.0 || getHPPercent() == 100.0) && !player) return; 
				else {
					g.setColor(new Color(127,0,0,180)); 
					g.fillRect((int)TLX, (int)TLY, len, height);
					g.setColor(new Color(0,180,0,180));
					g.fillRect((int)TLX, (int)TLY, (int)(len*getHPPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_SHIELD: 
				if ((shieldmax == 0.0 || getShieldPercent() == 100.0) && !player) return; 
				else {
					g.setColor(new Color(120,120,255,180)); 
					g.fillRect((int)TLX, (int)TLY, (int)(len*getShieldPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_CHARGE: 
				if ((chargemax == 0.0) && !player) return; 
				else {
					g.setColor(new Color(0,0,0,180)); 
					g.fillRect((int)TLX, (int)TLY, (int)(len), height);
					g.setColor(new Color(255,200,0,180)); 
					g.fillRect((int)TLX, (int)TLY, (int)(len*charge/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
		}
	}
	
	public void drawBarSquares(Graphics g, double TLX, double TLY, int width, int height) {
		g.setColor(new Color(0,0,0,180));
		for (int i = 0; i < width/10; i++) {
			g.drawRect((int)TLX+10*i, (int)TLY, 10, height);
		}
	}
}