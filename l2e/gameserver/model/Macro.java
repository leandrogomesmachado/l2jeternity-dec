package l2e.gameserver.model;

import java.util.List;
import l2e.gameserver.model.actor.templates.MacroTemplate;
import l2e.gameserver.model.interfaces.IIdentifiable;

public class Macro implements IIdentifiable {
   public static final int CMD_TYPE_SKILL = 1;
   public static final int CMD_TYPE_ACTION = 3;
   public static final int CMD_TYPE_SHORTCUT = 4;
   private int _id;
   private final int _icon;
   private final String _name;
   private final String _descr;
   private final String _acronym;
   private final List<MacroTemplate> _commands;

   public Macro(int id, int icon, String name, String descr, String acronym, List<MacroTemplate> list) {
      this.setId(id);
      this._icon = icon;
      this._name = name;
      this._descr = descr;
      this._acronym = acronym;
      this._commands = list;
   }

   @Override
   public int getId() {
      return this._id;
   }

   public void setId(int _id) {
      this._id = _id;
   }

   public int getIcon() {
      return this._icon;
   }

   public String getName() {
      return this._name;
   }

   public String getDescr() {
      return this._descr;
   }

   public String getAcronym() {
      return this._acronym;
   }

   public List<MacroTemplate> getCommands() {
      return this._commands;
   }
}
