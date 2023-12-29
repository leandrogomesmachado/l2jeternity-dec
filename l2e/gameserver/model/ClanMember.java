package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.SiegeManager;
import l2e.gameserver.model.actor.Player;

public class ClanMember {
   private static final Logger _log = Logger.getLogger(ClanMember.class.getName());
   private final Clan _clan;
   private int _objectId;
   private String _name;
   private String _title;
   private int _powerGrade;
   private int _level;
   private int _classId;
   private int _pvpKills;
   private boolean _sex;
   private int _raceOrdinal;
   private Player _player;
   private int _pledgeType;
   private int _apprentice;
   private int _sponsor;

   public ClanMember(Clan clan, ResultSet clanMember) throws SQLException {
      if (clan == null) {
         throw new IllegalArgumentException("Cannot create a Clan Member with a null clan.");
      } else {
         this._clan = clan;
         this._name = clanMember.getString("char_name");
         this._level = clanMember.getInt("level");
         this._classId = clanMember.getInt("classid");
         this._pvpKills = clanMember.getInt("pvpkills");
         this._objectId = clanMember.getInt("charId");
         this._pledgeType = clanMember.getInt("subpledge");
         this._title = clanMember.getString("title");
         this._powerGrade = clanMember.getInt("power_grade");
         this._apprentice = clanMember.getInt("apprentice");
         this._sponsor = clanMember.getInt("sponsor");
         this._sex = clanMember.getInt("sex") != 0;
         this._raceOrdinal = clanMember.getInt("race");
      }
   }

   public ClanMember(Clan clan, Player player) {
      if (clan == null) {
         throw new IllegalArgumentException("Cannot create a Clan Member if player has a null clan.");
      } else {
         this._player = player;
         this._clan = clan;
         this._name = player.getName();
         this._level = player.getLevel();
         this._classId = player.getClassId().getId();
         this._pvpKills = player.getPvpKills();
         this._objectId = player.getObjectId();
         this._pledgeType = player.getPledgeType();
         this._powerGrade = player.getPowerGrade();
         this._title = player.getTitle();
         this._sponsor = 0;
         this._apprentice = 0;
         this._sex = player.getAppearance().getSex();
         this._raceOrdinal = player.getRace().ordinal();
      }
   }

   public void setPlayerInstance(Player player) {
      if (player == null && this._player != null) {
         this._name = this._player.getName();
         this._level = this._player.getLevel();
         this._classId = this._player.getClassId().getId();
         this._pvpKills = this._player.getPvpKills();
         this._objectId = this._player.getObjectId();
         this._powerGrade = this._player.getPowerGrade();
         this._pledgeType = this._player.getPledgeType();
         this._title = this._player.getTitle();
         this._apprentice = this._player.getApprentice();
         this._sponsor = this._player.getSponsor();
         this._sex = this._player.getAppearance().getSex();
         this._raceOrdinal = this._player.getRace().ordinal();
      }

      if (player != null) {
         this._clan.addSkillEffects(player);
         if (this._clan.getLevel() > 3 && player.isClanLeader()) {
            SiegeManager.getInstance().addSiegeSkills(player);
         }

         if (player.isClanLeader()) {
            this._clan.setLeader(this);
         }
      }

      this._player = player;
   }

   public Player getPlayerInstance() {
      return this._player;
   }

   public boolean isOnline() {
      if (this._player == null || !this._player.isOnline()) {
         return false;
      } else if (this._player.getClient() == null) {
         return false;
      } else {
         return !this._player.getClient().isDetached();
      }
   }

   public int getClassId() {
      return this._player != null ? this._player.getClassId().getId() : this._classId;
   }

   public int getPvpKills() {
      return this._player != null ? this._player.getPvpKills() : this._pvpKills;
   }

   public int getLevel() {
      return this._player != null ? this._player.getLevel() : this._level;
   }

   public String getName() {
      return this._player != null ? this._player.getName() : this._name;
   }

   public int getObjectId() {
      return this._player != null ? this._player.getObjectId() : this._objectId;
   }

   public String getTitle() {
      return this._player != null ? this._player.getTitle() : this._title;
   }

   public int getPledgeType() {
      return this._player != null ? this._player.getPledgeType() : this._pledgeType;
   }

   public void setPledgeType(int pledgeType) {
      this._pledgeType = pledgeType;
      if (this._player != null) {
         this._player.setPledgeType(pledgeType);
      } else {
         this.updatePledgeType();
      }
   }

   public void updatePledgeType() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE charId=?");
         statement.setLong(1, (long)this._pledgeType);
         statement.setInt(2, this.getObjectId());
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Could not update pledge type: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public int getPowerGrade() {
      return this._player != null ? this._player.getPowerGrade() : this._powerGrade;
   }

   public void setPowerGrade(int powerGrade) {
      this._powerGrade = powerGrade;
      if (this._player != null) {
         this._player.setPowerGrade(powerGrade);
      } else {
         this.updatePowerGrade();
      }
   }

