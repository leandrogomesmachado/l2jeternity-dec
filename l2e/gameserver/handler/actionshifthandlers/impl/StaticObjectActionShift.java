package l2e.gameserver.handler.actionshifthandlers.impl;

import l2e.commons.util.StringUtil;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.StaticObjectInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.StaticObject;

public class StaticObjectActionShift implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.getAccessLevel().isGm()) {
         activeChar.setTarget(target);
         activeChar.sendPacket(new StaticObject((StaticObjectInstance)target));
         NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
         html.setHtml(
            activeChar,
            StringUtil.concat(
               "<html><body><center><font color=\"LEVEL\">Static Object Info</font></center><br><table border=0><tr><td>Coords X,Y,Z: </td><td>",
               String.valueOf(target.getX()),
               ", ",
               String.valueOf(target.getY()),
               ", ",
               String.valueOf(target.getZ()),
               "</td></tr><tr><td>Object ID: </td><td>",
               String.valueOf(target.getObjectId()),
               "</td></tr><tr><td>Static Object ID: </td><td>",
               String.valueOf(((StaticObjectInstance)target).getId()),
               "</td></tr><tr><td>Mesh Index: </td><td>",
               String.valueOf(((StaticObjectInstance)target).getMeshIndex()),
               "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>",
               target.getClass().getSimpleName(),
               "</td></tr></table></body></html>"
            )
         );
         activeChar.sendPacket(html);
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.StaticObjectInstance;
   }
}
