package l2e.gameserver.network.serverpackets;

import java.util.logging.Level;
import l2e.commons.util.HtmlUtil;
import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.NpcStringId;

public final class NpcHtmlMessage extends GameServerPacket {
   private final int _npcObjId;
   private String _html;
   private int _itemId = 0;

   public NpcHtmlMessage(int npcObjId, int itemId) {
      this._npcObjId = npcObjId;
      this._itemId = itemId;
   }

   public NpcHtmlMessage(Player player, int npcObjId, int itemId, String text) {
      this._npcObjId = npcObjId;
      this._itemId = itemId;
      this._html = text;
   }

   public NpcHtmlMessage(Player player, int npcObjId, String text) {
      this._npcObjId = npcObjId;
      this.setHtml(player, text);
   }

   public NpcHtmlMessage(int npcObjId) {
      this._npcObjId = npcObjId;
   }

   public void setHtml(Player player, String text) {
      if (text.length() > 24540) {
         _log.log(Level.WARNING, "Html is too long! this will crash the client!", new Throwable());
         this._html = text.substring(0, 24540);
      }

      if (!text.contains("<html")) {
         text = "<html><body>" + text + "</body></html>";
      }

      this._html = text;
   }

   public boolean setFile(Player player, String prefix, String path) {
      String oriPath = path;
      if (prefix != null && !prefix.equalsIgnoreCase("en") && path.contains("html/")) {
         path = path.replace("html/", "html-" + prefix + "/");
      }

      String content = HtmCache.getInstance().getHtm(player, path);
      if (content == null && !oriPath.equals(path)) {
         content = HtmCache.getInstance().getHtm(player, oriPath);
      }

      if (content == null) {
         this.setHtml(player, "<html><body>My Text is missing:<br>" + path + "</body></html>");
         _log.warning("missing html page " + path);
         return false;
      } else {
         this.setHtml(player, content);
         return true;
      }
   }

   public boolean setFile(Player player, String path) {
      String content = HtmCache.getInstance().getHtm(player, path);
      if (content == null) {
         this.setHtml(player, "<html><body>My Text is missing:<br>" + path + "</body></html>");
         _log.warning("Missing html page: " + path);
         return false;
      } else {
         this.setHtml(player, content);
         return true;
      }
   }

   public void replace(String pattern, String value) {
      this._html = this._html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
   }

   public void replace(String pattern, long value) {
      this.replace(pattern, String.valueOf(value));
   }

   public void replace2(String pattern, String value) {
      this._html = this._html.replaceAll(pattern, value);
   }

   public void replaceNpcString(String pattern, NpcStringId npcString, Object... arg) {
      if (pattern != null) {
         this._html = this._html.replaceAll(pattern, HtmlUtil.htmlNpcString(npcString, arg));
      }
   }

   public boolean setEventHtml(Player player, String path) {
      String content = HtmCache.getInstance().getHtm(player, path);
      if (content == null) {
         return false;
      } else {
         this.setHtml(player, content);
         return true;
      }
   }

   public String getText() {
      return this._html;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._npcObjId);
      this.writeS(this._html);
      if (this._npcObjId != 0) {
         this.writeD(this._itemId);
      }
   }

   public String getHtm() {
      return this._html;
   }
}
