package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import org.apache.commons.lang.ArrayUtils;

public class Maguen extends AbstractNpcAI {
   private static final int MAGUEN = 18839;
   private static final int[] MOBS = new int[]{22746, 22747, 22748, 22749, 22754, 22755, 22756, 22760, 22761, 22762};
   private static final int[] maguenStatsSkills = new int[]{6343, 6365, 6366};
   private static final int[] maguenRaceSkills = new int[]{6367, 6368, 6369};

   public Maguen(String name, String descr) {
      super(name, descr);
      this.addSpawnId(new int[]{18839});
      this.addSkillSeeId(new int[]{18839});

      for(int i : MOBS) {
         this.addKillId(i);
      }
   }

   @Override
   public String onSpawn(Npc npc) {
      ThreadPoolManager.getInstance().schedule(new Maguen.Plasma(npc), 2000L);
      return super.onSpawn(npc);
   }

   @Deprecated
   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (npc.getId() == 18839) {
         if (skill.getId() != 9060) {
            return null;
         }

         if (Rnd.chance(4)) {
            caster.addItem("Maguen", 15490, 1L, null, true);
         }

         if (Rnd.chance(2)) {
            caster.addItem("Maguen", 15491, 1L, null, true);
         }

         ZoneType zone = this.getZone(npc, "Seed of Annihilation", true);
         if (zone != null) {
            for(Creature ch : zone.getCharactersInside()) {
               if (ch != null && !ch.isDead()) {
                  npc.setTarget(caster);
                  switch(npc.getDisplayEffect()) {
                     case 1:
                        if (Rnd.chance(80)) {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[0], getRandom(2, 3)));
                        } else {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenStatsSkills[0], getRandom(1, 2)));
                        }
                        break;
                     case 2:
                        if (Rnd.chance(80)) {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[1], getRandom(2, 3)));
                        } else {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenStatsSkills[1], getRandom(1, 2)));
                        }
                        break;
                     case 3:
                        if (Rnd.chance(80)) {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[2], getRandom(2, 3)));
                        } else {
                           npc.doCast(SkillsParser.getInstance().getInfo(maguenStatsSkills[2], getRandom(1, 2)));
                        }
                  }
               } else {
                  switch(npc.getDisplayEffect()) {
                     case 1:
                        npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[0], 1));
                        break;
                     case 2:
                        npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[1], 1));
                        break;
                     case 3:
                        npc.doCast(SkillsParser.getInstance().getInfo(maguenRaceSkills[2], 1));
                  }
               }
            }
         }
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (ArrayUtils.contains(MOBS, npc.getId()) && Rnd.chance(5)) {
         Npc maguen = addSpawn(18839, npc.getX() + getRandom(10, 50), npc.getY() + getRandom(10, 50), npc.getZ(), 0, false, 10000L, true);
         maguen.setRunning();
         ((Attackable)maguen).addDamageHate(killer, 1, 99999);
         maguen.getAI().setIntention(CtrlIntention.ATTACK, killer);
         killer.sendPacket(new ExShowScreenMessage(NpcStringId.MAGUEN_APPEARANCE, 2, 5000));
      }

      return super.onKill(npc, killer, isSummon);
   }

   private ZoneType getZone(Npc npc, String nameTemplate, boolean currentLoc) {
      try {
         int x;
         int y;
         int z;
         if (currentLoc) {
            x = npc.getX();
            y = npc.getY();
            z = npc.getZ();
         } else {
            x = npc.getSpawn().getX();
            y = npc.getSpawn().getY();
            z = npc.getSpawn().getZ();
         }

         for(ZoneType zone : ZoneManager.getInstance().getZones(x, y, z)) {
            if (zone.getName().startsWith(nameTemplate)) {
               return zone;
            }
         }
      } catch (NullPointerException var9) {
      } catch (IndexOutOfBoundsException var10) {
      }

      return null;
   }

   public static void main(String[] args) {
      new Maguen(Maguen.class.getSimpleName(), "ai");
   }

   private class Plasma implements Runnable {
      private final Npc _npc;

      public Plasma(Npc npc) {
         this._npc = npc;
      }

      @Override
      public void run() {
         this._npc.setDisplayEffect(Quest.getRandom(1, 3));
      }
   }
}
