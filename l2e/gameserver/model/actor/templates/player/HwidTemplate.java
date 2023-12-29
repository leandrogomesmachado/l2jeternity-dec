package l2e.gameserver.model.actor.templates.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.actor.Player;

public class HwidTemplate {
   private int _amount;
   private final Map<String, List<Player>> _players = new HashMap<>();

   public HwidTemplate(int amount, Player player, String info) {
      this._amount = amount;
      List<Player> list = new ArrayList<>();
      list.add(player);
      this._players.put(info, list);
   }

   public int getAmount() {
      return this._amount;
   }

   public Map<String, List<Player>> getPlayers() {
      return this._players;
   }

   public void setAmount(boolean isAdd, Player player, String info) {
      if (isAdd) {
         ++this._amount;
         this._players.get(info).add(player);
      } else {
         for(List<Player> list : this._players.values()) {
            if (list != null && !list.isEmpty() && list.contains(player)) {
               --this._amount;
               list.remove(player);
            }
         }
      }
   }
}
