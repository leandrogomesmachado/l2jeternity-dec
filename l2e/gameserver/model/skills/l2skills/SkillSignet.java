package l2e.gameserver.model.skills.l2skills;

import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.EffectPointInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.StatsSet;

public final class SkillSignet extends Skill {
   public SkillSignet(StatsSet set) {
      super(set);
   }

   @Override
   public void useSkill(Creature caster, GameObject[] targets) {
      if (!caster.isAlikeDead()) {
         NpcTemplate template = NpcsParser.getInstance().getTemplate(this.getNpcId());
         EffectPointInstance effectPoint = new EffectPointInstance(IdFactory.getInstance().getNextId(), template, caster);
         effectPoint.setCurrentHp(effectPoint.getMaxHp());
         effectPoint.setCurrentMp(effectPoint.getMaxMp());
         int x = caster.getX();
         int y = caster.getY();
         int z = caster.getZ();
         if (caster.isPlayer() && this.getTargetType() == TargetType.GROUND) {
            Location wordPosition = caster.getActingPlayer().getCurrentSkillWorldPosition();
            if (wordPosition != null) {
               x = wordPosition.getX();
               y = wordPosition.getY();
               z = wordPosition.getZ();
            }
         }

         z = GeoEngine.getHeight(x, y, z, caster.getGeoIndex());
         this.getEffects(caster, effectPoint, true);
         effectPoint.setIsInvul(true);
         effectPoint.spawnMe(x, y, z);
      }
   }
}
