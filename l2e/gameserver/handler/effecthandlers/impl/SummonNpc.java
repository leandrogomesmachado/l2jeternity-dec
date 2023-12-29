package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ChronoMonsterInstance;
import l2e.gameserver.model.actor.instance.DecoyInstance;
import l2e.gameserver.model.actor.instance.EffectPointInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.serverpackets.NpcInfo;

public class SummonNpc extends Effect {
   private final int _despawnDelay;
   private final int _npcId;
   private final int _npcCount;
   private final boolean _randomOffset;
   private final boolean _isSummonSpawn;

   public SummonNpc(Env env, EffectTemplate template) {
      super(env, template);
      this._despawnDelay = template.getParameters().getInteger("despawnDelay", 20000);
      this._npcId = template.getParameters().getInteger("npcId", 0);
      this._npcCount = template.getParameters().getInteger("npcCount", 1);
      this._randomOffset = template.getParameters().getBool("randomOffset", false);
      this._isSummonSpawn = template.getParameters().getBool("isSummonSpawn", false);
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
      } else if (this._npcId > 0 && this._npcCount > 0) {
         Player player = this.getEffected().getActingPlayer();
         if (player.isMounted()) {
            return false;
         } else {
            NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(this._npcId);
            if (npcTemplate == null) {
               _log.warning(SummonNpc.class.getSimpleName() + ": Spawn of the nonexisting NPC Id: " + this._npcId + ", skill Id:" + this.getSkill().getId());
               return false;
            } else {
               String var3 = npcTemplate.getType();
               switch(var3) {
                  case "Decoy":
                     DecoyInstance decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), npcTemplate, player, this._despawnDelay);
                     decoy.setCurrentHp(decoy.getMaxHp());
                     decoy.setCurrentMp(decoy.getMaxMp());
                     decoy.setHeading(player.getHeading());
                     decoy.setReflectionId(player.getReflectionId());
                     decoy.spawnMe(player.getX(), player.getY(), player.getZ());
                     player.setDecoy(decoy);
                     break;
                  case "EffectPoint":
                     EffectPointInstance effectPoint = new EffectPointInstance(IdFactory.getInstance().getNextId(), npcTemplate, player);
                     effectPoint.setCurrentHp(effectPoint.getMaxHp());
                     effectPoint.setCurrentMp(effectPoint.getMaxMp());
                     int x = player.getX();
                     int y = player.getY();
                     int z = player.getZ();
                     if (this.getSkill().getTargetType() == TargetType.GROUND) {
                        Location wordPosition = player.getActingPlayer().getCurrentSkillWorldPosition();
                        if (wordPosition != null) {
                           x = wordPosition.getX();
                           y = wordPosition.getY();
                           z = wordPosition.getZ();
                        }
                     }

                     this.getSkill().getEffects(player, effectPoint, false);
                     effectPoint.setIsInvul(true);
                     effectPoint.spawnMe(x, y, z);
                     break;
                  default:
                     Spawner spawn;
                     try {
                        spawn = new Spawner(npcTemplate);
                     } catch (Exception var10) {
                        _log.warning(SummonNpc.class.getSimpleName() + ": " + var10.getMessage());
                        return false;
                     }

                     spawn.setReflectionId(player.getReflectionId());
                     spawn.setHeading(-1);
                     if (this._randomOffset) {
                        spawn.setX(player.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
                        spawn.setY(player.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
                     } else {
                        spawn.setX(player.getX());
                        spawn.setY(player.getY());
                     }

                     spawn.setZ(player.getZ() + 20);
                     spawn.stopRespawn();
                     Npc npc = spawn.doSpawn(this._isSummonSpawn);
                     npc.setName(npcTemplate.getName());
                     npc.setTitle(npcTemplate.getName());
                     npc.setSummoner(player);
                     if (this._despawnDelay > 0) {
                        npc.scheduleDespawn((long)this._despawnDelay);
                     }

                     if (npc instanceof ChronoMonsterInstance) {
                        ((ChronoMonsterInstance)npc).setOwner(player);
                        npc.setTitle(player.getName());
                        npc.broadcastPacket(new NpcInfo.Info(npc, null));
                     }

                     npc.setIsRunning(false);
               }

               return true;
            }
         }
      } else {
         _log.warning(SummonNpc.class.getSimpleName() + ": Invalid NPC Id or count skill Id: " + this.getSkill().getId());
         return false;
      }
   }
}
