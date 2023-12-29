package l2e.scripts.instances;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;

public class SufferingHallAttack extends AbstractReflection {
   public SufferingHallAttack(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32530, 32537});
      this.addTalkId(new int[]{32530, 32537});
      this.addAttackId(new int[]{25665, 25666});
      this.addSkillSeeId(new int[]{22509, 22510, 22511, 22512, 22513, 22514, 22515});
      this.addKillId(new int[]{18704, 25665});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new SufferingHallAttack.SHAWorld(), 115)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((SufferingHallAttack.SHAWorld)world).storeTime[0] = System.currentTimeMillis();
         this.spawnRoom((SufferingHallAttack.SHAWorld)world, 1);
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32537) {
         this.enterInstance(player, npc);
      }

      return null;
   }

   private void spawnRoom(SufferingHallAttack.SHAWorld world, int id) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         switch(id) {
            case 1:
               inst.spawnByGroup("soi_hos_attack_1");
               this.getActivatedZone(inst, 20029, true);
               break;
            case 2:
               inst.spawnByGroup("soi_hos_attack_2");
               this.getActivatedZone(inst, 20030, true);
               break;
            case 3:
               inst.spawnByGroup("soi_hos_attack_3");
               this.getActivatedZone(inst, 20031, true);
               break;
            case 4:
               inst.spawnByGroup("soi_hos_attack_4");
               this.getActivatedZone(inst, 20032, true);
               break;
            case 5:
               inst.spawnByGroup("soi_hos_attack_5");
               this.getActivatedZone(inst, 20033, true);
               break;
            case 6:
               inst.spawnByGroup("soi_hos_attack_6");
               this.getActivatedZone(inst, 20034, true);
               break;
            case 7:
               inst.spawnByGroup("soi_hos_attack_7");
               this.getActivatedZone(inst, 20034, false);
         }
      }
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (skill.hasEffectType(EffectType.REBALANCE_HP, EffectType.HEAL, EffectType.HEAL_PERCENT)) {
         int hate = 2 * skill.getAggroPoints();
         if (hate < 2) {
            hate = 1000;
         }

         ((Attackable)npc).addDamageHate(caster, 0, hate);
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof SufferingHallAttack.SHAWorld) {
         final SufferingHallAttack.SHAWorld world = (SufferingHallAttack.SHAWorld)tmpworld;
         if (npc.getId() == 18704) {
            npc.deleteMe();
            addSpawn(32531, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L, false, npc.getReflectionId());
            Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
            if (inst != null) {
               if (ZoneManager.getInstance().isInsideZone(20029, npc)) {
                  this.getActivatedZone(inst, 20029, false);
                  this.spawnRoom(world, 2);
               } else if (ZoneManager.getInstance().isInsideZone(20030, npc)) {
                  this.getActivatedZone(inst, 20030, false);
                  this.spawnRoom(world, 3);
               } else if (ZoneManager.getInstance().isInsideZone(20031, npc)) {
                  this.getActivatedZone(inst, 20031, false);
                  this.spawnRoom(world, 4);
               } else if (ZoneManager.getInstance().isInsideZone(20032, npc)) {
                  this.getActivatedZone(inst, 20032, false);
                  this.spawnRoom(world, 5);
               } else if (ZoneManager.getInstance().isInsideZone(20033, npc)) {
                  this.getActivatedZone(inst, 20033, false);
                  this.spawnRoom(world, 6);
               }
            }
         } else if (npc.getId() == 25665) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  SufferingHallAttack.this.spawnRoom(world, 7);
                  world.storeTime[1] = System.currentTimeMillis();
                  world.calcRewardItemId();
                  SufferingHallAttack.this.finishInstance(world, 300000, true);
               }
            }, 10000L);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new SufferingHallAttack(SufferingHallAttack.class.getSimpleName(), "instances");
   }

   private class SHAWorld extends ReflectionWorld {
      public long[] storeTime = new long[]{0L, 0L};

      protected void calcRewardItemId() {
         Long finishDiff = this.storeTime[1] - this.storeTime[0];
         if (finishDiff < 1260000L) {
            this.setTag(13777);
         } else if (finishDiff < 1380000L) {
            this.setTag(13778);
         } else if (finishDiff < 1500000L) {
            this.setTag(13779);
         } else if (finishDiff < 1620000L) {
            this.setTag(13780);
         } else if (finishDiff < 1740000L) {
            this.setTag(13781);
         } else if (finishDiff < 1860000L) {
            this.setTag(13782);
         } else if (finishDiff < 1980000L) {
            this.setTag(13783);
         } else if (finishDiff < 2100000L) {
            this.setTag(13784);
         } else if (finishDiff < 2220000L) {
            this.setTag(13785);
         } else {
            this.setTag(13786);
         }
      }

      public SHAWorld() {
         this.setTag(-1);
      }
   }
}
