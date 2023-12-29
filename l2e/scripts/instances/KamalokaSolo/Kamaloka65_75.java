package l2e.scripts.instances.KamalokaSolo;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;

public class Kamaloka65_75 extends KamalokaSolo {
   private static final int[] REW1 = new int[]{13002, 8, 13002, 8, 13002, 8, 13002, 8, 13002, 8};
   private static final int[] REW2 = new int[]{12832, 1, 10857, 1, 10858, 1, 10861, 1, 12834, 1};
   private final KamalokaSolo.KamaParam param = new KamalokaSolo.KamaParam();

   public Kamaloka65_75(String name, String descr) {
      super(name, descr);
      this.param.qn = "Kamaloka65_75";
      this.param.instanceId = 55;
      this.param.rewPosition = new Location(42638, -219781, -8759);
      this.addStartNpc(32484);
      this.addTalkId(new int[]{32484, 32485});
      this.addKillId(new int[]{22479, 22480, 22481});
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
      return this.onKillTo(npc, player, isPet, this.param.qn, 22479, new int[]{22480, 22481});
   }

   public static void main(String[] args) {
      new Kamaloka65_75(Kamaloka65_75.class.getSimpleName(), "Kamaloka65_75");
   }
}
