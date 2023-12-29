package l2e.gameserver.model.actor.templates.player.ranking;

public class PartyTemplate {
   private int _kills;
   private int _deaths;

   public void addKills() {
      ++this._kills;
   }

   public int getKills() {
      return this._kills;
   }

   public void addDeaths() {
      ++this._deaths;
   }

   public long getDeaths() {
      return (long)this._deaths;
   }
}
