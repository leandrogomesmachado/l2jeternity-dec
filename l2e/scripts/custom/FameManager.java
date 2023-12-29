package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.UserInfo;
import l2e.scripts.ai.AbstractNpcAI;

public class FameManager extends AbstractNpcAI {
   private static final int[] FAME_MANAGER = new int[]{36479, 36480};
   private static final int MIN_LVL = 40;
   private static final int DECREASE_COST = 5000;
   private static final int REPUTATION_COST = 1000;
   private static final int MIN_CLAN_LVL = 5;
   private static final int CLASS_LVL = 2;

   private FameManager(String name, String descr) {
      super(name, descr);
      this.addStartNpc(FAME_MANAGER);
      this.addTalkId(FAME_MANAGER);
      this.addFirstTalkId(FAME_MANAGER);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "36479.htm":
         case "36479-02.htm":
         case "36479-07.htm":
         case "36480.htm":
         case "36480-02.htm":
         case "36480-07.htm":
            htmltext = event;
            break;
         case "decreasePk":
            if (player.getPkKills() > 0) {
               if (player.getFame() >= 5000 && player.getLevel() >= 40 && player.getClassId().level() >= 2) {
                  player.setFame(player.getFame() - 5000);
                  player.setPkKills(player.getPkKills() - 1);
                  player.sendPacket(new UserInfo(player));
                  htmltext = npc.getId() + "-06.htm";
               } else {
                  htmltext = npc.getId() + "-01.htm";
               }
            } else {
               htmltext = npc.getId() + "-05.htm";
            }
            break;
         case "clanRep":
            if (player.getClan() == null || player.getClan().getLevel() < 5) {
               htmltext = npc.getId() + "-03.htm";
            } else if (player.getFame() >= 1000 && player.getLevel() >= 40 && player.getClassId().level() >= 2) {
               player.setFame(player.getFame() - 1000);
               player.getClan().addReputationScore(50, true);
               player.sendPacket(new UserInfo(player));
               player.sendPacket(SystemMessageId.ACQUIRED_50_CLAN_FAME_POINTS);
               htmltext = npc.getId() + "-04.htm";
            } else {
               htmltext = npc.getId() + "-01.htm";
            }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return player.getFame() > 0 && player.getLevel() >= 40 && player.getClassId().level() >= 2 ? npc.getId() + ".htm" : npc.getId() + "-01.htm";
   }

   public static void main(String[] args) {
      new FameManager(FameManager.class.getSimpleName(), "custom");
   }
}
