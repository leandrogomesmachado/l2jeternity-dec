package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka20_30 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 4, 13002, 4, 13002, 4, 13002, 4, 13002, 4};
   private static final int[] REW2 = new int[]{12824, 1, 10836, 1, 10837, 1, 10838, 1, 10844, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka20_30(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka20_30";
      this.param.instanceId = 46;
      this.param.rewPosition = new Location(9261, -219862, -8021);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22452, 22453, 22454});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22452, new int[]{22453, 22454});
   }

   public static void main(String[] args) {
      new Kamaloka20_30(Kamaloka20_30.class.getSimpleName(), "Kamaloka20_30");
   }
}
