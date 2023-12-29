package l2e.scripts.hellbound;

import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Kief extends Quest {
   private static final int KIEF = 32354;
   private static final int BOTTLE = 9672;
   private static final int DARION_BADGE = 9674;
   private static final int DIM_LIFE_FORCE = 9680;
   private static final int LIFE_FORCE = 9681;
   private static final int CONTAINED_LIFE_FORCE = 9682;
   private static final int STINGER = 10012;

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = player.getQuestState(this.getName());
      if (qs == null) {
         qs = this.newQuestState(player);
      }

      if ("Badges".equalsIgnoreCase(event)) {
         switch(HellboundManager.getInstance().getLevel()) {
            case 2:
            case 3:
               if (qs.hasQuestItems(9674)) {
                  HellboundManager.getInstance().updateTrust((int)qs.getQuestItemsCount(9674) * 10, true);
                  qs.takeItems(9674, -1L);
                  return "32354-10.htm";
               }
            default:
               return "32354-10a.htm";
         }
      } else {
         if ("Bottle".equalsIgnoreCase(event)) {
            if (HellboundManager.getInstance().getLevel() >= 7) {
               if (qs.getQuestItemsCount(10012) >= 20L) {
                  qs.takeItems(10012, 20L);
                  qs.giveItems(9672, 1L);
                  return "32354-11h.htm";
               }

               return "32354-11i.htm";
            }
         } else if ("dlf".equalsIgnoreCase(event)) {
            if (HellboundManager.getInstance().getLevel() == 7) {
               if (qs.hasQuestItems(9680)) {
                  HellboundManager.getInstance().updateTrust((int)qs.getQuestItemsCount(9680) * 20, true);
                  qs.takeItems(9680, -1L);
                  return "32354-11a.htm";
               }

               return "32354-11b.htm";
            }
         } else if ("lf".equalsIgnoreCase(event)) {
            if (HellboundManager.getInstance().getLevel() == 7) {
               if (qs.hasQuestItems(9681)) {
                  HellboundManager.getInstance().updateTrust((int)qs.getQuestItemsCount(9681) * 80, true);
                  qs.takeItems(9681, -1L);
                  return "32354-11c.htm";
               }

               return "32354-11d.htm";
            }
         } else if ("clf".equalsIgnoreCase(event) && HellboundManager.getInstance().getLevel() == 7) {
            if (qs.hasQuestItems(9682)) {
               HellboundManager.getInstance().updateTrust((int)qs.getQuestItemsCount(9682) * 200, true);
               qs.takeItems(9682, -1L);
               return "32354-11e.htm";
            }

            return "32354-11f.htm";
         }

         return event;
      }
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      if (player.getQuestState(this.getName()) == null) {
         this.newQuestState(player);
      }

      switch(HellboundManager.getInstance().getLevel()) {
         case 1:
            return "32354-01.htm";
         case 2:
         case 3:
            return "32354-01a.htm";
         case 4:
            return "32354-01e.htm";
         case 5:
            return "32354-01d.htm";
         case 6:
            return "32354-01b.htm";
         case 7:
            return "32354-01c.htm";
         default:
            return "32354-01f.htm";
      }
   }

   public Kief(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(32354);
      this.addStartNpc(32354);
      this.addTalkId(32354);
   }

   public static void main(String[] args) {
      new Kief(-1, "Kief", "hellbound");
   }
}
