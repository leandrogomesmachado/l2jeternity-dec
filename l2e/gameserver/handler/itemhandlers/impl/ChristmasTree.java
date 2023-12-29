package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.SystemMessageId;

public class ChristmasTree implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         NpcTemplate template1 = null;
         switch(item.getId()) {
            case 5560:
               template1 = NpcsParser.getInstance().getTemplate(13006);
               break;
            case 5561:
               template1 = NpcsParser.getInstance().getTemplate(13007);
         }

         if (template1 == null) {
            return false;
         } else {
            GameObject target = activeChar.getTarget();
            if (target == null) {
               target = activeChar;
            }

            try {
               Spawner spawn = new Spawner(template1);
               spawn.setX(target.getX());
               spawn.setY(target.getY());
               spawn.setZ(target.getZ());
               spawn.setReflectionId(activeChar.getReflectionId());
               Npc npc = spawn.spawnOne(false);
               npc.setSummoner(activeChar);
               activeChar.destroyItem("Consume", item.getObjectId(), 1L, null, false);
               activeChar.sendMessage("Created " + template1.getName() + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " + spawn.getZ());
               return true;
            } catch (Exception var9) {
               activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
               return false;
            }
         }
      }
   }
}
