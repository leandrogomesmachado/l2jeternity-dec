package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _092_SagaOfTheElementalMaster extends SagasSuperClass {
   public _092_SagaOfTheElementalMaster(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{30174, 31281, 31614, 31614, 31629, 31646, 31648, 31652, 31654, 31655, 31659, 31614};
      this.Items = new int[]{7080, 7605, 7081, 7507, 7290, 7321, 7352, 7383, 7414, 7445, 7111, 0};
      this.Mob = new int[]{27314, 27241, 27311};
      this.classid = new int[]{104};
      this.prevclass = new int[]{28};
      this.npcSpawnLocations = new Location[]{new Location(161719, -92823, -1893), new Location(124376, 82127, -2796), new Location(124355, 82155, -2803)};
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
      new _092_SagaOfTheElementalMaster(92, _092_SagaOfTheElementalMaster.class.getSimpleName(), "");
   }
}
