package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.serverpackets.MagicSkillUse;

public class PailakaDevilsLegacy extends AbstractReflection {
   public PailakaDevilsLegacy(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32498);
      this.addTalkId(32498);
      this.addSpawnId(new int[]{18634});
      this.addAttackId(new int[]{18622, 18633, 32495});
      this.addKillId(new int[]{18633, 18634, 32495, 18622});
      this.addEnterZoneId(new int[]{20109});
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

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new PailakaDevilsLegacy.PDLWorld(), 44)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((PailakaDevilsLegacy.PDLWorld)world)._lematanNpc = addSpawn(18633, 88108, -209252, -3744, 64255, false, 0L, false, world.getReflectionId());
      }
   }

   @Override
   protected void attackPlayer(Attackable npc, Playable attacker) {
      npc.setIsRunning(true);
      npc.addDamageHate(attacker, 0, 999);
      npc.getAI().setIntention(CtrlIntention.ATTACK, attacker);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("enter")) {
         this.enterInstance(player, npc);
         QuestState qs = player.getQuestState("_129_PailakaDevilsLegacy");
         if (qs != null && qs.isCond(1)) {
            qs.setCond(2, true);
         }

         return null;
      } else {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof PailakaDevilsLegacy.PDLWorld) {
            PailakaDevilsLegacy.PDLWorld world = (PailakaDevilsLegacy.PDLWorld)tmpworld;
            if (npc.getId() == 18634 && event.equals("follower_cast")) {
               if (!npc.isCastingNow() && !npc.isDead() && !world._lematanNpc.isDead()) {
                  npc.setTarget(world._lematanNpc);
                  npc.doCast(SkillsParser.getInstance().getInfo(5712, 1));
               }

               this.startQuestTimer("follower_cast", (long)(2000 + getRandom(100, 1000)), npc, null);
               return null;
            }

            if (event.equalsIgnoreCase("first_anim")) {
               if (!npc.isCastingNow() && !npc.isDead() && !world._lematanNpc.isDead()) {
                  npc.broadcastPacket(new MagicSkillUse(npc, npc, 5756, 1, 2500, 0));
               }

               return null;
            }

            if (event.equalsIgnoreCase("respawnMinions")) {
               if (world._lematanNpc != null && !world._lematanNpc.isDead()) {
                  int radius = 260;
                  int rnd = Rnd.get(6);
                  int x = (int)(260.0 * Math.cos((double)rnd * 0.918));
                  int y = (int)(260.0 * Math.sin((double)rnd * 0.918));
                  Npc mob = addSpawn(
                     18634,
                     world._lematanNpc.getX() + x,
                     world._lematanNpc.getY() + y,
                     world._lematanNpc.getZ(),
                     0,
                     false,
                     0L,
                     true,
                     world._lematanNpc.getReflectionId()
                  );
                  if (mob != null) {
                     world._followerslist.add(mob);
                  }
               }

               return null;
            }

            if (npc.getId() == 18622 && event.equalsIgnoreCase("keg_trigger")) {
               this.onAttack(npc, player, 600, false);
               return null;
            }

            if (event.equalsIgnoreCase("lematan_teleport")) {
               if (npc.getId() == 18633 && !npc.isMovementDisabled() && !world._isOnShip) {
                  ((Attackable)npc).reduceHate(player, 9999);
                  ((Attackable)npc).abortAttack();
                  ((Attackable)npc).abortCast();
                  npc.broadcastPacket(new MagicSkillUse(npc, 2100, 1, 1000, 0));
                  this.startQuestTimer("lematan_finish_teleport", 1500L, npc, player);
                  return null;
               }

               world._isTeleportScheduled = false;
               return null;
            }

            if (npc.getId() == 18633 && event.equalsIgnoreCase("lematan_finish_teleport") && !world._isOnShip) {
               npc.teleToLocation(84982, -208690, -3337, true);
               world._isOnShip = true;
               npc.getSpawn().setX(84982);
               npc.getSpawn().setY(-208690);
               npc.getSpawn().setZ(-3337);
               ((Attackable)npc).reduceHate(player, 9999);
               world._followerslist = new ArrayList<>();

               for(int i = 0; i < 6; ++i) {
                  int radius = 260;
                  int x = (int)(260.0 * Math.cos((double)i * 0.918));
                  int y = (int)(260.0 * Math.sin((double)i * 0.918));
                  Npc mob = addSpawn(18634, 84982 + x, -208690 + y, -3337, 0, false, 0L, true, player.getReflectionId());
                  if (mob != null) {
                     world._followerslist.add(mob);
                  }
               }

               return null;
            }
         }

         return event;
      }
   }

   @Override
   public final String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof PailakaDevilsLegacy.PDLWorld) {
         PailakaDevilsLegacy.PDLWorld world = (PailakaDevilsLegacy.PDLWorld)tmpworld;
         if (npc.getId() == 18622 && !npc.isDead()) {
            npc.doCast(SkillsParser.getInstance().getInfo(5714, 1));

            for(Creature target : World.getInstance().getAroundCharacters(npc, 900, 200)) {
               target.reduceCurrentHp((double)(500 + getRandom(0, 200)), npc, SkillsParser.getInstance().getInfo(5714, 1));
               if (target instanceof MonsterInstance) {
                  if (((MonsterInstance)target).getId() == 18622) {
                     this.startQuestTimer("keg_trigger", 500L, (Npc)target, attacker);
                  } else if (isSummon) {
                     this.attackPlayer((Attackable)npc, attacker.getSummon());
                  } else {
                     this.attackPlayer((Attackable)npc, attacker);
                  }
               }
            }

            if (!npc.isDead()) {
               npc.doDie(attacker);
            }
         } else if (npc.getId() == 18633 && npc.getCurrentHp() < npc.getMaxHp() / 2.0 && !world._isTeleportScheduled) {
            this.startQuestTimer("lematan_teleport", 1000L, npc, attacker);
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_129_PailakaDevilsLegacy");
      if (st != null && st.getState() == 1) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld != null && tmpworld instanceof PailakaDevilsLegacy.PDLWorld) {
            PailakaDevilsLegacy.PDLWorld world = (PailakaDevilsLegacy.PDLWorld)tmpworld;
            switch(npc.getId()) {
               case 18622:
               case 18634:
               case 32495:
                  if (world._isOnShip) {
                     if (world._followerslist.contains(npc)) {
                        world._followerslist.remove(npc);
                     }

                     this.startQuestTimer("respawnMinions", 10000L, npc, null);
                  }
                  break;
               case 18633:
                  if (world._followerslist != null && !world._followerslist.isEmpty()) {
                     for(Npc _follower : world._followerslist) {
                        _follower.deleteMe();
                     }

                     world._followerslist.clear();
                  }

                  st.setCond(4, true);
                  addSpawn(32511, 84983, -208736, -3336, 49915, false, 0L, false, npc.getReflectionId());
            }
         }

         return super.onKill(npc, player, isSummon);
      } else {
         return null;
      }
   }

   @Override
   public final String onSpawn(Npc npc) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof PailakaDevilsLegacy.PDLWorld) {
         this.startQuestTimer("first_anim", 500L, npc, null);
         this.startQuestTimer("follower_cast", (long)(1000 + getRandom(100, 1000)), npc, null);
         npc.disableCoreAI(true);
      }

      return null;
   }

   @Override
   public String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer() && !character.isDead() && !character.isTeleporting() && ((Player)character).isOnline()) {
         ReflectionWorld world = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (world != null && world.getTemplateId() == 44) {
            ThreadPoolManager.getInstance().schedule(new PailakaDevilsLegacy.Teleport(character, world.getReflectionId()), 1000L);
         }
      }

      return super.onEnterZone(character, zone);
   }

   public static void main(String[] args) {
      new PailakaDevilsLegacy(PailakaDevilsLegacy.class.getSimpleName(), "instances");
   }

   private class PDLWorld extends ReflectionWorld {
      public boolean _isTeleportScheduled = false;
      public boolean _isOnShip = false;
      public Npc _lematanNpc = null;
      public List<Npc> _followerslist;

      private PDLWorld() {
      }
   }

   private static final class Teleport implements Runnable {
      private final Creature _player;
      private final int _instanceId;

      public Teleport(Creature c, int id) {
         this._player = c;
         this._instanceId = id;
      }

      @Override
      public void run() {
         if (this._player != null) {
            this._player.getAI().setIntention(CtrlIntention.IDLE);
            this._player.setReflectionId(this._instanceId);
            this._player.teleToLocation(76428, -219038, -3752, true);
         }
      }
   }
}
