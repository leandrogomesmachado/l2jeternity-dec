package l2e.gameserver.handler.actionshifthandlers.impl;

import l2e.commons.util.StringUtil;
import l2e.gameserver.handler.actionhandlers.IActionHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ItemActionShift implements IActionHandler {
   @Override
   public boolean action(Player activeChar, GameObject target, boolean interact) {
      if (activeChar.getAccessLevel().isGm()) {
         NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
         String html1 = StringUtil.concat(
            "<html><body><center><font color=\"LEVEL\">Item Info</font></center><br><table border=0>",
            "<tr><td>Object ID: </td><td>",
            String.valueOf(target.getObjectId()),
            "</td></tr><tr><td>Item ID: </td><td>",
            String.valueOf(((ItemInstance)target).getId()),
            "</td></tr><tr><td>Owner ID: </td><td>",
            String.valueOf(((ItemInstance)target).getOwnerId()),
            "</td></tr><tr><td>Location: </td><td>",
            ""
               + ((ItemInstance)target).getLocation().getX()
               + " "
               + ((ItemInstance)target).getLocation().getY()
               + " "
               + ((ItemInstance)target).getLocation().getZ()
               + "",
            "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>",
            target.getClass().getSimpleName(),
            "</td></tr></table></body></html>"
         );
         html.setHtml(activeChar, html1);
         activeChar.sendPacket(html);
      }

      return true;
   }

   @Override
   public GameObject.InstanceType getInstanceType() {
      return GameObject.InstanceType.ItemInstance;
   }
}
