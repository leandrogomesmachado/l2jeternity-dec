package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.commons.util.Util;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.SoDManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class GraciaSeeds implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_gracia_seeds", "admin_opensod", "admin_defencesod", "admin_closesod", "admin_set_soistage"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      StringTokenizer st = new StringTokenizer(command, " ");
      String cmd = st.nextToken();
      String val = "";
      if (st.countTokens() >= 1) {
         val = st.nextToken();
      }

      if (cmd.equalsIgnoreCase("admin_defencesod")) {
         SoDManager.startDefenceStage(true);
      } else if (cmd.equalsIgnoreCase("admin_opensod")) {
         SoDManager.openSeed((long)(Integer.parseInt(val) * 60) * 1000L);
      } else if (cmd.equalsIgnoreCase("admin_closesod")) {
         Quest qs = QuestManager.getInstance().getQuest("SoDDefenceStage");
         if (qs != null) {
            qs.notifyEvent("EndDefence", null, null);
         } else {
            SoDManager.closeSeed();
         }
      } else if (cmd.equalsIgnoreCase("admin_set_soistage")) {
         SoIManager.setCurrentStage(Integer.parseInt(val));
      }

      this.showMenu(activeChar);
      return true;
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/gracia.htm");
      html.replace("%sodstate%", String.valueOf(SoDManager.isAttackStage() ? "1 (Attack)" : (SoDManager.isDefenceStage() ? "3 (Defence)" : "2 (Open)")));
      html.replace("%sodtiatkill%", String.valueOf(SoDManager.getTiatKills()));
      if (!SoDManager.isAttackStage()) {
         if (SoDManager.isDefenceStage()) {
            html.replace("%sodtime%", Util.formatTime((int)SoDManager.getDefenceStageTimeLimit() / 1000));
         } else {
            html.replace("%sodtime%", Util.formatTime((int)SoDManager.getOpenedTimeLimit() / 1000));
         }
      } else {
         html.replace("%sodtime%", "-1");
      }

      html.replace("%soistage%", (long)SoIManager.getCurrentStage());
      activeChar.sendPacket(html);
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
