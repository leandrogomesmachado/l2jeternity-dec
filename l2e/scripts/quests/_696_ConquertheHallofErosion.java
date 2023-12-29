package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _696_ConquertheHallofErosion extends Quest {
   public _696_ConquertheHallofErosion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32603);
      this.addTalkId(32603);
      this.addKillId(25634);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32603-02.htm")) {
            st.startQuest();
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
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId())) {
                  if (st.getQuestItemsCount(13691) <= 0L && st.getQuestItemsCount(13692) <= 0L) {
                     htmltext = "32603-05.htm";
                     st.exitQuest(true);
                  } else {
                     htmltext = "32603-01.htm";
                  }
               } else {
                  htmltext = "32603-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getInt("cohemenesDone") != 0) {
                  if (st.getQuestItemsCount(13692) < 1L) {
                     st.takeItems(13691, 1L);
                     st.calcReward(this.getId());
                  }

                  htmltext = "32603-04.htm";
                  st.exitQuest(true, true);
               } else {
                  htmltext = "32603-01a.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else {
            if (st.isCond(1)) {
               st.set("cohemenesDone", 1);
            }

            if (player.getParty() != null) {
               for(Player pmember : player.getParty().getMembers()) {
                  QuestState st2 = pmember.getQuestState(this.getName());
                  if (st2 != null && st2.isCond(1) && pmember.getObjectId() != partyMember.getObjectId()) {
                     st.set("cohemenesDone", 1);
                  }
               }
            }

            return super.onKill(npc, player, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _696_ConquertheHallofErosion(696, _696_ConquertheHallofErosion.class.getSimpleName(), "");
   }
}
