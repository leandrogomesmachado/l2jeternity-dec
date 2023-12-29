package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.List;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class ChatLink implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"Chat"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!target.isNpc()) {
         return false;
      } else {
         int val = 0;

         try {
            val = Integer.parseInt(command.substring(5));
         } catch (Exception var7) {
         }

         Npc npc = (Npc)target;
         List<Quest> firstTalk = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
         if (val == 0 && firstTalk != null && firstTalk.size() == 1) {
            firstTalk.get(0).notifyFirstTalk(npc, activeChar);
         } else {
            npc.showChatWindow(activeChar, val);
         }

         return false;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
