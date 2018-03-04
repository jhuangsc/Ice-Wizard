/// IWProjectile
///   Subclass of IWObject
///
/// IWProjectiles are the moving objects launched by IWBattlers as attacks
/// Some IWProjectiles can even fire more IWProjectiles.
///
/// Main differences from other subclasses:
///   Can use most of the variables defined by IWObject
///   Has no effects but can have AI and abilities
///   Uses angleFacing as the direction of base movement
///   Can move offscreen and despawn
///   Always moving

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

class IWProjectile extends IWObject {

	boolean hasAI = true;
	boolean buildsCharge = true;
	
	public IWProjectile(IWProjectile orig) {
		super(orig);
		this.hasAI = orig.hasAI;
		this.buildsCharge = orig.buildsCharge;
		velocities = new ArrayList<Vector>();
		
		if (hasAI) {
			ai = new AI(this);
			//for (int i = 0; i < abilityNames.size(); i++) 
			//	ai.addAbility(abilityNames.get(i));
			effects = new ArrayList<Effect>();
		}
	}
	
	public IWProjectile(String ID, double X, double Y, Game PARENT, IWBattler OWNER) {
		super(ID,X,Y,PARENT,OWNER);
		velocities = new ArrayList<Vector>();
		
		initStats();
	}
	
	public void initIWProjectile(IWBattler OWNER, double X, double Y, double dirAngle) {
		this.owner = OWNER;
		this.faction = owner.faction;
		this.setPosition(X,Y);
		this.setDirection(dirAngle);
		this.moveTowards(dirAngle);
	}
	
