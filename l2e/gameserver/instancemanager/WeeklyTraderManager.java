package l2e.gameserver.instancemanager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.parser.MultiSellParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.multisell.Entry;
import l2e.gameserver.model.items.multisell.Ingredient;
import l2e.gameserver.model.items.multisell.ListContainer;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.strings.server.ServerStorage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class WeeklyTraderManager {
   private static final Logger _log = Logger.getLogger(WeeklyTraderManager.class.getName());
   private final List<Entry> _selectedEntries = new ArrayList<>();
   private final List<WeeklyTraderManager.WeeklyTraderSpawn> _spawnList = new ArrayList<>();
   private final List<Npc> _npcList = new ArrayList<>();
   private static final int _activeTime = 604800000;
   private boolean _isTradingPeriod;
   private boolean _isAlwaysSpawn;
   private long _endingTime = 0L;
   private long _startTime = 0L;
   private int _validNpcId;

   public WeeklyTraderManager() {
      if (Config.WEEKLY_TRADER_ENABLE) {
         this.load(false);
         this.validatePeriod();
      }
   }

   public void validatePeriod() {
      this._endingTime = 0L;
      int period = this.getWeekOfTradingPeriod();
      if (period == 0) {
         this._isTradingPeriod = false;
         this.cleanEntries();
         this.generateNewEntries();
         this.saveEntries();
         this.createMultisell();
         this.scheduleStartPeriod();
      } else {
         this._isTradingPeriod = true;
         _log.info("WeeklyTraderManager: Currently in Trading Period.");
         this.loadEntries();
         this.createMultisell();
         this.spawnNPC();
         this.scheduleEndPeriod(period == 1);
      }
   }

   private void startPeriod() {
      _log.info("WeeklyTraderManager: New trading period has been started!");

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.isOnline()) {
            player.sendPacket(
               new CreatureSay(
                  0,
                  10,
                  ServerStorage.getInstance().getString(player.getLang(), "WeeklyTrader.TRADER"),
                  ServerStorage.getInstance().getString(player.getLang(), "WeeklyTrader.PERIOD_START")
               )
            );
         }
      }

      this.spawnNPC();
      this._isTradingPeriod = true;
      long duration = (long)(Config.WEEKLY_TRADER_DURATION * 60 * 1000);
      this._endingTime = System.currentTimeMillis() + duration;
      _log.info("WeeklyTraderManager: Period will end in " + duration + " min(s).");
      ThreadPoolManager.getInstance().schedule(() -> this.endPeriod(), duration);
   }

   private void scheduleStartPeriod() {
      Calendar thisWeek = this.getConfiguredCalendar();
      long nextStart;
      if (thisWeek.getTimeInMillis() > System.currentTimeMillis()) {
         nextStart = thisWeek.getTimeInMillis();
      } else {
         nextStart = thisWeek.getTimeInMillis() + 604800000L;
      }

      this._startTime = nextStart;
      _log.info("WeeklyTraderManager: Next Trading Period: " + new Date(nextStart));
      long delay = nextStart - System.currentTimeMillis();
      ThreadPoolManager.getInstance().schedule(() -> this.startPeriod(), delay);
   }

   private void scheduleEndPeriod(boolean startedThisWeek) {
      long duration = (long)(Config.WEEKLY_TRADER_DURATION * 60 * 1000);
      Calendar thisWeek = this.getConfiguredCalendar();
      long endTime = thisWeek.getTimeInMillis() + duration;
      if (!startedThisWeek) {
         endTime -= 604800000L;
      }

      _log.info("WeeklyTraderManager: Period ends " + new Date(endTime));
      long delay = endTime - System.currentTimeMillis();
      this._endingTime = endTime;
      ThreadPoolManager.getInstance().schedule(() -> this.endPeriod(), delay);
   }

   private void endPeriod() {
      _log.info("WeeklyTraderManager: Period ended!");

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.isOnline()) {
            player.sendPacket(
               new CreatureSay(
                  0,
                  10,
                  ServerStorage.getInstance().getString(player.getLang(), "WeeklyTrader.TRADER"),
                  ServerStorage.getInstance().getString(player.getLang(), "WeeklyTrader.PERIOD_END")
               )
            );
         }
      }

      this._isTradingPeriod = false;
      if (!this._npcList.isEmpty() && !this._isAlwaysSpawn) {
         for(Npc npc : this._npcList) {
            if (npc != null) {
               npc.deleteMe();
            }
         }

         this._npcList.clear();
      }

      this.validatePeriod();
   }

   private int getWeekOfTradingPeriod() {
      long currentTime = System.currentTimeMillis();
      long duration = (long)(Config.WEEKLY_TRADER_DURATION * 60 * 1000);
      Calendar thisWeek = this.getConfiguredCalendar();
      boolean after = currentTime > thisWeek.getTimeInMillis();
      boolean beforeEnd = currentTime < thisWeek.getTimeInMillis() + duration;
      if (after && beforeEnd) {
         return 1;
      } else {
         long previousWeek = thisWeek.getTimeInMillis() - 604800000L;
         after = currentTime > previousWeek;
         beforeEnd = currentTime < previousWeek + duration;
         return after && beforeEnd ? 2 : 0;
      }
   }

   private void spawnNPC() {
      if (this._npcList.size() <= 0) {
         for(WeeklyTraderManager.WeeklyTraderSpawn spawn : this._spawnList) {
            this._validNpcId = spawn.getNpcId();
            this._npcList
               .add(
                  Quest.addSpawn(
                     spawn.getNpcId(),
                     spawn.getLocation().getX(),
                     spawn.getLocation().getY(),
                     spawn.getLocation().getZ(),
                     spawn.getLocation().getHeading(),
                     false,
                     0L
                  )
               );
         }
      }
   }

   private void generateNewEntries() {
      this.load(true);
   }

   public void load(boolean isParseItems) {
      Document doc = null;
      File file = new File(Config.DATAPACK_ROOT, "data/stats/services/weeklyTrader.xml");
      if (!file.exists()) {
         _log.warning(this.getClass().getSimpleName() + ": weeklyTrader.xml file is missing.");
      } else {
         try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(file);
         } catch (Exception var6) {
            var6.printStackTrace();
         }

         try {
            if (isParseItems) {
               this.parseItemsInfo(doc);
            } else {
               this.parseNpcsInfo(doc);
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      }
   }

   public void parseNpcsInfo(Document doc) {
      this._npcList.clear();

      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
               if ("spawnlist".equalsIgnoreCase(c.getNodeName())) {
                  this._isAlwaysSpawn = Boolean.parseBoolean(c.getAttributes().getNamedItem("isAlwaysSpawn").getNodeValue());

                  for(Node d = c.getFirstChild(); d != null; d = d.getNextSibling()) {
                     if ("npc".equalsIgnoreCase(d.getNodeName())) {
                        int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                        int x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
                        int z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
                        int heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
                        this._spawnList.add(new WeeklyTraderManager.WeeklyTraderSpawn(id, new Location(x, y, z, heading)));
                     }
                  }
               }
            }
         }
      }

      _log.info("WeeklyTraderManager: loaded " + this._spawnList.size() + " npc locations...");
      if (this._isAlwaysSpawn) {
         this.spawnNPC();
      }
   }

   public void parseItemsInfo(Document doc) {
      _log.info("WeeklyTraderManager: selecting random entries...");
      this._selectedEntries.clear();

      for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("category".equalsIgnoreCase(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  double chance = (double)Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
                  if (Rnd.chance(chance)) {
                     List<WeeklyTraderManager.Item> entries = new ArrayList<>();

                     for(Node item = d.getFirstChild(); item != null; item = item.getNextSibling()) {
                        if ("item".equalsIgnoreCase(item.getNodeName())) {
                           WeeklyTraderManager.Item entry = new WeeklyTraderManager.Item();

                           for(Node ing = item.getFirstChild(); ing != null; ing = ing.getNextSibling()) {
                              if ("production".equalsIgnoreCase(ing.getNodeName())) {
                                 attrs = ing.getAttributes();
                                 int id = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
                                 long count = Long.parseLong(attrs.getNamedItem("count").getNodeValue());
                                 Ingredient i = new Ingredient(id, count, 0, -1, null, null, false, false);
                                 entry.products.add(i);
                              } else if ("ingredient".equalsIgnoreCase(ing.getNodeName())) {
                                 attrs = ing.getAttributes();
                                 int id = Integer.parseInt(attrs.getNamedItem("itemId").getNodeValue());
                                 long min = Long.parseLong(attrs.getNamedItem("minCount").getNodeValue());
                                 long max = Long.parseLong(attrs.getNamedItem("maxCount").getNodeValue());
                                 Ingredient i = new Ingredient(id, Rnd.get(min, max), 0, -1, null, null, false, false);
                                 entry.ingredients.add(i);
                              }
                           }

                           entries.add(entry);
                        }
                     }

                     WeeklyTraderManager.Item randomEntryOfThisCategory = entries.get(Rnd.get(entries.size()));
                     Entry e = new Entry(this._selectedEntries.size() + 1);

                     for(Ingredient i : randomEntryOfThisCategory.ingredients) {
                        e.addIngredient(i);
                     }

                     for(Ingredient i : randomEntryOfThisCategory.products) {
                        e.addProduct(i);
                     }

                     this._selectedEntries.add(e);
                  }
               }
            }
         }
      }
   }

   private Calendar getConfiguredCalendar() {
      Calendar c = Calendar.getInstance();
      c.set(7, Config.WEEKLY_TRADER_DAY_OF_WEEK);
      c.set(11, Config.WEEKLY_TRADER_HOUR_OF_DAY);
      c.set(12, Config.WEEKLY_TRADER_MINUTE_OF_DAY);
      c.set(13, 0);
      return c;
   }

   private void createMultisell() {
      ListContainer listContainer = new ListContainer(Config.WEEKLY_TRADER_MULTISELL_ID);

      for(Entry e : this._selectedEntries) {
         listContainer.getEntries().add(e);
      }

      int npcId = 0;
      if (!this._spawnList.isEmpty()) {
         for(WeeklyTraderManager.WeeklyTraderSpawn spawn : this._spawnList) {
            if (spawn != null) {
               npcId = spawn.getNpcId();
               break;
            }
         }
      }

      listContainer.allowNpc(npcId);
      MultiSellParser.getInstance().getEntries().put(Config.WEEKLY_TRADER_MULTISELL_ID, listContainer);
   }

   public boolean isTradingPeriod() {
      return this._isTradingPeriod;
   }

   public long getEndingTime() {
      return this._endingTime;
   }

   public long getStartTime() {
      return this._startTime;
   }

   private void cleanEntries() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM weekly_trader_entries");
      ) {
         int k = ps.executeUpdate();
         _log.info("WeeklyTraderManager: Cleaned " + k + " entries from database.");
      } catch (Exception var33) {
         _log.log(Level.WARNING, "WeeklyTraderManager: Error cleaning entries from database.", (Throwable)var33);
      }
   }

   private void loadEntries() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT * FROM weekly_trader_entries");
         ResultSet rset = ps.executeQuery();
      ) {
         int entryId = 0;

         while(rset.next()) {
            Entry e = new Entry(++entryId);
            String[] productSplit = rset.getString("products").split(";");

            for(String product : productSplit) {
               int id = Integer.parseInt(product.split(",")[0]);
               long count = Long.parseLong(product.split(",")[1]);
               e.addProduct(new Ingredient(id, count, 0, -1, null, null, false, false));
            }

            String[] ingredintSplit = rset.getString("ingredients").split(";");

            for(String ingredient : ingredintSplit) {
               int id = Integer.parseInt(ingredient.split(",")[0]);
               long count = Long.parseLong(ingredient.split(",")[1]);
               e.addIngredient(new Ingredient(id, count, 0, -1, null, null, false, false));
            }

            this._selectedEntries.add(e);
         }
      } catch (Exception var69) {
         _log.log(Level.WARNING, "WeeklyTraderManager: Error loading entries from database.", (Throwable)var69);
      }
   }

   private void saveEntries() {
      for(Entry e : this._selectedEntries) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO weekly_trader_entries (products,ingredients) values(?,?)");
         ) {
            String products = "";
            String ingredients = "";

            for(Ingredient product : e.getProducts()) {
               products = products + product.getId() + ",";
               products = products + product.getCount() + ";";
            }

            for(Ingredient ingredient : e.getIngredients()) {
               ingredients = ingredients + ingredient.getId() + ",";
               ingredients = ingredients + ingredient.getCount() + ";";
            }

            ps.setString(1, products);
            ps.setString(2, ingredients);
            ps.execute();
            con.close();
            ps.close();
         } catch (Exception var38) {
            _log.log(Level.WARNING, "WeeklyTraderManager: Error saving entries into database.", (Throwable)var38);
         }
      }
   }

   public int getValidNpcId() {
      return this._validNpcId;
   }

   public static WeeklyTraderManager getInstance() {
      return WeeklyTraderManager.SingletonHolder._instance;
   }

   private class Item {
      private final List<Ingredient> products = new ArrayList<>();
      private final List<Ingredient> ingredients = new ArrayList<>();

      private Item() {
      }
   }

   private static class SingletonHolder {
      protected static final WeeklyTraderManager _instance = new WeeklyTraderManager();
   }

   private class WeeklyTraderSpawn {
      protected final int _npcId;
      protected final Location _loc;

      public WeeklyTraderSpawn(int npcId, Location loc) {
         this._npcId = npcId;
         this._loc = loc;
      }

      public int getNpcId() {
         return this._npcId;
      }

      public Location getLocation() {
         return this._loc;
      }
   }
}
