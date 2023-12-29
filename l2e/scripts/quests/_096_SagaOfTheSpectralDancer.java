package l2e.scripts.quests;

import l2e.gameserver.model.Location;

public class _096_SagaOfTheSpectralDancer extends SagasSuperClass {
   public _096_SagaOfTheSpectralDancer(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{31582, 31623, 31284, 31284, 31611, 31646, 31649, 31653, 31654, 31655, 31656, 31284};
      this.Items = new int[]{7080, 7527, 7081, 7511, 7294, 7325, 7356, 7387, 7418, 7449, 7092, 0};
      this.Mob = new int[]{27272, 27245, 27264};
      this.classid = new int[]{107};
      this.prevclass = new int[]{34};
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
      new _096_SagaOfTheSpectralDancer(96, _096_SagaOfTheSpectralDancer.class.getSimpleName(), "");
   }
}
