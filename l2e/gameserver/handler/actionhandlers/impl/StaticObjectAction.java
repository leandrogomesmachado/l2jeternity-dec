package l2e.gameserver.handler.actionhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class StaticObjectAction implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      StaticObjectInstance staticObject = (StaticObjectInstance)target;
      if (staticObject.getType() < 0) {
         _log.info("StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " + staticObject.getId());
      }

      if (activeChar.getTarget() != staticObject) {
         activeChar.setTarget(staticObject);
      } else if (interact) {
         if (!activeChar.isInsideRadius(staticObject, 150, false, false)) {
            activeChar.getAI().setIntention(CtrlIntention.INTERACT, staticObject);
         } else if (staticObject.getType() == 2) {
            String filename = staticObject.getId() == 24230101 ? "data/html/signboards/tomb_of_crystalgolem.htm" : "data/html/signboards/pvp_signboard.htm";
            String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), filename);
            NpcHtmlMessage html = new NpcHtmlMessage(staticObject.getObjectId());
            if (content == null) {
               html.setHtml(activeChar, "<html><body>Signboard is missing:<br>" + filename + "</body></html>");
            } else {
               html.setHtml(activeChar, content);
            }

            activeChar.sendPacket(html);
         } else if (staticObject.getType() == 0) {
            activeChar.sendPacket(staticObject.getMap());
         }
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.StaticObjectInstance;
   }
}
