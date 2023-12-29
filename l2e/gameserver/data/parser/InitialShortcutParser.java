package l2e.gameserver.data.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.model.Macro;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.MacroTemplate;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.MacroType;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.ShortCutRegister;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class InitialShortcutParser extends DocumentParser {
   private final Map<ClassId, List<ShortCutTemplate>> _initialShortcutData = new HashMap<>();
   private final List<ShortCutTemplate> _initialGlobalShortcutList = new ArrayList<>();
   private final Map<Integer, Macro> _macroPresets = new HashMap<>();

   protected InitialShortcutParser() {
      this.load();
   }

   @Override
   public void load() {
      this._initialShortcutData.clear();
      this._initialGlobalShortcutList.clear();
      this._macroPresets.clear();
      this.parseDatapackFile("data/stats/chars/initialShortcuts.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._initialGlobalShortcutList.size() + " initial global shortcuts data.");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._initialShortcutData.size() + " initial shortcuts data.");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._macroPresets.size() + " macros presets.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      for(Node n = this.getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling()) {
         if ("list".equals(n.getNodeName())) {
            for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
               String var3 = d.getNodeName();
               switch(var3) {
                  case "shortcuts":
                     this.parseShortcuts(d);
                     break;
                  case "macros":
                     this.parseMacros(d);
               }
            }
         }
      }
   }

   private void parseShortcuts(Node d) {
      NamedNodeMap attrs = d.getAttributes();
      Node classIdNode = attrs.getNamedItem("classId");
      List<ShortCutTemplate> list = new ArrayList<>();

      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("page".equals(c.getNodeName())) {
            attrs = c.getAttributes();
            int pageId = parseInteger(attrs, "pageId");

            for(Node b = c.getFirstChild(); b != null; b = b.getNextSibling()) {
               if ("slot".equals(b.getNodeName())) {
                  list.add(this.createShortcut(pageId, b));
               }
            }
         }
      }

      if (classIdNode != null) {
         this._initialShortcutData.put(ClassId.getClassId(Integer.parseInt(classIdNode.getNodeValue())), list);
      } else {
         this._initialGlobalShortcutList.addAll(list);
      }
   }

   private void parseMacros(Node d) {
      for(Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
         if ("macro".equals(c.getNodeName())) {
            NamedNodeMap attrs = c.getAttributes();
            if (parseBoolean(attrs, "enabled", Boolean.valueOf(true))) {
               int macroId = parseInteger(attrs, "macroId");
               int icon = parseInteger(attrs, "icon");
               String name = parseString(attrs, "name");
               String description = parseString(attrs, "description");
               String acronym = parseString(attrs, "acronym");
               List<MacroTemplate> commands = new ArrayList<>(1);
               int entry = 0;

               for(Node b = c.getFirstChild(); b != null; b = b.getNextSibling()) {
                  if ("command".equals(b.getNodeName())) {
                     attrs = b.getAttributes();
                     MacroType type = parseEnum(attrs, MacroType.class, "type");
                     int d1 = 0;
                     int d2 = 0;
                     String cmd = b.getTextContent();
                     switch(type) {
                        case SKILL:
                           d1 = parseInteger(attrs, "skillId");
                           d2 = this.parseInteger(attrs, "skillLvl", Integer.valueOf(0));
                           break;
                        case ACTION:
                           d1 = parseInteger(attrs, "actionId");
                        case TEXT:
                        default:
                           break;
                        case SHORTCUT:
                           d1 = parseInteger(attrs, "page");
                           d2 = this.parseInteger(attrs, "slot", Integer.valueOf(0));
                           break;
                        case ITEM:
                           d1 = parseInteger(attrs, "itemId");
                           break;
                        case DELAY:
                           d1 = parseInteger(attrs, "delay");
                     }

                     commands.add(new MacroTemplate(entry++, type, d1, d2, cmd));
                  }
               }

               this._macroPresets.put(macroId, new Macro(macroId, icon, name, description, acronym, commands));
            }
         }
      }
   }

   private ShortCutTemplate createShortcut(int pageId, Node b) {
      NamedNodeMap attrs = b.getAttributes();
      int slotId = parseInteger(attrs, "slotId");
      ShortcutType shortcutType = parseEnum(attrs, ShortcutType.class, "shortcutType");
      int shortcutId = parseInteger(attrs, "shortcutId");
      int shortcutLevel = this.parseInteger(attrs, "shortcutLevel", Integer.valueOf(0));
      int characterType = this.parseInteger(attrs, "characterType", Integer.valueOf(0));
      return new ShortCutTemplate(slotId, pageId, shortcutType, shortcutId, shortcutLevel, characterType);
   }

   public List<ShortCutTemplate> getShortcutList(ClassId cId) {
      return this._initialShortcutData.get(cId);
   }

   public List<ShortCutTemplate> getShortcutList(int cId) {
      return this._initialShortcutData.get(ClassId.getClassId(cId));
   }

   public List<ShortCutTemplate> getGlobalMacroList() {
      return this._initialGlobalShortcutList;
   }

   public void registerAllShortcuts(Player player) {
      if (player != null) {
         for(ShortCutTemplate shortcut : this._initialGlobalShortcutList) {
            int shortcutId = shortcut.getId();
            switch(shortcut.getType()) {
               case ITEM:
                  ItemInstance item = player.getInventory().getItemByItemId(shortcutId);
                  if (item == null) {
                     continue;
                  }

                  shortcutId = item.getObjectId();
                  break;
               case SKILL:
                  if (!player.getSkills().containsKey(shortcutId)) {
                     continue;
                  }
                  break;
               case MACRO:
                  Macro macro = this._macroPresets.get(shortcutId);
                  if (macro == null) {
                     continue;
                  }

                  player.registerMacro(macro);
            }

            ShortCutTemplate newShortcut = new ShortCutTemplate(
               shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcutId, shortcut.getLevel(), shortcut.getCharacterType()
            );
            player.sendPacket(new ShortCutRegister(newShortcut));
            player.registerShortCut(newShortcut);
         }

         if (this._initialShortcutData.containsKey(player.getClassId())) {
            for(ShortCutTemplate shortcut : this._initialShortcutData.get(player.getClassId())) {
               int shortcutId = shortcut.getId();
               switch(shortcut.getType()) {
                  case ITEM:
                     ItemInstance item = player.getInventory().getItemByItemId(shortcutId);
                     if (item == null) {
                        continue;
                     }

                     shortcutId = item.getObjectId();
                     break;
                  case SKILL:
                     if (!player.getSkills().containsKey(shortcut.getId())) {
                        continue;
                     }
                     break;
                  case MACRO:
                     Macro macro = this._macroPresets.get(shortcutId);
                     if (macro == null) {
                        continue;
                     }

                     player.registerMacro(macro);
               }

               ShortCutTemplate newShortcut = new ShortCutTemplate(
                  shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcutId, shortcut.getLevel(), shortcut.getCharacterType()
               );
               player.sendPacket(new ShortCutRegister(newShortcut));
               player.registerShortCut(newShortcut);
            }
         }
      }
   }

   public static InitialShortcutParser getInstance() {
      return InitialShortcutParser.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final InitialShortcutParser _instance = new InitialShortcutParser();
   }
}
