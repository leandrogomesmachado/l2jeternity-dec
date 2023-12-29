package l2e.gameserver.model.actor.protection;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class AdminProtection extends ExtensionProtection {
   private boolean _safeadmin = false;
   private String _adminConfirmCmd = null;
   private boolean _inCameraMode = false;

   public AdminProtection(Player activeChar) {
      super(activeChar);
   }

   public void setIsSafeAdmin(boolean b) {
      this._safeadmin = b;
   }

   public boolean isSafeAdmin() {
      return this._safeadmin;
   }

   public boolean canUseAdminCommand() {
      if (Config.ENABLE_SAFE_ADMIN_PROTECTION && !this.getPlayer().getAdminProtection().isSafeAdmin()) {
         _log.warning("Character " + this.getPlayer().getName() + "(" + this.getPlayer().getObjectId() + ") tryed to use an admin command.");
         this.punishUnSafeAdmin();
         return false;
      } else {
         return true;
      }
   }

   public void punishUnSafeAdmin() {
      if (this.getPlayer() != null) {
         this.getPlayer().setAccessLevel(0);
         Util.handleIllegalPlayerAction(this.getPlayer(), "" + this.getPlayer().getName() + " not allowed to be a GM!");
      }
   }

   public String getAdminConfirmCmd() {
      return this._adminConfirmCmd;
   }

   public void setAdminConfirmCmd(String adminConfirmCmd) {
      this._adminConfirmCmd = adminConfirmCmd;
   }

   public void setCameraMode(boolean val) {
      this._inCameraMode = val;
   }

   public boolean inCameraMode() {
      return this._inCameraMode;
   }
}
