package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.network.SystemMessageId;
import l2e.scripts.ai.AbstractNpcAI;

public class ArenaManager extends AbstractNpcAI {
   private static final int[] ARENA_MANAGER = new int[]{31226, 31225};
   private static final SkillHolder[] BUFFS = new SkillHolder[]{
      new SkillHolder(6805, 1),
      new SkillHolder(6806, 1),
      new SkillHolder(6807, 1),
      new SkillHolder(6808, 1),
      new SkillHolder(6804, 1),
      new SkillHolder(6812, 1)
   };
   private static final SkillHolder CP_RECOVERY = new SkillHolder(4380, 1);
   private static final SkillHolder HP_RECOVERY = new SkillHolder(6817, 1);
   private static final int CP_COST = 1000;
   private static final int HP_COST = 1000;
   private static final int BUFF_COST = 2000;

   private ArenaManager() {
      super(ArenaManager.class.getSimpleName(), "custom");
      this.addStartNpc(ARENA_MANAGER);
      this.addTalkId(ARENA_MANAGER);
      this.addFirstTalkId(ARENA_MANAGER);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "CPrecovery":
            if (player.getAdena() >= 1000L) {
               takeItems(player, 57, 1000L);
               this.startQuestTimer("CPrecovery_delay", 2000L, npc, player);
            } else {
               player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            }
            break;
         case "CPrecovery_delay":
            if (player != null && !player.isInsideZone(ZoneId.PVP)) {
               npc.setTarget(player);
               npc.doCast(CP_RECOVERY.getSkill());
            }
            break;
         case "HPrecovery":
            if (player.getAdena() >= 1000L) {
               takeItems(player, 57, 1000L);
               this.startQuestTimer("HPrecovery_delay", 2000L, npc, player);
            } else {
               player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            }
            break;
         case "HPrecovery_delay":
            if (player != null && !player.isInsideZone(ZoneId.PVP)) {
               npc.setTarget(player);
               npc.doCast(HP_RECOVERY.getSkill());
            }
            break;
         case "Buff":
            if (player.getAdena() >= 2000L) {
               takeItems(player, 57, 2000L);
               npc.setTarget(player);

               for(SkillHolder skill : BUFFS) {
                  npc.doCast(skill.getSkill());
               }
            } else {
               player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            }
      }

      return null;
   }

   public static void main(String[] args) {
      new ArenaManager();
   }
}
