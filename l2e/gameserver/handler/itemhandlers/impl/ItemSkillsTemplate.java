package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.items.type.EtcItemType;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ItemSkillsTemplate implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer() && !playable.isSummon()) {
         return false;
      } else {
         if (item.getItemType() == EtcItemType.SCROLL) {
            for(AbstractFightEvent e : playable.getFightEvents()) {
               if (e != null && !e.canUseScroll(playable)) {
                  playable.sendActionFailed();
                  return false;
               }
            }

            if (!AerialCleftEvent.getInstance().onScrollUse(playable.getObjectId())) {
               playable.sendActionFailed();
               return false;
            }
         }

         if (playable.isPet() && !item.isTradeable()) {
            playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
            return false;
         } else if (!this.checkReuse(playable, null, item)) {
            return false;
         } else {
            SkillHolder[] skills = item.getEtcItem().getSkills();
            if (skills == null) {
               _log.info("Item " + item + " does not have registered any skill for handler.");
               return false;
            } else if (item.getItem().getItemConsume() > 0
               && playable.getInventory().getItemByItemId(item.getId()).getCount() < (long)item.getItem().getItemConsume()) {
               playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
               return false;
            } else {
               for(SkillHolder skillInfo : skills) {
                  if (skillInfo != null) {
                     Skill itemSkill = skillInfo.getSkill();
                     if (itemSkill != null) {
                        if (!itemSkill.checkCondition(playable, playable.getTarget(), false, true)) {
                           return false;
                        }

                        if (playable.isSkillDisabled(itemSkill)) {
                           return false;
                        }

                        if (!this.checkReuse(playable, itemSkill, item)) {
                           return false;
                        }

                        if (!item.isPotion() && !item.isElixir() && playable.isCastingNow() && !item.getItem().isHerb()) {
                           return false;
                        }

                        if (itemSkill.getItemConsumeId() == 0
                           && itemSkill.getItemConsume() > 0
                           && (item.isPotion() || item.isElixir() || itemSkill.isSimultaneousCast())
                           && !playable.destroyItem("Consume", item.getObjectId(), (long)itemSkill.getItemConsume(), playable, false)) {
                           playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                           return false;
                        }

                        if (playable.isSummon()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1);
                           sm.addSkillName(itemSkill);
                           playable.sendPacket(sm);
                        }

                        if (!item.isPotion() && !item.isElixir() && !item.getItem().isHerb() && !itemSkill.isSimultaneousCast()) {
                           playable.getAI().setIntention(CtrlIntention.IDLE);
                           if (!playable.useMagic(itemSkill, forceUse, false, true)) {
                              return false;
                           }

                           if (itemSkill.getItemConsumeId() == 0
                              && itemSkill.getItemConsume() > 0
                              && !playable.destroyItem("Consume", item.getObjectId(), (long)itemSkill.getItemConsume(), null, false)) {
                              playable.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                              return false;
                           }
                        } else {
                           playable.doSimultaneousCast(itemSkill);
                        }

                        if (itemSkill.getReuseDelay() > 0) {
                           playable.addTimeStamp(itemSkill, (long)itemSkill.getReuseDelay());
                        }
                     }
                  }
               }

               if (item.getItem().getItemConsume() > 0) {
                  playable.destroyItemByItemId("Consume", item.getId(), (long)item.getItem().getItemConsume(), null, false);
               }

               return true;
            }
         }
      }
   }

   private boolean checkReuse(Playable playable, Skill skill, ItemInstance item) {
      long remainingTime = skill != null
         ? playable.getSkillRemainingReuseTime(skill.getReuseHashCode())
         : playable.getItemRemainingReuseTime(item.getObjectId());
      boolean isAvailable = remainingTime <= 0L;
      if (playable.isPlayer() && !isAvailable) {
         int hours = (int)(remainingTime / 3600000L);
         int minutes = (int)(remainingTime % 3600000L) / 60000;
         int seconds = (int)(remainingTime / 1000L % 60L);
         SystemMessage sm = null;
         if (hours > 0) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
            if (skill != null && !skill.isStatic()) {
               sm.addSkillName(skill);
            } else {
               sm.addItemName(item);
            }

            sm.addNumber(hours);
            sm.addNumber(minutes);
         } else if (minutes <= 0) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
            if (skill != null && !skill.isStatic()) {
               sm.addSkillName(skill);
            } else {
               sm.addItemName(item);
            }
         } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
            if (skill != null && !skill.isStatic()) {
               sm.addSkillName(skill);
            } else {
               sm.addItemName(item);
            }

            sm.addNumber(minutes);
         }

         sm.addNumber(seconds);
         playable.sendPacket(sm);
      }

      return isAvailable;
   }
}
