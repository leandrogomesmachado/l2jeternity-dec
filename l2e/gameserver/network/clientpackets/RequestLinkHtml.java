package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestLinkHtml extends GameClientPacket {
   private String _link;

   @Override
   protected void readImpl() {
      this._link = this.readS();
   }

   @Override
   public void runImpl() {
      Player actor = this.getClient().getActiveChar();
      if (actor != null) {
         if (!this._link.contains("..") && this._link.contains(".htm")) {
            try {
               String filename = "data/html/" + this._link;
               NpcHtmlMessage msg = new NpcHtmlMessage(0);
               msg.setFile(actor, actor.getLang(), filename);
               this.sendPacket(msg);
            } catch (Exception var4) {
               _log.log(Level.WARNING, "Bad RequestLinkHtml: ", (Throwable)var4);
            }
         } else {
            _log.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + this._link + "', skipped");
         }
      }
   }
}
