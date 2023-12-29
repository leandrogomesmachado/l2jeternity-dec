package l2e.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.model.Macro;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.MacroTemplate;
import l2e.gameserver.model.base.MacroType;
import l2e.gameserver.network.SystemMessageId;

public final class RequestMakeMacro extends GameClientPacket {
   private Macro _macro;
   private int _commandsLenght = 0;
   private static final int MAX_MACRO_LENGTH = 12;

   @Override
   protected void readImpl() {
      int _id = this.readD();
      String _name = this.readS();
      String _desc = this.readS();
      String _acronym = this.readS();
      int _icon = this.readC();
      int _count = this.readC();
      if (_count > 12) {
         _count = 12;
      }

      if (Config.DEBUG) {
         _log.info("Make macro id:" + _id + "\tname:" + _name + "\tdesc:" + _desc + "\tacronym:" + _acronym + "\ticon:" + _icon + "\tcount:" + _count);
      }

      List<MacroTemplate> commands = new ArrayList<>(_count);

      for(int i = 0; i < _count; ++i) {
         int entry = this.readC();
         int type = this.readC();
         int d1 = this.readD();
         int d2 = this.readC();
         String command = this.readS().replace(";", "").replace(",", "");
         this._commandsLenght += command.length();
         commands.add(new MacroTemplate(entry, MacroType.values()[type >= 1 && type <= 6 ? type : 0], d1, d2, command));
         if (Config.DEBUG) {
            _log.info("entry:" + entry + "\ttype:" + type + "\td1:" + d1 + "\td2:" + d2 + "\tcommand:" + command);
         }
      }

      this._macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._commandsLenght > 255) {
            player.sendPacket(SystemMessageId.INVALID_MACRO);
         } else if (player.getMacros().getAllMacroses().size() > 48) {
            player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_48_MACROS);
         } else if (this._macro.getName().isEmpty()) {
            player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME);
         } else if (this._macro.getDescr().length() > 32) {
            player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS);
         } else {
            player.registerMacro(this._macro);
         }
      }
   }
}
