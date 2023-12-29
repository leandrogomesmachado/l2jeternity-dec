package l2e.gameserver.handler.skillhandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class GetPlayer implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.GET_PLAYER};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (!activeChar.isAlikeDead()) {
         for(GameObject target : targets) {
            if (target.isPlayer()) {
               Player trg = target.getActingPlayer();
               if (!trg.isAlikeDead()) {
                  trg.setXYZ(activeChar.getX() + Rnd.get(-10, 10), activeChar.getY() + Rnd.get(-10, 10), activeChar.getZ());
                  trg.sendPacket(new ValidateLocation(trg));
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
