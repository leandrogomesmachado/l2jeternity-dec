package l2e.gameserver.network.serverpackets;

import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.model.actor.Player;

public final class ExNpcQuestHtmlMessage extends GameServerPacket {
   private final int _npcObjId;
   private String _html;
   private int _questId = 0;

   public ExNpcQuestHtmlMessage(int npcObjId, int questId) {
      this._npcObjId = npcObjId;
      this._questId = questId;
   }

   public void setHtml(Player player, String text) {
      if (!text.contains("<html>")) {
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

   public void replace(String pattern, String value) {
      this._html = this._html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
   }

   public void replace(String pattern, long value) {
      this.replace(pattern, String.valueOf(value));
   }

   public void replace2(String pattern, String value) {
      this._html = this._html.replaceAll(pattern, value);
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._npcObjId);
      this.writeS(this._html);
      this.writeD(this._questId);
   }
}
