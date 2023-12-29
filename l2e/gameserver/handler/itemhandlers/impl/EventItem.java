package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BlockInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class EventItem implements IItemHandler {
   private static final Logger _log = Logger.getLogger(EventItem.class.getName());

   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         boolean used = false;
         Player activeChar = playable.getActingPlayer();
         int itemId = item.getId();
         switch(itemId) {
            case 13787:
               used = this.useBlockCheckerItem(activeChar, item);
               break;
            case 13788:
               used = this.useBlockCheckerItem(activeChar, item);
               break;
            default:
               _log.warning("EventItemHandler: Item with id: " + itemId + " is not handled");
         }

         return used;
      }
   }

   private final boolean useBlockCheckerItem(Player castor, ItemInstance item) {
      int blockCheckerArena = castor.getBlockCheckerArena();
      if (blockCheckerArena == -1) {
         SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
         msg.addItemName(item);
         castor.sendPacket(msg);
         return false;
      } else {
         Skill sk = item.getEtcItem().getSkills()[0].getSkill();
         if (sk == null) {
            return false;
         } else if (!castor.destroyItem("Consume", item, 1L, castor, true)) {
            return false;
         } else {
            BlockInstance block = (BlockInstance)castor.getTarget();
            ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(blockCheckerArena);
            if (holder != null) {
               int team = holder.getPlayerTeam(castor);

               for(Player pc : World.getInstance().getAroundPlayers(block, sk.getAffectRange(), 200)) {
                  int enemyTeam = holder.getPlayerTeam(pc);
                  if (enemyTeam != -1 && enemyTeam != team) {
                     sk.getEffects(castor, pc, false);
                  }
               }

               return true;
            } else {
               _log.warning("Char: " + castor.getName() + "[" + castor.getObjectId() + "] has unknown block checker arena");
               return false;
            }
         }
      }
   }
}
