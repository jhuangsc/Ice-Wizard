/// AI
/// 
/// Purpose: Handle abilities and behaviour of IWBattlers and some IWProjectiles
///
/// All IWBattlers and some IWProjectiles will have an AI attached to them. The AI
///   handles basic behaviour such as moving and using abilities. All Entities
///   that can use abilities must have an AI. 

import java.util.ArrayList;

class AI {
	Game main;
	IWObject owner;
	ArrayList<Ability> abilities;
	
	String id;
	boolean stopped = false;
	
	/// Used to store multiple targets and firing flags
	/// booleans are currently used only for the player
	/// while angles are used by any mobs that don't target the player with an ability
	boolean[] firing = {false,false,false,false};
	double[] angle = {0.0,0.0,0.0,0.0};
   boolean aggro = false;
	
	int cnt = 0;
	boolean reverse = false;
	
	int time = 0;
	
	/// Create a new AI for a mob - use the mob itself as the field
	public AI(IWBattler e) {
		this.owner = e;
		this.id = owner.id;
		this.main = owner.main;
		this.abilities = new ArrayList<Ability>();
		
		/// Copy abilities from the template mob in GameEntities
		/// If this is creating the template mob, then do nothing, the abilities will be read from files
		try {
			this.copyAbilities(main.entities.getIWBattlerByID(this.id).ai.abilities);
		} catch (NullPointerException exc) { }
	}
	
	public AI(IWProjectile e) {
		this.owner = e;
		this.id = owner.id;
		this.main = owner.main;
		this.abilities = new ArrayList<Ability>();
		
		/// Copy abilities from the template projectile in GameEntities
		/// Note that a projectile will only have abilities if it has an AI
		/// If this is creating the template projectile, then nothing will happen, the abilities will be read from files
		try {
			this.copyAbilities(main.entities.getIWProjectileByID(this.id).ai.abilities);
		} catch (NullPointerException exc) { }
	}
	
