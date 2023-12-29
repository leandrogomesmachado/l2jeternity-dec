package l2e.scripts.quests;

import java.util.StringTokenizer;
import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import org.apache.commons.lang3.ArrayUtils;

public class _455_WingsOfSand extends Quest {
   public _455_WingsOfSand(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870});
      this.addTalkId(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870});
      this.addKillId(new int[]{25718, 25719, 25720, 25721, 25722, 25723, 25724});
      this.questItemIds = new int[]{17250};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("sepsoul_q455_05.htm")) {
            st.startQuest();
         } else if (event.startsWith("sepsoul_q455_08.htm")) {
            st.takeItems(17250, -1L);
            StringTokenizer tokenizer = new StringTokenizer(event);
            tokenizer.nextToken();
            switch(Integer.parseInt(tokenizer.nextToken())) {
               case 1:
                  st.calcReward(this.getId(), 1, true);
                  break;
               case 2:
                  st.calcReward(this.getId(), 2, true);
                  break;
               case 3:
                  st.calcReward(this.getId(), 3, true);
                  break;
               case 4:
                  st.calcReward(this.getId(), 4, true);
            }

            htmltext = "sepsoul_q455_08.htm";
            st.exitQuest(QuestState.QuestType.DAILY, true);
         } else if (event.startsWith("sepsoul_q455_11.htm")) {
            st.takeItems(17250, -1L);
            StringTokenizer tokenizer = new StringTokenizer(event);
            tokenizer.nextToken();
            switch(Integer.parseInt(tokenizer.nextToken())) {
               case 1:
                  st.calcReward(this.getId(), 5, true);
                  break;
               case 2:
                  st.calcReward(this.getId(), 6, true);
                  break;
               case 3:
                  st.calcReward(this.getId(), 7, true);
                  break;
               case 4:
                  st.calcReward(this.getId(), 8, true);
            }

            if (Rnd.chance(25)) {
               st.calcReward(this.getId(), 9, true);
            }

            htmltext = "sepsoul_q455_11.htm";
            st.exitQuest(QuestState.QuestType.DAILY, true);
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
         if (ArrayUtils.contains(new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870}, npc.getId())) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 80) {
                     htmltext = "sepsoul_q455_01.htm";
                  } else {
                     htmltext = "sepsoul_q455_00.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(1)) {
                     htmltext = "sepsoul_q455_06.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "sepsoul_q455_07.htm";
                  } else if (st.isCond(3)) {
                     htmltext = "sepsoul_q455_10.htm";
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 80) {
                        htmltext = "sepsoul_q455_01.htm";
                     } else {
                        htmltext = "sepsoul_q455_00.htm";
                     }

                     st.setState((byte)0);
                  } else {
                     htmltext = "sepsoul_q455_00a.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (npcId == 25718 || npcId == 25719 || npcId == 25720 || npcId == 25721 || npcId == 25722 || npcId == 25723 || npcId == 25724) {
            if (player.getParty() != null) {
               for(Player partyMember : player.getParty().getMembers()) {
                  QuestState qs = partyMember.getQuestState(this.getName());
                  if (qs != null && qs.isCond(1) && qs.calcDropItems(this.getId(), 17250, npc.getId(), 1)) {
                     qs.setCond(2);
                  } else if (qs != null && qs.isCond(2) && qs.calcDropItems(this.getId(), 17250, npc.getId(), 2)) {
                     qs.setCond(3);
                  }
               }
            } else if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 17250, npc.getId(), 1)) {
               st.setCond(2);
            } else if (st != null && st.isCond(2) && st.calcDropItems(this.getId(), 17250, npc.getId(), 2)) {
               st.setCond(3);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _455_WingsOfSand(455, _455_WingsOfSand.class.getSimpleName(), "");
   }
}
