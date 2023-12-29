package l2e.scripts.ai;

import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.DefaultAI;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.skills.Skill;

public class KrateisCubeWatcherRed extends DefaultAI {
   private static final int[][] SKILLS = new int[][]{{1064, 14}, {1160, 15}, {1164, 19}, {1167, 6}, {1168, 7}};

   public KrateisCubeWatcherRed(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtThink() {
      Attackable actor = this.getActiveChar();

      for(Player cha : World.getInstance().getAroundPlayers(actor, 600, 200)) {
         if (!cha.isDead() && Rnd.chance(25)) {
            int rnd = Rnd.get(SKILLS.length);
            Skill skill = SkillsParser.getInstance().getInfo(SKILLS[rnd][0], SKILLS[rnd][1]);
            if (skill != null) {
               skill.getEffects(cha, cha, false);
            }
         }
      }
   }

   @Override
   public void onEvtDead(Creature killer) {
      final Attackable actor = this.getActiveChar();
      super.onEvtDead(killer);
      actor.deleteMe();
      ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
         @Override
         public void runImpl() throws Exception {
            NpcTemplate template = NpcsParser.getInstance().getTemplate(18602);
            if (template != null) {
               MonsterInstance a = new MonsterInstance(IdFactory.getInstance().getNextId(), template);
               a.setCurrentHpMp(a.getMaxHp(), a.getMaxMp());
               a.setLocation(actor.getLocation());
               a.spawnMe();
            }
         }
      }, 10000L);
   }
}
