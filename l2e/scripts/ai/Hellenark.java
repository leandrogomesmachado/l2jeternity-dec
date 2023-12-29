package l2e.scripts.ai;

import java.util.ArrayList;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;

public class Hellenark extends AbstractNpcAI {
   private static final int Hellenark = 22326;
   private static final int naia = 18484;
   private int status = 0;
   public ArrayList<Npc> spawnnaia = new ArrayList<>();
   private static final int[][] naialoc = new int[][]{
      {-24542, 245792, -3133, 19078},
      {-23839, 246056, -3133, 17772},
      {-23713, 244358, -3133, 53369},
      {-23224, 244524, -3133, 57472},
      {-24709, 245186, -3133, 63974},
      {-24394, 244379, -3133, 5923}
   };

   public Hellenark(String name, String descr) {
      super(name, descr);
      this.addAttackId(22326);
      this.addTalkId(18484);
      this.addFirstTalkId(18484);
      this.addStartNpc(18484);
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon, Skill skill) {
      if (npc.getId() == 22326) {
         if (this.status == 0) {
            this.startQuestTimer("spawn", 20000L, npc, null, false);
         }

         this.status = 1;
      }

      return null;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return null;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = "";
      if (event.equalsIgnoreCase("spawn")) {
         if (this.status == 1) {
            this.status = 3;
         }

         this.startQuestTimer("check", 30000L, npc, null, false);

         for(int i = 0; i < 6; ++i) {
            Npc mob = addSpawn(18484, naialoc[i][0], naialoc[i][1], naialoc[i][2], naialoc[i][3], false, 0L);
            this.spawnnaia.add(mob);
            mob.setIsInvul(true);
            mob.setIsImmobilized(true);
            mob.setIsOverloaded(true);
         }

         this.startQuestTimer("cast", 5000L, npc, null, false);
      }

      if (event.equalsIgnoreCase("check")) {
         if (this.status == 1) {
            this.startQuestTimer("check", 180000L, npc, null, false);
         }

         if (this.status == 3) {
            this.startQuestTimer("desp", 180000L, npc, null, false);
         }

         this.status = 3;
      }

      if (event.equalsIgnoreCase("desp")) {
         this.cancelQuestTimers("cast");

         for(Npc npc1 : this.spawnnaia) {
            npc1.deleteMe();
         }

         this.status = 0;
      }

      if (event.equalsIgnoreCase("cast")) {
         for(Npc npc1 : this.spawnnaia) {
            npc1.setTarget(player);
            npc1.doCast(SkillsParser.getInstance().getInfo(5765, 1));
         }

         this.startQuestTimer("cast", 5000L, npc, null, false);
      }

      return "";
   }

   public static void main(String[] args) {
      new Hellenark(Hellenark.class.getSimpleName(), "ai");
   }
}