   public void updatePowerGrade() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE charId=?");
         statement.setLong(1, (long)this._powerGrade);
         statement.setInt(2, this.getObjectId());
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.WARNING, "Could not update power _grade: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void setApprenticeAndSponsor(int apprenticeID, int sponsorID) {
      this._apprentice = apprenticeID;
      this._sponsor = sponsorID;
   }

   public int getRaceOrdinal() {
      return this._player != null ? this._player.getRace().ordinal() : this._raceOrdinal;
   }

   public boolean getSex() {
      return this._player != null ? this._player.getAppearance().getSex() : this._sex;
   }

   public int getSponsor() {
      return this._player != null ? this._player.getSponsor() : this._sponsor;
   }

   public int getApprentice() {
      return this._player != null ? this._player.getApprentice() : this._apprentice;
   }

   public String getApprenticeOrSponsorName() {
      if (this._player != null) {
         this._apprentice = this._player.getApprentice();
         this._sponsor = this._player.getSponsor();
      }

      if (this._apprentice != 0) {
         ClanMember apprentice = this._clan.getClanMember(this._apprentice);
         return apprentice != null ? apprentice.getName() : "Error";
      } else if (this._sponsor != 0) {
         ClanMember sponsor = this._clan.getClanMember(this._sponsor);
         return sponsor != null ? sponsor.getName() : "Error";
      } else {
         return "";
      }
   }

   public Clan getClan() {
      return this._clan;
   }

   public static int calculatePledgeClass(Player player) {
      int pledgeClass = 0;
      if (player == null) {
         return pledgeClass;
      } else {
         Clan clan = player.getClan();
         if (clan != null) {
            label117:
            switch(clan.getLevel()) {
               case 4:
                  if (player.isClanLeader()) {
                     pledgeClass = 3;
                  }
                  break;
               case 5:
                  if (player.isClanLeader()) {
                     pledgeClass = 5;
                  } else {
                     pledgeClass = 2;
                  }
                  break;
               case 6:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 5;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 3;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 4;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 2;
                     default:
                        break label117;
                  }
               case 7:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 7;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 4;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 6;
                                 break label117;
                              case 1001:
                              case 1002:
                              case 2001:
                              case 2002:
                                 pledgeClass = 5;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 3;
                        break label117;
                     case 1001:
                     case 1002:
                     case 2001:
                     case 2002:
                        pledgeClass = 2;
                     default:
                        break label117;
                  }
               case 8:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 8;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 5;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 7;
                                 break label117;
                              case 1001:
                              case 1002:
                              case 2001:
                              case 2002:
                                 pledgeClass = 6;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 4;
                        break label117;
                     case 1001:
                     case 1002:
                     case 2001:
                     case 2002:
                        pledgeClass = 3;
                     default:
                        break label117;
                  }
               case 9:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 9;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 6;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 8;
                                 break label117;
                              case 1001:
                              case 1002:
                              case 2001:
                              case 2002:
                                 pledgeClass = 7;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 5;
                        break label117;
                     case 1001:
                     case 1002:
                     case 2001:
                     case 2002:
                        pledgeClass = 4;
                     default:
                        break label117;
                  }
               case 10:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 10;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 7;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 9;
                                 break label117;
                              case 1001:
                              case 1002:
                              case 2001:
                              case 2002:
                                 pledgeClass = 8;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 6;
                        break label117;
                     case 1001:
                     case 1002:
                     case 2001:
                     case 2002:
                        pledgeClass = 5;
                     default:
                        break label117;
                  }
               case 11:
                  switch(player.getPledgeType()) {
                     case -1:
                        pledgeClass = 1;
                        break label117;
                     case 0:
                        if (player.isClanLeader()) {
                           pledgeClass = 11;
                        } else {
                           switch(clan.getLeaderSubPledge(player.getObjectId())) {
                              case -1:
                              default:
                                 pledgeClass = 8;
                                 break label117;
                              case 100:
                              case 200:
                                 pledgeClass = 10;
                                 break label117;
                              case 1001:
                              case 1002:
                              case 2001:
                              case 2002:
                                 pledgeClass = 9;
                           }
                        }
                        break label117;
                     case 100:
                     case 200:
                        pledgeClass = 7;
                        break label117;
                     case 1001:
                     case 1002:
                     case 2001:
                     case 2002:
                        pledgeClass = 6;
                     default:
                        break label117;
                  }
               default:
                  pledgeClass = 1;
            }
         }

         if (player.isNoble() && pledgeClass < 5) {
            pledgeClass = 5;
         }

         if (player.isHero() && pledgeClass < 8) {
            pledgeClass = 8;
         }

         return pledgeClass;
      }
   }

   public void saveApprenticeAndSponsor(int apprentice, int sponsor) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE charId=?");
         statement.setInt(1, apprentice);
         statement.setInt(2, sponsor);
         statement.setInt(3, this.getObjectId());
         statement.execute();
         statement.close();
      } catch (SQLException var16) {
         _log.log(Level.WARNING, "Could not save apprentice/sponsor: " + var16.getMessage(), (Throwable)var16);
      }
   }
}
