package l2e.scripts.custom;

import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;

public class IOPRace extends Quest {
   private static final int RIGNOS = 32349;
   private static final int STAMP = 10013;
   private static final int KEY = 9694;
   private int _player = -1;

   public IOPRace(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32349);
      this.addTalkId(32349);
      this.addFirstTalkId(32349);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (player.getLevel() < 78) {
         return "32349-notavailable.htm";
      } else if (this._player != -1 && this._player == player.getObjectId() && st.getQuestItemsCount(10013) == 4L) {
         return "32349-return.htm";
      } else if (this._player != -1) {
         return "32349-notavailable.htm";
      } else {
         npc.showChatWindow(player);
         return null;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (this._player == -1) {
         player.stopSkillEffects(5239);
         if (player.hasSummon()) {
            player.getSummon().stopSkillEffects(5239);
         }

         st.takeItems(10013, -1L);
         st.set("1st", "0");
         st.set("2nd", "0");
         st.set("3rd", "0");
         st.set("4th", "0");
         Skill skill = SkillsParser.getInstance().getInfo(5239, 5);
         if (skill != null) {
            skill.getEffects(npc, player, false);
            if (player.hasSummon()) {
               skill.getEffects(npc, player.getSummon(), false);
            }
         }

         this.startQuestTimer("timer", 1800000L, null, null);
         this._player = player.getObjectId();
      }

      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = "";
      if (event.equalsIgnoreCase("timer")) {
         this._player = -1;
         return null;
      } else {
         if (event.equalsIgnoreCase("finish") && this._player == player.getObjectId()) {
            QuestState st = player.getQuestState(this.getName());
            st.giveItems(9694, 3L);
            st.takeItems(10013, -1L);
            st.exitQuest(true);
         }

         return "";
      }
   }

   public static void main(String[] args) {
      new IOPRace(-1, "IOPRace", "custom");
   }
}
