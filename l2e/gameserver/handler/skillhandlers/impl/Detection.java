package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;

public class Detection implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.DETECTION};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      Player player = activeChar.getActingPlayer();
      if (player != null) {
         boolean hasParty = player.isInParty();
         boolean hasClan = player.getClanId() > 0;
         boolean hasAlly = player.getAllyId() > 0;

         for(Player target : World.getInstance().getAroundPlayers(activeChar, skill.getAffectRange(), 200)) {
            if (target != null
               && target.isInvisible()
               && (!hasParty || target.getParty() == null || player.getParty().getLeaderObjectId() != target.getParty().getLeaderObjectId())
               && (!hasClan || player.getClanId() != target.getClanId())
               && (!hasAlly || player.getAllyId() != target.getAllyId())) {
               Effect eHide = target.getFirstEffect(EffectType.HIDE);
               if (eHide != null) {
                  eHide.exit();
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
