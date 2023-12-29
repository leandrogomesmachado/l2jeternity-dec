package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10289_FadeToBlack extends Quest {
   public _10289_FadeToBlack(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32757);
      this.addTalkId(32757);
      this.addKillId(25701);
      this.registerQuestItems(new int[]{15527, 15528});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32757) {
            if (event.equalsIgnoreCase("32757-04.htm")) {
               st.startQuest();
            } else if (this.isNumber(event) && st.getQuestItemsCount(15527) > 0L) {
               st.takeItems(15527, 1L);
               st.calcReward(this.getId(), Integer.parseInt(event));
               st.exitQuest(true, true);
               htmltext = "32757-08.htm";
            }
         }

         return htmltext;
      }
   }

   private boolean isNumber(String str) {
      if (str != null && str.length() != 0) {
         for(int i = 0; i < str.length(); ++i) {
            if (!Character.isDigit(str.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      QuestState secretMission = player.getQuestState("_10288_SecretMission");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32757) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82 && secretMission != null && secretMission.getState() == 2) {
                     htmltext = "32757-02.htm";
                  } else if (player.getLevel() < 82) {
                     htmltext = "32757-00.htm";
                  } else {
                     htmltext = "32757-01.htm";
                  }
                  break;
               case 1:
                  if (st.isCond(1)) {
                     htmltext = "32757-04b.htm";
                  }

                  if (st.isCond(2)) {
                     htmltext = "32757-05.htm";
                     st.calcExpAndSp(this.getId());
                     st.setCond(1, true);
                  } else if (st.isCond(3)) {
                     htmltext = "32757-06.htm";
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
         if (player.isInParty()) {
            Player member = this.getRandomPartyMember(player, 1);
            QuestState st1 = member.getQuestState(this.getName());
            if (st1.calcDropItems(this.getId(), 15527, npc.getId(), 1)) {
               st1.setCond(3, true);
            }

            int rnd = getRandom(member.getParty().getMemberCount());
            int idx = 0;

            for(Player pl : member.getParty().getMembers()) {
               QuestState st2 = pl.getQuestState(this.getName());
               if (pl.getObjectId() != member.getObjectId() && st2.calcDropItems(this.getId(), idx == rnd ? 15527 : 15528, npc.getId(), 1)) {
                  st2.setCond(idx == rnd ? 3 : 2, true);
               }

               ++idx;
            }
         } else if (st.calcDropItems(this.getId(), 15527, npc.getId(), 1)) {
            st.setCond(3, true);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _10289_FadeToBlack(10289, _10289_FadeToBlack.class.getSimpleName(), "");
   }
}
