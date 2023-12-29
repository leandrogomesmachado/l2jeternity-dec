package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.editor.GeoEditorListener;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class GeoEditor implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_ge_status", "admin_ge_mode", "admin_ge_join", "admin_ge_leave"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (!Config.ACCEPT_GEOEDITOR_CONN) {
         activeChar.sendMessage("Server do not accepts geoeditor connections now.");
         return true;
      } else {
         if (command.startsWith("admin_ge_status")) {
            activeChar.sendMessage(GeoEditorListener.getInstance().getStatus());
         } else {
            if (command.startsWith("admin_ge_mode")) {
               if (GeoEditorListener.getInstance().getThread() == null) {
                  activeChar.sendMessage("Geoeditor not connected.");
                  return true;
               }

               try {
                  String val = command.substring("admin_ge_mode".length());
                  StringTokenizer st = new StringTokenizer(val);
                  if (st.countTokens() < 1) {
                     activeChar.sendMessage("Usage: //ge_mode X");
                     activeChar.sendMessage("Mode 0: Don't send coordinates to geoeditor.");
                     activeChar.sendMessage("Mode 1: Send coordinates at ValidatePosition from clients.");
                     activeChar.sendMessage("Mode 2: Send coordinates each second.");
                     return true;
                  }

                  int m = Integer.parseInt(st.nextToken());
                  GeoEditorListener.getInstance().getThread().setMode(m);
                  activeChar.sendMessage("Geoeditor connection mode set to " + m + ".");
               } catch (Exception var6) {
                  activeChar.sendMessage("Usage: //ge_mode X");
                  activeChar.sendMessage("Mode 0: Don't send coordinates to geoeditor.");
                  activeChar.sendMessage("Mode 1: Send coordinates at ValidatePosition from clients.");
                  activeChar.sendMessage("Mode 2: Send coordinates each second.");
                  var6.printStackTrace();
               }

               return true;
            }

            if (command.equals("admin_ge_join")) {
               if (GeoEditorListener.getInstance().getThread() == null) {
                  activeChar.sendMessage("Geoeditor not connected.");
                  return true;
               }

               GeoEditorListener.getInstance().getThread().addGM(activeChar);
               activeChar.sendMessage("You added to list for geoeditor.");
            } else if (command.equals("admin_ge_leave")) {
               if (GeoEditorListener.getInstance().getThread() == null) {
                  activeChar.sendMessage("Geoeditor not connected.");
                  return true;
               }

               GeoEditorListener.getInstance().getThread().removeGM(activeChar);
               activeChar.sendMessage("You removed from list for geoeditor.");
            }
         }

         return true;
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
