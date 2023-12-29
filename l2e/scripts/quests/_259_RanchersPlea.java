package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _259_RanchersPlea extends Quest {
   private static final String qn = "_259_RanchersPlea";
   private static final int EDMOND = 30497;
   private static final int MARIUS = 30405;
   private static final int GIANT_SPIDER = 20103;
   private static final int TALON_SPIDER = 20106;
   private static final int BLADE_SPIDER = 20108;
   private static final int GIANT_SPIDER_SKIN = 1495;
   private static final int HEALING_POTION = 1061;
   private static final int WOODEN_ARROW = 17;

   public _259_RanchersPlea(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30497);
      this.addTalkId(30497);
      this.addTalkId(30405);
      this.addKillId(new int[]{20103, 20106, 20108});
      this.questItemIds = new int[]{1495};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_259_RanchersPlea");
      if (st == null) {
         return event;
      } else {
         int count = (int)st.getQuestItemsCount(1495);
         if (event.equalsIgnoreCase("30497-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30497-06.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30405-04.htm")) {
            if (count >= 10) {
               st.rewardItems(1061, 1L);
               st.takeItems(1495, 10L);
            } else {
               htmltext = "item_count.htm";
            }
         } else if (event.equalsIgnoreCase("30405-05.htm")) {
            if (count >= 10) {
               st.rewardItems(17, 50L);
               st.takeItems(1495, 10L);
            } else {
               htmltext = "item_count.htm";
            }
         } else if (event.equalsIgnoreCase("30405-07.htm") && count >= 10) {
            htmltext = "30405-06.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_259_RanchersPlea");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                  htmltext = "30497-02.htm";
               } else {
                  htmltext = "30497-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30405:
                     if (st.getQuestItemsCount(1495) < 10L) {
                        htmltext = "30405-01.htm";
                     } else {
                        htmltext = "30405-02.htm";
                     }
                     break;
                  case 30497:
                     int count = (int)st.getQuestItemsCount(1495);
                     if (count == 0) {
                        htmltext = "30497-04.htm";
                     } else {
                        htmltext = "30497-05.htm";
                        int amount = count * 25;
                        if (count > 9) {
                           amount += 250;
                        }

                        st.rewardItems(57, (long)amount);
                        st.takeItems(1495, -1L);
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_259_RanchersPlea");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            st.giveItems(1495, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _259_RanchersPlea(259, "_259_RanchersPlea", "");
   }
}
