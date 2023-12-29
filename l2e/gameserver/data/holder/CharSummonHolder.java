package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.actor.instance.ServitorInstance;
import l2e.gameserver.model.actor.instance.SiegeSummonInstance;
import l2e.gameserver.model.actor.templates.npc.NpcTemplate;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.l2skills.SkillSummon;
import l2e.gameserver.network.serverpackets.PetItemList;

public class CharSummonHolder {
   private static final Logger _log = Logger.getLogger(CharSummonHolder.class.getName());
   private static final Map<Integer, Integer> _pets = new ConcurrentHashMap<>();
   private static final Map<Integer, Integer> _servitors = new ConcurrentHashMap<>();
   private static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
   private static final String INIT_SUMMONS = "SELECT ownerId, summonSkillId FROM character_summons";
   private static final String LOAD_SUMMON = "SELECT curHp, curMp, time FROM character_summons WHERE ownerId = ? AND summonSkillId = ?";
   private static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ?";
   private static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?)";

   public Map<Integer, Integer> getPets() {
      return _pets;
   }

   public Map<Integer, Integer> getServitors() {
      return _servitors;
   }

   public void init() {
      if (Config.RESTORE_SERVITOR_ON_RECONNECT) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT ownerId, summonSkillId FROM character_summons");
         ) {
            while(rs.next()) {
               _servitors.put(rs.getInt("ownerId"), rs.getInt("summonSkillId"));
            }
         } catch (Exception var180) {
            _log.warning(this.getClass().getSimpleName() + ": Error while loading saved summons");
         }
      }

      if (Config.RESTORE_PET_ON_RECONNECT) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery("SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'");
         ) {
            while(rs.next()) {
               _pets.put(rs.getInt("ownerId"), rs.getInt("item_obj_id"));
            }
         } catch (Exception var173) {
            _log.warning(this.getClass().getSimpleName() + ": Error while loading saved summons");
         }
      }
   }

   public void removeServitor(Player activeChar) {
      _servitors.remove(activeChar.getObjectId());

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("DELETE FROM character_summons WHERE ownerId = ?");
      ) {
         ps.setInt(1, activeChar.getObjectId());
         ps.execute();
      } catch (SQLException var34) {
         _log.warning(this.getClass().getSimpleName() + ": Summon cannot be removed: " + var34);
      }
   }

   public void restorePet(Player activeChar) {
      ItemInstance item = activeChar.getInventory().getItemByObjectId(_pets.get(activeChar.getObjectId()));
      if (item == null) {
         _log.warning(this.getClass().getSimpleName() + ": Null pet summoning item for: " + activeChar);
      } else {
         PetData petData = PetsParser.getInstance().getPetDataByItemId(item.getId());
         if (petData == null) {
            _log.warning(this.getClass().getSimpleName() + ": Null pet data for: " + activeChar + " and summoning item: " + item);
         } else {
            NpcTemplate npcTemplate = NpcsParser.getInstance().getTemplate(petData.getNpcId());
            if (npcTemplate == null) {
               _log.warning(this.getClass().getSimpleName() + ": Null pet NPC template for: " + activeChar + " and pet Id:" + petData.getNpcId());
            } else {
               PetInstance pet = PetInstance.spawnPet(npcTemplate, activeChar, item);
               if (pet == null) {
                  _log.warning(this.getClass().getSimpleName() + ": Null pet instance for: " + activeChar + " and pet NPC template:" + npcTemplate);
               } else {
                  pet.setShowSummonAnimation(true);
                  pet.setTitle(activeChar.getName());
                  if (!pet.isRespawned()) {
                     pet.setCurrentHp(pet.getMaxHp());
                     pet.setCurrentMp(pet.getMaxMp());
                     pet.getStat().setExp(pet.getExpForThisLevel());
                     pet.setCurrentFed(pet.getMaxFed());
                  }

                  pet.setRunning();
                  if (!pet.isRespawned()) {
                     pet.store();
                  }

                  activeChar.setPet(pet);
                  pet.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
                  pet.startFeed();
                  item.setEnchantLevel(pet.getLevel());
                  if (pet.getCurrentFed() <= 0) {
                     pet.unSummon(activeChar);
                  } else {
                     pet.startFeed();
                  }

                  pet.setFollowStatus(true);
                  pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
                  pet.broadcastStatusUpdate();
               }
            }
         }
      }
   }

   public void restoreServitor(Player activeChar) {
      int skillId = _servitors.get(activeChar.getObjectId());

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement("SELECT curHp, curMp, time FROM character_summons WHERE ownerId = ? AND summonSkillId = ?");
      ) {
         ps.setInt(1, activeChar.getObjectId());
         ps.setInt(2, skillId);

         try (ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
               int curHp = rs.getInt("curHp");
               int curMp = rs.getInt("curMp");
               int time = rs.getInt("time");
               SkillSummon skill = (SkillSummon)SkillsParser.getInstance().getInfo(skillId, activeChar.getSkillLevel(skillId));
               if (skill == null) {
                  this.removeServitor(activeChar);
                  return;
               }

               NpcTemplate summonTemplate = NpcsParser.getInstance().getTemplate(skill.getNpcId());
               if (summonTemplate == null) {
                  _log.warning(this.getClass().getSimpleName() + ": Summon attemp for nonexisting Skill ID:" + skillId);
                  return;
               }

               int id = IdFactory.getInstance().getNextId();
               ServitorInstance summon;
               if (summonTemplate.isType("SiegeSummon")) {
                  summon = new SiegeSummonInstance(id, summonTemplate, activeChar, skill);
               } else if (summonTemplate.isType("MerchantSummon")) {
                  summon = new ServitorInstance(id, summonTemplate, activeChar, skill);
               } else {
                  summon = new ServitorInstance(id, summonTemplate, activeChar, skill);
               }

               summon.setName(summonTemplate.getName());
               summon.setNameRu(summonTemplate.getNameRu());
               summon.setTitle(activeChar.getName());
               summon.setExpPenalty(skill.getExpPenalty());
               summon.setSharedElementals(skill.getInheritElementals());
               summon.setSharedElementalsValue(skill.getElementalSharePercent());
               if (summon.getLevel() >= ExperienceParser.getInstance().getMaxPetLevel()) {
                  summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(ExperienceParser.getInstance().getMaxPetLevel() - 1));
                  _log.warning(
                     this.getClass().getSimpleName()
                        + ": Summon ("
                        + summon.getName()
                        + ") NpcID: "
                        + summon.getId()
                        + " has a level above "
                        + ExperienceParser.getInstance().getMaxPetLevel()
                        + ". Please rectify."
                  );
               } else {
                  summon.getStat().setExp(ExperienceParser.getInstance().getExpForLevel(summon.getLevel() % ExperienceParser.getInstance().getMaxPetLevel()));
               }

               summon.setCurrentHp((double)curHp);
               summon.setCurrentMp((double)curMp);
               summon.setHeading(activeChar.getHeading());
               summon.setRunning();
               activeChar.setPet(summon);
               summon.setTimeRemaining(time);
               summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
            }
         }
      } catch (SQLException var79) {
         _log.warning(this.getClass().getSimpleName() + ": Servitor cannot be restored: " + var79);
      }
   }

   public void saveSummon(ServitorInstance summon) {
      if (summon != null && summon.getTimeRemaining() > 0) {
         _servitors.put(summon.getOwner().getObjectId(), summon.getReferenceSkill());

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("REPLACE INTO character_summons (ownerId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?)");
         ) {
            ps.setInt(1, summon.getOwner().getObjectId());
            ps.setInt(2, summon.getReferenceSkill());
            ps.setInt(3, (int)Math.round(summon.getCurrentHp()));
            ps.setInt(4, (int)Math.round(summon.getCurrentMp()));
            ps.setInt(5, summon.getTimeRemaining());
            ps.execute();
         } catch (Exception var34) {
            _log.warning(this.getClass().getSimpleName() + ": Failed to store summon: " + summon + " from " + summon.getOwner() + ", error: " + var34);
         }
      }
   }

   public static CharSummonHolder getInstance() {
      return CharSummonHolder.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CharSummonHolder _instance = new CharSummonHolder();
   }
}
