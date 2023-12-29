package l2e.gameserver.model;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.clientpackets.GameClientPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class Request {
   private static final int REQUEST_TIMEOUT = 15;
   protected Player _player;
   protected Player _partner;
   protected boolean _isRequestor;
   protected boolean _isAnswerer;
   protected GameClientPacket _requestPacket;

   public Request(Player player) {
      this._player = player;
   }

   protected void clear() {
      this._partner = null;
      this._requestPacket = null;
      this._isRequestor = false;
      this._isAnswerer = false;
   }

   private synchronized void setPartner(Player partner) {
      this._partner = partner;
   }

   public Player getPartner() {
      return this._partner;
   }

   private synchronized void setRequestPacket(GameClientPacket packet) {
      this._requestPacket = packet;
   }

   public GameClientPacket getRequestPacket() {
      return this._requestPacket;
   }

   public synchronized boolean setRequest(Player partner, GameClientPacket packet) {
      if (partner == null) {
         this._player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
         return false;
      } else if (partner.getRequest().isProcessingRequest()) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
         sm.addString(partner.getName());
         this._player.sendPacket(sm);
         SystemMessage var4 = null;
         return false;
      } else if (this.isProcessingRequest()) {
         this._player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
         return false;
      } else {
         this._partner = partner;
         this._requestPacket = packet;
         this.setOnRequestTimer(true);
         this._partner.getRequest().setPartner(this._player);
         this._partner.getRequest().setRequestPacket(packet);
         this._partner.getRequest().setOnRequestTimer(false);
         return true;
      }
   }

   private void setOnRequestTimer(boolean isRequestor) {
      this._isRequestor = isRequestor;
      this._isAnswerer = !isRequestor;
      ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Request.this.clear();
         }
      }, 15000L);
   }

   public void onRequestResponse() {
      if (this._partner != null) {
         this._partner.getRequest().clear();
      }

      this.clear();
   }

   public boolean isProcessingRequest() {
      return this._partner != null;
   }
}
