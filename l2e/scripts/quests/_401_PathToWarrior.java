package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _401_PathToWarrior extends Quest {
   private static final int AURONSLETTER = 1138;
   private static final int WARRIORGUILDMARK = 1139;
   private static final int RUSTEDBRONZESWORD1 = 1140;
   private static final int RUSTEDBRONZESWORD2 = 1141;
   private static final int RUSTEDBRONZESWORD3 = 1142;
   private static final int SIMPLONSLETTER = 1143;
   private static final int POISONSPIDERLEG = 1144;
   private static final int MEDALLIONOFWARRIOR = 1145;
   private static final int AURON = 30010;
   private static final int SIMPLON = 30253;
   private static final int[] MONSTERS = new int[]{20035, 20038, 20042, 20043};

   public _401_PathToWarrior(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30010);
      this.addTalkId(30010);
      this.addTalkId(30253);

      for(int i : MONSTERS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{1138, 1139, 1140, 1141, 1142, 1143, 1144};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("401_1")) {
            switch(player.getClassId()) {
               case fighter:
                  if (player.getLevel() >= 18) {
                     if (st.getQuestItemsCount(1145) == 1L) {
                        htmltext = "30010-04.htm";
                     } else {
                        htmltext = "30010-05.htm";
                     }
                  } else {
                     htmltext = "30010-02.htm";
                  }
                  break;
               case warrior:
                  htmltext = "30010-03.htm";
                  break;
               default:
                  htmltext = "30010-02b.htm";
            }
         } else if (event.equalsIgnoreCase("401_accept")) {
            st.startQuest();
            st.giveItems(1138, 1L);
            htmltext = "30010-06.htm";
         } else if (event.equalsIgnoreCase("30253_1")) {
            st.setCond(2, true);
            st.takeItems(1138, 1L);
            st.giveItems(1139, 1L);
            htmltext = "30253-02.html";
         } else if (event.equalsIgnoreCase("401_2")) {
            htmltext = "30010-10.html";
         } else if (event.equalsIgnoreCase("401_3")) {
            st.setCond(5, true);
            st.takeItems(1141, 1L);
            st.giveItems(1142, 1L);
            st.takeItems(1143, 1L);
            htmltext = "30010-11.html";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(npc.getId()) {
            case 30010:
               switch(st.getCond()) {
                  case 0:
                     return "30010-01.htm";
                  case 1:
                     return "30010-07.html";
                  case 2:
                  case 3:
                     return "30010-08.html";
                  case 4:
                     return "30010-09.html";
                  case 5:
                     return "30010-12.html";
                  case 6:
                     st.takeItems(1142, 1L);
                     st.takeItems(1144, -1L);
                     if (player.getLevel() >= 20) {
                        st.addExpAndSp(320534, 21012);
                     } else if (player.getLevel() == 19) {
                        st.addExpAndSp(456128, 27710);
                     } else {
                        st.addExpAndSp(160267, 34408);
                     }

                     st.rewardItems(57, 163800L);
                     st.giveItems(1145, 1L);
                     player.sendPacket(new SocialAction(player.getObjectId(), 3));
                     st.saveGlobalQuestVar("1ClassQuestFinished", "1");
                     st.exitQuest(false, true);
                     htmltext = "30010-13.html";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 30253:
               switch(st.getCond()) {
                  case 1:
                     htmltext = "30253-01.html";
                     break;
                  case 2:
                     htmltext = "30253-03.html";
                     break;
                  case 3:
                     st.setCond(4, true);
                     st.takeItems(1139, 1L);
                     st.takeItems(1140, 10L);
                     st.giveItems(1141, 1L);
                     st.giveItems(1143, 1L);
                     htmltext = "30253-04.html";
                     break;
                  case 4:
                     htmltext = "30253-05.html";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         switch(st.getCond()) {
            case 2:
               if (npc.getId() == MONSTERS[0] || npc.getId() == MONSTERS[2]) {
                  if (st.getQuestItemsCount(1140) < 10L && getRandom(10) < 4) {
                     st.giveItems(1140, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }

                  if (st.getQuestItemsCount(1140) == 10L) {
                     st.setCond(3, true);
                  }
               }
               break;
            case 5:
               if (st.getItemEquipped(5) == 1142 && (npc.getId() == MONSTERS[1] || npc.getId() == MONSTERS[3])) {
                  if (st.getQuestItemsCount(1144) < 20L) {
                     st.giveItems(1144, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }

                  if (st.getQuestItemsCount(1144) == 20L) {
                     st.setCond(6, true);
                  }
               }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _401_PathToWarrior(401, _401_PathToWarrior.class.getSimpleName(), "");
   }
}
