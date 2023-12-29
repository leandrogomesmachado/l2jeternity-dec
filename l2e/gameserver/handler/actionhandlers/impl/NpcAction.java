package l2e.gameserver.handler.actionhandlers.impl;

import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class NpcAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      Npc npc = (Npc)target;
      if (!npc.canTarget(activeChar)) {
         return false;
      } else {
         activeChar.setLastFolkNPC(npc);
         if (npc != activeChar.getTarget()) {
            activeChar.setTarget(target);
            activeChar.sendPacket(new ValidateLocation(npc));
            return false;
         } else {
            if (interact) {
               activeChar.sendPacket(new ValidateLocation(npc));
               if (npc.isAutoAttackable(activeChar) && !npc.isAlikeDead()) {
                  if (GeoEngine.canSeeTarget(activeChar, npc, activeChar.isFlying())) {
                     activeChar.getAI().setIntention(CtrlIntention.ATTACK, npc);
                  } else {
                     activeChar.getAI().setIntention(CtrlIntention.MOVING, npc.getLocation());
                  }
               } else if (!npc.isAutoAttackable(activeChar)) {
                  if (!npc.canInteract(activeChar)) {
                     activeChar.getAI().setIntention(CtrlIntention.INTERACT, npc);
                     return true;
                  }

                  if (npc.hasRandomAnimation()) {
                     npc.onRandomAnimation(Rnd.get(8));
                  }

                  List<Quest> qlsa = npc.getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
                  List<Quest> qlst = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
                  if (qlsa != null && !qlsa.isEmpty()) {
                     activeChar.setLastQuestNpcObject(target.getObjectId());
                  }

                  if (qlst != null && qlst.size() == 1) {
                     qlst.get(0).notifyFirstTalk(npc, activeChar);
                  } else {
                     npc.showChatWindow(activeChar);
                  }

                  if (Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM()) {
                     activeChar.updateNotMoveUntil();
                  }
               }
            }

            return true;
         }
      }
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.Npc;
   }
}
