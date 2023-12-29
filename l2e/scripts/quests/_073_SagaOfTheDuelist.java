package l2e.scripts.quests;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.QuestState;

public class _073_SagaOfTheDuelist extends SagasSuperClass {
   private final int TUNATUN = 31537;
   private final int TOPQUALITYMEAT = 7546;

   public _073_SagaOfTheDuelist(int id, String name, String descr) {
      super(id, name, descr);
      this.NPC = new int[]{30849, 31624, 31226, 31331, 31639, 31646, 31647, 31653, 31654, 31655, 31656, 31277};
      this.Items = new int[]{7080, 7537, 7081, 7488, 7271, 7302, 7333, 7364, 7395, 7426, 7096, 7546};
      this.Mob = new int[]{27289, 27222, 27281};
      this.classid = new int[]{88};
      this.prevclass = new int[]{2};
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
      this.addTalkId(31537);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      if (npc.getId() == 31537) {
         String htmltext = getNoQuestMsg(player);
         QuestState st = player.getQuestState(this.getName());
         if (st == null || !st.isCond(3)) {
            return htmltext;
         } else if (!st.hasQuestItems(7546)) {
            st.giveItems(7546, 1L);
            return "tunatun_01.htm";
         } else {
            return "tunatun_02.htm";
         }
      } else {
         return super.onTalk(npc, player);
      }
   }

   public static void main(String[] args) {
      new _073_SagaOfTheDuelist(73, _073_SagaOfTheDuelist.class.getSimpleName(), "");
   }
}
