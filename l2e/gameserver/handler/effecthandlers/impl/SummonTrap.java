package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.TrapInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class SummonTrap extends Effect {
   private final int _despawnTime;
   private final int _npcId;

   public SummonTrap(Env env, EffectTemplate template) {
      super(env, template);
      this._despawnTime = template.getParameters().getInteger("despawnTime", 0);
      this._npcId = template.getParameters().getInteger("npcId", 0);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() == null
         || !this.getEffected().isPlayer()
         || this.getEffected().isAlikeDead()
         || this.getEffected().getActingPlayer().inObserverMode()) {
         return false;
      } else if (this._npcId <= 0) {
         _log.warning(SummonTrap.class.getSimpleName() + ": Invalid NPC Id:" + this._npcId + " in skill Id: " + this.getSkill().getId());
         return false;
      } else {
         Player player = this.getEffected().getActingPlayer();
         if (!player.inObserverMode() && !player.isMounted()) {
            if (player.getTrap() != null) {
               player.getTrap().unSummon();
            }

            NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(this._npcId);
            if (npcTemplate == null) {
               _log.warning(
                  SummonTrap.class.getSimpleName() + ": Spawn of the non-existing Trap Id: " + this._npcId + " in skill Id:" + this.getSkill().getId()
               );
               return false;
            } else {
               TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), npcTemplate, player, this._despawnTime);
               trap.setCurrentHp(trap.getMaxHp());
               trap.setCurrentMp(trap.getMaxMp());
               trap.setIsInvul(true);
               trap.setHeading(player.getHeading());
               trap.spawnMe(player.getX(), player.getY(), player.getZ());
               player.setTrap(trap);
               return true;
            }
         } else {
            return false;
         }
      }
   }
}