	public void act() {
		if (this.stopped) return;
		
		double hpp = owner.getHPPercent();
		double sp = owner.getShieldPercent();
      if (hpp < 99.9 || sp < 99.9) aggro = true;
		
		if (id.equals("PLAYER")) {
		
			if (firing[0]) useAbility("SNOWFLAKE");
			if (firing[1]) useAbility("ICESHARD");
			
		} else if (id.equals("PROJECTILE_FROZENORB")) {
		
			target(angle[0] += 3);
			useAbility("SLEETSTORM");
			owner.rotate(Math.toRadians(3));
			
		} else if (id.equals("PROJECTILE_CHEAT_FROZENORB")) {
		
			target(angle[0] += 3);
			useAbility("CHEAT_SLEETSTORM");
			owner.rotate(Math.toRadians(3));
			
		} else if (id.equals("MOB_BLAZINGIMP")) {
			
         if (aggro) {
            target(main.player);
            owner.moveTowardsPlayer();
         }
         if (owner.getDistanceTo(main.player) <= 400) aggro = true;
			
		} else if (id.equals("MOB_FLAMESPRITE")) {
      
         if (aggro) {
            target(main.player);
            owner.moveTowardsPlayer();
            useAbility("FIREBALL");
         } 
         if (owner.getDistanceTo(main.player) <= 200) aggro = true;
         
		} else if (id.equals("MOB_FLAMINGJUSTICAR")) {
      
         if (aggro) {
            target(main.player);
            owner.moveTowardsPlayer();
            useAbility("INCINERATE");
            useAbility("BURNINGSHACKLES");
         } else {
            owner.stop();
         }
         if (owner.getDistanceTo(main.player) <= 400) aggro = true;
         
		} else if (id.equals("MOB_FIREGOLEM")) {
      
         if (aggro) {
            target(main.player);
            owner.moveTowardsPlayer();
            useAbility("FLAMEWAVE");
            useAbility("ERUPT");
         }
         if (owner.getDistanceTo(main.player) <= 600) aggro = true;
         
		} else if (id.equals("MOB_PYROTECHNIC")) {
         
         if (aggro) {
            target(main.player);
            useAbility("SEAR");
         }
         if (owner.getDistanceTo(main.player) <= 800) aggro = true;
         
      } else if (id.equals("MOB_FLAMETURRET")) {
			
         if (owner.getDistanceTo(main.player) <= 500) {
            target(angle[0] += Math.toRadians(0.2));
            useAbility("FLAMEBURST", owner.tx, owner.ty);
         }
			
		} else if (id.equals("MOB_INFERNOTURRET")) {
		
         if (owner.getDistanceTo(main.player) <= 500) {
            useAbility("ACTIVATE_3_6");
            
            if (isActive()) {
               target(angle[0] += Math.toRadians(0.2));
               useAbility("INFERNO");
            }
			}
         
		} else if (id.equals("BOSS_DIRTYOLDMAN")) {
		
			/// Shield 0%-75%: Use Vile Sleet Storm
			/// Shield 25%-100%: Use Tainted Blizzard
			/// Shield <50%: Use Fetid Blast (3/5 s cycles)
			/// Health <100%: Use Frozen Sewage
			
			if (sp < 50.0) useAbility("ACTIVATE_3_5");
		
			if (sp < 75.0) {
				target(angle[0] += Math.toRadians(0.6));
				useAbility("VILESLEETSTORM");
			}
			if (sp > 25.0) {
				target(angle[1] += Math.toRadians(0.5));
				useAbility("TAINTEDBLIZZARD");
			}
			if (sp < 50.0 && isActive()) {
				target(main.player);
				useAbility("FETIDBLAST");
			}
			if (hpp < 100.0) {
				target(main.player);
				useAbility("FROZENSEWAGE");
			}
			
		} else if (id.equals("PROJECTILE_FROZENSEWAGE")) {
		
			target(angle[0] += 3);
			useAbility("SEWAGESTORM");
			owner.rotate(Math.toRadians(3));
			
		}  else if (id.equals("BOSS_FROSTFIREGOLEM")) {
		
			/// Always: Use Incinerating Frost
			/// Health 50%-100%: Use Searing Blizzard
			/// Health 0%-75%: Use Howling Flames (3/5 s)
		
			useAbility("ACTIVATE_3_5");
			
			target(angle[0] += Math.toRadians(0.04));
			useAbility("INCINERATINGFROST");
			
			if (hpp > 50.0) {
				target(main.player);
				useAbility("SEARINGBLIZZARD");
			}
			
			if (hpp < 75.0 && isActive()) {
				target(angle[2] += Math.toRadians(0.8));
				useAbility("HOWLINGFLAMES");
			}
			
		} else if (id.equals("BOSS_MADMAGE")) {
		
			/// Shield 25%-100%: Use Chilling Firestorm
			/// Shield 50%-75%: Use Frostfire Stream (3/6 s)
			/// Shield 30%-50%: Chase player
			/// Shield 0-50%: Use Nova when close enough to player
			/// Shield 0%-50%: Use Frostfire Stream (3/4 s)
			/// Shield 0%-20%: Use Blue Flame Wave (sine wave)
			/// Shield 0%-20%: Use Glacial Spike
			/// Shield <20%: Teleport to top of screen and remain stationary
			
			if (sp < 20.0) useAbility("ACTIVATE_3_4");
			else if (sp < 75.0) useAbility("ACTIVATE_3_6");
			
			if (sp > 20.0 && sp < 50.0) owner.moveTowardsPlayer();
			if (sp < 50.0 && owner.getDistanceTo(main.player) <= 75.0) useAbility("NOVA");
			if (sp < 20.0) owner.setPosition(400, 150);
			
			/// Shield > 20%: Use Chilling Firestorm 
			if (sp > 20.0) {
				target(angle[0] += Math.toRadians(0.1));
				useAbility("CHILLINGFIRESTORM");
			} 
			
			if (sp < 20.0) {
				target(Math.toRadians(90));
				rotateTarget(Math.sin(Math.toRadians(time)/20.0));
				cycleTime();
				//rotateTarget(cnt*3);
				//cycle(-5,5); ///(0,5) for a cool effect with three-armed spiral
				useAbility("BLUEFLAMEWAVE");
			}
			
			if (sp < 20.0) {
				target(main.player);
				useAbility("GLACIALSPIKE");
			}
			
			if (sp < 75.0 && isActive()) {
				target(main.player);
				useAbility("FROSTFIRESTREAM");
			} 
			
			
		} else if (id.equals("BOSS_ASPECTOFCHAOS")) {
		
			/// Always: Use Creeping Firestorm
			/// Shield <50%: Use Ravenous Stream (active for 5s for every 10s)
			/// HP <72.5%: Use Doom Blast
		
			if (hpp < 75.0) useAbility("ACTIVATE_10_15");
			else useAbility("ACTIVATE_5_10");
			
			if (sp == 0.0 && isActive()) {
				target(main.player);
				useAbility("RAVENOUSSTREAM");
			}
			
			target(Random.randAngle());
			useAbility("CREEPINGFIRESTORM");
			
			if (hpp < 90) {
				target(Random.randAngle());
				useAbility("DOOMBLAST");
			}
			
		} else if (id.equals("BOSS_THEFIRELORD")) {
		
			/// Always: Hungering Flames (sine wave of fireballs)
			/// Always: Meteor every 8s (chasing fireball that does 1k damage, stops chasing if reflected)
			/// Always: Cauterizing Burst every 6s(ring of slower shackles)
			/// Shield >0%: Blast Wave (active 5/15 s)
			/// Shield =0%: Terror Wave (active 5/15 s)
			/// Shield =0%: Living Flame every 8s
			
			useAbility("ACTIVATE_5_15");
			
			/// Cast Hungering Flames in a back-and-forth wave pattern
			target(Math.toRadians(90));
			rotateTarget(Math.sin(Math.toRadians(time)/20.0));
			cycleTime();
			useAbility("HUNGERINGFLAMES");
			
			/// Cast Meteor and Cauterizing Burst at the player
			target(main.player);
			useAbility("METEOR");
			useAbility("CAUTERIZINGBURST");
			
			target(angle[0] -= Math.toRadians(1));
			useAbility("INCINERATE_2");
			
			/// Use either Blast Wave (four spiral) or Terror Wave (six fast spiral) when active
			if (isActive()) {
				if (sp > 0.0) {
					target(angle[1] += Math.toRadians(0.2));
					useAbility("BLASTWAVE");
				} else {
					target(angle[2] += Math.toRadians(0.8));
					useAbility("TERRORWAVE");
				}
			}
			
			if (sp == 0.0) {
				target(Random.randInt(45,135));
				useAbility("LIVINGFLAME");
			}
			
		} else if (id.equals("PROJECTILE_METEOR")) {
		
			if (!owner.reflectedOnce) owner.moveTowardsPlayer();
			
		} else if (id.equals("PROJECTILE_LIVING_FLAME")) {
			
			target(angle[0] += Math.toRadians(0.4));
			useAbility("FLAMEBURSTINTENSE");
		}
	}
	
