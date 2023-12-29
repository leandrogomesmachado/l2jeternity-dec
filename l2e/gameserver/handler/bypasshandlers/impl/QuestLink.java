package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.quest.State;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class QuestLink implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Quest"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (target != null && target.isNpc()) {
         String quest = "";

         try {
            quest = command.substring(5).trim();
         } catch (IndexOutOfBoundsException var6) {
         }

         if (quest.length() == 0) {
            showQuestWindow(activeChar, (Npc)target);
         } else {
            showQuestWindow(activeChar, (Npc)target, quest);
         }

         return true;
      } else {
         return false;
      }
   }

   public static void showQuestChooseWindow(Player player, Npc npc, Quest[] quests) {
      StringBuilder sb = StringUtil.startAppend(150, "<html><body>");

      for(Quest q : quests) {
         StringUtil.append(sb, "<a action=\"bypass -h npc_", String.valueOf(npc.getObjectId()), "_Quest ", q.getName(), "\">[", q.getDescr(player));
         QuestState qs = player.getQuestState(q.getScriptName());
         if (qs != null) {
            if (qs.getState() == 1 && qs.getInt("cond") > 0) {
               sb.append(new ServerMessage("quest.progress", player.getLang()).toString());
            } else if (qs.getState() == 2) {
               sb.append(new ServerMessage("quest.done", player.getLang()).toString());
            }
         }

         sb.append("]</a><br>");
      }

      sb.append("</body></html>");
      npc.insertObjectIdAndShowChatWindow(player, sb.toString());
   }

   public static void showQuestWindow(Player player, Npc npc, String questId) {
      String content = null;
      Quest q = QuestManager.getInstance().getQuest(questId);
      QuestState qs = player.getQuestState(questId);
      if (q != null) {
         if (q.getId() >= 1 && q.getId() < 20000 && (player.getWeightPenalty() >= 3 || !player.isInventoryUnder90(true))) {
            player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
            return;
         }

         if (qs == null) {
            if (q.getId() >= 1 && q.getId() < 20000 && player.getAllActiveQuests().length > 40) {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, player.getLang(), "data/html/fullquest.htm");
               player.sendPacket(html);
               return;
            }

            List<Quest> qlst = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
            if (qlst != null && !qlst.isEmpty()) {
               for(Quest temp : qlst) {
                  if (temp == q) {
                     qs = q.newQuestState(player);
                     break;
                  }
               }
            }
         }
      } else {
         content = Quest.getNoQuestMsg(player);
      }

      if (qs != null) {
         if (!qs.getQuest().notifyTalk(npc, qs)) {
            return;
         }

         questId = qs.getQuest().getName();
         String stateId = State.getStateName(qs.getState());
         String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
         content = HtmCache.getInstance().getHtm(player, player.getLang(), path);
         if (Config.DEBUG) {
            if (content != null) {
               _log.fine("Showing quest window for quest " + questId + " html path: " + path);
            } else {
               _log.fine("File not exists for quest " + questId + " html path: " + path);
            }
         }
      }

      if (content != null) {
         npc.insertObjectIdAndShowChatWindow(player, content);
      }

      player.sendActionFailed();
   }

   public static void showQuestWindow(Player player, Npc npc) {
      List<Quest> options = new ArrayList<>();
      QuestState[] awaits = player.getQuestsForTalk(npc.getTemplate().getId());
      List<Quest> starts = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
      if (awaits != null) {
         for(QuestState x : awaits) {
            if (!options.contains(x.getQuest()) && x.getQuest().getId() > 0 && x.getQuest().getId() < 20000) {
               options.add(x.getQuest());
            }
         }
      }

      if (starts != null) {
         for(Quest x : starts) {
            if (!options.contains(x) && x.getId() > 0 && x.getId() < 20000) {
               options.add(x);
            }
         }
      }

      if (options.size() > 1) {
         showQuestChooseWindow(player, npc, options.toArray(new Quest[options.size()]));
      } else if (options.size() == 1) {
         showQuestWindow(player, npc, options.get(0).getName());
      } else {
         showQuestWindow(player, npc, "");
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