	public void initStats() {
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
					imgID = val;
					img = main.images.get(val);
				} else if (tag.equals("ai")) {
					if (val.equals("1")) {
						hasAI = true;
						ai = new AI(this);
					}
				} else if (tag.equals("health")) {
					try {
						hpmax = Double.parseDouble(val);
						hp = hpmax;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HP");
					}
				} else if (tag.equals("hpregen")) {
					try {
						hpregen = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field HPREGEN");
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
						baseSpeed = Double.parseDouble(val)*main.speedMult;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating entity "+id+": Invalid input for field BASESPEED");
					}
					
				} else if (tag.equals("onhit")) {
					onHitEffectID = val;
				
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
						
				} else if (tag.equals("flag")) {
					if (val.equals("1")) {
						buildsCharge = false;
					}
				} else if (tag.equals("ignorescollision")) {
					ignoresCollision = (val.equals("1")? true : false);
				} else if (tag.equals("ability")) {
					try {
						abilityNames.add(val);
						addAbility(val);
					} catch (NullPointerException exc) { }
				}
			} file.close();
			
		} catch (IOException e) {
			System.out.println("Error: data\\entities\\"+id+".dat could not be found");
		}
	}
	
	public boolean isIWBattler() { return false; }
	public boolean isIWProjectile() { return true; }
	public boolean isIWWall() { return false; }
	public boolean isIWPickup() { return false; }
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Only update whether the projectile has hit each mob
		/// Update AI and abilities only if it has them
		public void update() {
			if (garbage) return;
			
			try {
				ai.act();
				/// update ability cooldowns
				for (int i = 0; i < ai.abilities.size(); i++)
					ai.abilities.get(i).update();
			} catch (NullPointerException e) { }
		}
		
		public boolean isGarbage() {
			return garbage;
		}
	
	//###################################################
	//## SPEED/Constants.VECTOR_VELOCITY METHODS #########################
	//###################################################
	
		/// Modify position based on current net speed
		/// Also check for collision with walls and other entities
		public void move() {
			if (garbage) return;
			
			double dy = getNetYVelocity()/main.timerdelay;
			double dx = getNetXVelocity()/main.timerdelay;
			
			/// if projectile was going to hit a wall
				/// damage the wall until the projectile is dead
				/// unless it ignores collision, then just hit the wall normally
			for (int i = 0; i < main.walls.size(); i++) 
				if (this.getCollision(main.walls.get(i))) 
					if (ignoresCollision)
						dealDamageTo(main.walls.get(i));
					else
						while (!this.isGarbage() && this.faction.hostileTo(main.walls.get(i).faction))
							dealDamageTo(main.walls.get(i));
			
			/// check collision with other entities
			for (int i = 0; i < main.mobs.size(); i++) {
				IWBattler e = main.mobs.get(i);
				
				if (getCollision(e))
					this.touched(e);
			}
			if (getCollision(main.player))
				touched(main.player);
			
			/// update movement within bounds
			this.x += dx;
			this.y += dy;
			
			/// if projectile was going to go offscreen
				/// despawn it
			if (y < 0) dead();
			if (y > main.currentLevel.h) dead();
			if (x < 0) dead();
			if (x > main.currentLevel.w) dead();
		}
      
      public void stop() { }
		
		public void moveTowards(double nx, double ny) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
					
			/// get angle to target location
			double ntheta = Math.atan2( (ny - y), (nx - x) );
			setDirection(ntheta);
			velocities.add(new Vector(Constants.VECTOR_OBJMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		public void moveTowards(double ntheta) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
			
			setDirection(ntheta);
			velocities.add(new Vector(Constants.VECTOR_OBJMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		
		
		
		public double getNetXVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude*Math.cos(v.direction);
			}
			return s;
		}
		
		public double getNetYVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude*Math.sin(v.direction);
			}
			return s;
		}
		
		public void applyKnockback(double magnitude, double angle, IWObject source) {
			super.applyKnockback(magnitude,angle,source);
			reflectedOnce = true;
		}
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	public void touched(IWBattler e) {
		if (this.faction.friendlyTo(e.faction) || garbage == true) return;
		if (e.invulnerable || e.isDead()) return;
		
		dealDamageTo(e);
	}
	
	public void touched(IWProjectile e) { }
	public void touched(IWPickup e) { }
	
	public void dead() {
		garbage = true;
	}
	
	public double damageDealt() {
		try {
			return baseDamage*owner.damageDealtMult*(reflectedOnce? 0.25 : 1.0);
		} catch (NullPointerException e) {
			return baseDamage*damageDealtMult*(reflectedOnce? 0.25 : 1.0);
		}
	}
	
	public void dealDamageTo(IWBattler e) {
		if (e.faction == this.faction) return;
		
		try {
			if (buildsCharge) owner.buildCharge(damageDealt());
		} catch (NullPointerException exc) { }
		
		/// Only apply these effects if the projectile did damage!
		/// Apply these effects before calculating damage
		/// IWProjectiles don't knock back a flinched target
		if (!e.hasEffect("FLINCHED") && e.damageTakenMult > 0.0) {
		
			if (!onHitEffectID.equals("0"))
				e.addEffect(onHitEffectID);
				
			/// IWProjectiles take 10 damage for each enemy they hit
			this.receiveDamage(10.0,this);	
			
			/// IWProjectiles apply knockback if they did damage
			e.applyKnockback(this.damageDealt()*main.knockbackMult/e.mass, this.getVelocityAngle(), this);
		}
		
		e.receiveDamage(damageDealt(),this);
	}
	
	public void dealDamageTo(IWWall w) {
      w.receiveDamage(damageDealt(),this);
      // no charge built from hitting walls
      this.receiveDamage(10.0,this);
	}
	
	public void receiveDamage(double dmg, IWObject source) {
		if (dead || invulnerable) return;
		
		hp -= dmg;
		
		if (hp <= 0.0) hp = 0.0;
		else if (hp > hpmax) hp = hpmax;
		
		if (hp <= 0.0) dead();
	}
	
	public void spawnIWProjectile(String projID, double dirAngle) {
		int i = main.projectiles.size();
		main.projectiles.add(main.entities.getIWProjectileCloneByID(projID));
		main.projectiles.get(i).initIWProjectile(this.owner, this.x, this.y, dirAngle);
	}
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
	
		try {
         double[] cb = main.camera.getCameraBounds();
         double nx = this.x - cb[0];
         double ny = this.y - cb[2];
         
			/// use an AffineTransform - changes occur in reverse order
			AffineTransform t = new AffineTransform();
			t.translate(nx, ny);		/// translate image to entity's position on the screen
			t.rotate(angleFacing);	/// rotate
			t.translate(-img.getWidth()/2, -img.getHeight()/2);	/// translate so it rotates around the center of the image
			
			/// draw this entity using the transformations
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(img,t,null);
		
		} catch (NullPointerException exc) {
			System.out.println("Error: IWProjectile "+id+" at ("+(int)x+","+(int)y+") is missing its image "+imgID);
		}
	}
	
	/// Never draw HP,MP,shield,charge bars for IWProjectiles
	public void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean player) { }
	public void drawBarSquares(Graphics g, double TLX, double TLY, int width, int height) { }
}