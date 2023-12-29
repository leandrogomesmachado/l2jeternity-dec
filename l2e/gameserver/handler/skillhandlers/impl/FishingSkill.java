package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;

public class FishingSkill implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.PUMPING, SkillType.REELING};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         Player player = activeChar.getActingPlayer();
         l2e.gameserver.model.fishing.Fishing fish = player.getFishCombat();
         if (fish == null) {
            if (skill.getSkillType() == SkillType.PUMPING) {
               player.sendPacket(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
            } else if (skill.getSkillType() == SkillType.REELING) {
               player.sendPacket(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING);
            }

            player.sendActionFailed();
         } else {
            Weapon weaponItem = player.getActiveWeaponItem();
            ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
            if (weaponInst != null && weaponItem != null) {
               int SS = 1;
               int pen = 0;
               if (activeChar.isChargedShot(ShotType.FISH_SOULSHOTS)) {
                  SS = 2;
               }

               double gradebonus = 1.0 + (double)weaponItem.getCrystalType() * 0.1;
               int dmg = (int)(skill.getPower() * gradebonus * (double)SS);
               if (player.getSkillLevel(1315) <= skill.getLevel() - 2) {
                  player.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
                  pen = 50;
                  int penatlydmg = dmg - pen;
                  if (player.isGM()) {
                     player.sendMessage("Dmg w/o penalty = " + dmg);
                  }

                  dmg = penatlydmg;
               }

               if (SS > 1) {
                  weaponInst.setChargedShot(ShotType.FISH_SOULSHOTS, false);
               }

               if (skill.getSkillType() == SkillType.REELING) {
                  fish.useReeling(dmg, pen);
               } else {
                  fish.usePumping(dmg, pen);
               }
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
