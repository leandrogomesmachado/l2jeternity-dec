package l2e.scripts.vehicles;

import l2e.gameserver.instancemanager.AirShipManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class EngineerLekon extends Quest {
   private static final int LEKON = 32557;
   private static final int LICENSE = 13559;
   private static final int STARSTONE = 13277;
   private static final int LICENSE_COST = 10;
   private static final SystemMessage SM_NEED_CLANLVL5 = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NEED_CLANLVL_5_TO_SUMMON);
   private static final SystemMessage SM_NO_PRIVS = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_NO_PRIVILEGES);
   private static final SystemMessage SM_LICENSE_ALREADY_ACQUIRED = SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_SUMMON_LICENSE_ALREADY_ACQUIRED);

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if ("license".equalsIgnoreCase(event)) {
         if (player.getClan() == null || player.getClan().getLevel() < 5) {
            player.sendPacket(SM_NEED_CLANLVL5);
            return null;
         } else if (!player.isClanLeader()) {
            player.sendPacket(SM_NO_PRIVS);
            return null;
         } else if (AirShipManager.getInstance().hasAirShipLicense(player.getId())) {
            player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
            return null;
         } else if (player.getInventory().getItemByItemId(13559) != null) {
            player.sendPacket(SM_LICENSE_ALREADY_ACQUIRED);
            return null;
         } else if (!player.destroyItemByItemId("AirShipLicense", 13277, 10L, npc, true)) {
            return null;
         } else {
            player.addItem("AirShipLicense", 13559, 1L, npc, true);
            return null;
         }
      } else {
         return event;
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return npc.getId() + ".htm";
   }

   public EngineerLekon(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32557);
      this.addFirstTalkId(32557);
      this.addTalkId(32557);
   }

   public static void main(String[] args) {
      new EngineerLekon(-1, EngineerLekon.class.getSimpleName(), "vehicles");
   }
}
