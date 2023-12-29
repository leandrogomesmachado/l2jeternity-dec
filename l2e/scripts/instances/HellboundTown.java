package l2e.scripts.instances;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.HellboundManager;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class HellboundTown extends AbstractReflection {
   private static final NpcStringId[] NPCSTRING_ID = new NpcStringId[]{
      NpcStringId.INVADER, NpcStringId.YOU_HAVE_DONE_WELL_IN_FINDING_ME_BUT_I_CANNOT_JUST_HAND_YOU_THE_KEY
   };
   private static final NpcStringId[] NATIVES_NPCSTRING_ID = new NpcStringId[]{
      NpcStringId.THANK_YOU_FOR_SAVING_ME, NpcStringId.GUARDS_ARE_COMING_RUN, NpcStringId.NOW_I_CAN_ESCAPE_ON_MY_OWN
   };

   public HellboundTown(String name, String descr) {
      super(name, descr);
      this.addFirstTalkId(32358);
      this.addStartNpc(new int[]{32346, 32358});
      this.addTalkId(new int[]{32346, 32358});
      this.addAttackId(new int[]{22359, 22361});
      this.addAggroRangeEnterId(new int[]{22359});
      this.addKillId(new int[]{22449, 32358, 22359, 22360, 22361});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new HellboundTown.TownWorld(), 2)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((HellboundTown.TownWorld)world).spawnedAmaskari = (MonsterInstance)addSpawn(
            22449, new Location(19424, 253360, -2032, 16860), false, 0L, false, world.getReflectionId()
         );
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
   public final String onFirstTalk(Npc npc, Player player) {
      return npc.getFirstEffect(4616) == null ? "32358-02.htm" : "32358-01.htm";
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = null;
      if (npc.getId() == 32346) {
         htmltext = this.checkConditions(player);
         if (htmltext == null) {
            this.enterInstance(player, npc);
         }
      } else if (npc.getId() == 32343) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
            HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
            Party party = player.getParty();
            if (party == null) {
               htmltext = "32343-02.htm";
            } else if (npc.isBusy()) {
               htmltext = "32343-02c.htm";
            } else if (player.getInventory().getInventoryItemCount(9714, -1, false) >= 1L) {
               for(Player partyMember : party.getMembers()) {
                  if (!Util.checkIfInRange(300, npc, partyMember, true)) {
                     return "32343-02b.htm";
                  }
               }

               if (player.destroyItemByItemId("Quest", 9714, 1L, npc, true)) {
                  npc.setBusy(true);
                  Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
                  inst.setDuration(300000);
                  inst.setEmptyDestroyTime(0L);
                  ThreadPoolManager.getInstance().schedule(new HellboundTown.ExitInstance(party, world), 285000L);
                  htmltext = "32343-02d.htm";
               }
            } else {
               htmltext = "32343-02a.htm";
            }
         }
      }

      return htmltext;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
         HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
         if (npc.getId() == 32358) {
            if (event.equalsIgnoreCase("rebuff") && !world.isAmaskariDead) {
               new SkillHolder(4616, 1).getSkill().getEffects(npc, npc, false);
            } else if (event.equalsIgnoreCase("break_chains")) {
               if (npc.getFirstEffect(4611) != null && !world.isAmaskariDead) {
                  this.cancelQuestTimer("rebuff", npc, null);

                  for(Effect e : npc.getAllEffects()) {
                     if (e.getSkill() == new SkillHolder(4616, 1).getSkill()) {
                        e.exit();
                     }
                  }

                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NATIVES_NPCSTRING_ID[0]), 2000);
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NATIVES_NPCSTRING_ID[1]), 2000);
                  HellboundManager.getInstance().updateTrust(10, true);
                  npc.scheduleDespawn(3000L);
                  if (world.spawnedAmaskari != null
                     && !world.spawnedAmaskari.isDead()
                     && getRandom(1000) < 25
                     && Util.checkIfInRange(5000, npc, world.spawnedAmaskari, false)) {
                     if (world.activeAmaskariCall != null) {
                        world.activeAmaskariCall.cancel(true);
                     }

                     world.activeAmaskariCall = ThreadPoolManager.getInstance().schedule(new HellboundTown.CallAmaskari(npc), 25000L);
                  }
               } else {
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NATIVES_NPCSTRING_ID[0]), 2000);
                  npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NATIVES_NPCSTRING_ID[2]), 2000);
               }
            }
         }
      }

      return null;
   }

   @Override
   public final String onSpawn(Npc npc) {
      if (npc.getId() == 32358) {
         ((QuestGuardInstance)npc).setPassive(true);
         ((QuestGuardInstance)npc).setAutoAttackable(false);
         new SkillHolder(4616, 1).getSkill().getEffects(npc, npc, false);
         this.startQuestTimer("rebuff", 357000L, npc, null);
      } else if (npc.getId() == 22359 || npc.getId() == 22361) {
         npc.setBusy(false);
         npc.setBusyMessage("");
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
         HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
         if (!npc.isBusy()) {
            npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NPCSTRING_ID[0]), 2000);
            npc.setBusy(true);
            if (world.spawnedAmaskari != null
               && !world.spawnedAmaskari.isDead()
               && getRandom(1000) < 25
               && Util.checkIfInRange(1000, npc, world.spawnedAmaskari, false)) {
               if (world.activeAmaskariCall != null) {
                  world.activeAmaskariCall.cancel(true);
               }

               world.activeAmaskariCall = ThreadPoolManager.getInstance().schedule(new HellboundTown.CallAmaskari(npc), 25000L);
            }
         }
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
         HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
         if (!world.isAmaskariDead && !npc.getBusyMessage().equalsIgnoreCase("atk") && !npc.isBusy()) {
            int msgId;
            int range;
            switch(npc.getId()) {
               case 22359:
                  msgId = 0;
                  range = 1000;
                  break;
               case 22361:
                  msgId = 1;
                  range = 5000;
                  break;
               default:
                  msgId = -1;
                  range = 0;
            }

            if (msgId >= 0) {
               npc.broadcastPacket(new NpcSay(npc.getObjectId(), 22, npc.getId(), NPCSTRING_ID[msgId]), 2000);
            }

            npc.setBusy(true);
            npc.setBusyMessage("atk");
            if (world.spawnedAmaskari != null
               && !world.spawnedAmaskari.isDead()
               && getRandom(1000) < 25
               && Util.checkIfInRange(range, npc, world.spawnedAmaskari, false)) {
               if (world.activeAmaskariCall != null) {
                  world.activeAmaskariCall.cancel(true);
               }

               world.activeAmaskariCall = ThreadPoolManager.getInstance().schedule(new HellboundTown.CallAmaskari(npc), 25000L);
            }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
         HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
         world.isAmaskariDead = true;
      }

      return super.onKill(npc, killer, isSummon);
   }

   private String checkConditions(Player player) {
      return HellboundManager.getInstance().getLevel() < 10 ? "32346-lvl.htm" : null;
   }

   public static void main(String[] args) {
      new HellboundTown(HellboundTown.class.getSimpleName(), "instances");
   }

   private static class CallAmaskari implements Runnable {
      private final Npc _caller;

      public CallAmaskari(Npc caller) {
         this._caller = caller;
      }

      @Override
      public void run() {
         if (this._caller != null && !this._caller.isDead()) {
            ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(this._caller.getReflectionId());
            if (tmpworld != null && tmpworld instanceof HellboundTown.TownWorld) {
               HellboundTown.TownWorld world = (HellboundTown.TownWorld)tmpworld;
               if (world.spawnedAmaskari != null && !world.spawnedAmaskari.isDead()) {
                  world.spawnedAmaskari.teleToLocation(this._caller.getX(), this._caller.getY(), this._caller.getZ(), true);
                  world.spawnedAmaskari
                     .broadcastPacket(
                        new NpcSay(
                           world.spawnedAmaskari.getObjectId(),
                           22,
                           world.spawnedAmaskari.getId(),
                           NpcStringId.ILL_MAKE_YOU_FEEL_SUFFERING_LIKE_A_FLAME_THAT_IS_NEVER_EXTINGUISHED
                        ),
                        2000
                     );
               }
            }
         }
      }
   }

   private class ExitInstance implements Runnable {
      private final Party _party;
      private final HellboundTown.TownWorld _world;

      public ExitInstance(Party party, HellboundTown.TownWorld world) {
         this._party = party;
         this._world = world;
      }

      @Override
      public void run() {
         if (this._party != null && this._world != null) {
            for(Player partyMember : this._party.getMembers()) {
               if (partyMember != null && !partyMember.isDead()) {
                  this._world.removeAllowed(partyMember.getObjectId());
                  HellboundTown.this.teleportPlayer(partyMember, new Location(16262, 283651, -9700), 0);
               }
            }
         }
      }
   }

   private class TownWorld extends ReflectionWorld {
      protected MonsterInstance spawnedAmaskari;
      protected ScheduledFuture<?> activeAmaskariCall = null;
      public boolean isAmaskariDead = false;

      public TownWorld() {
      }
   }
}
