package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.scripts.ai.AbstractNpcAI;

public final class FortressArcherCaptain extends AbstractNpcAI {
   private static final int[] ARCHER_CAPTAIN = new int[]{
      35661, 35692, 35730, 35761, 35799, 35830, 35861, 35899, 35930, 35968, 36006, 36037, 36075, 36113, 36144, 36175, 36213, 36251, 36289, 36320, 36358
   };

   private FortressArcherCaptain() {
      super(FortressArcherCaptain.class.getSimpleName(), "custom");
      this.addStartNpc(ARCHER_CAPTAIN);
      this.addFirstTalkId(ARCHER_CAPTAIN);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      int fortOwner = npc.getFort().getOwnerClan() == null ? 0 : npc.getFort().getOwnerClan().getId();
      return player.getClan() != null && player.getClanId() == fortOwner ? "FortressArcherCaptain.htm" : "FortressArcherCaptain-01.htm";
   }

   public static void main(String[] args) {
      new FortressArcherCaptain();
   }
}
