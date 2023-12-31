package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.SevenSigns;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.holders.SummonRequestHolder;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ConfirmDlg;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class CallPc extends Effect {
   private static int _itemId;
   private static int _itemCount;

   public CallPc(Env env, EffectTemplate template) {
      super(env, template);
      _itemId = template.getParameters().getInteger("itemId", 0);
      _itemCount = template.getParameters().getInteger("itemCount", 0);
   }

   @Override
   public boolean calcSuccess() {
      return true;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CALLPC;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == this.getEffector()) {
         return false;
      } else {
         Player target = this.getEffected().getActingPlayer();
         Player activeChar = this.getEffector().getActingPlayer();
         if (checkSummonTargetStatus(target, activeChar)) {
            if (_itemId != 0 && _itemCount != 0) {
               if (target.getInventory().getInventoryItemCount(_itemId, 0) < (long)_itemCount) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
                  sm.addItemName(_itemId);
                  target.sendPacket(sm);
                  return false;
               }

               target.getInventory().destroyItemByItemId("Consume", _itemId, (long)_itemCount, activeChar, target);
               SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
               sm.addItemName(_itemId);
               target.sendPacket(sm);
            }

            target.addScript(new SummonRequestHolder(activeChar, this.getSkill(), false));
            ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
            confirm.addCharName(activeChar);
            confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
            confirm.addTime(30000);
            confirm.addRequesterId(activeChar.getObjectId());
            target.sendPacket(confirm);
            return true;
         } else {
            return false;
         }
      }
   }

   public static boolean checkSummonTargetStatus(Player target, Player activeChar) {
      if (target == activeChar) {
         return false;
      } else if (target.isAlikeDead()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
         sm.addPcName(target);
         activeChar.sendPacket(sm);
         return false;
      } else if (target.isInStoreMode() || target.isSellingBuffs()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
         sm.addPcName(target);
         activeChar.sendPacket(sm);
         return false;
      } else if (target.isRooted() || target.isInCombat()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
         sm.addPcName(target);
         activeChar.sendPacket(sm);
         return false;
      } else if (target.isInOlympiadMode()) {
         activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
         return false;
      } else {
         for(AbstractFightEvent e : target.getFightEvents()) {
            if (e != null && !e.canUseEscape(target)) {
               activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
               return false;
            }
         }

         if (target.isFestivalParticipant() || target.isFlyingMounted() || target.isCombatFlagEquipped()) {
            activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
            return false;
         } else if (target.inObserverMode()) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING);
            sm.addCharName(target);
            activeChar.sendPacket(sm);
            return false;
         } else if (!target.isInsideZone(ZoneId.NO_SUMMON_FRIEND) && !target.isInsideZone(ZoneId.JAIL)) {
            if (activeChar.getReflectionId() > 0) {
               Reflection summonerInstance = ReflectionManager.getInstance().getReflection(activeChar.getReflectionId());
               if (!Config.ALLOW_SUMMON_TO_INSTANCE || !summonerInstance.isSummonAllowed()) {
                  activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
                  return false;
               }
            }

            if (activeChar.isIn7sDungeon()) {
               int targetCabal = SevenSigns.getInstance().getPlayerCabal(target.getObjectId());
               if (SevenSigns.getInstance().isSealValidationPeriod()) {
                  if (targetCabal != SevenSigns.getInstance().getCabalHighestScore()) {
                     activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
                     return false;
                  }
               } else if (targetCabal == 0) {
                  activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
                  return false;
               }
            }

            return true;
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
            sm.addString(target.getName());
            activeChar.sendPacket(sm);
            return false;
         }
      }
   }
}
