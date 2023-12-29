package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.DoorInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class NornilsPower implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.NORNILS_POWER};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer()) {
         ReflectionWorld world = null;
         int instanceId = activeChar.getReflectionId();
         if (instanceId > 0) {
            world = ReflectionManager.getInstance().getPlayerWorld(activeChar.getActingPlayer());
         }

         if (world == null || world.getReflectionId() != instanceId || world.getTemplateId() != 11) {
            activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
         } else if (activeChar.isInsideRadius(-107393, 83677, 100, true)) {
            activeChar.destroyItemByItemId("NornilsPower", 9713, 1L, activeChar, true);
            DoorInstance door = world.getReflection().getDoor(16200010);
            if (door != null) {
               door.setMeshIndex(1);
               door.setTargetable(true);
               door.broadcastStatusUpdate();
            }
         } else {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addSkillName(skill);
            activeChar.sendPacket(sm);
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
