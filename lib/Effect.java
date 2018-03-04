/// Effect
///
/// Purpose: Allow implementation of buffs and debuffs in the game
///
/// Note: Unlike Abilities and Entities, Effects are currently created from their
///   .dat files each time they are applied. This will be changed in a future version
///   of the game, but currently has a very low priority.

import java.awt.*;
import java.awt.image.*;
import java.io.*;

class Effect {

	Game main;
	IWObject owner;
	BufferedImage icon;
	Tooltip tooltip;
	VisualEffect vfx;
	Effect extra;
	
	String name = "", id = "", extraEffectID = "", description = "";
	
	double damageDealtMultMod = 0.0;
	double damageTakenMultMod = 0.0;
	double baseSpeedMultMod = 0.0;
	double mpCostPerTick = 0.0;
	double damagePerTick = 0.0;
	double hpRegenMultMod = 0.0;
	double mpRegenMultMod = 0.0;
	double shieldRegenMultMod = 0.0;
	int extraBulletsMod = 0;
	int duration = 0, durationMax = 0;
	boolean expired = false;
	boolean appliesIgnoreCollision = false;
	boolean appliesInvulnerability = false;
	boolean appliesActive = false;
	
	/// Effect constructor
	/// Immediately applies modifiers to its owner entity when created
	/// These will be removed when the effect expires
	public Effect(String ID, IWObject e) {
		this.main = e.main;
		this.owner = e;
		this.id = ID;
		
		initParams();
		if (!extraEffectID.equals("")) applyExtraEffect();
		
		owner.damageDealtMult += damageDealtMultMod;
		owner.baseSpeedMult += baseSpeedMultMod;
		owner.damageTakenMult += damageTakenMultMod;
		owner.hpRegenMult += hpRegenMultMod;
		owner.mpRegenMult += mpRegenMultMod;
		owner.shieldRegenMult += shieldRegenMultMod;
		owner.extraBullets += extraBulletsMod;
		if (appliesIgnoreCollision) 
			owner.ignoresCollision = true;
		if (appliesInvulnerability) 
			owner.invulnerable = true;
		if (appliesActive)
			owner.active = true;
		tooltip = new Tooltip(this);
	}
	
	/// Read data from a data\effects\*.dat file
	/// Use default values of 0 if fields are missing or invalid
	public void initParams() {
		BufferedReader file;
		try {
			file = new BufferedReader(new FileReader("data\\effects\\"+this.id+".dat"));
			while (file.ready()) {
				String ln = file.readLine();
				if (ln.indexOf("=") < 0) throw new IOException();
				String tag = ln.substring(0,ln.indexOf("="));
				String val = ln.substring(ln.indexOf("=")+1);
				
				if (tag.equals("name")) {
					name = val;
				
				} else if (tag.equals("icon")) {
					icon = GameImages.get(val);
					
				} else if (tag.equals("damagedealtmultmod")) {
					try {
						damageDealtMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field DAMAGEDEALTMULTMOD");
					}
					
				} else if (tag.equals("damagetakenmultmod")) {
					try {
						damageTakenMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field DAMAGETAKENMULTMOD");
					}
					
				} else if (tag.equals("basespeedmultmod")) {
					try {
						baseSpeedMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field BASESPEEDMULTMOD");
					}
					
				} else if (tag.equals("mpcostpertick")) {
					try {
						mpCostPerTick = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field MPCOSTPERTICK");
					}
					
				} else if (tag.equals("damagepertick")) {
					try {
						damagePerTick = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field DAMAGEPERTICK");
					}
					
				} else if (tag.equals("hpregenmultmod")) {
					try {
						hpRegenMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field HPREGENMULTMOD");
					}
					
				} else if (tag.equals("mpregenmultmod")) {
					try {
						mpRegenMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field MPREGENMULTMOD");
					}
					
				} else if (tag.equals("shieldregenmultmod")) {
					try {
						shieldRegenMultMod = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field SHIELDREGENMULTMOD");
					}
					
				} else if (tag.equals("extrabullets")) {
					try {
						extraBulletsMod = Integer.parseInt(val);
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field EXTRABULLETSMOD");
					}
					
				} else if (tag.equals("duration")) {
					try {
						setDuration((int)(Integer.parseInt(val)));
					} catch (NumberFormatException e) {
						System.out.println("Error while creating effect "+this.id+": Invalid input for field DURATION");
					}
				} else if (tag.equals("vfx")) {
					if (!val.equals("0")) vfx = new VisualEffect(val,main,owner,this.durationMax);
				} else if (tag.equals("extraeffect")) {
					if (!val.equals("0")) extraEffectID = val;
				} else if (tag.equals("flag")) {
					if (val.equals("1")) {
						appliesIgnoreCollision = true;
					} else if (val.equals("2")) {
						appliesInvulnerability = true;
					} else if (val.equals("3")) {
						appliesActive = true;
					}
				} else if (tag.equals("description")) {
					description = val;
				}
			} file.close();
		} catch (IOException e) {
			System.out.println("Error: data\\effects\\"+this.id+".dat could not be found");
		}
	}
	
