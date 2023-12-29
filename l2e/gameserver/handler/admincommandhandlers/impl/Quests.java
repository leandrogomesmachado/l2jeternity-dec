package l2e.gameserver.handler.admincommandhandlers.impl;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.script.ScriptException;
import l2e.commons.util.Util;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.listener.ScriptListenerLoader;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestTimer;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Quests implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_quest_reload", "admin_script_load", "admin_script_unload", "admin_show_quests", "admin_quest_info"
   };

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else {
         if (command.startsWith("admin_quest_reload")) {
            String[] parts = command.split(" ");
            if (parts.length < 2) {
               activeChar.sendMessage("Usage: //quest_reload <questFolder>.<questSubFolders...>.questName> or //quest_reload <id>");
            } else {
               try {
                  int questId = Integer.parseInt(parts[1]);
                  if (QuestManager.getInstance().reload(questId)) {
                     activeChar.sendMessage("Quest Reloaded Successfully.");
                  } else {
                     activeChar.sendMessage("Quest Reloaded Failed");
                  }
               } catch (NumberFormatException var18) {
                  if (QuestManager.getInstance().reload(parts[1])) {
                     activeChar.sendMessage("Quest Reloaded Successfully.");
                  } else {
                     activeChar.sendMessage("Quest Reloaded Failed");
                  }
               }
            }
         } else if (command.startsWith("admin_script_load")) {
            String[] parts = command.split(" ");
            if (parts.length < 2) {
               activeChar.sendMessage("Example: //script_load quests/SagasSuperclass/__init__.py");
            } else {
               File file = new File(ScriptListenerLoader.SCRIPT_FOLDER, parts[1]);
               if (!file.exists()) {
                  Quest quest = QuestManager.getInstance().getQuest(parts[1]);
                  if (quest != null) {
                     file = new File(ScriptListenerLoader.SCRIPT_FOLDER, quest.getClass().getName().replaceAll("\\.", "/") + ".java");
                  }
               }

               if (file.isFile()) {
                  try {
                     ScriptListenerLoader.getInstance().executeScript(file);
                     activeChar.sendMessage("Script Successfully Loaded.");
                  } catch (ScriptException var16) {
                     activeChar.sendMessage("Failed loading: " + parts[1]);
                     ScriptListenerLoader.getInstance().reportScriptFileError(file, var16);
                  } catch (Exception var17) {
                     activeChar.sendMessage("Failed loading: " + parts[1]);
                  }
               } else {
                  activeChar.sendMessage("File Not Found: " + parts[1]);
               }
            }
         } else if (command.startsWith("admin_script_unload")) {
            String[] parts = command.split(" ");
            if (parts.length < 2) {
               activeChar.sendMessage("Example: //script_unload questName/questId");
            } else {
               Quest q = Util.isDigit(parts[1])
                  ? QuestManager.getInstance().getQuest(Integer.parseInt(parts[1]))
                  : QuestManager.getInstance().getQuest(parts[1]);
               if (q != null) {
                  if (q.unload()) {
                     activeChar.sendMessage("Script Successfully Unloaded [" + q.getName() + "/" + q.getId() + "]");
                  } else {
                     activeChar.sendMessage("Failed unloading [" + q.getName() + "/" + q.getId() + "].");
                  }
               } else {
                  activeChar.sendMessage("The quest [" + parts[1] + "] was not found!.");
               }
            }
         } else if (command.startsWith("admin_show_quests")) {
            if (activeChar.getTarget() == null) {
               activeChar.sendMessage("Get a target first.");
            } else if (!activeChar.getTarget().isNpc()) {
               activeChar.sendMessage("Invalid Target.");
            } else {
               Npc npc = Npc.class.cast(activeChar.getTarget());
               NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId(), 1);
               msg.setFile(activeChar, activeChar.getLang(), "data/html/admin/npc-quests.htm");
               StringBuilder sb = new StringBuilder();
               Set<String> questset = new HashSet<>();

               for(Entry<Quest.QuestEventType, List<Quest>> quests : npc.getTemplate().getEventQuests().entrySet()) {
                  for(Quest quest : quests.getValue()) {
                     if (!questset.contains(quest.getName())) {
                        questset.add(quest.getName());
                        sb.append(
                           "<tr><td colspan=\"4\"><font color=\"LEVEL\"><a action=\"bypass -h admin_quest_info "
                              + quest.getName()
                              + "\">"
                              + quest.getName()
                              + "</a></font></td></tr>"
                        );
                     }
                  }
               }

               msg.replace("%quests%", sb.toString());
               msg.replace("%tmplid%", Integer.toString(npc.getTemplate().getId()));
               msg.replace("%questName%", "");
               activeChar.sendPacket(msg);
               questset.clear();
            }
         } else if (command.startsWith("admin_quest_info ")) {
            if (activeChar.getTarget() == null) {
               activeChar.sendMessage("Get a target first.");
            } else if (!activeChar.getTarget().isNpc()) {
               activeChar.sendMessage("Invalid Target.");
            } else {
               String questName = command.substring("admin_quest_info ".length());
               Quest quest = QuestManager.getInstance().getQuest(questName);
               if (quest == null) {
                  return false;
               }

               Npc npc = Npc.class.cast(activeChar.getTarget());
               StringBuilder sb = new StringBuilder();
               NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId(), 1);
               msg.setFile(activeChar, activeChar.getLang(), "data/html/admin/npc-quests.htm");
               String events = "";
               String npcs = "";
               String items = "";
               String timers = "";

               for(Quest.QuestEventType type : npc.getTemplate().getEventQuests().keySet()) {
                  events = events + ", " + type.toString();
               }

               events = events.substring(2);
               if (quest.getQuestInvolvedNpcs().size() < 100) {
                  for(int npcId : quest.getQuestInvolvedNpcs()) {
                     npcs = npcs + ", " + npcId;
                  }

                  npcs = npcs.substring(2);
               }

               if (quest.getRegisteredItemIds() != null) {
                  for(int itemId : quest.getRegisteredItemIds()) {
                     items = items + ", " + itemId;
                  }

                  items = items.substring(2);
               }

               for(List<QuestTimer> list : quest.getQuestTimers().values()) {
                  for(QuestTimer timer : list) {
                     timers = timers
                        + "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">"
                        + timer.getName()
                        + ":</font> <font color=00FF00>Active: "
                        + timer.getIsActive()
                        + " Repeatable: "
                        + timer.getIsRepeating()
                        + " Player: "
                        + timer.getPlayer()
                        + " Npc: "
                        + timer.getNpc()
                        + "</font></td></tr></table></td></tr>";
                  }
               }

               sb.append(
                  "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">ID:</font> <font color=00FF00>"
                     + quest.getId()
                     + "</font></td></tr></table></td></tr>"
               );
               sb.append(
                  "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Name:</font> <font color=00FF00>"
                     + quest.getName()
                     + "</font></td></tr></table></td></tr>"
               );
               sb.append(
                  "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Descr:</font> <font color=00FF00>"
                     + quest.getDescr(activeChar)
                     + "</font></td></tr></table></td></tr>"
               );
               sb.append(
                  "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Path:</font> <font color=00FF00>"
                     + quest.getClass().getName().substring(0, quest.getClass().getName().lastIndexOf(46)).replaceAll("\\.", "/")
                     + "</font></td></tr></table></td></tr>"
               );
               sb.append(
                  "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Events:</font> <font color=00FF00>"
                     + events
                     + "</font></td></tr></table></td></tr>"
               );
               if (!npcs.isEmpty()) {
                  sb.append(
                     "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">NPCs:</font> <font color=00FF00>"
                        + npcs
                        + "</font></td></tr></table></td></tr>"
                  );
               }

               if (!items.isEmpty()) {
                  sb.append(
                     "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Items:</font> <font color=00FF00>"
                        + items
                        + "</font></td></tr></table></td></tr>"
                  );
               }

               if (!timers.isEmpty()) {
                  sb.append(
                     "<tr><td colspan=\"4\"><table width=270 border=0 bgcolor=131210><tr><td width=270><font color=\"LEVEL\">Timers:</font> <font color=00FF00></font></td></tr></table></td></tr>"
                  );
                  sb.append(timers);
               }

               msg.replace("%quests%", sb.toString());
               msg.replace("%tmplid%", Integer.toString(npc.getId()));
               msg.replace(
                  "%questName%",
                  "<table><tr><td width=\"50\" align=\"left\"><a action=\"bypass -h admin_script_load "
                     + quest.getName()
                     + "\">Reload</a></td> <td width=\"150\"  align=\"center\"><a action=\"bypass -h admin_quest_info "
                     + quest.getName()
                     + "\">"
                     + quest.getName()
                     + "</a></td> <td width=\"50\" align=\"right\"><a action=\"bypass -h admin_script_unload "
                     + quest.getName()
                     + "\">Unload</a></tr></td></table>"
               );
               activeChar.sendPacket(msg);
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
