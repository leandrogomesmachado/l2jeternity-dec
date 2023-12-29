package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.listener.ScriptListenerLoader;
import l2e.gameserver.listener.ScriptManagerLoader;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;

public class QuestManager extends ScriptManagerLoader<Quest> {
   protected static final Logger _log = Logger.getLogger(QuestManager.class.getName());
   private final Map<String, Quest> _quests = new ConcurrentHashMap<>();
   private final Map<String, List<Integer>> _hwidList = new ConcurrentHashMap<>();

   protected QuestManager() {
      this._hwidList.clear();
      this.loadHwidList();
      this.checkTimeTask();
   }

   public final boolean reload(String questFolder) {
      Quest q = this.getQuest(questFolder);
      return q == null ? false : q.reload();
   }

   public final boolean reload(int questId) {
      Quest q = this.getQuest(questId);
      return q == null ? false : q.reload();
   }

   public final void reloadAllQuests() {
      _log.info("Reloading Server Scripts");

      for(Quest quest : this._quests.values()) {
         if (quest != null) {
            quest.unload(false);
         }
      }

      this._quests.clear();
      ScriptListenerLoader.getInstance().executeScriptList();
   }

   public final void save() {
      for(Quest q : this._quests.values()) {
         q.saveGlobalData();
      }
   }

   public final Quest getQuest(String name) {
      return this._quests.get(name);
   }

   public final Quest getQuest(int questId) {
      for(Quest q : this._quests.values()) {
         if (q.getId() == questId) {
            return q;
         }
      }

      return null;
   }

   public final void addQuest(Quest newQuest) {
      if (newQuest == null) {
         throw new IllegalArgumentException("Quest argument cannot be null");
      } else {
         Quest old = this._quests.get(newQuest.getName());
         if (old != null) {
            old.unload();
         }

         this._quests.put(newQuest.getName(), newQuest);
      }
   }

   public final boolean removeQuest(Quest q) {
      return this._quests.remove(q.getName()) != null;
   }

   @Override
   public Iterable<Quest> getAllManagedScripts() {
      return this._quests.values();
   }

   public boolean unload(Quest ms) {
      ms.saveGlobalData();
      return this.removeQuest(ms);
   }

   @Override
   public String getScriptManagerName() {
      return this.getClass().getSimpleName();
   }

   private void checkTimeTask() {
      Calendar currentTime = Calendar.getInstance();
      long lastUpdate = ServerVariables.getLong("Quest_HwidMap", 0L);
      if (currentTime.getTimeInMillis() > lastUpdate) {
         this.cleanHwidList();
      }
   }

   private void loadHwidList() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM character_quests_hwid_data");
         ResultSet rset = statement.executeQuery();
      ) {
         while(rset.next()) {
            List<Integer> quests;
            if ((quests = this._hwidList.get(rset.getString("hwid"))) == null) {
               this._hwidList.put(rset.getString("hwid"), quests = new ArrayList<>());
            }

            quests.add(rset.getInt("questId"));
            this._hwidList.put(rset.getString("hwid"), quests);
         }

         _log.info("QuestManager: Loaded " + this._hwidList.size() + " players quest hwid map.");
      } catch (Exception var59) {
         _log.log(Level.WARNING, "QuestManager: " + var59);
      }
   }

   public boolean isHwidAvailable(Player player, int questId) {
      String hwid = player.getHWID();
      if (this._hwidList.containsKey(hwid)) {
         for(int id : this._hwidList.get(hwid)) {
            if (id == questId) {
               return false;
            }
         }
      }

      return true;
   }

   public void insert(String hwid, int questId) {
      List<Integer> quests;
      if ((quests = this._hwidList.get(hwid)) == null) {
         this._hwidList.put(hwid, quests = new ArrayList<>());
      }

      quests.add(questId);
      this._hwidList.put(hwid, quests);

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_quests_hwid_data VALUES (?,?)");
      ) {
         statement.setString(1, hwid);
         statement.setInt(2, questId);
         statement.executeUpdate();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "QuestManager: " + var36);
      }
   }

   public void cleanHwidList() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests_hwid_data");
      ) {
         statement.execute();
      } catch (Exception var33) {
         _log.log(Level.SEVERE, "QuestManager: Failed to clean up quest hwid list.", (Throwable)var33);
      }

      this._hwidList.clear();
      Calendar newTime = Calendar.getInstance();
      newTime.setLenient(true);
      newTime.set(11, 6);
      newTime.set(12, 30);
      newTime.add(5, 1);
      ServerVariables.set("Quest_HwidMap", newTime.getTimeInMillis());
      _log.info("QuestManager: Hwids map refresh completed.");
   }

   public static final QuestManager getInstance() {
      return QuestManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final QuestManager _instance = new QuestManager();
   }
}
