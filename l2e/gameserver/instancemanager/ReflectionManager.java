package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2e.gameserver.Config;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.handler.voicedcommandhandlers.VoicedCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.daily.DailyTaskTemplate;
import l2e.gameserver.model.actor.templates.player.PlayerTaskTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionNameTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ReflectionManager extends DocumentParser {
   private static final Map<Integer, Reflection> _reflectionList = new ConcurrentHashMap<>();
   private final Map<Integer, ReflectionWorld> _reflectionWorlds = new ConcurrentHashMap<>();
   private int _dynamic = 300000;
   private static final Map<Integer, ReflectionNameTemplate> _reflectionNames = new HashMap<>();
   private final Map<Integer, Map<Integer, Long>> _playerRefTimes = new ConcurrentHashMap<>();
   private final ReadWriteLock lock = new ReentrantReadWriteLock();
   private final Lock readLock = this.lock.readLock();
   private static final String ADD_REFLECTION_TIME = "INSERT INTO character_instance_time (charId,instanceId,time) values (?,?,?) ON DUPLICATE KEY UPDATE time=?";
   private static final String RESTORE_REFLECTION_TIMES = "SELECT instanceId,time FROM character_instance_time WHERE charId=?";
   private static final String DELETE_REFLECTION_TIME = "DELETE FROM character_instance_time WHERE charId=? AND instanceId=?";

   protected ReflectionManager() {
      _reflectionList.put(-1, new Reflection(-1, "multiverse"));
      _reflectionList.put(0, new Reflection(0, "universe"));
      this.load();
   }

   @Override
   public void load() {
      _reflectionNames.clear();
      this.parseDatapackFile("data/stats/reflectionNames.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + _reflectionNames.size() + " reflection names.");
   }

   @Override
   protected void reloadDocument() {
   }

   public long getReflectionTime(int playerObjId, int id) {
      if (!this._playerRefTimes.containsKey(playerObjId)) {
         this.restoreReflectionTimes(playerObjId);
      }

      return this._playerRefTimes.get(playerObjId).containsKey(id) ? this._playerRefTimes.get(playerObjId).get(id) : -1L;
   }

   public Map<Integer, Long> getAllReflectionTimes(int playerObjId) {
      if (!this._playerRefTimes.containsKey(playerObjId)) {
         this.restoreReflectionTimes(playerObjId);
      }

      return this._playerRefTimes.get(playerObjId);
   }

   public List<Integer> getLockedReflectionList(int playerObjId) {
      List<Integer> result = new ArrayList<>();
      Map<Integer, Long> refTimes = this.getAllReflectionTimes(playerObjId);
      if (refTimes != null) {
         for(int reflectionId : refTimes.keySet()) {
            long remainingTime = (refTimes.get(reflectionId) - System.currentTimeMillis()) / 1000L;
            if (remainingTime > 60L) {
               result.add(reflectionId);
            }
         }
      }

      return result;
   }

   public void setReflectionTime(int playerObjId, int id, long time) {
      Player player = World.getInstance().getPlayer(playerObjId);
      if (player != null && player.isOnline()) {
         player.getCounters().addAchivementInfo("reflectionById", id, -1L, false, false, false);
         player.getCounters().addAchivementInfo("reflectionsFinish", 0, -1L, false, false, false);
         if (Config.ALLOW_DAILY_TASKS && player.getActiveDailyTasks() != null) {
            for(PlayerTaskTemplate taskTemplate : player.getActiveDailyTasks()) {
               if (taskTemplate.getType().equalsIgnoreCase("Reflection") && !taskTemplate.isComplete()) {
                  DailyTaskTemplate task = DailyTaskManager.getInstance().getDailyTask(taskTemplate.getId());
                  if (task.getReflectionId() == id) {
                     taskTemplate.setIsComplete(true);
                     player.updateDailyStatus(taskTemplate);
                     IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getHandler("missions");
                     if (vch != null) {
                        player.updateDailyStatus(taskTemplate);
                        vch.useVoicedCommand("missions", player, null);
                     }
                  }
               }
            }
         }
      }

      if (!this._playerRefTimes.containsKey(playerObjId)) {
         this.restoreReflectionTimes(playerObjId);
      }

      if (player != null && player.isOnline() && player.hasPremiumBonus()) {
         long nextTimte = time - System.currentTimeMillis();
         time = (long)((double)System.currentTimeMillis() + (double)nextTimte * player.getPremiumBonus().getReflectionReduce());
      }

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO character_instance_time (charId,instanceId,time) values (?,?,?) ON DUPLICATE KEY UPDATE time=?"
         );
         statement.setInt(1, playerObjId);
         statement.setInt(2, id);
         statement.setLong(3, time);
         statement.setLong(4, time);
         statement.execute();
         statement.close();
         this._playerRefTimes.get(playerObjId).put(id, time);
      } catch (Exception var20) {
         this._log.warning(this.getClass().getSimpleName() + ": Could not insert character reflection time data: " + var20.getMessage());
      }
   }

   public void deleteReflectionTime(int playerObjId, int id) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=? AND instanceId=?");
         statement.setInt(1, playerObjId);
         statement.setInt(2, id);
         statement.execute();
         statement.close();
         this._playerRefTimes.get(playerObjId).remove(id);
      } catch (Exception var16) {
         this._log.warning(this.getClass().getSimpleName() + ": Could not delete character reflection time data: " + var16.getMessage());
      }
   }

   public void restoreReflectionTimes(int playerObjId) {
      if (!this._playerRefTimes.containsKey(playerObjId)) {
         this._playerRefTimes.put(playerObjId, new ConcurrentHashMap<>());

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("SELECT instanceId,time FROM character_instance_time WHERE charId=?");
            statement.setInt(1, playerObjId);
            ResultSet rset = statement.executeQuery();

            while(rset.next()) {
               int id = rset.getInt("instanceId");
               long time = rset.getLong("time");
               if (time < System.currentTimeMillis()) {
                  this.deleteReflectionTime(playerObjId, id);
               } else {
                  this._playerRefTimes.get(playerObjId).put(id, time);
               }
            }

            rset.close();
            statement.close();
         } catch (Exception var19) {
            this._log.warning(this.getClass().getSimpleName() + ": Could not delete character reflection time data: " + var19.getMessage());
         }
      }
   }

   public String getReflectionName(Player player, int id) {
      if (_reflectionNames.containsKey(id)) {
         ReflectionNameTemplate tpl = _reflectionNames.get(id);
         if (tpl != null) {
            return player.getLang() != null && !player.getLang().equalsIgnoreCase("en") ? tpl.getNameRu() : tpl.getNameEn();
         }
      }

      return "UnknownInstance";
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("instance".equals(d.getNodeName())) {
                  NamedNodeMap attrs = d.getAttributes();
                  _reflectionNames.put(
                     parseInteger(attrs, "id"),
                     new ReflectionNameTemplate(attrs.getNamedItem("nameEn").getNodeValue(), attrs.getNamedItem("nameRu").getNodeValue())
                  );
               }
            }
         }
      }
   }

   public void addWorld(ReflectionWorld world) {
      this._reflectionWorlds.put(world.getReflectionId(), world);
   }

   public ReflectionWorld getWorld(int reflectionId) {
      return this._reflectionWorlds.get(reflectionId);
   }

   public ReflectionWorld getPlayerWorld(Player player) {
      for(ReflectionWorld temp : this._reflectionWorlds.values()) {
         if (temp != null && temp.isAllowed(player.getObjectId())) {
            return temp;
         }
      }

      return null;
   }

   public void destroyReflection(int reflectionId) {
      if (reflectionId > 0) {
         Reflection temp = _reflectionList.get(reflectionId);
         if (temp != null) {
            temp.cleanupNpcs();
            temp.cleanupPlayers();
            temp.cleanupDoors();
            temp.cleanupItems();
            temp.cleanupFences();
            temp.cleanupZones();
            temp.cancelTimer();
            GeoEngine.FreeGeoIndex(temp.getGeoIndex());
            _reflectionList.remove(reflectionId);
            if (this._reflectionWorlds.containsKey(reflectionId)) {
               this._reflectionWorlds.remove(reflectionId);
            }
         }
      }
   }

   public Reflection getReflection(int reflectionId) {
      return _reflectionList.get(reflectionId);
   }

   public Map<Integer, Reflection> getReflections() {
      return _reflectionList;
   }

   public int getPlayerReflection(int objectId) {
      for(Reflection temp : _reflectionList.values()) {
         if (temp != null && temp.containsPlayer(objectId)) {
            return temp.getId();
         }
      }

      return 0;
   }

   public int createReflection() {
      this._dynamic = 1;

      while(this.getReflection(this._dynamic) != null) {
         ++this._dynamic;
         if (this._dynamic == Integer.MAX_VALUE) {
            this._log.warning("ReflectionManager: More then 2147183647 reflections created");
            this._dynamic = 300000;
         }
      }

      Reflection ref = new Reflection(this._dynamic);
      _reflectionList.put(this._dynamic, ref);
      return this._dynamic;
   }

   public boolean createReflection(int id) {
      if (this.getReflection(id) != null) {
         return false;
      } else if (id > 0 && id < 300000 && !this.reflectionExist(id)) {
         Reflection ref = new Reflection(id);
         _reflectionList.put(id, ref);
         return true;
      } else {
         return false;
      }
   }

   public boolean createReflectionFromTemplate(int id, String template) {
      if (this.getReflection(id) != null) {
         return false;
      } else if (id > 0 && id < 300000 && !this.reflectionExist(id)) {
         Reflection ref = new Reflection(id);
         _reflectionList.put(id, ref);
         ref.loadInstanceTemplate(template);
         return true;
      } else {
         return false;
      }
   }

   public Reflection createDynamicReflection(String template) {
      while(this.getReflection(this._dynamic) != null) {
         ++this._dynamic;
         if (this._dynamic == Integer.MAX_VALUE) {
            this._log.warning(this.getClass().getSimpleName() + ": More then " + 2147183647 + " reflections created");
            this._dynamic = 300000;
         }
      }

      Reflection ref = new Reflection(this._dynamic);
      _reflectionList.put(this._dynamic, ref);
      if (template != null) {
         ref.loadInstanceTemplate(template);
      }

      return ref;
   }

   public Reflection createDynamicReflection(ReflectionTemplate template) {
      while(this.getReflection(this._dynamic) != null) {
         ++this._dynamic;
         if (this._dynamic == Integer.MAX_VALUE) {
            this._log.warning(this.getClass().getSimpleName() + ": More then " + 2147183647 + " reflections created");
            this._dynamic = 300000;
         }
      }

      Reflection ref = new Reflection(this._dynamic);
      _reflectionList.put(this._dynamic, ref);
      if (template != null) {
         ref.loadReflectionTemplate(template);
      }

      return ref;
   }

   public boolean reflectionExist(int reflectionId) {
      return _reflectionList.get(reflectionId) != null;
   }

   public int getCountByIzId(int izId) {
      this.readLock.lock();

      int var8;
      try {
         int i = 0;

         for(ReflectionWorld r : this._reflectionWorlds.values()) {
            if (r != null && r.getTemplateId() == izId) {
               ++i;
            }
         }

         var8 = i;
      } finally {
         this.readLock.unlock();
      }

      return var8;
   }

   public static final ReflectionManager getInstance() {
      return ReflectionManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ReflectionManager _instance = new ReflectionManager();
   }
}
