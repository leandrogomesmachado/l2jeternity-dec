package l2e.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.Broadcast;
import l2e.commons.util.StringUtil;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Announcements {
   private static Logger _log = Logger.getLogger(Announcements.class.getName());
   private final List<String> _announcements = new ArrayList<>();
   private final List<String> _critAnnouncements = new ArrayList<>();

   protected Announcements() {
      this.loadAnnouncements();
   }

   public static Announcements getInstance() {
      return Announcements.SingletonHolder._instance;
   }

   public void loadAnnouncements() {
      this._announcements.clear();
      this._critAnnouncements.clear();
      this.readFromDisk("data/announcements.txt", this._announcements);
      this.readFromDisk("data/critannouncements.txt", this._critAnnouncements);
      if (Config.DEBUG) {
         _log.info("Announcements: Loaded " + (this._announcements.size() + this._critAnnouncements.size()) + " announcements.");
      }
   }

   public void showAnnouncements(Player activeChar) {
      for(String announce : this._announcements) {
         CreatureSay cs = new CreatureSay(0, 10, activeChar.getName(), announce);
         activeChar.sendPacket(cs);
      }

      for(String critAnnounce : this._critAnnouncements) {
         CreatureSay cs = new CreatureSay(0, 18, activeChar.getName(), critAnnounce);
         activeChar.sendPacket(cs);
      }
   }

   public void listAnnouncements(Player activeChar) {
      String content = HtmCache.getInstance().getHtmForce(activeChar, activeChar.getLang(), "data/html/admin/announce.htm");
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setHtml(activeChar, content);
      StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");

      for(int i = 0; i < this._announcements.size(); ++i) {
         StringUtil.append(
            replyMSG,
            "<table width=260><tr><td width=220>",
            this._announcements.get(i),
            "</td><td width=40><button value=\"Delete\" action=\"bypass -h admin_del_announcement ",
            String.valueOf(i),
            "\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>"
         );
      }

      adminReply.replace("%announces%", replyMSG.toString());
      activeChar.sendPacket(adminReply);
   }

   public void listCritAnnouncements(Player activeChar) {
      String content = HtmCache.getInstance().getHtmForce(activeChar, activeChar.getLang(), "data/html/admin/critannounce.htm");
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
      adminReply.setHtml(activeChar, content);
      StringBuilder replyMSG = StringUtil.startAppend(500, "<br>");

      for(int i = 0; i < this._critAnnouncements.size(); ++i) {
         StringUtil.append(
            replyMSG,
            "<table width=260><tr><td width=220>",
            this._critAnnouncements.get(i),
            "</td><td width=40><button value=\"Delete\" action=\"bypass -h admin_del_critannouncement ",
            String.valueOf(i),
            "\" width=60 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>"
         );
      }

      adminReply.replace("%critannounces%", replyMSG.toString());
      activeChar.sendPacket(adminReply);
   }

   public void addAnnouncement(String text) {
      this._announcements.add(text);
      this.saveToDisk(false);
   }

   public void delAnnouncement(int line) {
      this._announcements.remove(line);
      this.saveToDisk(false);
   }

   public void addCritAnnouncement(String text) {
      this._critAnnouncements.add(text);
      this.saveToDisk(true);
   }

   public void delCritAnnouncement(int line) {
      this._critAnnouncements.remove(line);
      this.saveToDisk(true);
   }

   private void readFromDisk(String path, List<String> list) {
      File file = new File(Config.DATAPACK_ROOT, path);
      if (file.exists()) {
         try (LineNumberReader lnr = new LineNumberReader(new FileReader(file))) {
            String line = null;

            while((line = lnr.readLine()) != null) {
               StringTokenizer st = new StringTokenizer(line, Config.EOL);
               if (st.hasMoreTokens()) {
                  list.add(st.nextToken());
               }
            }
         } catch (IOException var18) {
            _log.log(Level.SEVERE, "Error reading announcements: ", (Throwable)var18);
         }
      } else {
         _log.warning(file.getAbsolutePath() + " doesn't exist");
      }
   }

   private void saveToDisk(boolean isCritical) {
      String path;
      List<String> list;
      if (isCritical) {
         path = "data/critannouncements.txt";
         list = this._critAnnouncements;
      } else {
         path = "data/announcements.txt";
         list = this._announcements;
      }

      File file = new File(path);

      try (FileWriter save = new FileWriter(file)) {
         for(String announce : list) {
            save.write(announce);
            save.write(Config.EOL);
         }
      } catch (IOException var19) {
         _log.log(Level.SEVERE, "Saving to the announcements file has failed: ", (Throwable)var19);
      }
   }

   public void announceToAll(ServerMessage msg) {
      for(Player onlinePlayer : World.getInstance().getAllPlayers()) {
         if (onlinePlayer.isOnline()) {
            onlinePlayer.sendPacket(new CreatureSay(0, 10, "", msg.toString(onlinePlayer.getLang())));
         }
      }
   }

   public void announceToAll(String text) {
      this.announceToAll(text, false);
   }

   public void gameAnnounceToAll(String text) {
      CreatureSay cs = new CreatureSay(0, 18, "", "Announcements: " + text);

      for(Player player : World.getInstance().getAllPlayers()) {
         if (player != null && player.isOnline()) {
            player.sendPacket(cs);
         }
      }

      cs = null;
   }

   public void announceToAll(String text, boolean isCritical) {
      Broadcast.announceToOnlinePlayers(text, isCritical);
   }

   public void announceToAll(SystemMessage sm) {
      Broadcast.toAllOnlinePlayers(sm);
   }

   public void announceToInstance(SystemMessage sm, int instanceId) {
      Broadcast.toPlayersInInstance(sm, instanceId);
   }

   public void handleAnnounce(String command, int lengthToTrim, boolean isCritical) {
      try {
         String text = command.substring(lengthToTrim);
         Announcements.SingletonHolder._instance.announceToAll(text, isCritical);
      } catch (StringIndexOutOfBoundsException var5) {
      }
   }

   private static class SingletonHolder {
      protected static final Announcements _instance = new Announcements();
   }
}
