package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ContactList {
   private final Logger _log = Logger.getLogger(this.getClass().getName());
   private final Player activeChar;
   private final List<String> _contacts = new CopyOnWriteArrayList<>();
   private final String QUERY_ADD = "INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)";
   private final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
   private final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";

   public ContactList(Player player) {
      this.activeChar = player;
      this.restore();
   }

   public void restore() {
      this._contacts.clear();

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT contactId FROM character_contacts WHERE charId = ?");
      ) {
         statement.setInt(1, this.activeChar.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               int contactId = rset.getInt(1);
               String contactName = CharNameHolder.getInstance().getNameById(contactId);
               if (contactName != null && !contactName.equals(this.activeChar.getName()) && contactId != this.activeChar.getObjectId()) {
                  this._contacts.add(contactName);
               }
            }
         }
      } catch (Exception var60) {
         this._log.log(Level.WARNING, "Error found in " + this.activeChar.getName() + "'s ContactsList: " + var60.getMessage(), (Throwable)var60);
      }
   }

   public boolean add(String name) {
      int contactId = CharNameHolder.getInstance().getIdByName(name);
      if (this._contacts.contains(name)) {
         this.activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
         return false;
      } else if (this.activeChar.getName().equals(name)) {
         this.activeChar.sendPacket(SystemMessageId.CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST);
         return false;
      } else if (this._contacts.size() >= 100) {
         this.activeChar.sendPacket(SystemMessageId.CONTACT_LIST_LIMIT_REACHED);
         return false;
      } else if (contactId < 1) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME);
         sm.addString(name);
         this.activeChar.sendPacket(sm);
         return false;
      } else {
         for(String contactName : this._contacts) {
            if (contactName.equalsIgnoreCase(name)) {
               this.activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
               return false;
            }
         }

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)");
         ) {
            statement.setInt(1, this.activeChar.getObjectId());
            statement.setInt(2, contactId);
            statement.execute();
            this._contacts.add(name);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST);
            sm.addString(name);
            this.activeChar.sendPacket(sm);
         } catch (Exception var36) {
            this._log.log(Level.WARNING, "Error found in " + this.activeChar.getName() + "'s ContactsList: " + var36.getMessage(), (Throwable)var36);
         }

         return true;
      }
   }

   public void remove(String name) {
      int contactId = CharNameHolder.getInstance().getIdByName(name);
      if (!this._contacts.contains(name)) {
         this.activeChar.sendPacket(SystemMessageId.NAME_NOT_REGISTERED_ON_CONTACT_LIST);
      } else if (contactId >= 1) {
         this._contacts.remove(name);

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_contacts WHERE charId = ? and contactId = ?");
         ) {
            statement.setInt(1, this.activeChar.getObjectId());
            statement.setInt(2, contactId);
            statement.execute();
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST);
            sm.addString(name);
            this.activeChar.sendPacket(sm);
         } catch (Exception var35) {
            this._log.log(Level.WARNING, "Error found in " + this.activeChar.getName() + "'s ContactsList: " + var35.getMessage(), (Throwable)var35);
         }
      }
   }

   public List<String> getAllContacts() {
      return this._contacts;
   }
}
