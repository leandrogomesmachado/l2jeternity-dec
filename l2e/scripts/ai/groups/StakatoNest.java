package l2e.scripts.ai.groups;

import java.util.Iterator;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.NpcUtils;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.MinionList;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.CannibalisticStakatoChiefInstance;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.type.EffectZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MagicSkillUse;
import org.apache.commons.lang.ArrayUtils;

public class StakatoNest extends Fighter {
   private static final int[] BIZARRE_COCOON = new int[]{18793, 18794, 18795, 18796, 18797, 18798};
   private static final int CANNIBALISTIC_STAKATO_LEADER = 22625;
   private static final int SPIKE_STAKATO_NURSE = 22630;
   private static final int SPIKE_STAKATO_NURSE_CHANGED = 22631;
   private static final int SPIKED_STAKATO_BABY = 22632;
   private static final int SPIKED_STAKATO_CAPTAIN = 22629;
   private static final int FEMALE_SPIKED_STAKATO = 22620;
   private static final int MALE_SPIKED_STAKATO = 22621;
   private static final int MALE_SPIKED_STAKATO_2 = 22622;
   private static final int SPIKED_STAKATO_GUARD = 22619;
   private static final int SKILL_GROWTH_ACCELERATOR = 2905;
   private static final int CANNIBALISTIC_STAKATO_CHIEF = 25667;
   private static final int QUEEN_SHYEED = 25671;
   private static final EffectZone _zone_mob_buff = ZoneManager.getInstance().getZoneById(200103, EffectZone.class);
   private static final EffectZone _zone_mob_buff_pc_display = ZoneManager.getInstance().getZoneById(200104, EffectZone.class);
   private static final EffectZone _zone_pc_buff = ZoneManager.getInstance().getZoneById(200105, EffectZone.class);
   private static boolean _debuffed = false;

   public StakatoNest(Attackable actor) {
      super(actor);
      if (ArrayUtils.contains(BIZARRE_COCOON, actor.getId())) {
         actor.setIsInvul(true);
         actor.setIsImmobilized(true);
      }
   }

   @Override
   protected void onEvtSpawn() {
      Attackable actor = this.getActiveChar();
      if (actor.getId() == 25671) {
         if (!_debuffed) {
            _debuffed = true;
            _zone_mob_buff.setZoneEnabled(true);
            _zone_mob_buff_pc_display.setZoneEnabled(true);
            _zone_pc_buff.setZoneEnabled(false);
         }

         for(GameObject player : World.getInstance().getAroundPlayers(actor, 1500, 400)) {
            if (player != null) {
               player.sendPacket(SystemMessageId.SHYEED_S_ROAR_FILLED_WITH_WRATH_RINGS_THROUGHOUT_THE_STAKATO_NEST);
            }
         }
      }

      super.onEvtSpawn();
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      MonsterInstance mob = (MonsterInstance)actor;
      if (attacker != null && mob.getId() == 22625 && Rnd.chance(10) && mob.getCurrentHpPercents() < 30.0) {
         MonsterInstance follower = this.getAliveMinion(actor);
         if (follower != null && follower.getCurrentHpPercents() > 30.0) {
            mob.abortAttack();
            mob.abortCast();
            mob.setHeading(PositionUtils.getHeadingTo(mob, follower));
            mob.setTarget(follower);
            mob.doCast(SkillsParser.getInstance().getInfo(4485, 1));
            mob.setCurrentHp(mob.getCurrentHp() + follower.getCurrentHp());
            follower.doDie(follower);
            follower.deleteMe();
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      MinionInstance minion = this.getAliveMinion(actor);
      MonsterInstance leader = null;
      switch(actor.getId()) {
         case 22620:
            leader = ((MinionInstance)actor).getLeader();
            if (leader != null && !leader.isDead()) {
               ThreadPoolManager.getInstance().schedule(new StakatoNest.ChangeMonster(22622, actor, killer), 3000L);
            }
            break;
         case 22621:
            if (minion != null) {
               actor.broadcastPacket(new MagicSkillUse(actor, 2046, 1, 1000, 0));

               for(int i = 0; i < 3; ++i) {
                  this.spawnMonster(minion, killer, 22619);
               }
            }
            break;
         case 22630:
            if (minion != null) {
               actor.broadcastPacket(new MagicSkillUse(actor, 2046, 1, 1000, 0));

               for(int i = 0; i < 3; ++i) {
                  this.spawnMonster(minion, killer, 22629);
               }
            }
            break;
         case 22632:
            leader = ((MinionInstance)actor).getLeader();
            if (leader != null && !leader.isDead()) {
               ThreadPoolManager.getInstance().schedule(new StakatoNest.ChangeMonster(22631, actor, killer), 3000L);
            }
            break;
         case 25671:
            if (_debuffed) {
               _debuffed = false;
               _zone_pc_buff.setZoneEnabled(true);
               _zone_mob_buff.setZoneEnabled(false);
               _zone_mob_buff_pc_display.setZoneEnabled(false);
            }
      }

      super.onEvtDead(killer);
   }

   @Override
   protected void onEvtSeeSpell(Skill skill, Creature caster) {
      Attackable actor = this.getActiveChar();
      if (actor == null || !ArrayUtils.contains(BIZARRE_COCOON, actor.getId()) || caster == null || skill.getId() != 2905) {
         super.onEvtSeeSpell(skill, caster);
      } else if (Rnd.chance(8)) {
         caster.getActingPlayer().sendPacket(SystemMessageId.NOTHING_HAPPENED);
      } else {
         actor.doDie(null);
         actor.endDecayTask();

         try {
            CannibalisticStakatoChiefInstance mob = new CannibalisticStakatoChiefInstance(
               IdFactory.getInstance().getNextId(), NpcsParser.getInstance().getTemplate(25667)
            );
            mob.setReflectionId(actor.getReflectionId());
            mob.setHeading(actor.getHeading());
            mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
            mob.spawnMe(actor.getLocation().getX(), actor.getLocation().getY(), actor.getLocation().getZ());
            mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster.getActingPlayer(), Integer.valueOf(Rnd.get(1, 100)));
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         super.onEvtSeeSpell(skill, caster);
      }
   }

   private MinionInstance getAliveMinion(Attackable npc) {
      MinionList ml = npc.getMinionList();
      if (ml != null && ml.hasAliveMinions()) {
         Iterator var3 = ml.getAliveMinions().iterator();
         if (var3.hasNext()) {
            return (MinionInstance)var3.next();
         }
      }

      return null;
   }

   private void spawnMonster(Attackable actor, Creature killer, int mobId) {
      MonsterInstance npc = NpcUtils.spawnSingle(mobId, actor.getLocation());
      if (killer != null) {
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(Rnd.get(1, 100)));
      }
   }

   protected class ChangeMonster extends RunnableImpl {
      private final int _monsterId;
      private final Creature _killer;
      private final Attackable _npc;

      public ChangeMonster(int mobId, Attackable npc, Creature killer) {
         this._monsterId = mobId;
         this._npc = npc;
         this._killer = killer;
      }

      @Override
      public void runImpl() {
         StakatoNest.this.spawnMonster(this._npc, this._killer, this._monsterId);
      }
   }
}
