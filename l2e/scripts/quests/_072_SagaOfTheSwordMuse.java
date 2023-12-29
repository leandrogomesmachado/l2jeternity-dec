package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _072_SagaOfTheSwordMuse extends SagasSuperClass {
   public _072_SagaOfTheSwordMuse(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{30853, 31624, 31583, 31537, 31618, 31646, 31649, 31652, 31654, 31655, 31659, 31281};
      this.Items = new int[]{7080, 7536, 7081, 7487, 7270, 7301, 7332, 7363, 7394, 7425, 7095, 6482};
      this.Mob = new int[]{27288, 27221, 27280};
      this.classid = new int[]{100};
      this.prevclass = new int[]{21};
      this.npcSpawnLocations = new Location[]{new Location(161719, -92823, -1893), new Location(124355, 82155, -2803), new Location(124376, 82127, -2796)};
      this.Text = new String[]{
         "PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
         "... Oh ... good! So it was ... let's begin!",
         "I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
         "Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
         "Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
         "Why do you interfere others' battles?",
         "This is a waste of time.. Say goodbye...!",
         "...That is the enemy",
         "...Goodness! PLAYERNAME you are still looking?",
         "PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
         "Your sword is not an ornament. Don't you think, PLAYERNAME?",
         "Goodness! I no longer sense a battle there now.",
         "let...",
         "Only engaged in the battle to bar their choice. Perhaps you should regret.",
         "The human nation was foolish to try and fight a giant's strength.",
         "Must...Retreat... Too...Strong.",
         "PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
         "....! Fight...Defeat...It...Fight...Defeat...It..."
      };
      this.registerNPCs();
   }

   public static void main(String[] args) {
      new _072_SagaOfTheSwordMuse(72, _072_SagaOfTheSwordMuse.class.getSimpleName(), "");
   }
}
