package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.scripts.ai.AbstractNpcAI;

public class Rafforty extends AbstractNpcAI {
   private static final int RAFFORTY = 32020;
   private static final int NECKLACE = 16025;
   private static final int BLESSED_NECKLACE = 16026;
   private static final int BOTTLE = 16027;

   public Rafforty(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32020);
      this.addFirstTalkId(32020);
      this.addTalkId(32020);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      switch(event) {
         case "32020-01.htm":
            if (!hasQuestItems(player, 16025)) {
               htmltext = "32020-02.htm";
            }
            break;
         case "32020-04.htm":
            if (!hasQuestItems(player, 16027)) {
               htmltext = "32020-05.htm";
            }
            break;
         case "32020-07.htm":
            if (!hasQuestItems(player, new int[]{16027, 16025})) {
               return "32020-08.htm";
            }

            takeItems(player, 16025, 1L);
            takeItems(player, 16027, 1L);
            giveItems(player, 16026, 1L);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new Rafforty(Rafforty.class.getSimpleName(), "custom");
   }
}
