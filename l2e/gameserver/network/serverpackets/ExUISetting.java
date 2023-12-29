package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.model.UIKeysSettings;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ActionKeyTemplate;

public class ExUISetting extends GameServerPacket {
   private final UIKeysSettings _uiSettings;
   private int buffsize;
   private int categories;

   public ExUISetting(Player player) {
      this._uiSettings = player.getUISettings();
      this.calcSize();
   }

   private void calcSize() {
      int size = 16;
      int category = 0;
      int numKeyCt = this._uiSettings.getKeys().size();

      for(int i = 0; i < numKeyCt; ++i) {
         ++size;
         if (this._uiSettings.getCategories().containsKey(category)) {
            List<Integer> catElList1 = this._uiSettings.getCategories().get(category);
            size += catElList1.size();
         }

         ++category;
         ++size;
         if (this._uiSettings.getCategories().containsKey(category)) {
            List<Integer> catElList2 = this._uiSettings.getCategories().get(category);
            size += catElList2.size();
         }

         ++category;
         size += 4;
         if (this._uiSettings.getKeys().containsKey(i)) {
            List<ActionKeyTemplate> keyElList = this._uiSettings.getKeys().get(i);
            size += keyElList.size() * 20;
         }
      }

      this.buffsize = size;
      this.categories = category;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this.buffsize);
      this.writeD(this.categories);
      int category = 0;
      int numKeyCt = this._uiSettings.getKeys().size();
      this.writeD(numKeyCt);

      for(int i = 0; i < numKeyCt; ++i) {
         if (this._uiSettings.getCategories().containsKey(category)) {
            List<Integer> catElList1 = this._uiSettings.getCategories().get(category);
            this.writeC(catElList1.size());

            for(int cmd : catElList1) {
               this.writeC(cmd);
            }
         } else {
            this.writeC(0);
         }

         if (this._uiSettings.getCategories().containsKey(++category)) {
            List<Integer> catElList2 = this._uiSettings.getCategories().get(category);
            this.writeC(catElList2.size());

            for(int cmd : catElList2) {
               this.writeC(cmd);
            }
         } else {
            this.writeC(0);
         }

         ++category;
         if (this._uiSettings.getKeys().containsKey(i)) {
            List<ActionKeyTemplate> keyElList = this._uiSettings.getKeys().get(i);
            this.writeD(keyElList.size());

            for(ActionKeyTemplate akey : keyElList) {
               this.writeD(akey.getCommandId());
               this.writeD(akey.getKeyId());
               this.writeD(akey.getToogleKey1());
               this.writeD(akey.getToogleKey2());
               this.writeD(akey.getShowStatus());
            }
         } else {
            this.writeD(0);
         }
      }

      this.writeD(17);
      this.writeD(16);
   }
}
