package l2e.scripts.clanhallsiege;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2e.commons.util.Util;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class FortressOfResistanceSiege extends ClanHallSiegeEngine {
   private static final String qn = "FortressOfResistanceSiege";
   private final int MESSENGER = 35382;
   private final int BLOODY_LORD_NURKA = 35375;
   private final Location[] NURKA_COORDS = new Location[]{
      new Location(45109, 112124, -1900), new Location(47653, 110816, -2110), new Location(47247, 109396, -2000)
   };
   private Spawner _nurka;
   private final Map<Integer, Long> _damageToNurka = new HashMap<>();

   public FortressOfResistanceSiege(int questId, String name, String descr, int hallId) {
      super(questId, name, descr, hallId);
      this.addFirstTalkId(35382);
      this.addKillId(35375);
      this.addAttackId(35375);

      try {
         this._nurka = new Spawner(NpcsParser.getInstance().getTemplate(35375));
         this._nurka.setAmount(1);
         this._nurka.setRespawnDelay(10800);
         this._nurka.setLocation(this.NURKA_COORDS[0]);
      } catch (Exception var6) {
         this._log.warning(this.getName() + ": Couldnt set the Bloody Lord Nurka spawn");
         var6.printStackTrace();
      }
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      if (npc.getId() == 35382) {
         NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
         html.setFile(player, player.getLang(), "data/html/default/35382.htm");
         html.replace("%nextSiege%", Util.formatDate(this._hall.getSiegeDate().getTime(), "yyyy-MM-dd HH:mm:ss"));
         player.sendPacket(html);
         return null;
      } else {
         return super.onFirstTalk(npc, player);
      }
   }

   @Override
   public String onAttack(Npc npc, Player player, int damage, boolean isSummon) {
      if (!this._hall.isInSiege()) {
         return null;
      } else {
         int clanId = player.getId();
         if (clanId > 0) {
            long clanDmg = this._damageToNurka.containsKey(clanId) ? this._damageToNurka.get(clanId) + (long)damage : (long)damage;
            this._damageToNurka.put(clanId, clanDmg);
         }

         return null;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (!this._hall.isInSiege()) {
         return null;
      } else {
         this._missionAccomplished = true;
         synchronized(this) {
            npc.getSpawn().stopRespawn();
            npc.deleteMe();
            this.cancelSiegeTask();
            this.endSiege();
            return null;
         }
      }
   }

   @Override
   public Clan getWinner() {
      int winnerId = 0;
      long counter = 0L;

      for(Entry<Integer, Long> e : this._damageToNurka.entrySet()) {
         long dam = e.getValue();
         if (dam > counter) {
            winnerId = e.getKey();
            counter = dam;
         }
      }

      return ClanHolder.getInstance().getClan(winnerId);
   }

   @Override
   public void onSiegeStarts() {
      this._nurka.init();
   }

   public static void main(String[] args) {
      new FortressOfResistanceSiege(-1, "FortressOfResistanceSiege", "clanhallsiege", 21);
   }
}
