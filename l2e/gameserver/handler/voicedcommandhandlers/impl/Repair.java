package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Repair implements IVoicedCommandHandler {
   static final Logger _log = Logger.getLogger(Repair.class.getName());
   private static final String[] _voicedCommands = new String[]{"repair", "startrepair"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (Config.ALLOW_REPAIR_COMMAND && activeChar != null) {
         String repairChar = null;

         try {
            if (target != null && target.length() > 1) {
               String[] cmdParams = target.split(" ");
               repairChar = cmdParams[0];
            }
         } catch (Exception var6) {
            repairChar = null;
         }

         if (command.startsWith("repair")) {
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/repair/repair.htm");
            html.replace("%acc_chars%", this.getCharList(activeChar));
            activeChar.sendPacket(html);
            return true;
         } else if (!command.startsWith("startrepair") || repairChar == null) {
            return false;
         } else if (!this.checkAcc(activeChar, repairChar)) {
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/repair/repair-error.htm");
            activeChar.sendPacket(html);
            return false;
         } else if (this.checkChar(activeChar, repairChar)) {
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/repair/repair-self.htm");
            activeChar.sendPacket(html);
            return false;
         } else {
            this.repairBadCharacter(repairChar);
            NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
            html.setFile(activeChar, activeChar.getLang(), "data/html/mods/repair/repair-done.htm");
            activeChar.sendPacket(html);
            return true;
         }
      } else {
         return false;
      }
   }

   private String getCharList(Player activeChar) {
      String result = "";
      String repCharAcc = activeChar.getAccountName();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters WHERE account_name=?");
         statement.setString(1, repCharAcc);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            if (activeChar.getName().compareTo(rset.getString(1)) != 0) {
               result = result + rset.getString(1) + ";";
            }
         }

         rset.close();
         statement.close();
         return result;
      } catch (Exception var18) {
         var18.printStackTrace();
         return result;
      }
   }

   private boolean checkAcc(Player activeChar, String repairChar) {
      boolean result = false;
      String repCharAcc = "";

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
         statement.setString(1, repairChar);
         ResultSet rset = statement.executeQuery();
         if (rset.next()) {
            repCharAcc = rset.getString(1);
         }

         rset.close();
         statement.close();
      } catch (Exception var19) {
         var19.printStackTrace();
         return result;
      }

      if (activeChar.getAccountName().compareTo(repCharAcc) == 0) {
         result = true;
      }

      return result;
   }

   private boolean checkChar(Player activeChar, String repairChar) {
      boolean result = false;
      if (activeChar.getName().compareTo(repairChar) == 0) {
         result = true;
      }

      return result;
   }

   private void repairBadCharacter(String charName) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT charId FROM characters WHERE char_name=?");
         statement.setString(1, charName);
         ResultSet rset = statement.executeQuery();
         int objId = 0;
         if (rset.next()) {
            objId = rset.getInt(1);
         }

         rset.close();
         statement.close();
         if (objId == 0) {
            return;
         }

         statement = con.prepareStatement("UPDATE characters SET x=17867, y=170259, z=-3503 WHERE charId=?");
         statement.setInt(1, objId);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
         statement.setInt(1, objId);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("UPDATE items SET loc=\"WAREHOUSE\" WHERE owner_id=? AND loc=\"PAPERDOLL\"");
         statement.setInt(1, objId);
         statement.execute();
         statement.close();
      } catch (Exception var19) {
         _log.warning("GameServer: could not repair character:" + var19);
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
