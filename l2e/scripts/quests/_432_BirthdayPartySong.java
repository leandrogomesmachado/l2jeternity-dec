package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _432_BirthdayPartySong extends Quest {
   private static final int OCTAVIA = 31043;
   private static final int GOLEM = 21103;
   private static final int RED_CRYSTAL = 7541;
   private static final int ECHO_CRYSTAL = 7061;

   public _432_BirthdayPartySong(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31043);
      this.addTalkId(31043);
      this.addKillId(21103);
      this.registerQuestItems(new int[]{7541});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "31043-02.htm":
               st.startQuest();
               break;
            case "31043-05.htm":
               if (st.getQuestItemsCount(7541) < 50L) {
                  return "31043-06.htm";
               }

               st.giveItems(7061, 25L);
               st.exitQuest(true, true);
               break;
            default:
               htmltext = null;
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
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 31 ? "31043-01.htm" : "31043-00.htm";
               break;
            case 1:
               htmltext = st.isCond(1) ? "31043-03.htm" : "31043-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isCond(1) && getRandomBoolean()) {
         st.giveItems(7541, 1L);
         if (st.getQuestItemsCount(7541) == 50L) {
            st.setCond(2, true);
         } else {
            st.playSound("ItemSound.quest_itemget");
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _432_BirthdayPartySong(432, _432_BirthdayPartySong.class.getSimpleName(), "");
   }
}
