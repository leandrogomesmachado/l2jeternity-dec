package l2e.gameserver.model.actor.templates.player.vip;

public class VipNpcTemplate {
   private final int _npcId;
   private final long _points;

   public VipNpcTemplate(int id, long points) {
      this._npcId = id;
      this._points = points;
   }

   public int getId() {
      return this._npcId;
   }

   public long getPoints() {
      return this._points;
   }
}
