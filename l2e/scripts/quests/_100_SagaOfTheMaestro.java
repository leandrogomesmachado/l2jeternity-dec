package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _100_SagaOfTheMaestro extends SagasSuperClass {
   public _100_SagaOfTheMaestro(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{31592, 31273, 31597, 31597, 31596, 31646, 31648, 31653, 31654, 31655, 31656, 31597};
      this.Items = new int[]{7080, 7607, 7081, 7515, 7298, 7329, 7360, 7391, 7422, 7453, 7108, 0};
      this.Mob = new int[]{27260, 27249, 27308};
      this.classid = new int[]{118};
      this.prevclass = new int[]{57};
      this.npcSpawnLocations = new Location[]{new Location(164650, -74121, -2871), new Location(47429, -56923, -2383), new Location(47391, -56929, -2370)};
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
      new _100_SagaOfTheMaestro(100, _100_SagaOfTheMaestro.class.getSimpleName(), "");
   }
}
