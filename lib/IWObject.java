/// IWObject
///
///	General class for objects that can take actions in the game
///
/// Provides fields and methods for basic things such as movement, physics,
///   gameplay data, abilities, effects, as well as meta gameplay variables
///   that affect how the entity interacts with other entities
///
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

abstract class IWObject {

	/// meta variables
	Game main;
	IWBattler owner;
	AI ai;
	BufferedImage img;
	String imgID = "";
	ArrayList<Vector> velocities;
	ArrayList<IWProjectile> projectiles;
	ArrayList<Effect> effects;
	ArrayList<String> abilityNames = new ArrayList<String>();
	String onHitEffectID = "0";
	boolean garbage = false, player = false, dead = false;
	boolean ignoresCollision = false, ignoresKnockback = false, active = false;
	
	boolean reflectedOnce = false; /// used for projectile AI
	
	/// game variables
	String name = "MissingName", id;
	Faction faction = new Faction(Constants.FACTION_NEUTRAL);
	double tx = 0.0, ty = 0.0;
	double hp = 100.0, hpmax = 100.0, mp = 0.0, mpmax = 0.0, shield = 0.0, shieldmax = 0.0;
	double hpregen = 0.0, mpregen = 0.0, shieldregen = 0.0;
	double baseDamage = 0.0;
	double charge = 0, chargemax = 0;	

	/// physics variables
	double radius = 10.0, mass = 0.1, baseSpeed = 100.0, angleFacing = Math.PI/2.0; // radius in pixels, mass in kg
	double x = 0.0, y = 0.0;
	
	/// effects values/flags
	boolean invulnerable = false;
   boolean battler = false;
	int extraBullets = 0;
	double damageDealtMult = 1.0, damageTakenMult = 1.0, baseSpeedMult = 1.0;
	double hpRegenMult = 1.0, mpRegenMult = 1.0, shieldRegenMult = 1.0;
	
	/// Clones an existing entity
	/// Use this via GameEntities.getIWObjectClone to create new instances
	/// Note: arraylists, ai, owner, x, y, tx, ty not cloned
	public IWObject(IWObject orig) {
		this.main = orig.main;
		this.img = orig.img;
		this.imgID = orig.imgID;
		this.garbage = orig.garbage;
		this.player = orig.player;
		this.dead = orig.dead;
		this.ignoresCollision = orig.ignoresCollision;
		this.ignoresKnockback = orig.ignoresKnockback;
		this.onHitEffectID = orig.onHitEffectID;
		this.radius = orig.radius;
		this.mass = orig.mass;
		this.baseSpeed = orig.baseSpeed;
		this.angleFacing = orig.angleFacing;
		this.name = orig.name;
		this.id = orig.id;
		this.faction = orig.faction;
		this.hp = orig.hp;
		this.hpmax = orig.hpmax;
		this.mp = orig.mpmax;
		this.mpmax = orig.mpmax;
		this.shield = orig.shield;
		this.shieldmax = orig.shieldmax;
		this.hpregen = orig.hpregen;
		this.mpregen = orig.mpregen;
		this.shieldregen = orig.shieldregen;
		this.baseDamage = orig.baseDamage;
		this.charge = orig.charge;
		this.chargemax = orig.chargemax;
		this.invulnerable = orig.invulnerable;
		this.extraBullets = orig.extraBullets;
		this.damageDealtMult = orig.damageDealtMult;
		this.damageTakenMult = orig.damageTakenMult;
		this.baseSpeedMult = orig.baseSpeedMult;
		this.hpRegenMult = orig.hpRegenMult;
		this.mpRegenMult = orig.mpRegenMult;
		this.shieldRegenMult = orig.shieldRegenMult;
	}
	
	/// General constructor that sets most of the unique variables that are
	///   common to all subclasses
	/// These subclasses will call super(String,double,double,Game,IWBattler) in their constructors
	public IWObject(String ID, double X, double Y, Game PARENT, IWBattler OWNER) {
		this.main = PARENT;
		this.owner = OWNER;
		this.id = ID;
		this.x = X;
		this.y = Y;
	}
	
	/// Read stats from .dat files
	/// Each subclass will have slightly varying data types
	public abstract void initStats();
	
