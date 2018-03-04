/// IWBattler
///   Subclass of IWObject
///
/// IWBattlers are the living entities in the game - the player, allies, or enemies.
///
/// Main differences from other subclasses:
///   Can use pretty much all the variables defined by IWObject
///   All IWBattlers have abilities, effects, AIs
///   Some methods are designed around effects or abilities and assume the IWBattler has them

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

class IWBattler extends IWObject {

	public IWBattler(IWBattler orig) {
		super(orig);
		ai = new AI(this);
      this.battler = true;
	}
	
	public IWBattler(String ID, double X, double Y, Game PARENT, IWBattler OWNER) {
		super(ID,X,Y,PARENT,OWNER);
		ai = new AI(this);
      this.battler = true;
		initStats();
	}
	
	public void initIWBattler(IWBattler OWNER, double X, double Y, double dirAngle) {
		setPosition(X,Y);
		setDirection(dirAngle);
		velocities = new ArrayList<Vector>();
		projectiles = new ArrayList<IWProjectile>();
		effects = new ArrayList<Effect>();
		//for (int i = 0; i < abilityNames.size(); i++) 
		//	ai.addAbility(abilityNames.get(i));
	}
	
	public void initStats() {
		if (id.equals("PLAYER")) player = true;
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
				} else if (tag.equals("faction")) {
					try {
						faction = (val.equals("PROJECTILE")? owner.faction : new Faction(Integer.parseInt(val)));
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
					// placeholder
				} else if (tag.equals("ability")) {
					abilityNames.add(val);
					addAbility(val);
				} else if (tag.equals("passive")) {
					// temp: placeholder for an Effect with duration -1 that will be applied on spawn
				} else if (tag.equals("ignoresknockback")) {
					ignoresKnockback = (val.equals("1")? true : false);
				}
				
			} file.close();
			
		} catch (IOException e) {
			System.out.println("Error: data\\entities\\"+id+".dat could not be found");
		}
	}
	
	public boolean isIWBattler() { return true; }
	public boolean isIWProjectile() { return false; }
	public boolean isIWWall() { return false; }
	public boolean isIWPickup() { return false; }
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Update AI, effect durations, ability cooldowns
		/// Uses tdelay timer in Game
		public void update() {
			if (isDead()) return;
			
			try {
				ai.act();
				
				/// update ability cooldowns
				for (int i = 0; i < ai.abilities.size(); i++)
					ai.abilities.get(i).update();
			} catch (NullPointerException e) { }
			
			/// regenerate passively
			regenerateShield();
			regenerateHP();
			regenerateMP();
			
			/// update effect durations, where applicable
			for (int i = 0; i < effects.size(); i++) {
				if (effects.get(i).isExpired()) clearEffects();
				else effects.get(i).update();
			}
		}
		
		public boolean isDead() {
			return hasEffect("DEAD");
		}
		
		/// Resets the cooldown of a specific ability if the mob has it
		public void refreshCooldown(String abilityID) {
			if (!hasAbility(abilityID)) return;
			
			for (int i = 0; i < ai.abilities.size(); i++) {
				if (ai.abilities.get(i).id.equals(abilityID))
					ai.abilities.get(i).refresh();
			}
		}
		
		/// Checks whether the ability is on cooldown
		/// Returns false if mob doesn't have the ability
		public boolean getIsAbilityReady(String abilityID) {
			if (!hasAbility(abilityID)) return false;
			
			return ai.getAbility(abilityID).ready();
		}
	
	//###################################################
	//## SPEED/Constants.VECTOR_VELOCITY METHODS #########################
	//###################################################
	
		/// Modify position based on current net speed
		/// Also check for collision with walls and other entities
		public void move() {
			if (isDead()) stop();
		
			double dy = getNetYVelocity()/main.timerdelay;
			double dx = getNetXVelocity()/main.timerdelay;
			double ny = y + dy;
			double nx = x + dx;
			int bounceX = 0;
			int bounceY = 0;
			
			/// if mob was going to hit a wall
				/// prevent it from passing through the wall
			for (int i = 0; i < main.walls.size(); i++) {
				IWWall w = main.walls.get(i);
				
				if (this.getCollision(w, nx, ny)) {
					if (nx > w.getLeftBound(this.battler) && x <= w.getLeftBound(this.battler)) bounceX = -1;
					else if (nx < w.getRightBound(this.battler) && x >= w.getRightBound(this.battler) ) bounceX = 1;
					
					if (ny > w.getTopBound(this.battler) && y <= w.getTopBound(this.battler)) bounceY = -1;
					else if (ny < w.getBottomBound(this.battler) && y >= w.getBottomBound(this.battler)) bounceY = 1;
				}
			}
			
			/// check collision with other entities
			for (int i = 0; i < main.mobs.size(); i++) {
				IWBattler e = main.mobs.get(i);
				
				if (getCollision(e)) 
					this.touched(e);
			}
			if (!this.player)
				if (getCollision(main.player))
					this.touched(main.player);
					
			/// check collision with items (player only)
			if (player)
				for (int i = 0; i < main.items.size(); i++) {
					IWPickup e = main.items.get(i);
					
					if (getCollision(e))
						this.touched(e);
				}
			
			/// update movement within bounds if no walls or enemies were hit
			if (bounceX == 0) this.x = nx;
			if (bounceY == 0) this.y = ny;
			
			/// if entity was going to go offscreen
				/// despawn if it is a projectile
				/// else keep it within the boundaries
			if (y < radius)
				y = radius;
			if (y > main.currentLevel.h - radius)
				y = main.currentLevel.h - radius;
			if (x < radius)
				x = radius;
			if (x > main.currentLevel.w - radius)
				x = main.currentLevel.w - radius;
		}
		
		public void moveTowards(double nx, double ny) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_MOBMOVEMENT)) velocities.remove(i);
					
			// get angle to target location
			double ntheta = Math.atan2( (ny - y), (nx - x) );
			
			if (!hasEffect("IMMOBILIZED"))
				velocities.add(new Vector(Constants.VECTOR_MOBMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		public void moveTowards(double ntheta) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_MOBMOVEMENT)) velocities.remove(i);
			
			if (!hasEffect("IMMOBILIZED"))
				velocities.add(new Vector(Constants.VECTOR_MOBMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		
		
		public double getNetXVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude*Math.cos(v.direction)*(v.id.equals(Constants.VECTOR_XBASESPEED) || v.id.equals(Constants.VECTOR_MOBMOVEMENT)? baseSpeedMult : 1.0);
			}
			return s;
		}
		
		public double getNetYVelocity() {
			double s = 0.0; 
			Vector v;
			for (int i = 0; i < velocities.size(); i++) {
				v = velocities.get(i);
				s += v.magnitude * (v.id.equals(Constants.VECTOR_YBASESPEED) || v.id.equals(Constants.VECTOR_MOBMOVEMENT)? baseSpeedMult : 1.0) * Math.sin(v.direction);
			}
			return s;
		}
		
		public void applyBaseXSpeed(int mult) {
			if (getHasVelocityID(Constants.VECTOR_XBASESPEED) || hasEffect("IMMOBILIZED")) return;
			velocities.add(new Vector(Constants.VECTOR_XBASESPEED,baseSpeed,0.0+(Math.PI*mult),-1,main));
		}
		
		public void removeBaseXSpeed() {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(Constants.VECTOR_XBASESPEED))
					velocities.remove(i);
			}
		}
		
		public void applyBaseYSpeed(int mult) {
			if (getHasVelocityID(Constants.VECTOR_YBASESPEED) || hasEffect("IMMOBILIZED")) return;
			velocities.add(new Vector(Constants.VECTOR_YBASESPEED,baseSpeed,(Math.PI/2.0)+(Math.PI*mult),-1,main));
		}
		
		public void removeBaseYSpeed() {
			for (int i = 0; i < velocities.size(); i++) {
				if (velocities.get(i).matchID(Constants.VECTOR_YBASESPEED))
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
         if (this.player) {
            removeBaseYSpeed();
            removeBaseXSpeed();
         } else {
            for (int i = 0; i < velocities.size(); i++) {
               if (velocities.get(i).matchID(Constants.VECTOR_MOBMOVEMENT))
                  velocities.remove(i);
            }
         }
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
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	public void touched(IWBattler e) {
		if (this.faction.friendlyTo(e.faction)) return;
		if (e.invulnerable || e.isDead()) return;
		
		dealDamageTo(e);
	}
	
	/// Do nothing here - this is handled by IWProjectile's touched(IWBattler) method
	public void touched(IWProjectile e) { }
	
	public void touched(IWPickup e) {
		if (player)
			e.pickedUpBy(this);
	}
	
	public void dead() {
		if (hasEffect("IMMORTAL")) return;
		
		for (int i = 0; i < effects.size(); i++) 
			effects.get(i).removeMods();
		
		effects = new ArrayList<Effect>();
		addEffect("DEAD");
		try {
			ai.stop();
		} catch (NullPointerException e) { }
		
		/// Chance to spawn an item depending on the type
		if (player) return;
		
		int roll = Random.randInt(0,100);
		if (roll > 97) dropIWPickup("ITEM_RECHARGE");
		else if (roll > 94) dropIWPickup("ITEM_POTION_HEALTH");
		else if (roll > 90) dropIWPickup("ITEM_POTION_MANA");
		
		if (id.equals("MOB_FLAMESPRITE")) {
		
		} else if (id.equals("MOB_BLAZINGIMP")) {
		
		} else if (id.equals("MOB_FLAMINGJUSTICAR")) {
			if (roll < 6) dropIWPickup("ITEM_1UP");
			else if (roll < 10) dropIWPickup("ITEM_BOOST_MANA");
		} else if (id.equals("MOB_FIREGOLEM")) {
			if (roll < 15) dropIWPickup("ITEM_1UP");
			else if (roll < 20) dropIWPickup("ITEM_BOOST_SHIELD");
			else if (roll < 25) dropIWPickup("ITEM_AMMO");
		} else if (id.equals("MOB_FLAMETURRET")) {
			if (roll < 3) dropIWPickup("ITEM_AMMO");
		} else if (id.equals("MOB_INFERNOTURRET")) {
			if (roll < 6) dropIWPickup("ITEM_AMMO");
		}
		
		if (id.startsWith("BOSS")) {
			dropIWPickup("ITEM_1UP");
		}
	}
	
	public double damageDealt() {
		try {
			return baseDamage*owner.damageDealtMult;
		} catch (NullPointerException e) {
			return baseDamage*damageDealtMult;
		}
	}
	
	public void dealDamageTo(IWBattler e) {
		
		try {
			if (owner.player) owner.buildCharge(damageDealt());
		} catch (NullPointerException exc) { }
		
		if (e.hasEffect("FLINCHED") || this.hasEffect("PACIFIED")) return;
		
		/// Knock back both mobs if HP damage would be dealt
      if (this.damageDealt() > e.shield) {
         applyKnockback(e.damageDealt()*main.knockbackMult/this.mass, getAngleTo(e)+Math.PI, e);
         e.applyKnockback(this.damageDealt()*main.knockbackMult/e.mass, getAngleTo(e), this);
      }
		
		/// Calculate damage 
		e.receiveDamage(this.damageDealt(),this);
		if (!onHitEffectID.equals("0"))
			e.addEffect(onHitEffectID);
		
		/// Pacify the mob so it can't touch attack again briefly
		if (!this.player) addEffect("PACIFIED");
	}
	
	public void dealDamageTo(IWWall w) {
		if (hasEffect("PACIFIED")) return;
		
		w.receiveDamage(damageDealt(),this);
		// no charge built from hitting walls
	}
	
	public void receiveDamage(double dmg, IWObject source) {
		if (isDead()) return;
		
		dmg *= damageTakenMult;
		
		if (dmg < 0.0) {
			/// If incoming damage is negative, heal instead
			recoverHP(-1.0*dmg);
		} else {
			if (invulnerable) return;
			
			/// If hit by a projectile while flinched and not using Reversal, don't do anything
			if (hasEffect("FLINCHED")) {
				if (source.isIWProjectile())
					if (!ignoresCollision) return;
					else { }
				else return;
			}
			
			/// Subtract damage from shield first
			shield -= dmg;
			if (shield < 0.0) {
				hp += shield;
				shield = 0.0;
			}
			/// Don't go outside the range of HP
			if (hp <= 0.0) hp = 0.0;
			else if (hp > hpmax) hp = hpmax;
			
			/// Flinch if this mob is the player
			if (this.player) addEffect("FLINCHED");
			
			if (hp <= 0.0) dead();
		}
	}
	
	public void spawnIWProjectile(String projID, double angleFacing) {
		int i = main.projectiles.size();
		main.projectiles.add(main.entities.getIWProjectileCloneByID(projID));
		main.projectiles.get(i).initIWProjectile(this, this.x, this.y, angleFacing);
	}
	
	public void dropIWPickup(String itemID) {
		main.spawnIWPickupAt(itemID, this.x, this.y);
	}
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
	
		try {
         double[] cb = main.camera.getCameraBounds();
         double nx = this.x - cb[0] - this.img.getWidth()/2.0;
         double ny = this.y - cb[2] - this.img.getHeight()/2.0;
			g.drawImage(img, (int)nx, (int)ny, null);
		} catch (NullPointerException exc) {
			System.out.println("Error: IWPickup "+id+" at ("+(int)x+","+(int)y+") is missing its image "+imgID);
		}
	}
   
	public void drawBars(Graphics g) {
      double[] cb = main.camera.getCameraBounds();
      double nx = this.x - cb[0];
      double ny = this.y - cb[2] - 15 - (this.radius>60? 10 : 0);
      int len = (int)(this.radius < 60? 50 : this.radius < 200? 100 : 200);
		drawBar(g, BAR_SHIELD, nx - len/2.0, ny - 10, len, false);
		drawBar(g, BAR_HP, nx - len/2.0, ny - 5, len, false);
		drawBar(g, BAR_MP, nx - len/2.0, ny + 0, len, false);
	}
	
	/// more specific drawBar method - use this instead of drawBar(Graphics,int) for player UI
	public void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean player) {
		int height = (player? 12 : 5);
		switch(type) {
			case BAR_MP: 
				if ((mpmax == 0.0 || getMPPercent() == 100.0) && !player) return; 
				else {
					g.setColor(Constants.COLOR_PURPLE);
					g.fillRect((int)TLX, (int)TLY, (int)(len*getMPPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_HP: 
				if ((hpmax == 0.0 || getHPPercent() == 100.0) && !player) return; 
				else {
					g.setColor(Constants.COLOR_RED); 
					g.fillRect((int)TLX, (int)TLY, len, height);
					g.setColor(Constants.COLOR_GREEN);
					g.fillRect((int)TLX, (int)TLY, (int)(len*getHPPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_SHIELD: 
				if ((shieldmax == 0.0 || getShieldPercent() == 100.0) && !player) return; 
				else {
					g.setColor(Constants.COLOR_CYAN); 
					g.fillRect((int)TLX, (int)TLY, (int)(len*getShieldPercent()/100.0), height);
					drawBarSquares(g,TLX,TLY,len,height);
					break;
				}
			case BAR_CHARGE: 
				if ((chargemax == 0.0) && !player) return; 
				else {
					g.setColor(Constants.COLOR_BLACK); 
					g.fillRect((int)TLX, (int)TLY, (int)(len), height);
					g.setColor(Constants.COLOR_YELLOW); 
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