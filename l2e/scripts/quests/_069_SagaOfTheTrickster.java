package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _069_SagaOfTheTrickster extends SagasSuperClass {
   public _069_SagaOfTheTrickster(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{32138, 31270, 31282, 32228, 32237, 31646, 31649, 31653, 31654, 31655, 31659, 31283};
      this.Items = new int[]{7080, 9761, 7081, 9742, 9724, 9727, 9730, 9733, 9736, 9739, 9718, 0};
      this.Mob = new int[]{27333, 27334, 27335};
      this.classid = new int[]{134};
      this.prevclass = new int[]{130};
      this.npcSpawnLocations = new Location[]{new Location(164014, -74733, -3093), new Location(124355, 82155, -2803), new Location(124376, 82127, -2796)};
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
      new _069_SagaOfTheTrickster(69, _069_SagaOfTheTrickster.class.getSimpleName(), "");
   }
}
