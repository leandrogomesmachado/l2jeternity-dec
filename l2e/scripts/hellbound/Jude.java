package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Jude extends Quest {
   private static final int JUDE = 32356;
   private static final int NativeTreasure = 9684;
   private static final int RingOfWindMastery = 9677;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      if ("TreasureSacks".equalsIgnoreCase(event)) {
         if (HellboundManager.getInstance().getLevel() == 3 && qs.getQuestItemsCount(9684) >= 40L) {
            qs.takeItems(9684, 40L);
            qs.giveItems(9677, 1L);
            return "32356-02.htm";
         } else {
            return "32356-02a.htm";
         }
      } else {
         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      switch(HellboundManager.getInstance().getLevel()) {
         case 0:
         case 1:
         case 2:
            return "32356-01.htm";
         case 3:
         case 4:
            return "32356-01c.htm";
         case 5:
            return "32356-01a.htm";
         default:
            return "32356-01b.htm";
      }
   }

   public Jude(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32356);
      this.addStartNpc(32356);
      this.addTalkId(32356);
   }

   public static void main(String[] args) {
      new Jude(-1, "Jude", "hellbound");
   }
}