	/// Check which subclass the entity is
	public abstract boolean isIWBattler();
	public abstract boolean isIWProjectile();
	public abstract boolean isIWWall();
	public abstract boolean isIWPickup();
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Update AI, effect durations, ability cooldowns
		/// Uses 'main' timer in Game (tdelay)
		/// Not all subclasses will use this method
		public abstract void update();
		
		/// "Garbage" is mainly used for projectiles, which become
		///   garbage when they are too far offscreen. IWPickups will also
		///   become garbage when they have been picked up and used.
		///
		/// At this point, they are simply removed at the next call of
		///   the Game's  garbage collection methods.
		public boolean isGarbage() {
			return garbage;
		}
		
		/// "Dead" is used by IWBattlers, and serves the same purpose as Garbage
		///   for mobs other than the player. IWBattlers will only be Dead if they
		///   have 0 health remaining.
		public boolean isDead() {
			return dead;
		}
		
		/// Check if the entity has health remaining. Mainly used for IWBattlers.
		public boolean alive() {
			return !dead;
		}
		
		/// WARNING: ONLY USE ON MOBS
		/// PROBABLY ONLY USE ON THE PLAYER
		/// Fully removes the Dead state and gives a tiny amount of health
		public void revive() {
			if (!isDead()) return;
			
			dead = false;
			garbage = false;
			clearAllEffects();
			ai.stopped = false;
			recoverHP(0.1);
         removeEffect("DEAD");
			addEffect("SHIELDED");
		}
		
		/// Garbage collection: Remove expired effects from this entity
		public void clearEffects() {
			for (int i = 0; i < effects.size(); i++) {
				if (effects.get(i).isExpired()) {
					effects.remove(i);
					clearEffects();
					break;
				}
			}
		}
      
      public void clearAllEffects() {
         for (int i = 0; i < effects.size(); i++) {
            effects.get(i).clear();
         }
      }
	
	//###################################################
	//## SPEED/Constants.VECTOR_VELOCITY METHODS #########################
	//###################################################
	
		/// Modify position based on current net speed
		/// Also check for collision with walls and other entities
		public abstract void move();
      public abstract void stop();
		
		public abstract void moveTowards(double nx, double ny);
		public abstract void moveTowards(double ntheta);
		
		public void moveTowardsPlayer() {
			moveTowards(main.player.x, main.player.y);
		}
		
		/// Applies a velocity with indefinite duration to this entity
		public void applyVelocity(double magnitude, double theta) {
			velocities.add(new Vector(Constants.VECTOR_VELOCITY,magnitude,theta,-1,main));
		}
		
		/// Applies a velocity with finite duration to this entity
		/// Note: Negative durations will be treated as indefinite
		public void applyVelocity(double magnitude, double theta, int durMS) {
			velocities.add(new Vector(Constants.VECTOR_VELOCITY,magnitude,theta,durMS,main));
		}
		
		/// Teleport the entity to the given coordinates
		public void setPosition(double nx, double ny) {
			this.x = nx;
			this.y = ny;
		}
		
		/// Teleport the entity to the coordinates of the target entity's centre point
		public void setPosition(IWObject e) {
			this.x = e.x;
			this.y = e.y;
		}
		
