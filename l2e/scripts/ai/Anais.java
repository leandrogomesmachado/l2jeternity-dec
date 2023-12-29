package l2e.scripts.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.serverpackets.NpcInfo;

public class Anais extends AbstractNpcAI {
   private static final int ANAIS = 25701;
   private static final int GUARD = 25702;
   private static boolean FIGHTHING = false;
   private final List<Npc> burners = new ArrayList<>();
   private final List<Npc> guards = new ArrayList<>();
   private final Map<Npc, Player> targets = new HashMap<>();
   private static int BURNERS_ENABLED = 0;
   private static final int[][] BURNERS = new int[][]{{113632, -75616, 50}, {111904, -75616, 58}, {111904, -77424, 51}, {113696, -77393, 48}};
   Skill guard_skill = SkillsParser.getInstance().getInfo(6326, 1);

   public Anais(String name, String descr) {
      super(name, descr);
      this.registerMobs(new int[]{25701, 25702});
      this.spawnBurners();
   }

   private void spawnBurners() {
      for(int[] SPAWN : BURNERS) {
         Npc npc = addSpawn(18915, SPAWN[0], SPAWN[1], SPAWN[2], 0, false, 0L);
         if (npc != null) {
            this.burners.add(npc);
         }
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("check_status")) {
         if (FIGHTHING) {
            if (npc.getAI().getIntention() != CtrlIntention.ACTIVE && npc.getAI().getIntention() != CtrlIntention.IDLE) {
               this.startQuestTimer("check_status", 50000L, npc, null);
            } else {
               this.stopFight();
            }
         }
      } else if (event.equalsIgnoreCase("burner_action")) {
         if (FIGHTHING && npc != null) {
            Npc guard = addSpawn(25702, npc);
            if (guard != null) {
               this.guards.add(guard);
               this.startQuestTimer("guard_action", 500L, guard, null);
            }

            this.startQuestTimer("burner_action", 20000L, npc, null);
         }
      } else if (event.equalsIgnoreCase("guard_action") && FIGHTHING && npc != null && !npc.isDead()) {
         if (this.targets.containsKey(npc)) {
            Player target = this.targets.get(npc);
            if (target != null && target.isOnline() && target.isInsideRadius(npc, 5000, false, false)) {
               npc.setIsRunning(true);
               npc.setTarget(target);
               if (target.isInsideRadius(npc, 200, false, false)) {
                  npc.doCast(this.guard_skill);
               } else {
                  npc.getAI().setIntention(CtrlIntention.FOLLOW, target);
               }
            } else {
               npc.deleteMe();
               if (this.targets.containsKey(npc)) {
                  this.targets.remove(npc);
               }
            }
         } else {
            List<Player> result = new ArrayList<>();
            Player target = null;

            for(Player pl : World.getInstance().getAroundPlayers(npc, 3000, 200)) {
               if (pl != null && !pl.isAlikeDead() && pl.isInsideRadius(npc, 3000, true, false) && GeoEngine.canSeeTarget(npc, pl, false)) {
                  result.add(pl);
               }
            }

            if (!result.isEmpty()) {
               target = result.get(getRandom(result.size() - 1));
            }

            if (target != null) {
               npc.setTitle(target.getName());
               npc.broadcastPacket(new NpcInfo.Info(npc, target));
               npc.setIsRunning(true);
               this.targets.put(npc, target);
            }
         }

         this.startQuestTimer("guard_action", 1000L, npc, null);
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon) {
      if (npc.getId() == 25701) {
         if (!FIGHTHING) {
            FIGHTHING = true;
            this.startQuestTimer("check_status", 50000L, npc, null);
         } else if (getRandom(10) == 0 && BURNERS_ENABLED < 4) {
            this.checkBurnerStatus(npc);
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      if (npc.getId() == 25702) {
         if (this.guards.contains(npc)) {
            this.guards.remove(npc);
         }

         npc.doDie(npc);
         npc.deleteMe();
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (npc.getId() == 25701) {
         this.stopFight();
      }

      return super.onKill(npc, killer, isSummon);
   }

   private synchronized void checkBurnerStatus(Npc anais) {
      switch(BURNERS_ENABLED) {
         case 0:
            this.enableBurner(1);
            BURNERS_ENABLED = 1;
            break;
         case 1:
            if (!(anais.getCurrentHp() > anais.getMaxHp() * 0.75)) {
               this.enableBurner(2);
               BURNERS_ENABLED = 2;
            }
            break;
         case 2:
            if (!(anais.getCurrentHp() > anais.getMaxHp() * 0.5)) {
               this.enableBurner(3);
               BURNERS_ENABLED = 3;
            }
            break;
         case 3:
            if (!(anais.getCurrentHp() > anais.getMaxHp() * 0.25)) {
               this.enableBurner(4);
               BURNERS_ENABLED = 4;
            }
      }
   }

   private void enableBurner(int index) {
      if (!this.burners.isEmpty()) {
         Npc burner = this.burners.get(index - 1);
         if (burner != null) {
            burner.setDisplayEffect(1);
            this.startQuestTimer("burner_action", 1000L, burner, null);
         }
      }
   }

   private void stopFight() {
      if (!this.targets.isEmpty()) {
         this.targets.clear();
      }

      if (!this.burners.isEmpty()) {
         for(Npc burner : this.burners) {
            if (burner != null) {
               burner.setDisplayEffect(2);
            }
         }
      }

      if (!this.guards.isEmpty()) {
         for(Npc guard : this.guards) {
            if (guard != null) {
               guard.deleteMe();
            }
         }
      }

      this.cancelQuestTimers("guard_action");
      this.cancelQuestTimers("burner_action");
      BURNERS_ENABLED = 0;
      FIGHTHING = false;
   }

   public static void main(String[] args) {
      new Anais(Anais.class.getSimpleName(), "ai");
   }
}
