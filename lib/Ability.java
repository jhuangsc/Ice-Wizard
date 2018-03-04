/// Ability
///
/// Represents an action made by an entity that does something other than move
/// 
/// Currently implemented: fire projectile(s), cause point-blank AOE damage/knockback,
///   apply effect to self

import java.awt.*;
import java.awt.image.*;
import java.io.*;

class Ability {

	String id;
	int type = 0;
	int cooldown = 0, cd = 0;
	boolean cooledDown = true, toggled = true;
	
	double[] angles;
	int spread = 0, projectiles = 0;
	double radius = 0.0, knockback = 0.0, damage = 0.0;
	
	int chargemin = 0, chargecost = 0;
	double chargebonus = 0.0, chargemultmod = 0.0;
	double mpcost = 0.0, hpcost = 0.0;
	
	String name = "MissingName", description = "";
	Game main;
	IWObject owner;
	BufferedImage icon;
	Tooltip tooltip;
	VisualEffect vfx; int vfxdur = 0;
	String effectSelfID = "", effectEnemiesID = "", effectLocationID = "", vfxID = "", projectileID = "";
	boolean hidden = false, alwaysUsable = false, allowsExtraBullets = false;
	
	/// 'Default' constructor - use only in GameAbilities class
	public Ability(String ID, Game p) {
		this.id = ID;
		this.main = p;
		initData();
	}
	
	/// Use this constructor via GameAbilities.getCloneByID or getClone to create instances of the ability
	public Ability(Ability orig, IWObject OWNER) {
		this.owner = OWNER;
		this.id = orig.id;
		this.main = orig.main;
		this.name = orig.name;
		this.description = orig.description;
		this.type = orig.type;
		this.cd = orig.cd;
		this.toggled = orig.toggled;
		this.spread = orig.spread;
		this.projectiles = orig.projectiles;
		this.radius = orig.radius;
		this.knockback = orig.knockback;
		this.damage = orig.damage;
		this.chargemin = orig.chargemin;
		this.chargecost = orig.chargecost;
		this.chargebonus = orig.chargebonus;
		this.chargemultmod = orig.chargemultmod;
		this.mpcost = orig.mpcost;
		this.hpcost = orig.hpcost;
		this.icon = orig.icon;
		this.vfxdur = orig.vfxdur;
		this.effectSelfID = orig.effectSelfID;
		this.effectEnemiesID = orig.effectEnemiesID;
		this.effectLocationID = orig.effectLocationID;
		this.vfxID = orig.vfxID;
		this.projectileID = orig.projectileID;
		this.hidden = orig.hidden;
		this.alwaysUsable = orig.alwaysUsable;
		this.allowsExtraBullets = orig.allowsExtraBullets;
		tooltip = new Tooltip(this);
	}
	
