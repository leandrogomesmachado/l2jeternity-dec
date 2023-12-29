package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _085_SagaOfTheCardinal extends SagasSuperClass {
   public _085_SagaOfTheCardinal(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{30191, 31626, 31588, 31280, 31644, 31646, 31647, 31651, 31654, 31655, 31658, 31280};
      this.Items = new int[]{7080, 7522, 7081, 7500, 7283, 7314, 7345, 7376, 7407, 7438, 7087, 0};
      this.Mob = new int[]{27267, 27234, 27274};
      this.classid = new int[]{97};
      this.prevclass = new int[]{16};
      this.npcSpawnLocations = new Location[]{new Location(119518, -28658, -3811), new Location(181215, 36676, -4812), new Location(181227, 36703, -4816)};
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
      new _085_SagaOfTheCardinal(85, _085_SagaOfTheCardinal.class.getSimpleName(), "");
   }
}
