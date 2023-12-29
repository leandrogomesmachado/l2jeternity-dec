package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.model.zone.type.ClanHallZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class CHSiegeManager {
   private static final Logger _log = Logger.getLogger(CHSiegeManager.class.getName());
   private static final String SQL_LOAD_HALLS = "SELECT * FROM siegable_clanhall";
   private final Map<Integer, SiegableHall> _siegableHalls = new HashMap<>();

   protected CHSiegeManager() {
      this.loadClanHalls();
   }

   private final void loadClanHalls() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM siegable_clanhall");
         ResultSet rs = statement.executeQuery();
         this._siegableHalls.clear();

         while(rs.next()) {
            int id = rs.getInt("id");
            StatsSet set = new StatsSet();
            set.set("id", id);
            set.set("ownerId", rs.getInt("ownerId"));
            set.set("grade", rs.getInt("Grade"));
            set.set("nextSiege", rs.getLong("nextSiege"));
            set.set("siegeLenght", rs.getLong("siegeLenght"));
            set.set("scheduleConfig", rs.getString("schedule_config"));
            SiegableHall hall = new SiegableHall(set);
            this._siegableHalls.put(id, hall);
            ClanHallManager.addClanHall(hall);
         }

         _log.info(this.getClass().getSimpleName() + ": Loaded " + this._siegableHalls.size() + " siege clan halls.");
         rs.close();
         statement.close();
      } catch (Exception var18) {
         _log.warning("CHSiegeManager: Could not load siegable clan halls!:");
      }
   }

   public Map<Integer, SiegableHall> getConquerableHalls() {
      return this._siegableHalls;
   }

   public SiegableHall getSiegableHall(int clanHall) {
      return this.getConquerableHalls().get(clanHall);
   }

   public final SiegableHall getNearbyClanHall(Creature activeChar) {
      return this.getNearbyClanHall(activeChar.getX(), activeChar.getY(), 10000);
   }

   public final SiegableHall getNearbyClanHall(int x, int y, int maxDist) {
      ClanHallZone zone = null;

      for(Entry<Integer, SiegableHall> ch : this._siegableHalls.entrySet()) {
         ClanHallZone var7 = ch.getValue().getZone();
         if (var7 != null && var7.getDistanceToZone(x, y) < (double)maxDist) {
            return ch.getValue();
         }
      }

      return null;
   }

   public final ClanHallSiegeEngine getSiege(Creature character) {
      SiegableHall hall = this.getNearbyClanHall(character);
      return hall == null ? null : hall.getSiege();
   }

   public final void registerClan(Clan clan, SiegableHall hall, Player player) {
      if (clan.getLevel() < Config.CHS_CLAN_MINLEVEL) {
         player.sendMessage("Only clans of level " + Config.CHS_CLAN_MINLEVEL + " or higher may register for a castle siege");
      } else if (hall.isWaitingBattle()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
         sm.addString(Util.clanHallName(player, hall.getId()));
         player.sendPacket(sm);
      } else if (hall.isInSiege()) {
         player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
      } else if (hall.getOwnerId() == clan.getId()) {
         player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
      } else if (clan.getHideoutId() != 0) {
         player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
      } else if (hall.getSiege().checkIsAttacker(clan)) {
         player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
      } else if (this.isClanParticipating(clan)) {
         player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
      } else if (hall.getSiege().getAttackers().size() >= Config.CHS_MAX_ATTACKERS) {
         player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
      } else {
         hall.addAttacker(clan);
      }
   }

   public final void unRegisterClan(Clan clan, SiegableHall hall) {
      if (hall.isRegistering()) {
         hall.removeAttacker(clan);
      }
   }

   public final boolean isClanParticipating(Clan clan) {
      for(SiegableHall hall : this.getConquerableHalls().values()) {
         if (hall.getSiege() != null && hall.getSiege().checkIsAttacker(clan)) {
            return true;
         }
      }

      return false;
   }

   public final void onServerShutDown() {
      for(SiegableHall hall : this.getConquerableHalls().values()) {
         if (hall.getSiege() != null) {
            hall.getSiege().saveAttackers();
         }
      }
   }

   public static CHSiegeManager getInstance() {
      return CHSiegeManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CHSiegeManager _instance = new CHSiegeManager();
   }
}
