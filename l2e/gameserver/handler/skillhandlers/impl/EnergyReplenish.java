package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class EnergyReplenish implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.ENERGY_REPLENISH};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      int energy = skill.getEnergyConsume();
      boolean emptyEnergy = false;
      ItemInstance item = activeChar.getInventory().getPaperdollItem(15);
      if (item != null) {
         if (item.getAgathionEnergy() == 0) {
            emptyEnergy = true;
         }

         if (energy > 0) {
            item.setAgathionEnergy(item.getAgathionEnergy() + energy);
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENERGY_S1_REPLENISHED).addNumber(energy));
            activeChar.getActingPlayer().sendItemList(false);
            if (emptyEnergy) {
               item.decreaseEnergy(false);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
