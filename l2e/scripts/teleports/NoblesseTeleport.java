package l2e.scripts.teleports;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class NoblesseTeleport extends Quest {
   public NoblesseTeleport(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(
         new int[]{30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964, 32163}
      );
      this.addTalkId(
         new int[]{30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964, 32163}
      );
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("teleportWithToken")) {
            if (!st.hasQuestItems(13722)) {
               return "noble-nopass.htm";
            }

            npc.showChatWindow(player, 3);
         }

         return null;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         if (player.isNoble()) {
            htmltext = "nobleteleporter.htm";
         } else {
            htmltext = "nobleteleporter-no.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new NoblesseTeleport(-1, NoblesseTeleport.class.getSimpleName(), "teleports");
   }
}
