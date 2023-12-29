package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _078_SagaOfTheDoomcryer extends SagasSuperClass {
   public _078_SagaOfTheDoomcryer(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{31336, 31624, 31589, 31290, 31642, 31646, 31649, 31650, 31654, 31655, 31657, 31290};
      this.Items = new int[]{7080, 7539, 7081, 7493, 7276, 7307, 7338, 7369, 7400, 7431, 7101, 0};
      this.Mob = new int[]{27295, 27227, 27285};
      this.classid = new int[]{116};
      this.prevclass = new int[]{52};
      this.npcSpawnLocations = new Location[]{new Location(191046, -40640, -3042), new Location(46087, -36372, -1685), new Location(46066, -36396, -1685)};
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
      new _078_SagaOfTheDoomcryer(78, _078_SagaOfTheDoomcryer.class.getSimpleName(), "");
   }
}