	/// Call when the entity is destroyed
	public void stop() {
		stopped = true;
	}
	
	public void target(double tx, double ty) {
		owner.tx = tx;
		owner.ty = ty;
	}
	
	public void target(double angle) {
		owner.tx = owner.x + 100*Math.cos(angle);
		owner.ty = owner.y + 100*Math.sin(angle);
	}
	
	public void target(IWBattler e) {
		owner.tx = e.x;
		owner.ty = e.y;
	}
	
	public void rotateTarget(int degrees) {
		rotateTarget(Math.toRadians(degrees));
	}
	
	public void rotateTarget(double rads) {
		target(Math.atan2( (owner.ty - owner.y), (owner.tx - owner.x) ) + rads);
	}
	
	public void addAbility(String abilityID) {
		int i = abilities.size();
		if (abilityID.startsWith("CHEAT") && !main.cheatsEnabled) 
			return;
		else
			abilities.add(GameAbilities.getCloneByID(abilityID,this.owner));
	}
	
	public void useAbility(String abilityID) {
		useAbility(abilityID, owner.tx, owner.ty);
	}
	
	public void useAbility(String abilityID, double tx, double ty) {
		if (stopped) return;
		for (int i = 0; i < abilities.size(); i++) {
			if (abilities.get(i).id.equals(abilityID)) {
				abilities.get(i).use(owner.x, owner.y, tx, ty);
				break;
			}
		}
	}
	
	public boolean hasAbility(String abilityID) {
		for (int i = 0; i < abilities.size(); i++)
			if (abilities.get(i).id.equals(abilityID)) return true;
		return false;
	}
	
	/// Gets reference to ability with matching ID
	public Ability getAbility(String abilityID) {
		for (int i = 0; i < abilities.size(); i++)
			if (abilities.get(i).id.equals(abilityID)) return abilities.get(i);
		return null;
	}
	
	/// Clones the abilities of another AI's ability list
	public void copyAbilities(ArrayList<Ability> n) {
		for (int i = 0; i < n.size(); i++) {
			abilities.add(n.get(i).getClone(this.owner));
		}
	}
	
	/// Resets cooldowns for all abilities
	public void resetCooldowns() {
		for (int i = 0; i < abilities.size(); i++)
			abilities.get(i).refresh();
	}
	
	/// Removes VFX for all abilities
	/// Generally used only when moving from one level to the next
	public void clearVFX() {
		for (int i = 0; i < abilities.size(); i++)
			abilities.get(i).clearVFX();
	}
	
	/// Check if the AI's owner is "Active", meaning certain mob
	///   abilities are temporarily usable
	public boolean isActive() {
		return owner.active;
	}
	
	public void cycle(int min, int max) {
		if (reverse) {
			cnt--;
			if (cnt == min) reverse = false;
		} else {
			cnt++;
			if (cnt == max) reverse = true;
		}
	}
	
	public void cycleTime() {
		time += main.timerdelay;
	}
	
}