package l2e.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class FishingChampionship {
   protected static final Logger _log = Logger.getLogger(FishingChampionship.class.getName());
   private static FishingChampionship _instance;
   private long _enddate = 0L;
   protected List<String> _playersName = new ArrayList<>();
   protected List<String> _fishLength = new ArrayList<>();
   protected List<String> _winPlayersName = new ArrayList<>();
   protected List<String> _winFishLength = new ArrayList<>();
   protected List<FishingChampionship.Fisher> _tmpPlayer = new ArrayList<>();
   protected List<FishingChampionship.Fisher> _winPlayer = new ArrayList<>();
   private float _minFishLength = 0.0F;
   private int x;

   public static FishingChampionship getInstance() {
      if (_instance == null) {
         _instance = new FishingChampionship();
      }

      return _instance;
   }

   protected FishingChampionship() {
      this.restoreData();
      this.refreshWinResult();
      this.setNewMin();
      if (this._enddate <= System.currentTimeMillis()) {
         this._enddate = System.currentTimeMillis();
         new FishingChampionship.finishChamp().run();
      } else {
         ThreadPoolManager.getInstance().schedule(new FishingChampionship.finishChamp(), this._enddate - System.currentTimeMillis());
      }

      _log.info("Fishing Championship : Loaded functions.");
   }

   protected void setEndOfChamp() {
      Calendar finishtime = Calendar.getInstance();
      finishtime.setTimeInMillis(this._enddate);
      finishtime.set(12, 0);
      finishtime.set(13, 0);
      finishtime.add(5, 6);
      finishtime.set(7, 3);
      finishtime.set(11, 19);
      this._enddate = finishtime.getTimeInMillis();
   }

   private void restoreData() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT finish_date FROM fishing_championship_date");
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this._enddate = rs.getLong("finish_date");
         }

         rs.close();
         statement.close();
         statement = con.prepareStatement("SELECT PlayerName,fishLength,rewarded FROM fishing_championship");
         rs = statement.executeQuery();

         while(rs.next()) {
            int rewarded = rs.getInt("rewarded");
            if (rewarded == 0) {
               FishingChampionship.Fisher fisher = new FishingChampionship.Fisher();
               fisher._name = rs.getString("PlayerName");
               fisher._length = rs.getFloat("fishLength");
               this._tmpPlayer.add(fisher);
            }

            if (rewarded > 0) {
               FishingChampionship.Fisher fisher = new FishingChampionship.Fisher();
               fisher._name = rs.getString("PlayerName");
               fisher._length = rs.getFloat("fishLength");
               fisher._rewarded = rewarded;
               this._winPlayer.add(fisher);
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var17) {
         _log.log(Level.SEVERE, "Exception: can't get fishing championship info: " + var17.getMessage(), (Throwable)var17);
      }
   }

   public synchronized void newFish(Player player) {
      float p1 = (float)Rnd.get(60, 90);
      float len = (float)(Rnd.get(0, 99) / 100) + p1;
      if (this._tmpPlayer.size() < 5) {
         for(this.x = 0; this.x < this._tmpPlayer.size(); ++this.x) {
            if (this._tmpPlayer.get(this.x)._name.equalsIgnoreCase(player.getName())) {
               if (this._tmpPlayer.get(this.x)._length < len) {
                  this._tmpPlayer.get(this.x)._length = len;
                  player.sendMessage(new ServerMessage("FishingChampionship.IMPROVED_RESULT", player.getLang()).toString());
                  this.setNewMin();
               }

               return;
            }
         }

         FishingChampionship.Fisher newFisher = new FishingChampionship.Fisher();
         newFisher._name = player.getName();
         newFisher._length = len;
         this._tmpPlayer.add(newFisher);
         player.sendMessage(new ServerMessage("FishingChampionship.GOT_TO_LIST", player.getLang()).toString());
         this.setNewMin();
      } else {
         if (this._minFishLength >= len) {
            return;
         }

         for(this.x = 0; this.x < this._tmpPlayer.size(); ++this.x) {
            if (this._tmpPlayer.get(this.x)._name.equalsIgnoreCase(player.getName())) {
               if (this._tmpPlayer.get(this.x)._length < len) {
                  this._tmpPlayer.get(this.x)._length = len;
                  player.sendMessage(new ServerMessage("FishingChampionship.IMPROVED_RESULT", player.getLang()).toString());
                  this.setNewMin();
               }

               return;
            }
         }

         FishingChampionship.Fisher minFisher = null;
         float minLen = 99999.0F;

         for(FishingChampionship.Fisher a_tmpPlayer : this._tmpPlayer) {
            if (a_tmpPlayer._length < minLen) {
               minFisher = a_tmpPlayer;
               minLen = a_tmpPlayer._length;
            }
         }

         this._tmpPlayer.remove(minFisher);
         FishingChampionship.Fisher newFisher = new FishingChampionship.Fisher();
         newFisher._name = player.getName();
         newFisher._length = len;
         this._tmpPlayer.add(newFisher);
         player.sendMessage(new ServerMessage("FishingChampionship.GOT_TO_LIST", player.getLang()).toString());
         this.setNewMin();
      }
   }

   private void setNewMin() {
      float minLen = 99999.0F;

      for(FishingChampionship.Fisher a_tmpPlayer : this._tmpPlayer) {
         if (a_tmpPlayer._length < minLen) {
            minLen = a_tmpPlayer._length;
         }
      }

      this._minFishLength = minLen;
   }

   public long getTimeRemaining() {
      return (this._enddate - System.currentTimeMillis()) / 60000L;
   }

   public String getWinnerName(Player player, int par) {
      return this._winPlayersName.size() >= par
         ? this._winPlayersName.get(par - 1)
         : "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
   }

   public String getCurrentName(Player player, int par) {
      return this._playersName.size() >= par
         ? this._playersName.get(par - 1)
         : "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NO") + "";
   }

   public String getFishLength(int par) {
      return this._winFishLength.size() >= par ? this._winFishLength.get(par - 1) : "0";
   }

   public String getCurrentFishLength(int par) {
      return this._fishLength.size() >= par ? this._fishLength.get(par - 1) : "0";
   }

   public void getReward(Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
      String str = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
      str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ACCEPT_CONGRATULATIONS") + "<br>";
      str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.HERE_YOUR_PRIZE") + "<br>";
      str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.GOOD_LUCK") + "";
      str = str + "</body></html>";
      html.setHtml(player, str);
      player.sendPacket(html);

      for(FishingChampionship.Fisher fisher : this._winPlayer) {
         if (fisher._name.equalsIgnoreCase(player.getName()) && fisher._rewarded != 2) {
            int rewardCnt = 0;

            for(int x = 0; x < this._winPlayersName.size(); ++x) {
               if (this._winPlayersName.get(x).equalsIgnoreCase(player.getName())) {
                  switch(x) {
                     case 0:
                        rewardCnt = 800000;
                        break;
                     case 1:
                        rewardCnt = 500000;
                        break;
                     case 2:
                        rewardCnt = 300000;
                        break;
                     case 3:
                        rewardCnt = 200000;
                        break;
                     case 4:
                        rewardCnt = 100000;
                  }
               }
            }

            fisher._rewarded = 2;
            if (rewardCnt > 0) {
               ItemInstance item = player.getInventory().addItem("reward", 57, (long)rewardCnt, player, player);
               player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(item).addNumber(rewardCnt));
               player.sendItemList(false);
            }
         }
      }
   }

   public void showMidResult(Player player) {
      NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
      String str = "<html><head><title>" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ROYAL_TOURNAMENT") + "</title></head>";
      str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.NOW_PASS_COMPETITIONS") + "<br><br>";
      str = str + "" + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.UPON_CPMPETITIONS") + "<br>";
      str = str
         + "<table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + "</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.FISHERMAN")
         + "</td><td width=80 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.LENGTH")
         + "</td></tr></table><table width=280>";

      for(int x = 1; x <= 5; ++x) {
         str = str
            + "<tr><td width=70 align=center>"
            + x
            + " "
            + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
            + ":</td>";
         str = str + "<td width=110 align=center>" + this.getCurrentName(player, x) + "</td>";
         str = str + "<td width=80 align=center>" + this.getCurrentFishLength(x) + "</td></tr>";
      }

      str = str + "<td width=80 align=center>0</td></tr></table><br>";
      str = str
         + ""
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZES_LIST")
         + "<br><table width=280 border=0 bgcolor=\"000000\"><tr><td width=70 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + "</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PRIZE")
         + "</td><td width=80 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.AMOUNT")
         + "</td></tr></table><table width=280>";
      str = str
         + "<tr><td width=70 align=center>1 "
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + ":</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA")
         + "</td><td width=80 align=center>800000</td></tr><tr><td width=70 align=center>2 "
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + ":</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA")
         + "</td><td width=80 align=center>500000</td></tr><tr><td width=70 align=center>3 "
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + ":</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA")
         + "</td><td width=80 align=center>300000</td></tr>";
      str = str
         + "<tr><td width=70 align=center>4 "
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + ":</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA")
         + "</td><td width=80 align=center>200000</td></tr><tr><td width=70 align=center>5 "
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.PLACES")
         + ":</td><td width=110 align=center>"
         + ServerStorage.getInstance().getString(player.getLang(), "FishingChampionship.ADENA")
         + "</td><td width=80 align=center>100000</td></tr></table></body></html>";
      html.setHtml(player, str);
      player.sendPacket(html);
   }

   public void shutdown() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM fishing_championship_date");
         statement.execute();
         statement.close();
         statement = con.prepareStatement("INSERT INTO fishing_championship_date (finish_date) VALUES (?)");
         statement.setLong(1, this._enddate);
         statement.execute();
         statement.close();
         statement = con.prepareStatement("DELETE FROM fishing_championship");
         statement.execute();
         statement.close();

         for(FishingChampionship.Fisher fisher : this._winPlayer) {
            statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
            statement.setString(1, fisher._name);
            statement.setFloat(2, fisher._length);
            statement.setInt(3, fisher._rewarded);
            statement.execute();
            statement.close();
         }

         for(FishingChampionship.Fisher fisher : this._tmpPlayer) {
            statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
            statement.setString(1, fisher._name);
            statement.setFloat(2, fisher._length);
            statement.setInt(3, 0);
            statement.execute();
            statement.close();
         }
      } catch (Exception var16) {
         _log.log(Level.SEVERE, "Exception: can't update player vitality: " + var16.getMessage(), (Throwable)var16);
      }
   }

   protected synchronized void refreshResult() {
      this._playersName.clear();
      this._fishLength.clear();

      for(int x = 0; x <= this._tmpPlayer.size() - 1; ++x) {
         for(int y = 0; y <= this._tmpPlayer.size() - 2; ++y) {
            FishingChampionship.Fisher fisher1 = this._tmpPlayer.get(y);
            FishingChampionship.Fisher fisher2 = this._tmpPlayer.get(y + 1);
            if (fisher1._length < fisher2._length) {
               this._tmpPlayer.set(y, fisher2);
               this._tmpPlayer.set(y + 1, fisher1);
            }
         }
      }

      for(this.x = 0; this.x <= this._tmpPlayer.size() - 1; ++this.x) {
         this._playersName.add(this._tmpPlayer.get(this.x)._name);
         this._fishLength.add("" + this._tmpPlayer.get(this.x)._length);
      }
   }

   protected void refreshWinResult() {
      this._winPlayersName.clear();
      this._winFishLength.clear();

      for(int x = 0; x <= this._winPlayer.size() - 1; ++x) {
         for(int y = 0; y <= this._winPlayer.size() - 2; ++y) {
            FishingChampionship.Fisher fisher1 = this._winPlayer.get(y);
            FishingChampionship.Fisher fisher2 = this._winPlayer.get(y + 1);
            if (fisher1._length < fisher2._length) {
               this._winPlayer.set(y, fisher2);
               this._winPlayer.set(y + 1, fisher1);
            }
         }
      }

      for(this.x = 0; this.x <= this._winPlayer.size() - 1; ++this.x) {
         this._winPlayersName.add(this._winPlayer.get(this.x)._name);
         this._winFishLength.add("" + this._winPlayer.get(this.x)._length);
      }
   }

   protected class Fisher {
      float _length = 0.0F;
      String _name;
      int _rewarded = 0;
   }

   protected class finishChamp implements Runnable {
      @Override
      public void run() {
         FishingChampionship.this._winPlayer.clear();

         for(FishingChampionship.Fisher fisher : FishingChampionship.this._tmpPlayer) {
            fisher._rewarded = 1;
            FishingChampionship.this._winPlayer.add(fisher);
         }

         FishingChampionship.this._tmpPlayer.clear();
         FishingChampionship.this.refreshWinResult();
         FishingChampionship.this.setEndOfChamp();
         FishingChampionship.this.shutdown();
         FishingChampionship._log.info("Fishing Championship Manager : start new event period.");
      }
   }
}
