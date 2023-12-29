package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.util.StringUtil;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.MacroTemplate;
import l2e.gameserver.model.actor.templates.ShortCutTemplate;
import l2e.gameserver.model.base.MacroType;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.interfaces.IRestorable;
import l2e.gameserver.network.serverpackets.MacrosList;

public class MacroList implements IRestorable {
   private static final Logger _log = Logger.getLogger(MacroList.class.getName());
   private final Player _owner;
   private int _revision;
   private int _macroId;
   private final Map<Integer, Macro> _macroses = Collections.synchronizedMap(new LinkedHashMap<>());

   public MacroList(Player owner) {
      this._owner = owner;
      this._revision = 1;
      this._macroId = 1000;
   }

   public int getRevision() {
      return this._revision;
   }

   public Map<Integer, Macro> getAllMacroses() {
      return this._macroses;
   }

   public void registerMacro(Macro macro) {
      if (macro.getId() == 0) {
         macro.setId(this._macroId++);

         while(this._macroses.containsKey(macro.getId())) {
            macro.setId(this._macroId++);
         }

         this._macroses.put(macro.getId(), macro);
         this.registerMacroInDb(macro);
      } else {
         Macro old = this._macroses.put(macro.getId(), macro);
         if (old != null) {
            this.deleteMacroFromDb(old);
         }

         this.registerMacroInDb(macro);
      }

      this.sendUpdate();
   }

   public void deleteMacro(int id) {
      Macro removed = this._macroses.remove(id);
      if (removed != null) {
         this.deleteMacroFromDb(removed);
      }

      ShortCutTemplate[] allShortCuts = this._owner.getAllShortCuts();

      for(ShortCutTemplate sc : allShortCuts) {
         if (sc.getId() == id && sc.getType() == ShortcutType.MACRO) {
            this._owner.deleteShortCut(sc.getSlot(), sc.getPage());
         }
      }

      this.sendUpdate();
   }

   public void sendUpdate() {
      ++this._revision;
      Collection<Macro> allMacros = this._macroses.values();
      synchronized(this._macroses) {
         if (allMacros.isEmpty()) {
            this._owner.sendPacket(new MacrosList(this._revision, 0, null));
         } else {
            for(Macro m : allMacros) {
               this._owner.sendPacket(new MacrosList(this._revision, allMacros.size(), m));
            }
         }
      }
   }

   private void registerMacroInDb(Macro macro) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("INSERT INTO character_macroses (charId,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
      ) {
         ps.setInt(1, this._owner.getObjectId());
         ps.setInt(2, macro.getId());
         ps.setInt(3, macro.getIcon());
         ps.setString(4, macro.getName());
         ps.setString(5, macro.getDescr());
         ps.setString(6, macro.getAcronym());
         StringBuilder sb = new StringBuilder(300);

         for(MacroTemplate cmd : macro.getCommands()) {
            StringUtil.append(sb, String.valueOf(cmd.getType().ordinal()), ",", String.valueOf(cmd.getD1()), ",", String.valueOf(cmd.getD2()));
            if (cmd.getCmd() != null && cmd.getCmd().length() > 0) {
               StringUtil.append(sb, ",", cmd.getCmd());
            }

            sb.append(';');
         }

         if (sb.length() > 255) {
            sb.setLength(255);
         }

         ps.setString(7, sb.toString());
         ps.execute();
      } catch (Exception var36) {
         _log.log(Level.WARNING, "could not store macro:", (Throwable)var36);
      }
   }

   private void deleteMacroFromDb(Macro macro) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=? AND id=?");
      ) {
         ps.setInt(1, this._owner.getObjectId());
         ps.setInt(2, macro.getId());
         ps.execute();
      } catch (Exception var34) {
         _log.log(Level.WARNING, "could not delete macro:", (Throwable)var34);
      }
   }

   @Override
   public boolean restoreMe() {
      this._macroses.clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT charId, id, icon, name, descr, acronym, commands FROM character_macroses WHERE charId=?");
      ) {
         ps.setInt(1, this._owner.getObjectId());

         try (ResultSet rset = ps.executeQuery()) {
            while(rset.next()) {
               int id = rset.getInt("id");
               int icon = rset.getInt("icon");
               String name = rset.getString("name");
               String descr = rset.getString("descr");
               String acronym = rset.getString("acronym");
               List<MacroTemplate> commands = new ArrayList<>();
               StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");

               while(st1.hasMoreTokens()) {
                  StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
                  if (st.countTokens() >= 3) {
                     MacroType type = MacroType.values()[Integer.parseInt(st.nextToken())];
                     int d1 = Integer.parseInt(st.nextToken());
                     int d2 = Integer.parseInt(st.nextToken());
                     String cmd = "";
                     if (st.hasMoreTokens()) {
                        cmd = st.nextToken();
                     }

                     commands.add(new MacroTemplate(commands.size(), type, d1, d2, cmd));
                  }
               }

               this._macroses.put(id, new Macro(id, icon, name, descr, acronym, commands));
            }
         }

         return true;
      } catch (Exception var70) {
         _log.log(Level.WARNING, "could not store shortcuts:", (Throwable)var70);
         return false;
      }
   }
}
