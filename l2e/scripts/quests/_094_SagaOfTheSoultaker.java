package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _094_SagaOfTheSoultaker extends SagasSuperClass {
   public _094_SagaOfTheSoultaker(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{30832, 31623, 31279, 31279, 31645, 31646, 31648, 31650, 31654, 31655, 31657, 31279};
      this.Items = new int[]{7080, 7533, 7081, 7509, 7292, 7323, 7354, 7385, 7416, 7447, 7085, 0};
      this.Mob = new int[]{27257, 27243, 27265};
      this.classid = new int[]{95};
      this.prevclass = new int[]{13};
      this.npcSpawnLocations = new Location[]{new Location(191046, -40640, -3042), new Location(46066, -36396, -1685), new Location(46087, -36372, -1685)};
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
      new _094_SagaOfTheSoultaker(94, _094_SagaOfTheSoultaker.class.getSimpleName(), "");
   }
}
