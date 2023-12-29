package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.data.parser.ClassMasterParser;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.data.parser.EnchantItemGroupsParser;
import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.data.parser.FightEventMapParser;
import l2e.gameserver.data.parser.FightEventParser;
import l2e.gameserver.data.parser.ItemsParser;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PromoCodeParser;
import l2e.gameserver.data.parser.ReflectionParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.data.parser.TeleLocationParser;
import l2e.gameserver.data.parser.TransformParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.instancemanager.RewardManager;
import l2e.gameserver.instancemanager.WalkingManager;
import l2e.gameserver.listener.ScriptListenerLoader;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.model.FightEventManager;
import l2e.gameserver.model.entity.events.model.FightEventNpcManager;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Admin implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Admin.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_admin",
      "admin_admin1",
      "admin_admin2",
      "admin_admin3",
      "admin_admin4",
      "admin_admin5",
      "admin_admin6",
      "admin_admin7",
      "admin_html",
      "admin_gmliston",
      "admin_gmlistoff",
      "admin_silence",
      "admin_diet",
      "admin_tradeoff",
      "admin_reload"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      if (command.startsWith("admin_admin")) {
         this.showMainPage(activeChar, command);
      } else if (command.startsWith("admin_html")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("Usage: //html path");
            return false;
         }

         String path = st.nextToken();
         if (path != null) {
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/" + path);
            activeChar.sendPacket(adminhtm);
         }
      } else if (command.startsWith("admin_gmliston")) {
         AdminParser.getInstance().showGm(activeChar);
         activeChar.sendMessage("Registered into gm list");
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_gmlistoff")) {
         AdminParser.getInstance().hideGm(activeChar);
         activeChar.sendMessage("Removed from gm list");
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_silence")) {
         if (activeChar.isSilenceMode()) {
            activeChar.setSilenceMode(false);
            activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
         } else {
            activeChar.setSilenceMode(true);
            activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_diet")) {
         try {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            if (st.nextToken().equalsIgnoreCase("on")) {
               activeChar.setDietMode(true);
               activeChar.sendMessage("Diet mode on");
            } else if (st.nextToken().equalsIgnoreCase("off")) {
               activeChar.setDietMode(false);
               activeChar.sendMessage("Diet mode off");
            }
         } catch (Exception var15) {
            if (activeChar.getDietMode()) {
               activeChar.setDietMode(false);
               activeChar.sendMessage("Diet mode off");
            } else {
               activeChar.setDietMode(true);
               activeChar.sendMessage("Diet mode on");
            }
         } finally {
            activeChar.refreshOverloaded();
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_tradeoff")) {
         try {
            String mode = command.substring(15);
            if (mode.equalsIgnoreCase("on")) {
               activeChar.setVar("useBlockTrade@", "1");
               activeChar.sendMessage("Trade refusal enabled");
            } else if (mode.equalsIgnoreCase("off")) {
               activeChar.setVar("useBlockTrade@", "0");
               activeChar.sendMessage("Trade refusal disabled");
            }
         } catch (Exception var14) {
            if (activeChar.getTradeRefusal()) {
               activeChar.setVar("useBlockTrade@", "0");
               activeChar.sendMessage("Trade refusal disabled");
            } else {
               activeChar.setVar("useBlockTrade@", "1");
               activeChar.sendMessage("Trade refusal enabled");
            }
         }

         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      } else if (command.startsWith("admin_reload")) {
         StringTokenizer st = new StringTokenizer(command);
         st.nextToken();
         if (!st.hasMoreTokens()) {
            activeChar.sendMessage("You need to specify a type to reload!");
            activeChar.sendMessage("Usage: //reload <multisell|buylist|teleport|skill|npc|htm|item|config|access|quests|door|walker|handler>");
            return false;
         }

         String type = st.nextToken();

         try {
            if (type.equals("multisell")) {
               MultiSellParser.getInstance().load();
               activeChar.sendMessage("All Multisells have been reloaded");
            } else if (type.startsWith("buylist")) {
               BuyListParser.getInstance().load();
               activeChar.sendMessage("All BuyLists have been reloaded");
            } else if (type.startsWith("teleport")) {
               TeleLocationParser.getInstance().load();
               activeChar.sendMessage("Teleport Locations have been reloaded");
            } else if (type.startsWith("skill")) {
               SkillsParser.getInstance().reload();
               activeChar.sendMessage("All Skills have been reloaded");
            } else if (type.equals("npc")) {
               NpcsParser.getInstance();
               QuestManager.getInstance().reloadAllQuests();
               activeChar.sendMessage("All NPCs have been reloaded");
            } else if (type.startsWith("drop")) {
               NpcsParser.getInstance().reloadAllDropAndSkills();
               activeChar.sendMessage("All NPCs drop have been reloaded");
            } else if (type.startsWith("htm")) {
               HtmCache.getInstance().reload();
               activeChar.sendMessage(
                  "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded"
               );
            } else if (type.startsWith("item")) {
               ItemsParser.getInstance().reload();
               activeChar.sendMessage("Item Templates have been reloaded");
            } else if (type.startsWith("config")) {
               Config.load();
               ClassMasterParser.getInstance().reload();
               RewardManager.getInstance().reload();
               activeChar.sendMessage("All Config Settings have been reloaded");
            } else if (type.startsWith("access")) {
               AdminParser.getInstance().load();
               activeChar.sendMessage("Access Rights have been reloaded");
            } else if (type.startsWith("quests")) {
               QuestManager.getInstance().reloadAllQuests();
               activeChar.sendMessage("All Quests have been reloaded");
            } else if (type.startsWith("door")) {
               DoorParser.getInstance().load();
               activeChar.sendMessage("All Doors have been reloaded.");
            } else if (type.startsWith("walker")) {
               WalkingManager.getInstance().load();
               activeChar.sendMessage("All Walkers have been reloaded");
            } else if (type.startsWith("reflection")) {
               ReflectionParser.getInstance().load();
               activeChar.sendMessage("All Reflections have been reloaded.");
            } else if (type.startsWith("enchant")) {
               EnchantItemGroupsParser.getInstance().load();
               EnchantItemParser.getInstance().load();
               activeChar.sendMessage(activeChar.getName() + ": Reloaded item enchanting data.");
            } else if (type.startsWith("transform")) {
               TransformParser.getInstance().load();
               activeChar.sendMessage(activeChar.getName() + ": Reloaded transform data.");
            } else if (type.startsWith("scripts")) {
               ScriptListenerLoader.getInstance().executeScriptList();
               activeChar.sendMessage(activeChar.getName() + ": Reloaded all scripts.");
            } else if (type.startsWith("worldevents")) {
               for(Quest event : QuestManager.getInstance().getAllManagedScripts()) {
                  if (event instanceof AbstractWorldEvent) {
                     AbstractWorldEvent _event = (AbstractWorldEvent)QuestManager.getInstance().getQuest(event.getName());
                     if (_event != null && !_event.isEventActive() && _event.isReloaded()) {
                        activeChar.sendMessage(_event.getEventTemplate().getName() + ": Reloaded!");
                     }
                  }
               }
            } else if (type.startsWith("fightevents")) {
               for(AbstractFightEvent event : FightEventParser.getInstance().getEvents().valueCollection()) {
                  if (event != null && FightEventManager.getInstance().getActiveEventTask(event.getId())
                     || event.getState() == AbstractFightEvent.EVENT_STATE.STARTED) {
                     event.stopEvent();
                  }

                  FightEventManager.getInstance().clearEventIdTask(event.getId());
               }

               activeChar.sendMessage(activeChar.getName() + ": Disable all fight events.");
               FightEventParser.getInstance().reload();
               FightEventMapParser.getInstance().reload();
               FightEventNpcManager.getInstance().reload();
               FightEventManager.getInstance().reload();
               activeChar.sendMessage(activeChar.getName() + ": Reloaded all fight events.");
            } else if (type.startsWith("promocodes")) {
               PromoCodeParser.getInstance().reload();
               activeChar.sendMessage(activeChar.getName() + ": Reloaded all promocodes.");
            }
         } catch (Exception var13) {
            activeChar.sendMessage("An error occured while reloading " + type + " !");
            activeChar.sendMessage("Usage: //reload <multisell|buylist|teleport|skill|npc|htm|item|config|access|quests|door|walker|handler>");
            _log.log(java.util.logging.Level.WARNING, "An error occured while reloading " + type + ": " + var13, (Throwable)var13);
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void showMainPage(Player activeChar, String command) {
      NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
      int mode = 0;

      try {
         mode = Integer.parseInt(command.substring(11));
      } catch (Exception var6) {
      }

      switch(mode) {
         case 1:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/main_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 2:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/game_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 3:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/effects_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 4:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/server_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 5:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/mods_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 6:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/char_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         case 7:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
            activeChar.sendPacket(adminhtm);
            break;
         default:
            adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/main_menu.htm");
            activeChar.sendPacket(adminhtm);
      }
   }
}
