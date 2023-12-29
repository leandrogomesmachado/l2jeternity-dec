package l2e.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.UIParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ActionKeyTemplate;
import l2e.gameserver.network.GameClient;

public class RequestSaveKeyMapping extends GameClientPacket {
   private int _tabNum;
   private final Map<Integer, List<ActionKeyTemplate>> _keyMap = new HashMap<>();
   private final Map<Integer, List<Integer>> _catMap = new HashMap<>();

   @Override
   protected void readImpl() {
      int category = 0;
      this.readD();
      this.readD();
      this._tabNum = this.readD();

      for(int i = 0; i < this._tabNum; ++i) {
         int cmd1Size = this.readC();

         for(int j = 0; j < cmd1Size; ++j) {
            UIParser.addCategory(this._catMap, category, this.readC());
         }

         ++category;
         int cmd2Size = this.readC();

         for(int j = 0; j < cmd2Size; ++j) {
            UIParser.addCategory(this._catMap, category, this.readC());
         }

         ++category;
         int cmdSize = this.readD();

         for(int j = 0; j < cmdSize; ++j) {
            int cmd = this.readD();
            int key = this.readD();
            int tgKey1 = this.readD();
            int tgKey2 = this.readD();
            int show = this.readD();
            UIParser.addKey(this._keyMap, i, new ActionKeyTemplate(i, cmd, key, tgKey1, tgKey2, show));
         }
      }

      this.readD();
      this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getActiveChar();
      if (Config.STORE_UI_SETTINGS && player != null && this.getClient().getState() == GameClient.GameClientState.IN_GAME) {
         player.getUISettings().storeAll(this._catMap, this._keyMap);
      }
   }
}
