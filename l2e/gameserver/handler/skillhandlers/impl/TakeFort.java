package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;

public class TakeFort implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.TAKEFORT};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      if (activeChar.isPlayer() && targets.length != 0) {
         Player player = activeChar.getActingPlayer();
         if (player.getClan() != null) {
            Fort fort = FortManager.getInstance().getFort(player);
            if (fort != null && player.checkIfOkToCastFlagDisplay(fort, true, skill, targets[0])) {
               try {
                  fort.endOfSiege(player.getClan());
                  player.getCounters().addAchivementInfo("fortSiegesWon", 0, -1L, false, false, true);
               } catch (Exception var7) {
                  var7.printStackTrace();
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
