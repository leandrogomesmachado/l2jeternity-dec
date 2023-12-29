package l2e.scripts.teleports;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class StrongholdsTeleports extends Quest {
   public StrongholdsTeleports(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(new int[]{32163, 32181, 32184, 32186});
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      String htmltext = "";
      if (player.getLevel() < 20) {
         htmltext = npc.getId() + ".htm";
      } else {
         htmltext = npc.getId() + "-no.htm";
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new StrongholdsTeleports(-1, StrongholdsTeleports.class.getSimpleName(), "teleports");
   }
}
