package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka60_70 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 7, 13002, 7, 13002, 7, 13002, 7, 13002, 7};
   private static final int[] REW2 = new int[]{10855, 1, 10856, 1, 10857, 1, 10858, 1, 10859, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka60_70(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka60_70";
      this.param.instanceId = 54;
      this.param.rewPosition = new Location(23229, -206316, -7991);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22476, 22477, 22478});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      return this.onAdvEventTo(event, npc, player, this.param.qn, REW1, REW2);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      return npc.getId() == 32484 ? this.onEnterTo(npc, player, this.param) : this.onTalkTo(npc, player, this.param.qn);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      return this.onKillTo(npc, player, isPet, this.param.qn, 22476, new int[]{22477, 22478});
   }

   public static void main(String[] args) {
      new Kamaloka60_70(Kamaloka60_70.class.getSimpleName(), "Kamaloka60_70");
   }
}
