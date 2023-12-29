package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _634_InSearchOfFragmentsOfDimension extends Quest {
   public _634_InSearchOfFragmentsOfDimension(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int npcId = 31494; npcId < 31508; ++npcId) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }

      for(int mobs = 21208; mobs < 21256; ++mobs) {
         this.addKillId(mobs);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("05.htm")) {
            st.exitQuest(true, true);
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
               if (player.getLevel() < 20) {
                  htmltext = "01a.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "01.htm";
               }
               break;
            case 1:
               htmltext = "03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(1)) {
            st.calcDoDropItems(this.getId(), 7079, npc.getId(), Integer.MAX_VALUE);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _634_InSearchOfFragmentsOfDimension(634, _634_InSearchOfFragmentsOfDimension.class.getSimpleName(), "");
   }
}
