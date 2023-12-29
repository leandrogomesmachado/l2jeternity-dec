package l2e.gameserver.model;

import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.model.stats.StatsSet;

public class AdminCommandAccessRight {
   private final String _adminCommand;
   private final int _accessLevel;
   private final boolean _requireConfirm;

   public AdminCommandAccessRight(StatsSet set) {
      this._adminCommand = set.getString("command");
      this._requireConfirm = set.getBool("confirmDlg", false);
      this._accessLevel = set.getInteger("accessLevel", 7);
   }

   public AdminCommandAccessRight(String command, boolean confirm, int level) {
      this._adminCommand = command;
      this._requireConfirm = confirm;
      this._accessLevel = level;
   }

   public String getAdminCommand() {
      return this._adminCommand;
   }

   public boolean hasAccess(AccessLevel characterAccessLevel) {
      AccessLevel accessLevel = AdminParser.getInstance().getAccessLevel(this._accessLevel);
      return accessLevel.getLevel() == characterAccessLevel.getLevel() || characterAccessLevel.hasChildAccess(accessLevel);
   }

   public boolean getRequireConfirm() {
      return this._requireConfirm;
   }
}
