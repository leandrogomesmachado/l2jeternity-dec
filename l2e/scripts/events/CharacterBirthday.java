package l2e.scripts.events;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class CharacterBirthday extends Quest {
   private static int _spawns = 0;

   public CharacterBirthday(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(
         new int[]{32600, 30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964, 32163}
      );
      this.addTalkId(
         new int[]{32600, 30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964, 32163}
      );
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (event.equalsIgnoreCase("despawn_npc")) {
         npc.doDie(player);
         --_spawns;
         htmltext = null;
      } else if (event.equalsIgnoreCase("change")) {
         if (st.hasQuestItems(10250)) {
            st.takeItems(10250, 1L);
            st.giveItems(21594, 1L);
            htmltext = null;
            npc.doDie(player);
            --_spawns;
         } else {
            htmltext = "32600-nohat.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      if (_spawns >= 3) {
         return "busy.htm";
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            st = this.newQuestState(player);
         }

         if (!Util.checkIfInRange(80, npc, player, true)) {
            Npc spawned = st.addSpawn(32600, player.getX() + 10, player.getY() + 10, player.getZ() + 10, 0, false, 0, true);
            st.setState((byte)1);
            st.startQuestTimer("despawn_npc", 180000L, spawned);
            ++_spawns;
            return null;
         } else {
            return "tooclose.htm";
         }
      }
   }

   public static void main(String[] args) {
      new CharacterBirthday(-1, CharacterBirthday.class.getSimpleName(), "events");
   }
}
