package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.parser.ManorParser;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.instancemanager.MapRegionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ChestInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Seed implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else if (CastleManorManager.getInstance().isDisabled()) {
         return false;
      } else {
         GameObject tgt = playable.getTarget();
         if (!(tgt instanceof Npc)) {
            playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
            playable.sendActionFailed();
            return false;
         } else if (tgt instanceof MonsterInstance && !(tgt instanceof ChestInstance) && !((Creature)tgt).isRaid()) {
            MonsterInstance target = (MonsterInstance)tgt;
            if (target.isDead()) {
               playable.sendPacket(SystemMessageId.INCORRECT_TARGET);
               playable.sendActionFailed();
               return false;
            } else if (target.isSeeded()) {
               playable.sendActionFailed();
               return false;
            } else {
               int seedId = item.getId();
               if (!this.areaValid(seedId, MapRegionManager.getInstance().getAreaCastle(playable))) {
                  playable.sendPacket(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
                  return false;
               } else {
                  target.setSeeded(seedId, (Player)playable);
                  SkillHolder[] skills = item.getItem().getSkills();
                  Player activeChar = playable.getActingPlayer();
                  if (skills != null) {
                     for(SkillHolder sk : skills) {
                        activeChar.useMagic(sk.getSkill(), false, false, true);
                     }
                  }

                  return true;
               }
            }
         } else {
            playable.sendPacket(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
            playable.sendActionFailed();
            return false;
         }
      }
   }

   private boolean areaValid(int seedId, int castleId) {
      return ManorParser.getInstance().getCastleIdForSeed(seedId) == castleId;
   }
}
