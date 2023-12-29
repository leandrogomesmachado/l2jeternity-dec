package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.AccessLevel;
import l2e.gameserver.model.AdminCommandAccessRight;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.StatsSet;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AdminParser extends DocumentParser {
   private static final Map<Integer, AccessLevel> _accessLevels = new HashMap<>();
   private static final Map<String, AdminCommandAccessRight> _adminCommandAccessRights = new HashMap<>();
   private static final Map<Player, Boolean> _gmList = new ConcurrentHashMap<>();
   private int _highestLevel = 0;

   protected AdminParser() {
      this.load();
   }

   @Override
   public synchronized void load() {
      _accessLevels.clear();
      _adminCommandAccessRights.clear();
      this.parseDatapackFile("data/stats/admin/accessLevels.xml");
      this.parseDatapackFile("data/stats/admin/adminCommands.xml");
      this._log
         .log(
            Level.INFO,
            this.getClass().getSimpleName()
               + ": Loaded: "
               + _accessLevels.size()
               + " access levels and "
               + _adminCommandAccessRights.size()
               + " access commands."
         );
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equalsIgnoreCase(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               if ("access".equalsIgnoreCase(d.getNodeName())) {
                  StatsSet set = new StatsSet();
                  NamedNodeMap attrs = d.getAttributes();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node attr = attrs.item(i);
                     set.set(attr.getNodeName(), attr.getNodeValue());
                  }

                  AccessLevel level = new AccessLevel(set);
                  if (level.getLevel() > this._highestLevel) {
                     this._highestLevel = level.getLevel();
                  }

                  _accessLevels.put(level.getLevel(), level);
               } else if ("admin".equalsIgnoreCase(d.getNodeName())) {
                  StatsSet set = new StatsSet();
                  NamedNodeMap attrs = d.getAttributes();

                  for(int i = 0; i < attrs.getLength(); ++i) {
                     Node attr = attrs.item(i);
                     set.set(attr.getNodeName(), attr.getNodeValue());
                  }

                  AdminCommandAccessRight command = new AdminCommandAccessRight(set);
                  _adminCommandAccessRights.put(command.getAdminCommand(), command);
               }
            }
         }
      }
   }

   public AccessLevel getAccessLevel(int accessLevelNum) {
      if (accessLevelNum < 0) {
         return _accessLevels.get(-1);
      } else {
         if (!_accessLevels.containsKey(accessLevelNum)) {
            _accessLevels.put(accessLevelNum, new AccessLevel());
         }

         return _accessLevels.get(accessLevelNum);
      }
   }

   public AccessLevel getMasterAccessLevel() {
      return _accessLevels.get(this._highestLevel);
   }

   public boolean hasAccessLevel(int id) {
      return _accessLevels.containsKey(id);
   }

   public boolean hasAccess(String adminCommand, AccessLevel accessLevel) {
      AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);
      if (acar == null) {
         if (accessLevel.getLevel() <= 0 || accessLevel.getLevel() != this._highestLevel) {
            this._log.info(this.getClass().getSimpleName() + ": No rights defined for admin command " + adminCommand + " !");
            return false;
         }

         acar = new AdminCommandAccessRight(adminCommand, true, accessLevel.getLevel());
         _adminCommandAccessRights.put(adminCommand, acar);
         this._log
            .info(
               this.getClass().getSimpleName()
                  + ": No rights defined for admin command "
                  + adminCommand
                  + " auto setting accesslevel: "
                  + accessLevel.getLevel()
                  + " !"
            );
      }

      return acar.hasAccess(accessLevel);
   }

   public boolean requireConfirm(String command) {
      AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
      if (acar == null) {
         this._log.info(this.getClass().getSimpleName() + ": No rights defined for admin command " + command + ".");
         return false;
      } else {
         return acar.getRequireConfirm();
      }
   }

   public List<Player> getAllGms(boolean includeHidden) {
      List<Player> tmpGmList = new ArrayList<>();

      for(Entry<Player, Boolean> entry : _gmList.entrySet()) {
         if (includeHidden || !entry.getValue()) {
            tmpGmList.add(entry.getKey());
         }
      }

      return tmpGmList;
   }

   public List<String> getAllGmNames(boolean includeHidden) {
      List<String> tmpGmList = new ArrayList<>();

      for(Entry<Player, Boolean> entry : _gmList.entrySet()) {
         if (!entry.getValue()) {
            tmpGmList.add(entry.getKey().getName());
         } else if (includeHidden) {
            tmpGmList.add(entry.getKey().getName() + " (invis)");
         }
      }

      return tmpGmList;
   }

   public void addGm(Player player, boolean hidden) {
      _gmList.put(player, hidden);
   }

   public void deleteGm(Player player) {
      _gmList.remove(player);
   }

   public void showGm(Player player) {
      if (_gmList.containsKey(player)) {
         _gmList.put(player, false);
      }
   }

   public void hideGm(Player player) {
      if (_gmList.containsKey(player)) {
         _gmList.put(player, true);
      }
   }

   public boolean isGmOnline(boolean includeHidden) {
      for(Entry<Player, Boolean> entry : _gmList.entrySet()) {
         if (includeHidden || !entry.getValue()) {
            return true;
         }
      }

      return false;
   }

   public void sendListToPlayer(Player player) {
      if (this.isGmOnline(player.isGM())) {
         player.sendPacket(SystemMessageId.GM_LIST);

         for(String name : this.getAllGmNames(player.isGM())) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_C1);
            sm.addString(name);
            player.sendPacket(sm);
         }
      } else {
         player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
      }
   }

   public void broadcastToGMs(GameServerPacket packet) {
      for(Player gm : this.getAllGms(true)) {
         gm.sendPacket(packet);
      }
   }

   public void broadcastMessageToGMs(String message) {
      for(Player gm : this.getAllGms(true)) {
         gm.sendMessage(message);
      }
   }

   public static AdminParser getInstance() {
      return AdminParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final AdminParser _instance = new AdminParser();
   }
}
