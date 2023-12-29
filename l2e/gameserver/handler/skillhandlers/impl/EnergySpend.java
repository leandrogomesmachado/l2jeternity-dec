package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.SystemMessageId;

public class EnergySpend implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.ENERGY_SPEND};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      ItemInstance item = activeChar.getInventory().getPaperdollItem(15);
      if (item != null) {
         int energy = skill.getEnergyConsume();
         if (energy > 0) {
            item.setAgathionEnergy(item.getAgathionEnergy() - energy);
            if (skill.hasEffects()) {
               for(Creature target : (Creature[])targets) {
                  skill.getEffects(activeChar, target, true);
               }
            }

            if (skill.hasSelfEffects()) {
               Effect effect = activeChar.getFirstEffect(skill.getId());
               if (effect != null && effect.isSelfEffect()) {
                  effect.exit();
               }

               skill.getEffectsSelf(activeChar);
            }

            activeChar.getActingPlayer().sendItemList(false);
         }
      } else {
         activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
