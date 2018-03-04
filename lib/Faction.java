/// Neutral
///   hostile to all except Gaia
///   friendly to Gaia
/// Player
///   hostile to Enemy, Neutral
///   friendly to NPC, Gaia
/// NPC
///   hostile to Enemy, Neutral
///   friendly to Player, Gaia
/// Enemy
///   hostile to Player, NPC, Neutral
///   friendly to Gaia
/// Gaia
///   friendly to all

class Faction {
   
   int id = 0;
   
   public Faction(String label) {
      if (label.equals("NEUTRAL"))
         id = Constants.FACTION_NEUTRAL;
      else if (label.equals("PLAYER"))
         id = Constants.FACTION_PLAYER;
      else if (label.equals("NPC"))
         id = Constants.FACTION_NPC;
      else if (label.equals("ENEMY"))
         id = Constants.FACTION_ENEMY;
      else if (label.equals("GAIA"))
         id = Constants.FACTION_GAIA;
      else 
         id = Constants.FACTION_NEUTRAL;
   }
   
   public Faction(int faction) {
      this.id = faction;
   }
   
   public boolean hostileTo(Faction f) {
      switch(this.id) {
         case Constants.FACTION_NEUTRAL:
            if (f.id == Constants.FACTION_GAIA) return false;
            else return true;
         case Constants.FACTION_PLAYER:
            if (f.id == Constants.FACTION_ENEMY || f.id == Constants.FACTION_NEUTRAL) return true;
            else return false;
         case Constants.FACTION_NPC:
            if (f.id == Constants.FACTION_ENEMY || f.id == Constants.FACTION_NEUTRAL) return true;
            else return false;
         case Constants.FACTION_ENEMY:
            if (f.id == Constants.FACTION_PLAYER || f.id == Constants.FACTION_NPC || f.id == Constants.FACTION_NEUTRAL) return true;
            else return false;
         case Constants.FACTION_GAIA:
            return false;
      }
      return false;
   }
   
   public boolean friendlyTo(Faction f) {
      return !this.hostileTo(f);
   }
   
}