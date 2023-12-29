package l2e.gameserver.model.actor.listener;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.handler.communityhandlers.impl.CommunityGeneral;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class LevelAnswerListener implements OnAnswerListener {
   private final Player _player;
   private final int _level;

   public LevelAnswerListener(Player player, int level) {
      this._player = player;
      this._level = level;
   }

   @Override
   public void sayYes() {
      if (this._player != null && this._player.isOnline()) {
         if (!CommunityGeneral.correct(this._level, this._player.getBaseClass() == this._player.getClassId().getId())) {
            this._player.sendMessage(new ServerMessage("ServiceBBS.INCORRECT_LVL", this._player.getLang()).toString());
         } else {
            boolean delevel = this._level < this._player.getLevel();
            if (delevel && !Config.SERVICES_DELEVEL_ENABLE) {
               this._player.sendMessage(new ServerMessage("ServiceBBS.CANT_DELEVEL", this._player.getLang()).toString());
            } else if (!delevel && !Config.SERVICES_LEVELUP_ENABLE) {
               this._player.sendMessage(new ServerMessage("ServiceBBS.CANT_LVLUP", this._player.getLang()).toString());
            } else {
               int item = delevel ? Config.SERVICES_DELEVEL_ITEM[0] : Config.SERVICES_LEVELUP_ITEM[0];
               long count = delevel
                  ? (long)((this._player.getLevel() - this._level) * Config.SERVICES_DELEVEL_ITEM[1])
                  : (long)((this._level - this._player.getLevel()) * Config.SERVICES_LEVELUP_ITEM[1]);
               if (this._player.getInventory().getItemByItemId(item) == null) {
                  this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               } else if (this._player.getInventory().getItemByItemId(item).getCount() < count) {
                  this._player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
               } else if (!delevel && !this._player.getExpOn()) {
                  this._player.sendMessage(new ServerMessage("CommunityGeneral.CANT_ADD_LVL", this._player.getLang()).toString());
               } else {
                  this._player.destroyItemByItemId("Lvlcalc", item, count, this._player, true);
                  long pXp = this._player.getExp();
                  long tXp = ExperienceParser.getInstance().getExpForLevel(this._level);
                  if (delevel) {
                     this._player
                        .getStat()
                        .removeExpAndSp(
                           this._player.getExp()
                              - ExperienceParser.getInstance().getExpForLevel(this._player.getStat().getLevel() - (this._player.getLevel() - this._level)),
                           0
                        );
                  } else {
                     this._player.addExpAndSp(tXp - pXp, 0);
                  }

                  ServerMessage msg = new ServerMessage("ServiceBBS.LVL_CHANGE", this._player.getLang());
                  msg.add(this._level);
                  this._player.sendMessage(msg.toString());
               }
            }
         }
      }
   }

   @Override
   public void sayNo() {
   }
}
