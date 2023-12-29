package l2e.scripts.quests;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10273_GoodDayToFly extends Quest {
   public _10273_GoodDayToFly(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32557);
      this.addTalkId(32557);
      this.addKillId(new int[]{22614, 22615});
      this.questItemIds = new int[]{13856};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "32557-06.htm":
               st.startQuest();
               break;
            case "32557-09.htm":
               st.set("transform", "1");
               SkillsParser.getInstance().getInfo(5982, 1).getEffects(player, player, false);
               break;
            case "32557-10.htm":
               st.set("transform", "2");
               SkillsParser.getInstance().getInfo(5983, 1).getEffects(player, player, false);
               break;
            case "32557-13.htm":
               if (st.getInt("transform") == 1) {
                  SkillsParser.getInstance().getInfo(5982, 1).getEffects(player, player, false);
               } else if (st.getInt("transform") == 2) {
                  SkillsParser.getInstance().getInfo(5983, 1).getEffects(player, player, false);
               }
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
         int transform = st.getInt("transform");
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() < 75 ? "32557-00.htm" : "32557-01.htm";
               break;
            case 2:
               htmltext = "32557-0a.htm";
               break;
            default:
               if (st.getQuestItemsCount(13856) >= 5L) {
                  htmltext = "32557-14.htm";
                  st.calcExpAndSp(this.getId());
                  if (transform == 1) {
                     st.calcReward(this.getId(), 1);
                  } else if (transform == 2) {
                     st.calcReward(this.getId(), 2);
                  }

                  st.exitQuest(false, true);
               } else if (transform == 0) {
                  htmltext = "32557-07.htm";
               } else {
                  htmltext = "32557-11.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(killer, 1);
      if (partyMember == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 13856, npc.getId(), 5)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10273_GoodDayToFly(10273, _10273_GoodDayToFly.class.getSimpleName(), "");
   }
}
