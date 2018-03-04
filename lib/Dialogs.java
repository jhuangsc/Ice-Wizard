class Dialogs {
   
   public static final String
                  
      GAME_PAUSED =  "Game Paused"+
                     "\n\n"+
                     "Press Space to resume"+
                     "\n\n"+
                     "If you need help, press H or refer to the documentation.",
      PLAYER_DIED =  "You have melted."+
                     "\n\n"+
                     "You have lost one life."+
                     "\n\n"+
                     "Press Space to restart the level",
      GAME_OVER = "Game Over"+
                  "\n\n"+
                  "The forces of the Firelord have prevailed."+
                  "\n\n"+
                  "Press Space to restart the game",
      LEVEL_CLEARED = "Level Cleared!\n\nCongratulations.\n\nPress Space to move on to the next battle.",
      
      HELP_DIALOG =  "Controls\n\n"+
                     "[WASD] Move\n"+
                     "[Q] Use Health Potion\n"+
                     "[E] Use Mana Potion\n"+
                     "[Left Click] Cast Snowflakes\n"+
                     "[Middle Click] Cast Frost Nova\n"+
                     "[Right Click] Cast Ice Shards\n\n"+
                     "[1] Cast Haste (Toggled)\n"+
                     "[2] Cast Fury\n"+
                     "[3] Cast Frost Shield\n"+
                     "[4] Cast Ultimate: Reversal"+
                     "[5] Cast Ultimate: Frozen Orb\n", 
      
      DEBUG_DIALOG = "Debug Controls - Spawning and Cheats\n\n"+
                     "[6] Cast Frozen Orb (Cheat)\n"+
                     "[7] Toggle Infinity\n"+
                     "[8] Toggle Immortality\n"+
                     "[9] Toggle Bulletstorm\n"+
                     "[0] Toggle Divine Shield\n"+
                     "[R] Kill Everything\n"+
                     "[T] Set Charge to 100\n"+
                     "[F] Spawn IWPickup: 1-Up\n\n"+
                     "[UIOP] Set to Spawn IWBattlers/Bosses/IWWalls/IWPickups\n"+
                     "[Z] Spawn IWBattler: Flame Sprite\n"+
                     "[X] Spawn IWBattler: Blazing Imp\n"+
                     "[C] Spawn IWBattler: Flaming Justicar\n"+
                     "[V] Spawn IWBattler: Fire Golem\n"+
                     "[B] Spawn IWBattler: Flame Turret\n"+
                     "[N] Spawn IWBattler: Inferno Turret\n\n"+
                     "[Z] Spawn Boss: Dirty Old Man\n"+
                     "[X] Spawn Boss: Frostfire Golem\n"+
                     "[C] Spawn Boss: Mad Mage\n"+
                     "[V] Spawn Boss: Aspect of Chaos\n"+
                     "[B] Spawn Boss: The Firelord\n\n"+
                     "[Z] Spawn IWWall: Arcane Barrier (Horizontal)\n"+
                     "[X] Spawn IWWall: Arcane Barrier (Vertical)\n"+
                     "[C] Spawn IWWall: Stone IWWall (Horizontal)\n"+
                     "[V] Spawn IWWall: Stone IWWall (Vertical)\n"+
                     "[B] Spawn IWWall: Barricade (lol weak)\n\n"+
                     "[Z] Spawn IWPickup: Ammo Pickup\n"+
                     "[X] Spawn IWPickup: Health Potion Pickup\n"+
                     "[C] Spawn IWPickup: Mana Potion Pickup\n"+
                     "[V] Spawn IWPickup: Mana Boost\n"+
                     "[B] Spawn IWPickup: Shield Boost\n"+
                     "[N] Spawn IWPickup: Shield Recharge\n",
      
      STORY_INTRODUCTION = "Ice Wizard"+
                           "\n\n"+
                           "By Jonathan Huang"+
                           "\n\n"+
                           "2012, ICS4U1-02 Summative"+
                           "\n\n\n"+
                           "Midrime was once a peaceful place, home to all manner of frosty creatures. "+
                           "They lived simple, quiet lives, dedicating their lives to the study of magic."+
                           "\n\n"+
                           "But that all changed when the Firelord attacked."+
                           "\n\n"+
                           "Now, all that stands between the armies of Flame and the destruction of Midrime is a lone ice wizard..."+
                           "\n\n"+
                           "Press Space to start playing"+
                           "\n\n"+
                           "Press H ingame to view Controls"+
                           "\n\n"+
                           "Press G ingame to view Debug Controls",
                     
      LEVEL_0 = "Level Start Dialog",
      
      LEVEL_1 = "Level 1: Open Battle"+
                "\n\n"+
                "The Firelord has sent his armies to establish a foothold in Midrime."+
                "\n\n"+
                "They must be stopped."+
                "\n\n"+
                "OBJECTIVES: Defeat the squadron of Flame Sprites."+
                "\n\n"+
                "TIP: Use your speed and abilities to your advantage. Don't just sit still and shoot snowflakes!"+
                "\n\n"+
                "NEW ENEMY: Flame Sprites are the Firelord's foot soldiers, capable of throwing balls of flame and burning their foes with a single touch of their fiery skin."+
                "",
                
      LEVEL_2 = "Level 2: The Forward Post"+
                "\n\n"+
                "The forces of the Firelord have rampaged through the coast of Midrime, destroying everything in their path."+
                "\n\n"+
                "They have already begun to build fortifications, preventing the people of Midrime from escaping."+
                "\n\n"+
                "As the only combat-capable ice wizard still alive, you have but two choices."+
                "\n\n"+
                "Be melted by the heat of the invaders, or fight back and bring the war to the Firelord's doorstep."+
                "\n\n"+
                "OBJECTIVES: Raze the outpost's Flame Turrets and defeat its garrison of Flame Sprites."+
                "\n\n"+
                "TIP: Luring the Flame Sprites away from the barricades will make it easier to deal with Flame Turret fire."+
                "\n\n"+
                "TIP: Once you have accumulated 100% charge (yellow bar), you can unleash it through a devastating Frozen Orb or a life-saving Reversal."+
                "\n\n"+
                "NEW ENEMY: Flame Turrets are a stationary defense used by the armies of Flame. They launch a spread of fireballs in a fixed pattern when enemies are within sight."+
                "",
                
      LEVEL_3 = "Level 3: Ambushed!"+
                "\n\n"+
                "The rabble of the Firelord's armies have been loosed on Midrime."+
                "\n\n"+
                "A campaign of terror has begun. The Firelord's imps now wander throughout Midrime, preying on fleeing civilians."+
                "\n\n"+
                "But how will they deal with the best of Midrime's mages?"+
                "\n\n"+
                "OBJECTIVES: Defeat the Blazing Imp ambush."+
                "\n\n"+
                "TIP: Your spells, particularly Frost Nova, can keep Imps at bay. You can also use Haste to temporarily outrun them."+
                "\n\n"+
                "NEW ENEMY: Blazing Imps are fast, vicious creatures that use tooth, claw and flame as their main weapons. They are relatively fragile and cannot attack from range."+
                ""+
                "",
                
      LEVEL_4 = "Level 4: The Gauntlet"+
                "\n\n"+
                "There is but one bridge across the Rust River between Midrime and Pyraheim, and it has been heavily fortified by the Flame armies."+
                "\n\n"+
                "To reach the Firelord, you must cross this bridge."+
                "\n\n"+
                "OBJECTIVES: Raze the bridge's defenses to proceed."+
                "\n\n"+
                "TIP: You can pick up items to improve your staying power in combat. Located behind you are three items: a health potion, a mana potion and a shield recharge."+
                "\n\n"+
                "NEW ENEMY: Inferno Turrets are the deadliest defensive structure in the Flame arsenal. When an enemy is in sight range, they spew almost-continuous streams of flame to incinerate their foes."+
                "",
                
      LEVEL_5 = "Level 5: Terminators"+
                "\n\n"+
                "The Firelord must be utterly bent on the destruction of Midrime. His armies have already routed the militia of Midrime, yet he still sends his most powerful soldiers to finish the job."+
                "\n\n"+
                "You have intercepted a trio of these Fire Golems in transit to Midrime. They must be destroyed, or the surviving Midrimeans will be massacred."+
                "\n\n"+
                "OBJECTIVES: Destroy the three Fire Golems."+
                "\n\n"+
                "TIP: The icy barricades you have set up can delay the golems for a short time."+
                "\n\n"+
                "NEW ENEMY: Fire Golems are powerful soldiers that attack from range using streams of flame. They can cause a periodic eruption that causes burning and reflects projectiles. Due to their unique shield, they take less damage from all attacks."+
                "",
                
      LEVEL_6 = "Level 6: Besieged"+
                "\n\n"+
                "In ages long past, Pyraheim and Midrime were on friendly terms. You have discovered a remnant of that bygone era: a lighthouse built by Midrime's mages to guide Flame ships through the Rust Sea."+
                "\n\n"+
                "The wards protecting this lighthouse still persist, and you decide to shelter inside for the night. But a large Flame battlegroup has arrived, with different plans..."+
                "\n\n"+
                "OBJECTIVES: Defeat the enemy battlegroup and destroy their siege weapons."+
                "\n\n"+
                "TIP: Use the lighthouse's wards to your advantage. They are extremely sturdy and can protect you from enemy fire while you retaliate from within."+
                "\n\n"+
                "TIP: Some items can permanently increase your maxmimum mana or shields in addition to replenishing them."+
                "",
                
      LEVEL_7 = "Level 7: Through the Sewers"+
                "\n\n"+
                "You have discovered another remnant of a past age: an entrance to the sewers of Pyraheim."+
                "\n\n"+
                "Although the prospect of travelling through sewers is repulsive, it is an opportunity to bypass the majority of the Flame armies, who travel on the surface."+
                "\n\n"+
                "Fortunately, most of the waste generated by Pyraheimians consists of ash. And the sewers are uninhabited... or are they?"+
                "\n\n"+
                "OBJECTIVES: A dirty old man is attempting to mug you. Defeat him and escape."+
                "\n\n"+
                "TIP: The mugger wields Midrime magic, tainted by the residues of Flame sewage. Beware his Frozen Sewage attack: it is as deadly as your own Frozen Orb!"+
                "\n\n"+
                "TIP: The mugger appears to be severely arthritic, so you have an advantage in agility. Use it and the nearby side passage to maximum effect!"+
                "",
                
      LEVEL_8 = "Level 8: Ash City"+
                "\n\n"+
                "Emerging from the sewers, you find yourself near the gates of Ash City, the capital of Pyraheim and the Firelord's seat of power."+
                "\n\n"+
                "The battles ahead will surely be the most difficult you will ever face. But the survival of your homeland is at stake. You must break on through."+
                "\n\n"+
                "OBJECTIVES: Defeat the garrison defending the outer gates of Pyraheim and raze the defenses."+
                "\n\n"+
                "TIP: Like other projectiles, Shackles can be reflected using either your Frost Nova or Shield."+
                "\n\n"+
                "NEW ENEMY: Flaming Justicars are the praetorians of the Firelord's armies. Their yellow-hot flames are far more powerful than the red-hot flames of the foot soldiers. In addition, they throw burning shackles that can temporarily fuse you to the ground, leading you vulnerable to an immediate melting."+
                "",
                
      LEVEL_9 = "Level 9: The Blazing Curtain"+
                "\n\n"+
                "You broke through the outer gates of Ash City, finding only empty streets. Did the civilians of Pyraheim evacuate or is the situation even more sinister than you had thought?"+
                "\n\n"+
                "Another wall lies between you and the Firelord's citadel. This time, you face the Blazing Curtain, the inner wall of Ash City. The only entrance is a set of double gates protected by a tower. You must proceed cautiously if you are to overcome such odds."+
                "\n\n"+
                "OBJECTIVES: Destroy the garrison's defenses and defeat the Flame soldiers within."+
                "\n\n"+
                "TIP: Most of your flaming foes won't begin attacking until they can see you. Use this, and the wide spaces, to your advantage."+
                "\n\n"+
                "TIP: You don't have to break through the stone walls of the fortress. You might be able to find an easier entrance."+
                "\n\n"+
                "NEW ENEMY: Pyrotechnics are the hunters of the Flame army, using a stream of rapidly-firing intense flames to melt their prey. They do not chase their prey on foot."+
                "",
                
      LEVEL_10 = "Level 10: The Gate to Hell"+
                 "\n\n"+
                 "At last, your destination is in sight! The Firelord awaits in the sanctum of the Phoenix Citadel."+
                 "\n\n"+
                 "This is the most heavily defended place on the continent. And the gates have been removed."+
                 "\n\n"+
                 "OBJECTIVES: Breach the citadel's walls and defeat its defenders to proceed."+
                 "\n\n"+
                 "TIP: You have established a temporary fortification to assist in your siege of the keep. Take advantage of your range to batter down the enemy's walls!"+
                 "\n\n"+
                 "TIP: Flame enemies usually don't begin attacking until you are within their sight."+
                 "",
                 
      LEVEL_11 = "Level 11: Devastation"+
                 "\n\n"+
                 "A strange and terrifying foe has emerged in the foyer of the citadel: a golem powered by both flame and frost."+
                 "\n\n"+
                 "The unstable combination could wreak untold destruction on both Pyraheim and Midrime if allowed to persist."+
                 "\n\n"+
                 "Such an experiment nearly destroyed the two continents in a past age. It cannot be allowed to happen again."+
                 "\n\n"+
                 "OBJECTIVES: Annihilate the Frostfire Golem."+
                 "\n\n"+
                 "TIP: The golem's attacks will funnel you into a potentially unfavourable position. Frost Nova can create an opening to allow you to reposition yourself."+
                 "",
                 
      LEVEL_12 = "Level 12: Into the Heart of Madness"+
                 "\n\n"+
                 "The next room holds a bone-chilling sight: your former mentor, now servant to the Firelord. He must be the source of the terrible golem you fought earlier. Now he wields the conflicting magics of flame and frost, uncaring to the destruction it would cause."+
                 "\n\n"+
                 "If he has truly fallen so deeply into madness, then he is already lost to you."+
                 "\n\n"+
                 "OBJECTIVES: Defeat the Mad Mage."+
                 "\n\n"+
                 "TIP: The Mad Mage eventually wields a stronger but slower analogue to your Ice Shards. You may be able to turn it against him."+
                 "\n\n"+
                 "TIP: The Mad Mage knows a Frost Nova similar to yours. Fortunately, he is less proactive with its use."+
                 "",
                 
      LEVEL_13 = "Level 13: The Pyros Council"+
                 "\n\n"+
                 "This place was once the seat of government for the Pyros Council, advisors to the Firelord. Now it is merely a war room for the Firelord's generals."+
                 "\n\n"+
                 "The neck of the serpent lies exposed. Defeat the Council and the armies assaulting Midrime will fall into disarray. Maybe then your people will have a chance to rally and escape."+
                 "\n\n"+
                 "OBJECTIVES: Defeat the Pyros Council and their entourage."+
                 "\n\n"+
                 "TIP: The Council has fortified the war chamber. They can attack over the temporary walls, but you cannot."+
                 "\n\n"+
                 "TIP: The walls of the citadel can provide cover, if you find the right angle."+
                 "",
                 
      LEVEL_14 = "Level 14: Pandemonium"+
                 "\n\n"+
                 "The death of your mentor did not avert the potential catastrophe resulting from the use of frostfire magic. A swirling vortex of flame has manifested in the sanctum's atrium, threatening to tear the citadel apart."+
                 "\n\n"+
                 "A being as powerful as the Firelord could survive the collapse, but not you. You must neutralize the flame before it consumes the citadel, and with it, you."+
                 "\n\n"+
                 "OBJECTIVES: Destroy the Aspect of Chaos."+
                 "\n\n"+
                 "TIP: Use the sanctum's walls for cover while they are still standing."+
                 "",
                 
      LEVEL_15 = "Level 15: Final Destination"+
                 "\n\n"+
                 "You have reached the heart of Pyraheim: the sanctum of the Phoenix Citadel in Ash City."+
                 "\n\n"+
                 "The Firelord stares at you, silent and grim. Its aspect is both magnificent and terrifying."+
                 "\n\n"+
                 "You cannot help but ask. \"Why?\" The creature smiles, ignoring your query."+
                 "\n\n"+
                 "Just as well. The time for words ended when innocent Midrimeans were slaughtered without reason. Now is the time for the battle that will decide the fate of Midrime."+
                 "\n\n"+
                 "You must not fail."+
                 "\n\n"+
                 "OBJECTIVES: Defeat the Firelord. Save the world."+
                 "\n\n"+
                 "TIP: The Firelord's living meteors are slow but utterly lethal. Avoid them at all costs."+
                 "",
      
      STORY_EPILOGUE =  "Victory!"+
                        "\n\n"+
                        "You have journeyed through a land of your kind's worst nightmares and survived! "+
                        "\n\n"+
                        "You have prevailed over the Firelord, saved both Midrime and Pyraheim and earned yourself a place in legend."+
                        "\n\n"+
                        "Press Space to return to the main menu and play again with increased difficulty. Your accumulated lives and boosters will be retained.";
      
   public Dialogs() { }
}