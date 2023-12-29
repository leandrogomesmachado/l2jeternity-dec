package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class _641_AttackSailren extends Quest {
   public static String qn = "_641_AttackSailren";
   public static int _statue = 32109;
   public static int[] _mobs = new int[]{22196, 22197, 22198, 22218, 22223, 22199};
   public static int GAZKH_FRAGMENT = 8782;
   public static int GAZKH = 8784;
   public static int DROP_CHANCE = 400;

   public _641_AttackSailren(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(_statue);
      this.addTalkId(_statue);

      for(int npcId : _mobs) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{GAZKH_FRAGMENT};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32109-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32109-05.htm")) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
            sm.addString("Shilen's Protection");
            player.sendPacket(sm);
            st.takeItems(GAZKH_FRAGMENT, -1L);
            st.giveItems(GAZKH, 1L);
            st.set("cond", "3");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_126_TheNameOfEvil2");
               if (qs != null && qs.isCompleted()) {
                  if (player.getLevel() >= 77) {
                     htmltext = "32109-01.htm";
                  } else {
                     htmltext = "32109-01a.htm";
                  }
               } else {
                  htmltext = "32109-00.htm";
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "32109-03.htm";
               } else if (cond == 2) {
                  htmltext = "32109-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(qn);
         if (st == null) {
            return null;
         } else {
            int id = st.getState();
            int cond = st.getInt("cond");
            if (id == 1) {
               long count = st.getQuestItemsCount(GAZKH_FRAGMENT);
               if (cond == 1) {
                  int chance = (int)((float)DROP_CHANCE * Config.RATE_QUEST_DROP);
                  int numItems = chance / 1000;
                  chance %= 1000;
                  if (getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems > 0) {
                     if ((count + (long)numItems) / 30L > count / 30L) {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "2");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                        st.giveItems(GAZKH_FRAGMENT, (long)numItems);
                     }
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _641_AttackSailren(641, qn, "");
   }
}
