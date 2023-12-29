package l2e.gameserver.data.holder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.holders.ItemHolder;
import l2e.gameserver.model.holders.SkillHolder;

public class NpcBufferHolder {
   private static Logger _log = Logger.getLogger(NpcBufferHolder.class.getName());
   private final Map<Integer, NpcBufferHolder.NpcBufferSkills> _buffers = new HashMap<>();

   protected NpcBufferHolder() {
      int skillCount = 0;

      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement s = con.createStatement();
         ResultSet rset = s.executeQuery(
            "SELECT `npc_id`,`skill_id`,`skill_level`,`skill_fee_id`,`skill_fee_amount`,`buff_group` FROM `npc_buffer` ORDER BY `npc_id` ASC"
         );
      ) {
         int lastNpcId = 0;

         NpcBufferHolder.NpcBufferSkills skills;
         for(skills = null; rset.next(); ++skillCount) {
            int npcId = rset.getInt("npc_id");
            int skillId = rset.getInt("skill_id");
            int skillLevel = rset.getInt("skill_level");
            int skillFeeId = rset.getInt("skill_fee_id");
            int skillFeeAmount = rset.getInt("skill_fee_amount");
            int buffGroup = rset.getInt("buff_group");
            if (npcId != lastNpcId) {
               if (lastNpcId != 0) {
                  this._buffers.put(lastNpcId, skills);
               }

               skills = new NpcBufferHolder.NpcBufferSkills(npcId);
               skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount, buffGroup);
            } else if (skills != null) {
               skills.addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount, buffGroup);
            }

            lastNpcId = npcId;
         }

         if (lastNpcId != 0) {
            this._buffers.put(lastNpcId, skills);
         }
      } catch (SQLException var67) {
         _log.log(Level.SEVERE, this.getClass().getSimpleName() + ": Error reading npc_buffer table: " + var67.getMessage(), (Throwable)var67);
      }

      _log.info(this.getClass().getSimpleName() + ": Loaded " + this._buffers.size() + " buffers and " + skillCount + " skills.");
   }

   public NpcBufferHolder.NpcBufferData getSkillInfo(int npcId, int buffGroup) {
      if (this._buffers.containsKey(npcId)) {
         NpcBufferHolder.NpcBufferSkills skills = this._buffers.get(npcId);
         if (skills != null) {
            return skills.getSkillGroupInfo(buffGroup);
         }
      }

      return null;
   }

   public static NpcBufferHolder getInstance() {
      return NpcBufferHolder.SingletonHolder._instance;
   }

   public static class NpcBufferData {
      private final SkillHolder _skill;
      private final ItemHolder _fee;

      protected NpcBufferData(int skillId, int skillLevel, int feeId, int feeAmount) {
         this._skill = new SkillHolder(skillId, skillLevel);
         this._fee = new ItemHolder(feeId, (long)feeAmount);
      }

      public SkillHolder getSkill() {
         return this._skill;
      }

      public ItemHolder getFee() {
         return this._fee;
      }
   }

   protected static class NpcBufferSkills {
      private final int _npcId;
      private final Map<Integer, NpcBufferHolder.NpcBufferData> _skills = new HashMap<>();

      protected NpcBufferSkills(int npcId) {
         this._npcId = npcId;
      }

      public void addSkill(int skillId, int skillLevel, int skillFeeId, int skillFeeAmount, int buffGroup) {
         this._skills.put(buffGroup, new NpcBufferHolder.NpcBufferData(skillId, skillLevel, skillFeeId, skillFeeAmount));
      }

      public NpcBufferHolder.NpcBufferData getSkillGroupInfo(int buffGroup) {
         return this._skills.get(buffGroup);
      }

      public int getId() {
         return this._npcId;
      }
   }

   private static class SingletonHolder {
      protected static final NpcBufferHolder _instance = new NpcBufferHolder();
   }
}
