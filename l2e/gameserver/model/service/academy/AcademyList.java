package l2e.gameserver.model.service.academy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.dbutils.DbUtils;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Functions;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.Request;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.JoinPledge;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import l2e.gameserver.network.serverpackets.PledgeShowMemberListAll;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class AcademyList {
   protected static final Logger _log = Logger.getLogger(AcademyList.class.getName());
   private static List<Player> _academyList = new ArrayList<>();

   public static void addToAcademy(Player player) {
      _academyList.add(player);
   }

   public static void deleteFromAcdemyList(Player player) {
      if (_academyList.contains(player)) {
         _academyList.remove(player);
      }
   }

   public static List<Player> getAcademyList() {
      return _academyList;
   }

   public static boolean isInAcademyList(Player player) {
      for(Player plr : _academyList) {
         if (plr != null && plr.getName().equalsIgnoreCase(player.getName())) {
            return true;
         }
      }

      return false;
   }

   public static void inviteInAcademy(Player activeChar, Player academyChar) {
      if (activeChar == null) {
         academyChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
         academyChar.sendActionFailed();
      } else {
         Request request = activeChar.getRequest();
         if (request != null) {
            if (!request.isProcessingRequest()) {
               request.onRequestResponse();
               academyChar.sendActionFailed();
            } else if (activeChar.isOutOfControl()) {
               request.onRequestResponse();
               activeChar.sendActionFailed();
            } else {
               Clan clan = activeChar.getClan();
               if (clan == null) {
                  request.onRequestResponse();
                  academyChar.sendActionFailed();
               } else if (clan.checkClanJoinCondition(activeChar, academyChar, -1)) {
                  if (clan.getAvailablePledgeTypes(-1) != 0) {
                     activeChar.sendPacket(
                        new CreatureSay(
                           activeChar.getObjectId(),
                           20,
                           ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityAcademy.ACADEMY"),
                           ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityAcademy.MSG15")
                        )
                     );
                     academyChar.sendPacket(
                        new CreatureSay(
                           academyChar.getObjectId(),
                           20,
                           ServerStorage.getInstance().getString(academyChar.getLang(), "CommunityAcademy.ACADEMY"),
                           ServerStorage.getInstance().getString(academyChar.getLang(), "CommunityAcademy.MSG16")
                        )
                     );
                  } else {
                     try {
                        if (academyChar.getPledgePrice() == 0L) {
                           return;
                        }

                        int itemId = academyChar.getPledgeItemId();
                        long price = academyChar.getPledgePrice();
                        if (activeChar.destroyItemByItemId("Academy Recruitment", itemId, price, activeChar, true)) {
                           registerAcademy(activeChar.getClan(), academyChar, itemId, price);
                           academyChar.sendPacket(new JoinPledge(activeChar.getClanId()));
                           academyChar.setPledgeType(-1);
                           academyChar.setPowerGrade(9);
                           academyChar.setLvlJoinedAcademy(academyChar.getLevel());
                           clan.addClanMember(academyChar);
                           academyChar.setClanPrivileges(academyChar.getClan().getRankPrivs(academyChar.getPowerGrade()));
                           academyChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN);
                           sm.addString(academyChar.getName());
                           clan.broadcastToOnlineMembers(sm);
                           if (academyChar.getClan().getCastleId() > 0) {
                              CastleManager.getInstance().getCastleByOwner(academyChar.getClan()).giveResidentialSkills(academyChar);
                           }

                           if (academyChar.getClan().getFortId() > 0) {
                              FortManager.getInstance().getFortByOwner(academyChar.getClan()).giveResidentialSkills(academyChar);
                           }

                           academyChar.sendSkillList(false);
                           clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(academyChar), academyChar);
                           clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                           academyChar.sendPacket(new PledgeShowMemberListAll(clan, academyChar));
                           academyChar.setClanJoinExpiryTime(0L);
                           academyChar.broadcastCharInfo();
                           deleteFromAcdemyList(academyChar);
                           return;
                        }

                        ServerMessage msg = new ServerMessage("CommunityAcademy.MSG17", activeChar.getLang());
                        msg.add(academyChar.getName());
                        ServerMessage msg1 = new ServerMessage("CommunityAcademy.MSG18", academyChar.getLang());
                        msg1.add(activeChar.getName());
                        activeChar.sendPacket(
                           new CreatureSay(
                              activeChar.getObjectId(),
                              20,
                              ServerStorage.getInstance().getString(activeChar.getLang(), "CommunityAcademy.ACADEMY"),
                              msg.toString()
                           )
                        );
                        academyChar.sendPacket(
                           new CreatureSay(
                              academyChar.getObjectId(),
                              20,
                              ServerStorage.getInstance().getString(academyChar.getLang(), "CommunityAcademy.ACADEMY"),
                              msg1.toString()
                           )
                        );
                     } finally {
                        request.onRequestResponse();
                     }
                  }
               }
            }
         }
      }
   }

   private static void registerAcademy(Clan clan, Player player, int itemId, long price) {
      Connection connection = null;
      PreparedStatement statement = null;

      try {
         connection = DatabaseFactory.getInstance().getConnection();
         statement = connection.prepareStatement("INSERT INTO character_academy (clanId,charId,itemId,price,time) values(?,?,?,?,?)");
         statement.setInt(1, clan.getId());
         statement.setInt(2, player.getObjectId());
         statement.setInt(3, itemId);
         statement.setLong(4, price);
         statement.setLong(5, System.currentTimeMillis() + Config.MAX_TIME_IN_ACADEMY * 6000L);
         statement.execute();
         deleteFromAcdemyList(player);
         ThreadPoolManager.getInstance().schedule(new AcademyList.CleanUpTask(clan, player.getObjectId()), Config.MAX_TIME_IN_ACADEMY * 6000L);
      } catch (Exception var11) {
         var11.printStackTrace();
      } finally {
         DbUtils.closeQuietly(connection, statement);
      }
   }

   public static boolean isAcademyChar(int objId) {
      String result = "";
      Connection con = null;
      PreparedStatement statement = null;
      ResultSet rset = null;

      try {
         con = DatabaseFactory.getInstance().getConnection();
         statement = con.prepareStatement("SELECT * FROM `character_academy` WHERE `charId` = ?");
         statement.setInt(1, objId);
         rset = statement.executeQuery();
         if (rset.next()) {
            result = rset.getString("clanId");
         }
      } catch (Exception var9) {
         _log.log(Level.WARNING, "AcademyList: " + var9);
      } finally {
         DbUtils.closeQuietly(con, statement, rset);
      }

      return result != "";
   }

   public static void removeAcademyFromDB(Clan clan, int charId, boolean giveReward, boolean kick) {
      if (giveReward) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT itemId, price FROM character_academy WHERE charId=?");
         ) {
            statement.setInt(1, charId);

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  String charName = CharNameHolder.getInstance().getNameById(charId);
                  Map<Integer, Long> reward = new HashMap<>();
                  reward.put(rset.getInt("itemId"), rset.getLong("price"));
                  Player player = World.getInstance().getPlayer(charId);
                  String lang = player != null ? player.getLang() : Config.MULTILANG_DEFAULT;
                  ServerMessage msg = new ServerMessage("CommunityAcademy.MAIL_DESCR1", lang);
                  msg.add(charName);
                  Functions.sendSystemMail(
                     charName, charId, ServerStorage.getInstance().getString(lang, "CommunityAcademy.MAIL_TITLE1"), msg.toString(), reward
                  );
               }
            }
         } catch (Exception var309) {
            _log.log(Level.SEVERE, "AcademyList: Could not select char from character_academy", (Throwable)var309);
         }
      }

      if (kick) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT itemId, price FROM character_academy WHERE clanId=? AND charId=?");
         ) {
            statement.setInt(1, clan.getId());
            statement.setInt(2, charId);

            try (ResultSet rset = statement.executeQuery()) {
               while(rset.next()) {
                  String acadCharName = CharNameHolder.getInstance().getNameById(charId);
                  Map<Integer, Long> reward = new HashMap<>();
                  reward.put(rset.getInt("itemId"), rset.getLong("price"));
                  Player player = World.getInstance().getPlayer(clan.getLeaderId());
                  String lang = player != null ? player.getLang() : Config.MULTILANG_DEFAULT;
                  ServerMessage msg = new ServerMessage("CommunityAcademy.MAIL_DESCR2", lang);
                  msg.add(clan.getLeaderName());
                  msg.add(acadCharName);
                  Functions.sendSystemMail(
                     clan.getLeaderName(),
                     clan.getLeaderId(),
                     ServerStorage.getInstance().getString(lang, "CommunityAcademy.MAIL_TITLE2"),
                     msg.toString(),
                     reward
                  );
                  clan.removeClanMember(charId, System.currentTimeMillis() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L);
               }
            }
         } catch (Exception var302) {
            _log.log(Level.SEVERE, "AcademyList: Could not select char from character_academy", (Throwable)var302);
         }
      }

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_academy WHERE charId=?");
      ) {
         statement.setInt(1, charId);
         statement.execute();
      } catch (Exception var295) {
         _log.log(Level.WARNING, "AcademyList: Could not delete char from character_academy: " + var295.getMessage(), (Throwable)var295);
      }
   }

   public static void restore() {
      Connection connection = null;
      PreparedStatement statement = null;
      ResultSet rs = null;

      try {
         connection = DatabaseFactory.getInstance().getConnection();
         statement = connection.prepareStatement("SELECT clanId,charId,time FROM character_academy");
         rs = statement.executeQuery();

         while(rs.next()) {
            try {
               int clanId = rs.getInt("clanId");
               Clan clan = ClanHolder.getInstance().getClan(clanId);
               if (clan != null) {
                  int charId = rs.getInt("charId");
                  long date = rs.getLong("time");
                  ThreadPoolManager.getInstance().schedule(new AcademyList.CleanUpTask(clan, charId), date - System.currentTimeMillis());
               }
            } catch (Exception var12) {
               var12.printStackTrace();
            }
         }
      } catch (Exception var13) {
         _log.log(Level.WARNING, "AcademyList: Could not restore clan for academy " + var13);
      } finally {
         DbUtils.closeQuietly(connection, statement, rs);
      }
   }

   private static class CleanUpTask extends RunnableImpl {
      private final Clan _clan;
      private final int _charId;

      public CleanUpTask(Clan clan, int charId) {
         this._clan = clan;
         this._charId = charId;
      }

      @Override
      public void runImpl() {
         if (this._clan == null) {
            AcademyList._log.warning("AcademyList: Clan was null for charID " + this._charId);
         } else {
            String charName = CharNameHolder.getInstance().getNameById(this._charId);
            ClanMember member = this._clan.getClanMember(charName);
            if (member != null) {
               if (member.getLevel() < 40 && member.getPledgeType() == -1) {
                  AcademyList.removeAcademyFromDB(this._clan, this._charId, false, true);
                  this._clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + (long)Config.ALT_CLAN_JOIN_DAYS * 3600000L);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
                  sm.addString(charName);
                  this._clan.broadcastToOnlineMembers(sm);
               }
            }
         }
      }
   }
}
