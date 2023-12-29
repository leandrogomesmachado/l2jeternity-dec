package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka30_40 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 4, 13002, 4, 13002, 4, 13002, 4, 13002, 4};
   private static final int[] REW2 = new int[]{10840, 1, 10841, 1, 10842, 1, 10843, 1, 10844, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka30_40(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka30_40";
      this.param.instanceId = 48;
      this.param.rewPosition = new Location(23478, -220079, -7799);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22458, 22459, 22460});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22458, new int[]{22459, 22460});
   }

   public static void main(String[] args) {
      new Kamaloka30_40(Kamaloka30_40.class.getSimpleName(), "Kamaloka30_40");
   }
}
