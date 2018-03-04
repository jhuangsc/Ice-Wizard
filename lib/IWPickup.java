/// IWPickup
///   Subclass of IWObject
///
/// Main differences:
///   IWPickups cannot interact with other entities except applying a meta effect on touch
///   IWPickups generally despawn when they are touched by the appropriate entity
///   IWPickups can move, but this is not currently used
///   IWPickups have no AI or effects

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*; 
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.*;

class IWPickup extends IWObject {

	public IWPickup(IWPickup orig) {
		super(orig);
	}
	
	public IWPickup(String ID, double X, double Y, Game PARENT) {
		super(ID,X,Y,PARENT,null);
		initStats();
	}
	
	public void initIWPickup(double X, double Y) {
		this.setPosition(X,Y);
		this.invulnerable = true;
		this.velocities = new ArrayList<Vector>();
      
      /// if item was spawned offscreen
         /// move it onscreen
      if (y < radius)
         y = radius;
      if (y > main.currentLevel.h - radius)
         y = main.currentLevel.h - radius;
      if (x < radius) 
         x = radius;
      if (x > main.currentLevel.w - radius)
         x = main.currentLevel.w - radius;
	}
	
	public void initStats() {
	
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("data\\pickups\\"+id+".dat"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) throw new IOException();
				String tag = ln.substring(0,ln.indexOf("="));
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					name = val;
				} else if (tag.equals("image")) {
					imgID = val;
					img = GameImages.get(imgID);
				} else if (tag.equals("basespeed")) {
					try {
						baseSpeed = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating item "+id+": Invalid input for field BASESPEED");
					}
						
				} else if (tag.equals("radius")) {
					try {
						radius = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating item "+id+": Invalid input for field RADIUS");
					}
						
				} else if (tag.equals("mass")) {
					try {
						mass = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating item "+id+": Invalid input for field MASS");
					}
						
				} else if (tag.equals("flags")) {
					// placeholder
				} 
				
			} file.close();
			
		} catch (IOException e) {
			System.out.println("Error: data\\items\\"+id+".dat could not be found");
		}
	}
	
	public boolean isIWBattler() { return false; }
	public boolean isIWProjectile() { return false; }
	public boolean isIWWall() { return false; }
	public boolean isIWPickup() { return true; }
	
	//###################################################
	//## META ###########################################
	//###################################################
	
		/// Nothing to update here
		public void update() { }
		
		public boolean isGarbage() {
			return garbage;
		}
	
	//###################################################
	//## SPEED/VELOCITY METHODS #########################
	//###################################################
   
		public void move() { }
      public void stop() { }
		
		public void moveTowards(double nx, double ny) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
					
			// get angle to target location
			double ntheta = Math.atan2( (ny - y), (nx - x) );
			
			velocities.add(new Vector(Constants.VECTOR_OBJMOVEMENT,baseSpeed,ntheta,-1,main));
		} 
		
		public void moveTowards(double ntheta) {
			for (int i = 0; i < velocities.size(); i++)
				if (velocities.get(i).matchID(Constants.VECTOR_OBJMOVEMENT)) velocities.remove(i);
			
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
	
	//###################################################
	//## GAMEPLAY METHODS ###############################
	//###################################################
	
	public void pickedUpBy(IWBattler e) {
		if (garbage) return;
		garbage = true;
	
      /// Always pick up lives
		if (id.equals("ITEM_1UP")) {
		
			e.main.lives++;
		
      /// Always pick up ammo
		} else if (id.equals("ITEM_AMMO")) {
			
			e.addEffect("WELLSUPPLIED");
		
      /// Pick up mana potion if not at full mana or potion is on cooldown
		} else if (id.equals("ITEM_POTION_MANA")) {
		
			if (e.getMPPercent() < 100.0) {
				e.recoverMP(e.mpmax);
				e.addEffect("ENERGIZED");
			} else if (!e.getIsAbilityReady("POTION_MANA")) {
				e.refreshCooldown("POTION_MANA");
			} else {
            garbage = false;
         }
			
      /// Pick up health potion if not at full health or potion is on cooldown
		} else if (id.equals("ITEM_POTION_HEALTH")) {
		
			if (e.getHPPercent() < 100.0) {
				e.recoverHP(e.hpmax);
			} else if (!e.getIsAbilityReady("POTION_HEAL")) {
				e.refreshCooldown("POTION_HEAL");
			} else {
            garbage = false;
         }
		
      /// Pick up recharge if not at full shields
		} else if (id.equals("ITEM_RECHARGE")) {
		
			if (e.getShieldPercent() < 100.0) {
            e.rechargeShield(e.shieldmax);
         } else {
            garbage = false;
         }
      
      /// Always pick up mana boosters
		} else if (id.equals("ITEM_BOOST_MANA")) {
		
			e.modMaxMP(30.0);
			e.recoverMP(e.mpmax);
		
      /// Always pick up shield boosters
		} else if (id.equals("ITEM_BOOST_SHIELD")) {
		
			e.modMaxShield(20.0);
			e.rechargeShield(e.shieldmax);
		
		} 
	}
	
	public void touched(IWBattler e) { }
	public void touched(IWProjectile e) { }
	public void touched(IWPickup e) { }
	public void dead() { }
	public void dealDamageTo(IWBattler e) { }
	public void dealDamageTo(IWWall w) { }
	public void receiveDamage(double dmg, IWObject source) { }
	public void spawnIWProjectile(String id, double angleFacing) { }
	
	//###################################################
	//## DRAWING METHODS ################################
	//###################################################
	
	public void draw(Graphics g) {
	
		try {
         double[] cb = main.camera.getCameraBounds();
         double nx = (this.x - cb[0] - this.img.getWidth()/2.0);
         double ny = (this.y - cb[2] - this.img.getHeight()/2.0);
			g.drawImage(img, (int)nx, (int)ny, null);
		} catch (NullPointerException exc) {
			System.out.println("Error: IWPickup "+id+" at ("+(int)x+","+(int)y+") is missing its image "+imgID);
		}
	}
	
	public void drawBar(Graphics g, int type, double TLX, double TLY, int len, boolean player) { }
	public void drawBarSquares(Graphics g, double TLX, double TLY, int width, int height) { }
}