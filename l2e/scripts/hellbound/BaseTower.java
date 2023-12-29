package l2e.scripts.hellbound;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.data.parser.DoorParser;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.skills.effects.Effect;

public class BaseTower extends Quest {
   private static final int GUZEN = 22362;
   private static final int KENDAL = 32301;
   private static final int BODY_DESTROYER = 22363;
   private static final Map<Integer, Player> BODY_DESTROYER_TARGET_LIST = new ConcurrentHashMap<>();
   private static final SkillHolder DEATH_WORD = new SkillHolder(5256, 1);

   public BaseTower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addKillId(22362);
      this.addKillId(22363);
      this.addFirstTalkId(32301);
      this.addAggroRangeEnterId(new int[]{22363});
   }

   @Override
   public final String onFirstTalk(Npc npc, Player player) {
      ClassId classId = player.getClassId();
      return !classId.equalsOrChildOf(ClassId.hellKnight) && !classId.equalsOrChildOf(ClassId.soultaker) ? "32301-01.htm" : "32301-02.htm";
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("close")) {
         DoorParser.getInstance().getDoor(20260004).closeMe();
      }

      return null;
   }

   @Override
   public String onAggroRangeEnter(Npc npc, Player player, boolean isSummon) {
      if (!BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId())) {
         BODY_DESTROYER_TARGET_LIST.put(npc.getObjectId(), player);
         npc.setTarget(player);
         npc.doSimultaneousCast(DEATH_WORD.getSkill());
      }

      return super.onAggroRangeEnter(npc, player, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      switch(npc.getId()) {
         case 22362:
            addSpawn(32301, npc.getSpawn().getX(), npc.getSpawn().getY(), npc.getSpawn().getZ(), 0, false, (long)npc.getSpawn().getRespawnDelay(), false);
            DoorParser.getInstance().getDoor(20260003).openMe();
            DoorParser.getInstance().getDoor(20260004).openMe();
            this.startQuestTimer("close", 60000L, npc, null, false);
            break;
         case 22363:
            if (BODY_DESTROYER_TARGET_LIST.containsKey(npc.getObjectId())) {
               Player pl = BODY_DESTROYER_TARGET_LIST.get(npc.getObjectId());
               if (pl != null && pl.isOnline() && !pl.isDead()) {
                  Effect e = pl.getFirstEffect(DEATH_WORD.getSkill());
                  if (e != null) {
                     e.exit();
                  }
               }

               BODY_DESTROYER_TARGET_LIST.remove(npc.getObjectId());
            }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new BaseTower(-1, BaseTower.class.getSimpleName(), "hellbound");
   }
}
