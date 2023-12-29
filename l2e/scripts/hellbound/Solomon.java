package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class Solomon extends Quest {
   private static final int SOLOMON = 32355;

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (HellboundManager.getInstance().getLevel() == 5) {
         return "32355-01.htm";
      } else {
         return HellboundManager.getInstance().getLevel() > 5 ? "32355-01a.htm" : null;
      }
   }

   public Solomon(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32355);
   }

   public static void main(String[] args) {
      new Solomon(-1, "Solomon", "hellbound");
   }
}
