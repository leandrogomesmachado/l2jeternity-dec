package l2e.scripts.teleports;

import java.util.HashMap;
import java.util.Map;
import l2e.commons.util.Util;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;

public class NewbieTravelToken extends Quest {
   private static final Map<Integer, Location> DATA = new HashMap<>();

   public NewbieTravelToken(int questId, String name, String descr) {
      super(questId, name, descr);
      DATA.put(30600, new Location(12160, 16554, -4583));
      DATA.put(30601, new Location(115594, -177993, -912));
      DATA.put(30599, new Location(45470, 48328, -3059));
      DATA.put(30602, new Location(-45067, -113563, -199));
      DATA.put(30598, new Location(-84053, 243343, -3729));
      DATA.put(32135, new Location(-119712, 44519, 368));

      for(int npcId : DATA.keySet()) {
         this.addStartNpc(npcId);
         this.addTalkId(npcId);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (Util.isDigit(event)) {
         int npcId = Integer.parseInt(event);
         if (DATA.keySet().contains(npcId)) {
            if (BotFunctions.getInstance().isAutoTpByIdEnable(player)) {
               BotFunctions.getInstance().getAutoTeleportById(player, player.getLocation(), DATA.get(npcId), 1000);
               return null;
            }

            player.teleToLocation(DATA.get(npcId), false);
            return super.onAdvEvent(event, npc, player);
         }
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st != null) {
         if (player.getLevel() >= 20) {
            htmltext = "cant-travel.htm";
            st.exitQuest(true);
         } else {
            htmltext = npc.getId() + ".htm";
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new NewbieTravelToken(-1, NewbieTravelToken.class.getSimpleName(), "teleports");
   }
}