	public void initData() {
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("data\\abilities\\"+id+".dat"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) throw new IOException();
				String tag = ln.substring(0,ln.indexOf("="));
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					name = val;
				} else if (tag.equals("cooldown")) {
					try {
						cd = (int)(Integer.parseInt(val));
						if (cd > 0) toggled = false;
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field COOLDOWN");
					}
				} else if (tag.equals("icon")) {
					icon = GameImages.get(val);
				} else if (tag.equals("alwaysusable")) {
					alwaysUsable = (val.equals("1")? true : false);
				} else if (tag.equals("mpcost")) {
					try {
						mpcost = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field MPCOST");
					}
				} else if (tag.equals("hpcost")) {
					try {
						hpcost = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field HPCOST");
					}
				} else if (tag.equals("chargemin")) {
					try {
						chargemin = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field CHARGEMIN");
					}
				} else if (tag.equals("chargecost")) {
					try {
						chargecost = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field CHARGECOST");
					}
				} else if (tag.equals("chargebonus")) {
					try {
						chargebonus = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field CHARGEBONUS");
					}
				} else if (tag.equals("chargemultmod")) {
					try {
						chargemultmod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field CHARGEMULTMOD");
					}
				} else if (tag.equals("type")) {
					try {
						type = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field TYPE");
					}
				} else if (tag.equals("spread")) {
					try {
						spread = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field SPREAD");
					}
				} else if (tag.equals("radius")) {
					try {
						radius = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field RADIUS");
					}
				} else if (tag.equals("knockback")) {
					try {
						knockback = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field KNOCKBACK");
					}
				} else if (tag.equals("damage")) {
					try {
						damage = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field DAMAGE");
					}
				} else if (tag.equals("effectappliedself")) {
					effectSelfID = val;
				} else if (tag.equals("effectappliedenemies")) {
					effectEnemiesID = val;
				} else if (tag.equals("effectappliedlocation")) {
					effectLocationID = val;
				} else if (tag.equals("vfx")) {
					vfxID = val;
				} else if (tag.equals("vfxdur")) {
					try {
						vfxdur = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field VFXDUR");
					}
				} else if (tag.equals("projectile")) {
					projectileID = val;
				} else if (tag.equals("projectiles")) {
					try {
						projectiles = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating ability "+this.id+": Invalid input for field PROJECTILES");
					}
				} else if (tag.equals("allowsextrabullets")) {
					allowsExtraBullets = (val.equals("1")? true : false);
				} else if (tag.equals("hidden")) {
					hidden = (val.equals("1")? true : false);
				} else if (tag.equals("description")) {
					description = val;
				}
				
			}
		} catch (IOException e) {
			System.out.println("Error: data\\abilities\\"+this.id+".dat could not be found");
		}
	}
	
	public boolean ready() {
		return cooledDown;
	}
	
	public void use(double ox, double oy, double tx, double ty) {
   
		/// don't do anything if ability is on cooldown
		if (!ready()) return;
		
		/// don't do anything if user doesn't have enough HP/MP
		if (owner.mp < this.mpcost || owner.hp < this.hpcost || owner.charge < this.chargemin) return;
		
		/// don't do anything if user is stunned and the ability can't be used while stunned
		if (owner.hasEffect("STUNNED") && !alwaysUsable) return;
		
		/// if it has an effectAppliedSelf...
		if (!effectSelfID.equals("0")) {
			/// If it's a toggled effect and already active, remove it
			/// Otherwise add the effect
			if (toggled && owner.hasEffect(effectSelfID)) {
				owner.removeEffect(effectSelfID);
				return;
			} else owner.addEffect(effectSelfID);
		}
		
		/// Apply AOE effects if applicable
		if (radius > 0.0) {
		
			/// For enemies, apply damage, effect and knockback
			for (int i = 0; i < main.mobs.size(); i++) {
				IWBattler e = main.mobs.get(i);
					
				if (owner.getDistanceTo(e) <= radius + e.radius) {
					if (owner.faction.hostileTo(e.faction) && !e.invulnerable) {
						e.receiveDamage(damage*owner.damageDealtMult,this.owner);
						e.applyKnockback(knockback*main.knockbackMult/e.mass, owner.getAngleTo(e), this.owner);
						if (!effectEnemiesID.equals("0")) e.addEffect(effectEnemiesID);
					}
				}
			}
			
			IWBattler p = main.player;
			if (owner.getDistanceTo(p) <= radius + p.radius) {
				if (owner.faction.hostileTo(p.faction) && !p.invulnerable) {
					p.receiveDamage(damage*owner.damageDealtMult,this.owner);
					p.applyKnockback(knockback*main.knockbackMult/p.mass, owner.getAngleTo(p), this.owner);
					if (!effectEnemiesID.equals("0")) p.addEffect(effectEnemiesID);
				}
			}
			
			/// For enemy projectiles, apply (less) knockback, gain ownership of projectile, set new direction
			for (int i = 0; i < main.projectiles.size(); i++) {
				IWProjectile e = main.projectiles.get(i);
				if (owner.getDistanceTo(e) <= radius + e.radius && (owner.faction.hostileTo(e.faction) || owner != e.owner)) {
					e.applyKnockback(knockback/e.mass, owner.getAngleTo(e), this.owner);
					e.faction = owner.faction;
					e.moveTowards(owner.getAngleTo(e));
				}
			}
		}
		
		/// Fire projectile if applicable
		if (type == 2) fireIWProjectiles(projectileID, owner.tx, owner.ty);
		
		/// Create VFX either attached to owner or location, if ability has one
		try {
			if (!vfxID.equals("0")) vfx = new VisualEffect(vfxID,main,owner,vfxdur);
		} catch (NullPointerException exc) { }
		
		/// Start cooldown and spend HP/MP/charge
		cooldown = cd;
		if (cd > 0) cooledDown = false;
		owner.spendMP(this.mpcost);
		owner.spendHP(this.hpcost);
		owner.spendCharge(this.chargecost);
	}
	
	/// Resets cooldown
	public void refresh() {
		cooldown = 0;
		cooledDown = true;
	}
	
	/// Removes the current VFX
	public void clearVFX() {
		vfx = null;
	}
	
	/// Spawn projectiles moving towards a target location
	/// Spawn extra projectiles if owner has an extraBullets bonus
	public void fireIWProjectiles(String projectileID, double tx, double ty) {
		double baseAngle;
		if (owner.id.equals("PLAYER")) baseAngle = main.getAngleToMouse();
		else baseAngle = owner.getAngleTo(tx,ty);
		double[] angles = getArrayOfAngles(owner.extraBullets, baseAngle);
		for (int i = 0; i < angles.length; i++) 
			owner.spawnIWProjectile(projectileID, angles[i]);
	}
	
	/// Based on default number of projectiles, the owner's extraBullets bonus, and the
	/// spread of projectiles, returns an array of all the angles to fire projectiles at
	public double[] getArrayOfAngles(int extraBullets, double baseAngle) {
		double[] angles = new double[projectiles + (allowsExtraBullets? extraBullets : 0)];
		double theta;
		theta = baseAngle - Math.toRadians(spread)*(angles.length/2);
		for (int i = 0; i < angles.length; i++)
			angles[i] = theta + Math.toRadians(spread)*i;
		return angles;
	}
	
	/// Update cooldown, tooltip text, vfx duration
	public void update() {
		cooldown -= main.timerdelay;
		if (cooldown <= 0) { 
			cooledDown = true; 
			cooldown = 0; 
		}
		tooltip.update();
		
		try {
			vfx.update();
			if (vfx.isExpired()) vfx = null;
		} catch (NullPointerException exc) { }
	}
	
	/// Creates a new copy of this ability with a new owner
	public Ability getClone(IWObject OWNER) {
		return new Ability(this,OWNER);
	}
	
	public void draw(Graphics g) {
		try {
			if (!vfx.isExpired()) vfx.draw(g);
		} catch (NullPointerException e) { }
	}
	
	public void drawIcon(Graphics g, int TLX, int TLY) {
	
		/// draw icon
		g.drawImage(icon, TLX, TLY, null);
		
		/// draw cooldown indicator if applicable - don't bother if cooldown is very short
		if (cd > 200) {
			int pxCooledDown = (int)Math.round(20.0 * (1.0*cooldown/cd));
			g.setColor(new Color(40,40,40,160));
			g.fillRect(TLX, TLY + (20 - pxCooledDown), 20, pxCooledDown);
			
		/// grey out icon if not enough to use it
		} else if (owner.hp < hpcost || owner.mp < mpcost || owner.charge < chargecost || (owner.hasEffect("STUNNED") && !alwaysUsable)) {
			g.setColor(new Color(40,40,40,160));
			g.fillRect(TLX, TLY, 20, 20);
		}
		/// draw border
		g.setColor(new Color(255,255,255,255));
		g.drawRect(TLX, TLY, 20, 20);
	}
	
	public void drawTooltip(Graphics g, int BLX, int BLY) {
		tooltip.draw(g,BLX,BLY);
	}
}