		/// Checks if the entity has a velocity with the given ID
		/// Mainly used to prevent BaseSpeed type vectors from stacking
		public boolean getHasVelocityID(String ID) {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(ID)) return true;
			} return false;
		}
		
		/// Calculates component in given dimension of the resultant vector of all existing vectors
		public abstract double getNetXVelocity();
		public abstract double getNetYVelocity();
		
		/// Get the magnitude of the resultant vector of all existing vectors
		public double getVelocityMagnitude() {
			return Math.sqrt( Math.pow(getNetXVelocity(),2) + Math.pow(getNetYVelocity(),2) );
		}
		
		/// Get the angle of the resultant vector of all existing vectors
		public double getVelocityAngle() {
			return Math.atan2( getNetYVelocity(), getNetXVelocity() );
		}
		
		/// Applies a knockback vector, which lasts for 0.2 seconds
		/// Reference to source of knockback is used for the implementation of the Reversal spell
		public void applyKnockback(double magnitude, double angle, IWObject source) {
			if (invulnerable || ignoresKnockback) return;
			if (damageTakenMult <= 0.0) return;
			if (hasEffect("FLINCHED") && source.isIWProjectile() && ignoresCollision) return;
			applyVelocity(magnitude, angle, 100);
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
		public boolean getCollision(IWBattler obj) {
			double distance = getDistanceTo(obj);
			if (distance - this.radius - obj.radius <= 0.0) return true;
			return false;
		}
		
		public boolean getCollision(IWProjectile obj) {
			double distance = getDistanceTo(obj);
			if (distance - this.radius - obj.radius <= 0.0) return true;
			return false;
		}
		
		public boolean getCollision(IWPickup obj) {
			double distance = getDistanceTo(obj);
			if (distance - this.radius - obj.radius <= 0.0) return true;
			return false;
		}
		
		public boolean getCollision(IWWall w) {
			if (this.x + this.radius > w.getLeftBound(this.battler) && this.x - this.radius < w.getRightBound(this.battler) &&
				this.y + this.radius > w.getTopBound(this.battler) && this.y - this.radius < w.getBottomBound(this.battler)) 
				return true;
			return false;
		}
		
		public boolean getCollision(IWWall w, double nx, double ny) {
			if (nx + this.radius > w.getLeftBound(this.battler) && nx - this.radius < w.getRightBound(this.battler) &&
				ny + this.radius > w.getTopBound(this.battler) && ny - this.radius < w.getBottomBound(this.battler) ) 
				return true;
			return false;
		}
		
		public double getDistanceTo(IWObject e) {
			return Math.sqrt( Math.pow((this.x - e.x),2) + Math.pow((this.y - e.y),2) );
		}
		
		public double getAngleTo(IWObject e) {
			return Math.atan2( (e.y - this.y), (e.x - this.x) );
		}
		
		public double getAngleTo(double tx, double ty) {
			return Math.atan2( (ty - this.y), (tx - this.x) );
		}
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	
	public abstract void touched(IWBattler e);
	public abstract void touched(IWProjectile e);
	public abstract void touched(IWPickup e);
	
	/// Set the entity's state to dead
	/// Implementation varies with subclass
	public abstract void dead();
	
	public double damageDealt() {
		try {
			return baseDamage*owner.damageDealtMult;
		} catch (NullPointerException e) {
			return baseDamage*damageDealtMult;
		}
	}
	
	public abstract void dealDamageTo(IWBattler e);
	public abstract void dealDamageTo(IWWall w);
	
	public abstract void receiveDamage(double dmg, IWObject source);
	
	/// Spend HP, either due to a damage-over-time effect or to
	///   use a spell... both can be lethal
	public void spendHP(double h) {
		hp -= h;
		if (hp > hpmax)
			hp = hpmax;
		
		if (hp < 0.0) { 
			hp = 0.0;
			dead();
		}
	}
	
	public void spendMP(double m) {
		mp -= m;
		if (mp > mpmax) mp = mpmax;
		if (mp < 0.0) mp = 0.0;
	}
	
	public void spendCharge(double c) {
		charge -= c;
		if (charge > chargemax) charge = chargemax;
		if (charge < 0.0) charge = 0.0;
	}
	
	public void releaseCharge() {
		charge = 0.0;
	}
	
	public double getShieldPercent() {
		if (shieldmax == 0.0) return -1.0;
		if (shield >= shieldmax) return 100.0;
		return Math.round(10000*(this.shield/this.shieldmax))/100.0;
	}
	
	public double getHPPercent() {
		if (hpmax == 0.0) return -1.0;
		if (hp >= hpmax) return 100.0;
		return Math.round(10000*(this.hp/this.hpmax))/100.0;
	}
	
	public double getMPPercent() {
		if (mpmax == 0.0) return -1.0;
		if (mp >= mpmax) return 100.0;
		return Math.round(10000*(this.mp/this.mpmax))/100.0;
	}
	
	public double getChargePercent() {
		if (chargemax == 0.0) return -1.0;
		if (charge > chargemax) return 100.0;
		return Math.round(10000*(1.0*this.charge/this.chargemax))/100.0;
	}
	
	/// Passively regenerate shields (on Game's tdelay)
	public void regenerateShield() {
		if (this.shieldmax == 0.0) return;
		
		shield += shieldregen*shieldRegenMult; 
		if (shield > shieldmax) shield = shieldmax;
	}
	
	/// Passively regenerate HP (on Game's tdelay)
	public void regenerateHP() {
		hp += hpregen*hpRegenMult; 
		if (hp > hpmax) hp = hpmax;
	}
	
	/// Passively regenerate MP (on Game's tdelay)
	public void regenerateMP() {
		mp += mpregen*mpRegenMult; 
		if (mp > mpmax) mp = mpmax;
	}
	
	/// Gain charge based on damage dealt
	/// IWProjectiles fired by projectiles may or may not build charge
	/// Reflected projectiles will NOT build charge
	public void buildCharge(double dmg) {
		charge += dmg/200.0;
		if (charge > chargemax) charge = chargemax;
	}
	
	/// Instantly recover shields
	public void rechargeShield(double s) {
		shield += s;
		if (shield > shieldmax) shield = shieldmax;
	}
	
	/// Instantly recover health
	public void recoverHP(double h) {
		hp += h;
		if (hp > hpmax) hp = hpmax;
	}
	
	/// Instantly recover mana
	public void recoverMP(double m) {
		mp += m;
		if (mp > mpmax) mp = mpmax;
	}
   
   /// Increase maximum health
   public void modMaxHP(double h) {
      hpmax += h;
      if (h > 0.0) recoverHP(h);
   }
   
   /// Increase maximum mana
   public void modMaxMP(double m) {
      mpmax += m;
      if (m > 0.0) recoverMP(m);
   }
   
   /// Increase maximum shields
   public void modMaxShield(double s) {
      shieldmax += s;
      if (s > 0.0) rechargeShield(s);
   }
	
	//###################################################
	//## EFFECTS AND ABILITIES ##########################
	//###################################################
	
	/// Applies an effect with the given file ID unless the entity is dead
	public void addEffect(String effectID) {
		if (hasEffect("DEAD")) return;
	
		effects.add(new Effect(effectID, this));
	}
	
	/// Removes all instances of the effect from the entity
	public void removeEffect(String effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id.equals(effectID)) {
				effects.get(i).removeMods();
				effects.remove(i);
            removeEffect(effectID);
				return;
			}
		}
	}
	
	/// Checks if the entity has any effect with a matching file ID
	public boolean hasEffect(String effectID) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).id.equals(effectID)) return true;
		} return false;
	}
	
	/// Adds an ability to the entity if it has an AI
	/// WARNING: SHOULD NOT BE CALLED IF ENTITY DOES NOT HAVE AN AI
	public void addAbility(String abilityID) {
		if (hasAbility(abilityID)) return;
		ai.addAbility(abilityID);
	}
	
	/// Version of useAbility for non-targeted abilities
	/// WARNING: SHOULD NOT BE CALLED IF ENTITY DOES NOT HAVE AN AI
	public void useAbility(String abilityID) {
		if (!hasAbility(abilityID) || dead) return;
		ai.useAbility(abilityID, this.x, this.y);
	}
	
	/// useAbility for targeted abilities
	/// WARNING: SHOULD NOT BE CALLED IF ENTITY DOES NOT HAVE AN AI
	public void useAbility(String abilityID, double tx, double ty) {
		if (!hasAbility(abilityID)) return;
		ai.useAbility(abilityID, tx, ty);
	}
	
	/// Checks if the entity has an ability
	/// WARNING: SHOULD NOT BE CALLED IF ENTITY DOES NOT HAVE AN AI
	public boolean hasAbility(String abilityID) {
		return ai.hasAbility(abilityID);
	}
	
	public abstract void spawnIWProjectile(String id, double angleFacing);
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public abstract void draw(Graphics g);
	
	public static final int BAR_MP = 0, BAR_HP = 1, BAR_SHIELD = 2, BAR_CHARGE = 3;
	
	/// more specific drawBar method - use this instead of drawBar(Graphics,int) for player UI
	public abstract void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean player);
	
	public abstract void drawBarSquares(Graphics g, double TLX, double TLY, int width, int height);
	
}