	public void setDuration(int durMS) {
		if (durMS == -1) {
			/// do nothing
		} else {
			durationMax = durMS;
			duration = durationMax;
		}
	}
	
	/// Resets the effect's duration
	public void refresh() {
		duration = durationMax;
		try {
			extra.refresh();
		} catch (NullPointerException exc) { }
		try {
			vfx.refresh();
		} catch (NullPointerException exc) { }
	}
	
	/// Check if the effect's duration has run out
	public boolean isExpired() {
		return expired;
	}
	
	/// Reverse the changes to the target applied by this effect
	public void removeMods() {
		expired = true;
		owner.damageDealtMult -= damageDealtMultMod;
		owner.baseSpeedMult -= baseSpeedMultMod;
		owner.damageTakenMult -= damageTakenMultMod;
		owner.hpRegenMult -= hpRegenMultMod;
		owner.mpRegenMult -= mpRegenMultMod;
		owner.shieldRegenMult -= shieldRegenMultMod;
		owner.extraBullets -= extraBulletsMod;
		if (appliesIgnoreCollision) owner.ignoresCollision = getKeepsIgnoreCollision();
		if (appliesInvulnerability) owner.invulnerable = getKeepsInvulnerability();
		if (appliesActive) owner.active = getKeepsActive();
	}
	
	/// Apply the bonus effect tied to this effect
	public void applyExtraEffect() {
		if (extraEffectID.equals("")) return;
		if (extraEffectID.equals("STUNNED") && owner.id.startsWith("BOSS")) return;
		
		owner.addEffect(extraEffectID);
		owner.effects.get(owner.effects.size()-1).setDuration(this.duration);
		extra = owner.effects.get(owner.effects.size()-1);
	}
	
	/// Check if, after this effect is removed, the owner has another effect
	///   that applies the Invulnerable state
	public boolean getKeepsInvulnerability() {
		int n = -1;
		for (int i = 0; i < owner.effects.size(); i++) 
			if (owner.effects.get(i).appliesInvulnerability)
				n++;
		if (n <= 0) return false;
		return true;
	}
	
	/// Check if, after this effect is removed, the owner has another effect
	///   that applies the IgnoreCollision state
	public boolean getKeepsIgnoreCollision() {
		int n = -1;
		for (int i = 0; i < owner.effects.size(); i++) 
			if (owner.effects.get(i).appliesIgnoreCollision)
				n++;
		if (n <= 0) return false;
		return true;
	}
	
	/// Check if, after this effect is removed, the owner has another effect
	///   that applies the Active state
	public boolean getKeepsActive() {
		int n = -1;
		for (int i = 0; i < owner.effects.size(); i++) {
			if (owner.effects.get(i).appliesActive)
				n++;
		}
		if (n <= 0) return false;
		return true;
	}
	
	public void update() {
		if (expired) return;
	
		/// Update duration
		if (durationMax > 0) {
			duration -= main.timerdelay;
			if (duration <= 0) clear();
		}
		//tooltip.update();
		
		/// Update VFX
		try {
			if (vfx.isExpired()) vfx = null;
			else vfx.update();
		} catch (NullPointerException exc) { }
		
		/// Apply periodic effects
		if (damagePerTick != 0.0) {
			owner.spendHP(damagePerTick); 
		}
		if (mpCostPerTick != 0.0) {
			owner.spendMP(mpCostPerTick); 
			/// Remove the effect if it requires MP and owner is out of MP
			if (owner.mp <= 0.0 && this.mpCostPerTick > 0.0) {
				owner.mp = 0.0;
				removeMods();
			}
		}
	}
   
   public void clear() {
      removeMods();
   }
	
	/// Draw the effect associated with this VFX (if there is one)
	public void draw(Graphics g) {
		try {
			if (!vfx.isExpired()) vfx.draw(g);
		} catch (NullPointerException e) { }
	}
	
	public void drawIcon(Graphics g, int TLX, int TLY) {
		/// draw the icon
		g.setColor(new Color(255,255,255,255));
		g.drawImage(icon, TLX, TLY, null);
		
		/// draw duration indicator if applicable
		/// translucent black box that expands upwards to cover the entire icon
		///   as the effect's duration runs out
		if (durationMax > 0) {
			int pxExpended = (int)Math.round(20.0*(1.0*duration/durationMax));
			g.setColor(new Color(40,40,40,160));
			g.fillRect(TLX, TLY + pxExpended, 20, 20 - pxExpended);
		}
		/// draw 1px white border
		g.setColor(new Color(255,255,255,255));
		g.drawRect(TLX, TLY, 20, 20);
	}
	
	public void drawTooltip(Graphics g, int BLX, int BLY) {
		tooltip.draw(g,BLX,BLY);
	}
	
}