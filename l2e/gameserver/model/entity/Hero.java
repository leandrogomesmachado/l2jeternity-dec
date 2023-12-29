package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.Config;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Hero {
   private static final Logger _log = Logger.getLogger(Hero.class.getName());
   private static final String GET_HEROES = "SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.charId = heroes.charId AND heroes.played = 1";
   private static final String GET_ALL_HEROES = "SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.charId = heroes.charId";
   private static final String UPDATE_ALL = "UPDATE heroes SET played = 0, active = 0";
   private static final String INSERT_HERO = "INSERT INTO heroes (charId, class_id, count, played, active) VALUES (?,?,?,?,?)";
   private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ?, active = ? WHERE charId = ?";
   private static final String GET_CLAN_ALLY = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.charId = ?";
   private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE charId = ?)";
   private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390) AND owner_id NOT IN (SELECT charId FROM characters WHERE accesslevel > 0)";
   private static final Map<Integer, StatsSet> _heroes = new ConcurrentHashMap<>();
   private static final Map<Integer, StatsSet> _completeHeroes = new ConcurrentHashMap<>();
   private static final Map<Integer, StatsSet> _herocounts = new ConcurrentHashMap<>();
   private static final Map<Integer, List<StatsSet>> _herofights = new ConcurrentHashMap<>();
   private static final Map<Integer, List<StatsSet>> _herodiary = new ConcurrentHashMap<>();
   private static final Map<Integer, String> _heroMessage = new ConcurrentHashMap<>();
   public static final String COUNT = "count";
   public static final String PLAYED = "played";
   public static final String ACTIVE = "active";
   public static final String CLAN_NAME = "clan_name";
   public static final String CLAN_CREST = "clan_crest";
   public static final String ALLY_NAME = "ally_name";
   public static final String ALLY_CREST = "ally_crest";
   public static final int ACTION_RAID_KILLED = 1;
   public static final int ACTION_HERO_GAINED = 2;
   public static final int ACTION_CASTLE_TAKEN = 3;

   public static Hero getInstance() {
      return Hero.SingletonHolder._instance;
   }

   protected Hero() {
      this.init();
   }

   private void init() {
      _heroes.clear();
      _completeHeroes.clear();
      _herocounts.clear();
      _herofights.clear();
      _herodiary.clear();
      _heroMessage.clear();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.charId = heroes.charId AND heroes.played = 1"
         );
         ResultSet rset = statement.executeQuery();
         PreparedStatement statement2 = con.prepareStatement(
            "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.charId = ?"
         );

         while(rset.next()) {
            StatsSet hero = new StatsSet();
            int charId = rset.getInt("charId");
            hero.set("char_name", rset.getString("char_name"));
            hero.set("class_id", rset.getInt("class_id"));
            hero.set("count", rset.getInt("count"));
            hero.set("played", rset.getInt("played"));
            hero.set("active", rset.getInt("active"));
            this.loadFights(charId);
            this.loadDiary(charId);
            this.loadMessage(charId);
            this.processHeros(statement2, charId, hero);
            _heroes.put(charId, hero);
         }

         rset.close();
         statement.close();
         statement = con.prepareStatement(
            "SELECT heroes.charId, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.charId = heroes.charId"
         );
         rset = statement.executeQuery();

         while(rset.next()) {
            StatsSet hero = new StatsSet();
            int charId = rset.getInt("charId");
            hero.set("char_name", rset.getString("char_name"));
            hero.set("class_id", rset.getInt("class_id"));
            hero.set("count", rset.getInt("count"));
            hero.set("played", rset.getInt("played"));
            hero.set("active", rset.getInt("active"));
            this.processHeros(statement2, charId, hero);
            _completeHeroes.put(charId, hero);
         }

         statement2.close();
         rset.close();
         statement.close();
      } catch (SQLException var18) {
         _log.log(Level.WARNING, "Hero System: Couldnt load Heroes", (Throwable)var18);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + _heroes.size() + " heroes.");
      _log.info(this.getClass().getSimpleName() + ": Loaded " + _completeHeroes.size() + " all time heroes.");
   }

   private void processHeros(PreparedStatement ps, int charId, StatsSet hero) throws SQLException {
      ps.setInt(1, charId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
         int clanId = rs.getInt("clanid");
         int allyId = rs.getInt("allyId");
         String clanName = "";
         String allyName = "";
         int clanCrest = 0;
         int allyCrest = 0;
         if (clanId > 0) {
            clanName = ClanHolder.getInstance().getClan(clanId).getName();
            clanCrest = ClanHolder.getInstance().getClan(clanId).getCrestId();
            if (allyId > 0) {
               allyName = ClanHolder.getInstance().getClan(clanId).getAllyName();
               allyCrest = ClanHolder.getInstance().getClan(clanId).getAllyCrestId();
            }
         }

         hero.set("clan_crest", clanCrest);
         hero.set("clan_name", clanName);
         hero.set("ally_crest", allyCrest);
         hero.set("ally_name", allyName);
      }

      rs.close();
      ps.clearParameters();
   }

   private String calcFightTime(long FightTime) {
      String format = String.format("%%0%dd", 2);
      FightTime /= 1000L;
      String seconds = String.format(format, FightTime % 60L);
      String minutes = String.format(format, FightTime % 3600L / 60L);
      return minutes + ":" + seconds;
   }

   public void loadMessage(int charId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         String message = null;
         PreparedStatement statement = con.prepareStatement("SELECT message FROM heroes WHERE charId=?");
         statement.setInt(1, charId);
         ResultSet rset = statement.executeQuery();
         rset.next();
         message = rset.getString("message");
         _heroMessage.put(charId, message);
         rset.close();
         statement.close();
      } catch (SQLException var17) {
         _log.log(Level.WARNING, "Hero System: Couldnt load Hero Message for CharId: " + charId, (Throwable)var17);
      }
   }

   public void loadDiary(int charId) {
      List<StatsSet> _diary = new ArrayList<>();
      int diaryentries = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
         statement.setInt(1, charId);

         ResultSet rset;
         for(rset = statement.executeQuery(); rset.next(); ++diaryentries) {
            StatsSet _diaryentry = new StatsSet();
            long time = rset.getLong("time");
            int action = rset.getInt("action");
            int param = rset.getInt("param");
            String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(time));
            _diaryentry.set("date", date);
            if (action == 1) {
               NpcTemplate template = NpcsParser.getInstance().getTemplate(param);
               if (template != null) {
                  _diaryentry.set("action", template.getName() + " was defeated");
               }
            } else if (action == 2) {
               _diaryentry.set("action", "Gained Hero status");
            } else if (action == 3) {
               Castle castle = CastleManager.getInstance().getCastleById(param);
               if (castle != null) {
                  _diaryentry.set("action", castle.getName() + " Castle was successfuly taken");
               }
            }

            _diary.add(_diaryentry);
         }

         rset.close();
         statement.close();
         _herodiary.put(charId, _diary);
         _log.info(
            this.getClass().getSimpleName() + ": Loaded " + diaryentries + " diary entries for hero: " + CharNameHolder.getInstance().getNameById(charId)
         );
      } catch (SQLException var25) {
         _log.log(Level.WARNING, "Hero System: Couldnt load Hero Diary for CharId: " + charId, (Throwable)var25);
      }
   }

   public void loadFights(int charId) {
      List<StatsSet> _fights = new ArrayList<>();
      StatsSet _herocountdata = new StatsSet();
      Calendar _data = Calendar.getInstance();
      _data.set(5, 1);
      _data.set(11, 0);
      _data.set(12, 0);
      _data.set(14, 0);
      long from = _data.getTimeInMillis();
      int numberoffights = 0;
      int _victorys = 0;
      int _losses = 0;
      int _draws = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC");
         statement.setInt(1, charId);
         statement.setInt(2, charId);
         statement.setLong(3, from);
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            int charOneId = rset.getInt("charOneId");
            int charOneClass = rset.getInt("charOneClass");
            int charTwoId = rset.getInt("charTwoId");
            int charTwoClass = rset.getInt("charTwoClass");
            int winner = rset.getInt("winner");
            long start = rset.getLong("start");
            int time = rset.getInt("time");
            int classed = rset.getInt("classed");
            if (charId == charOneId) {
               String name = CharNameHolder.getInstance().getNameById(charTwoId);
               String cls = ClassListParser.getInstance().getClass(charTwoClass).getClientCode();
               if (name != null && cls != null) {
                  StatsSet fight = new StatsSet();
                  fight.set("oponent", name);
                  fight.set("oponentclass", cls);
                  fight.set("time", this.calcFightTime((long)time));
                  String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));
                  fight.set("start", date);
                  fight.set("classed", classed);
                  if (winner == 1) {
                     fight.set("result", "<font color=\"00ff00\">victory</font>");
                     ++_victorys;
                  } else if (winner == 2) {
                     fight.set("result", "<font color=\"ff0000\">loss</font>");
                     ++_losses;
                  } else if (winner == 0) {
                     fight.set("result", "<font color=\"ffff00\">draw</font>");
                     ++_draws;
                  }

                  _fights.add(fight);
                  ++numberoffights;
               }
            } else if (charId == charTwoId) {
               String name = CharNameHolder.getInstance().getNameById(charOneId);
               String cls = ClassListParser.getInstance().getClass(charOneClass).getClientCode();
               if (name != null && cls != null) {
                  StatsSet fight = new StatsSet();
                  fight.set("oponent", name);
                  fight.set("oponentclass", cls);
                  fight.set("time", this.calcFightTime((long)time));
                  String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));
                  fight.set("start", date);
                  fight.set("classed", classed);
                  if (winner == 1) {
                     fight.set("result", "<font color=\"ff0000\">loss</font>");
                     ++_losses;
                  } else if (winner == 2) {
                     fight.set("result", "<font color=\"00ff00\">victory</font>");
                     ++_victorys;
                  } else if (winner == 0) {
                     fight.set("result", "<font color=\"ffff00\">draw</font>");
                     ++_draws;
                  }

                  _fights.add(fight);
                  ++numberoffights;
               }
            }
         }

         rset.close();
         statement.close();
         _herocountdata.set("victory", _victorys);
         _herocountdata.set("draw", _draws);
         _herocountdata.set("loss", _losses);
         _herocounts.put(charId, _herocountdata);
         _herofights.put(charId, _fights);
         _log.info(this.getClass().getSimpleName() + ": Loaded " + numberoffights + " fights for hero: " + CharNameHolder.getInstance().getNameById(charId));
      } catch (SQLException var38) {
         _log.log(Level.WARNING, "Hero System: Couldnt load Hero fights history for CharId: " + charId, (Throwable)var38);
      }
   }

   public Map<Integer, StatsSet> getHeroes() {
      return _heroes;
   }

   public int getHeroByClass(int classid) {
      for(Entry<Integer, StatsSet> e : _heroes.entrySet()) {
         if (e.getValue().getInteger("class_id") == classid) {
            return e.getKey();
         }
      }

      return 0;
   }

   public void resetData() {
      _herodiary.clear();
      _herofights.clear();
      _herocounts.clear();
      _heroMessage.clear();
   }

   public void showHeroDiary(Player activeChar, int heroclass, int charid, int page) {
      int perpage = 10;
      if (_herodiary.containsKey(charid)) {
         List<StatsSet> _mainlist = _herodiary.get(charid);
         NpcHtmlMessage DiaryReply = new NpcHtmlMessage(5);
         String htmContent = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/olympiad/herodiary.htm");
         if (htmContent != null && _heroMessage.containsKey(charid)) {
            DiaryReply.setHtml(activeChar, htmContent);
            DiaryReply.replace("%heroname%", CharNameHolder.getInstance().getNameById(charid));
            DiaryReply.replace("%message%", _heroMessage.get(charid));
            if (_mainlist.isEmpty()) {
               DiaryReply.replace("%list%", "");
               DiaryReply.replace("%buttprev%", "");
               DiaryReply.replace("%buttnext%", "");
            } else {
               List<StatsSet> _list = new ArrayList<>(_mainlist);
               Collections.reverse(_list);
               boolean color = true;
               StringBuilder fList = new StringBuilder(500);
               int counter = 0;
               int breakat = 0;

               for(int i = (page - 1) * 10; i < _list.size(); ++i) {
                  breakat = i;
                  StatsSet _diaryentry = _list.get(i);
                  StringUtil.append(fList, "<tr><td>");
                  if (color) {
                     StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
                  } else {
                     StringUtil.append(fList, "<table width=270>");
                  }

                  StringUtil.append(fList, "<tr><td width=270><font color=\"LEVEL\">" + _diaryentry.getString("date") + ":xx</font></td></tr>");
                  StringUtil.append(fList, "<tr><td width=270>" + _diaryentry.getString("action") + "</td></tr>");
                  StringUtil.append(fList, "<tr><td>&nbsp;</td></tr></table>");
                  StringUtil.append(fList, "</td></tr>");
                  color = !color;
                  if (++counter >= 10) {
                     break;
                  }
               }

               if (breakat < _list.size() - 1) {
                  DiaryReply.replace(
                     "%buttprev%",
                     "<button value=\"Prev\" action=\"bypass _diary?class="
                        + heroclass
                        + "&page="
                        + (page + 1)
                        + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                  );
               } else {
                  DiaryReply.replace("%buttprev%", "");
               }

               if (page > 1) {
                  DiaryReply.replace(
                     "%buttnext%",
                     "<button value=\"Next\" action=\"bypass _diary?class="
                        + heroclass
                        + "&page="
                        + (page - 1)
                        + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                  );
               } else {
                  DiaryReply.replace("%buttnext%", "");
               }

               DiaryReply.replace("%list%", fList.toString());
            }

            activeChar.sendPacket(DiaryReply);
         }
      }
   }

   public void showHeroFights(Player activeChar, int heroclass, int charid, int page) {
      int perpage = 20;
      int _win = 0;
      int _loss = 0;
      int _draw = 0;
      if (_herofights.containsKey(charid)) {
         List<StatsSet> _list = _herofights.get(charid);
         NpcHtmlMessage FightReply = new NpcHtmlMessage(5);
         String htmContent = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), "data/html/olympiad/herohistory.htm");
         if (htmContent != null) {
            FightReply.setHtml(activeChar, htmContent);
            FightReply.replace("%heroname%", CharNameHolder.getInstance().getNameById(charid));
            if (_list.isEmpty()) {
               FightReply.replace("%list%", "");
               FightReply.replace("%buttprev%", "");
               FightReply.replace("%buttnext%", "");
            } else {
               if (_herocounts.containsKey(charid)) {
                  StatsSet _herocount = _herocounts.get(charid);
                  _win = _herocount.getInteger("victory");
                  _loss = _herocount.getInteger("loss");
                  _draw = _herocount.getInteger("draw");
               }

               boolean color = true;
               StringBuilder fList = new StringBuilder(500);
               int counter = 0;
               int breakat = 0;

               for(int i = (page - 1) * 20; i < _list.size(); ++i) {
                  breakat = i;
                  StatsSet fight = _list.get(i);
                  StringUtil.append(fList, "<tr><td>");
                  if (color) {
                     StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
                  } else {
                     StringUtil.append(fList, "<table width=270>");
                  }

                  StringUtil.append(
                     fList,
                     "<tr><td width=220><font color=\"LEVEL\">"
                        + fight.getString("start")
                        + "</font>&nbsp;&nbsp;"
                        + fight.getString("result")
                        + "</td><td width=50 align=right>"
                        + (fight.getInteger("classed") > 0 ? "<font color=\"FFFF99\">cls</font>" : "<font color=\"999999\">non-cls<font>")
                        + "</td></tr>"
                  );
                  StringUtil.append(
                     fList,
                     "<tr><td width=220>vs "
                        + fight.getString("oponent")
                        + " ("
                        + fight.getString("oponentclass")
                        + ")</td><td width=50 align=right>("
                        + fight.getString("time")
                        + ")</td></tr>"
                  );
                  StringUtil.append(fList, "<tr><td colspan=2>&nbsp;</td></tr></table>");
                  StringUtil.append(fList, "</td></tr>");
                  color = !color;
                  if (++counter >= 20) {
                     break;
                  }
               }

               if (breakat < _list.size() - 1) {
                  FightReply.replace(
                     "%buttprev%",
                     "<button value=\"Prev\" action=\"bypass _match?class="
                        + heroclass
                        + "&page="
                        + (page + 1)
                        + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                  );
               } else {
                  FightReply.replace("%buttprev%", "");
               }

               if (page > 1) {
                  FightReply.replace(
                     "%buttnext%",
                     "<button value=\"Next\" action=\"bypass _match?class="
                        + heroclass
                        + "&page="
                        + (page - 1)
                        + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
                  );
               } else {
                  FightReply.replace("%buttnext%", "");
               }

               FightReply.replace("%list%", fList.toString());
            }

            FightReply.replace("%win%", String.valueOf(_win));
            FightReply.replace("%draw%", String.valueOf(_draw));
            FightReply.replace("%loos%", String.valueOf(_loss));
            activeChar.sendPacket(FightReply);
         }
      }
   }

   public synchronized void clearHeroes() {
      this.updateHeroes(true);
      if (!_heroes.isEmpty()) {
         for(StatsSet hero : _heroes.values()) {
            String name = hero.getString("char_name");
            Player player = World.getInstance().getPlayer(name);
            if (player != null) {
               try {
                  player.setHero(false, true);
                  if (player.getClan() != null) {
                     player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                  } else {
                     player.setPledgeClass(player.isNoble() ? 5 : 1);
                  }

                  for(int i = 0; i < 25; ++i) {
                     ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
                     if (equippedItem != null && equippedItem.isHeroItem()) {
                        player.getInventory().unEquipItemInSlot(i);
                     }
                  }

                  for(ItemInstance item : player.getInventory().getAvailableItems(false, false, false)) {
                     if (item != null && item.isHeroItem()) {
                        player.destroyItem("Hero", item, null, true);
                        InventoryUpdate iu = new InventoryUpdate();
                        iu.addRemovedItem(item);
                        player.sendPacket(iu);
                     }
                  }

                  player.broadcastUserInfo(true);
               } catch (NullPointerException var10) {
               }
            }
         }
      }

      _heroes.clear();
   }

   public synchronized void computeNewHeroes(List<StatsSet> newHeroes) {
      if (newHeroes.isEmpty()) {
         _heroes.clear();
      } else {
         Map<Integer, StatsSet> heroes = new HashMap<>();

         for(StatsSet hero : newHeroes) {
            int charId = hero.getInteger("charId");
            if (_completeHeroes != null && _completeHeroes.containsKey(charId)) {
               StatsSet oldHero = _completeHeroes.get(charId);
               int count = oldHero.getInteger("count");
               oldHero.set("count", count + 1);
               oldHero.set("played", 1);
               if (Config.AUTO_GET_HERO) {
                  oldHero.set("active", 1);
               } else {
                  oldHero.set("active", 0);
               }

               heroes.put(charId, oldHero);
            } else {
               StatsSet newHero = new StatsSet();
               newHero.set("char_name", hero.getString("char_name"));
               newHero.set("class_id", hero.getInteger("class_id"));
               newHero.set("count", 1);
               newHero.set("played", 1);
               if (Config.AUTO_GET_HERO) {
                  newHero.set("active", 1);
               } else {
                  newHero.set("active", 0);
               }

               heroes.put(charId, newHero);
            }
         }

         this.deleteItemsInDb();
         _heroes.clear();
         _heroes.putAll(heroes);
         heroes.clear();
         this.updateHeroes(false);

         for(Integer charId : _heroes.keySet()) {
            Player player = World.getInstance().getPlayer(charId);
            if (player != null) {
               if (Config.AUTO_GET_HERO) {
                  player.setHero(true, true);
                  player.broadcastPacket(new SocialAction(player.getObjectId(), 20016));
                  player.getCounters().addAchivementInfo("setHero", 0, -1L, false, false, false);
                  Clan clan = player.getClan();
                  if (clan != null) {
                     player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                     clan.addReputationScore(Config.HERO_POINTS, true);
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
                     sm.addString(CharNameHolder.getInstance().getNameById(charId));
                     sm.addNumber(Config.HERO_POINTS);
                     clan.broadcastToOnlineMembers(sm);
                  } else {
                     player.setPledgeClass(8);
                  }

                  player.broadcastUserInfo(true);
                  this.setHeroGained(player.getObjectId());
                  this.loadDiary(player.getObjectId());
               }

               this.loadFights(player.getObjectId());
               _heroMessage.put(player.getObjectId(), "");
            } else {
               if (Config.AUTO_GET_HERO) {
                  this.setHeroGained(charId);
                  this.loadDiary(charId);

                  try (Connection con = DatabaseFactory.getInstance().getConnection()) {
                     PreparedStatement statement = con.prepareStatement(
                        "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE charId = ?)"
                     );
                     statement.setInt(1, charId);
                     ResultSet rset = statement.executeQuery();
                     if (rset.next()) {
                        String clanName = rset.getString("clan_name");
                        if (clanName != null) {
                           Clan clan = ClanHolder.getInstance().getClanByName(clanName);
                           if (clan != null) {
                              clan.addReputationScore(Config.HERO_POINTS, true);
                              SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
                              sm.addString(CharNameHolder.getInstance().getNameById(charId));
                              sm.addNumber(Config.HERO_POINTS);
                              clan.broadcastToOnlineMembers(sm);
                           }
                        }
                     }

                     rset.close();
                     statement.close();
                  } catch (Exception var23) {
                     _log.warning("could not get clan name of player with objectId:" + charId + ": " + var23);
                  }
               }

               this.loadFights(charId);
               _heroMessage.put(charId, "");
            }
         }
      }
   }

   public void updateHeroes(boolean setDefault) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         if (setDefault) {
            PreparedStatement statement = con.prepareStatement("UPDATE heroes SET played = 0, active = 0");
            statement.execute();
            statement.close();
         } else {
            for(Entry<Integer, StatsSet> entry : _heroes.entrySet()) {
               StatsSet hero = entry.getValue();
               int heroId = entry.getKey();
               if (!_completeHeroes.isEmpty() && _completeHeroes.containsKey(heroId)) {
                  PreparedStatement statement = con.prepareStatement("UPDATE heroes SET count = ?, played = ?, active = ? WHERE charId = ?");
                  statement.setInt(1, hero.getInteger("count"));
                  statement.setInt(2, hero.getInteger("played"));
                  statement.setInt(3, hero.getInteger("active"));
                  statement.setInt(4, heroId);
                  statement.execute();
                  statement.close();
               } else {
                  PreparedStatement statement = con.prepareStatement("INSERT INTO heroes (charId, class_id, count, played, active) VALUES (?,?,?,?,?)");
                  statement.setInt(1, heroId);
                  statement.setInt(2, hero.getInteger("class_id"));
                  statement.setInt(3, hero.getInteger("count"));
                  statement.setInt(4, hero.getInteger("played"));
                  statement.setInt(5, hero.getInteger("active"));
                  statement.execute();
                  statement.close();
                  statement = con.prepareStatement(
                     "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.charId = ?"
                  );
                  statement.setInt(1, heroId);
                  ResultSet rset = statement.executeQuery();
                  if (rset.next()) {
                     int clanId = rset.getInt("clanid");
                     int allyId = rset.getInt("allyId");
                     String clanName = "";
                     String allyName = "";
                     int clanCrest = 0;
                     int allyCrest = 0;
                     if (clanId > 0) {
                        clanName = ClanHolder.getInstance().getClan(clanId).getName();
                        clanCrest = ClanHolder.getInstance().getClan(clanId).getCrestId();
                        if (allyId > 0) {
                           allyName = ClanHolder.getInstance().getClan(clanId).getAllyName();
                           allyCrest = ClanHolder.getInstance().getClan(clanId).getAllyCrestId();
                        }
                     }

                     hero.set("clan_crest", clanCrest);
                     hero.set("clan_name", clanName);
                     hero.set("ally_crest", allyCrest);
                     hero.set("ally_name", allyName);
                  }

                  rset.close();
                  statement.close();
                  _heroes.remove(heroId);
                  _heroes.put(heroId, hero);
                  _completeHeroes.put(heroId, hero);
               }
            }
         }
      } catch (SQLException var26) {
         _log.log(Level.WARNING, "Hero System: Couldnt update Heroes", (Throwable)var26);
      }
   }

   public void setHeroGained(int charId) {
      this.setDiaryData(charId, 2, 0);
   }

   public void setRBkilled(int charId, int npcId) {
      this.setDiaryData(charId, 1, npcId);
      NpcTemplate template = NpcsParser.getInstance().getTemplate(npcId);
      if (_herodiary.containsKey(charId) && template != null) {
         List<StatsSet> _list = _herodiary.get(charId);
         _herodiary.remove(charId);
         StatsSet _diaryentry = new StatsSet();
         String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(System.currentTimeMillis()));
         _diaryentry.set("date", date);
         _diaryentry.set("action", template.getName() + " was defeated");
         _list.add(_diaryentry);
         _herodiary.put(charId, _list);
      }
   }

   public void setCastleTaken(int charId, int castleId) {
      this.setDiaryData(charId, 3, castleId);
      Castle castle = CastleManager.getInstance().getCastleById(castleId);
      if (castle != null && _herodiary.containsKey(charId)) {
         List<StatsSet> _list = _herodiary.get(charId);
         _herodiary.remove(charId);
         StatsSet _diaryentry = new StatsSet();
         String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(System.currentTimeMillis()));
         _diaryentry.set("date", date);
         _diaryentry.set("action", castle.getName() + " Castle was successfuly taken");
         _list.add(_diaryentry);
         _herodiary.put(charId, _list);
      }
   }

   public void setDiaryData(int charId, int action, int param) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
         statement.setInt(1, charId);
         statement.setLong(2, System.currentTimeMillis());
         statement.setInt(3, action);
         statement.setInt(4, param);
         statement.execute();
         statement.close();
      } catch (SQLException var17) {
         _log.log(Level.SEVERE, "SQL exception while saving DiaryData.", (Throwable)var17);
      }
   }

   public void setHeroMessage(Player player, String message) {
      _heroMessage.put(player.getObjectId(), message);
   }

   public void saveHeroMessage(int charId) {
      if (_heroMessage.get(charId) != null) {
         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE heroes SET message=? WHERE charId=?;");
            statement.setString(1, _heroMessage.get(charId));
            statement.setInt(2, charId);
            statement.execute();
            statement.close();
         } catch (SQLException var15) {
            _log.log(Level.SEVERE, "SQL exception while saving HeroMessage.", (Throwable)var15);
         }
      }
   }

   private void deleteItemsInDb() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390) AND owner_id NOT IN (SELECT charId FROM characters WHERE accesslevel > 0)"
         );
         statement.execute();
         statement.close();
      } catch (SQLException var14) {
         _log.log(Level.WARNING, "", (Throwable)var14);
      }
   }

   public void shutdown() {
      for(int charId : _heroMessage.keySet()) {
         this.saveHeroMessage(charId);
      }
   }

   public boolean isHero(int objectId) {
      if (_heroes != null && !_heroes.isEmpty()) {
         return _heroes.containsKey(objectId) && _heroes.get(objectId).getInteger("active") == 1;
      } else {
         return false;
      }
   }

   public boolean isInactiveHero(int id) {
      if (_heroes != null && !_heroes.isEmpty()) {
         return _heroes.containsKey(id) && _heroes.get(id).getInteger("played") == 1 && _heroes.get(id).getInteger("active") == 0;
      } else {
         return false;
      }
   }

   public void activateHero(Player player) {
      StatsSet hero = _heroes.get(player.getObjectId());
      hero.set("active", 1);
      _heroes.remove(player.getObjectId());
      _heroes.put(player.getObjectId(), hero);
      player.setHero(true, true);
      player.broadcastPacket(new SocialAction(player.getObjectId(), 20016));
      player.getCounters().addAchivementInfo("setHero", 0, -1L, false, false, false);
      Clan clan = player.getClan();
      if (clan != null) {
         clan.addReputationScore(Config.HERO_POINTS, true);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
         sm.addString(CharNameHolder.getInstance().getNameById(player.getObjectId()));
         sm.addNumber(Config.HERO_POINTS);
         clan.broadcastToOnlineMembers(sm);
      }

      player.broadcastUserInfo(true);
      this.setHeroGained(player.getObjectId());
      this.loadDiary(player.getObjectId());
      this.updateHero(player.getObjectId());
   }

   public void updateHero(int id) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE heroes SET count = ?, played = ?, active = ? WHERE charId = ?");

         for(Integer heroId : _heroes.keySet()) {
            if (id <= 0 || heroId == id) {
               StatsSet hero = _heroes.get(heroId);
               statement.setInt(1, hero.getInteger("count"));
               statement.setInt(2, hero.getInteger("played"));
               statement.setInt(3, hero.getInteger("active"));
               statement.setInt(4, heroId);
               statement.execute();
               statement.close();
            }
         }
      } catch (SQLException var18) {
         _log.log(Level.WARNING, "Hero System: Couldnt update Hero", (Throwable)var18);
      }
   }

   private static class SingletonHolder {
      protected static final Hero _instance = new Hero();
   }
}
