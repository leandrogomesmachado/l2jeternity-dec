package l2e.scripts.quests;

import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class _024_InhabitantsOfTheForrestOfTheDead extends Quest {
   public _024_InhabitantsOfTheForrestOfTheDead(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31389);
      this.addTalkId(31389);
      this.addTalkId(31531);
      this.addTalkId(31532);
      this.addTalkId(31522);
      this.addAggroRangeEnterId(new int[]{25332});
      this.addKillId(new int[]{21557, 21558, 21560, 21563, 21564, 21565, 21566, 21567});
      this.questItemIds = new int[]{7065, 7148, 7152, 7153, 7154, 7151};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31389-02.htm")) {
            st.giveItems(7152, 1L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31389-16.htm")) {
            st.playSound("InterfaceSound.charstat_open_01");
         } else if (event.equalsIgnoreCase("31389-17.htm")) {
            st.takeItems(7154, -1L);
            st.giveItems(7148, 1L);
            st.setCond(5, false);
         } else if (event.equalsIgnoreCase("31522-03.htm")) {
            st.takeItems(7151, -1L);
         } else if (event.equalsIgnoreCase("31522-07.htm")) {
            st.setCond(11, false);
         } else if (event.equalsIgnoreCase("31522-19.htm")) {
            st.giveItems(7156, 1L);
            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
         } else if (event.equalsIgnoreCase("31531-02.htm")) {
            st.setCond(2, true);
            st.takeItems(7152, -1L);
         } else if (event.equalsIgnoreCase("31532-04.htm")) {
            st.giveItems(7065, 1L);
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("31532-06.htm")) {
            st.takeItems(7148, -1L);
            st.takeItems(7065, -1L);
         } else if (event.equalsIgnoreCase("31532-16.htm")) {
            st.setCond(9, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte state = st.getState();
         int cond = st.getCond();
         if (state == 2) {
            if (npcId == 31522) {
               htmltext = "31522-20.htm";
            } else {
               htmltext = getAlreadyCompletedMsg(player);
            }
         }

         if (npcId == 31389) {
            if (state == 0) {
               QuestState st2 = player.getQuestState("_023_LidiasHeart");
               if (st2 != null) {
                  if (st2.getState() == 2 && player.getLevel() >= 65) {
                     htmltext = "31389-01.htm";
                  } else {
                     htmltext = "31389-00.htm";
                  }
               } else {
                  htmltext = "31389-00.htm";
               }
            } else if (cond == 1) {
               htmltext = "31389-03.htm";
            } else if (cond == 2) {
               htmltext = "31389-04.htm";
            } else if (cond == 3) {
               htmltext = "31389-12.htm";
            } else if (cond == 4) {
               htmltext = "31389-13.htm";
            } else if (cond == 5) {
               htmltext = "31389-18.htm";
            }
         } else if (npcId == 31531) {
            if (cond == 1) {
               st.playSound("AmdSound.d_wind_loot_02");
               htmltext = "31531-01.htm";
            } else if (cond == 2) {
               htmltext = "31531-03.htm";
            }
         } else if (npcId == 31532) {
            if (cond == 5) {
               htmltext = "31532-01.htm";
            } else if (cond == 6) {
               if (st.getQuestItemsCount(7065) >= 1L && st.getQuestItemsCount(7148) >= 1L) {
                  htmltext = "31532-05.htm";
               } else {
                  htmltext = "31532-07.htm";
               }
            } else if (cond == 9) {
               htmltext = "31532-16.htm";
            }
         } else if (npcId == 31522) {
            if (cond == 10) {
               htmltext = "31522-01.htm";
            } else if (cond == 11) {
               htmltext = "31522-08.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (npc.getId() == 25332) {
         if (isSummon) {
            ((Attackable)npc).getAggroList().remove(player.getSummon());
         } else {
            ((Attackable)npc).getAggroList().remove(player);
            QuestState st = player.getQuestState(this.getName());
            if (st != null && st.getQuestItemsCount(7153) >= 1L) {
               st.takeItems(7153, -1L);
               st.giveItems(7154, 1L);
               st.setCond(4, true);
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NpcStringId.THAT_SIGN), 2000);
            }
         }
      }

      return null;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (st.getQuestItemsCount(7151) == 0L && st.getCond() == 9 && st.calcDropItems(this.getId(), 7151, npc.getId(), 1)) {
            st.setCond(10);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _024_InhabitantsOfTheForrestOfTheDead(24, _024_InhabitantsOfTheForrestOfTheDead.class.getSimpleName(), "");
   }
}
