package l2e.gameserver.model.actor.templates.items;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.StatsSet;

public class Henna {
   private final int _dyeId;
   private final String _dyeName;
   private final int _dyeItemId;
   private final int _str;
   private final int _con;
   private final int _dex;
   private final int _int;
   private final int _men;
   private final int _wit;
   private final int _wear_fee;
   private final int _wear_count;
   private final int _cancel_fee;
   private final int _cancel_count;
   private final List<ClassId> _wear_class;
   private final List<Skill> _skillList;

   public Henna(StatsSet set) {
      this._dyeId = set.getInteger("dyeId");
      this._dyeName = set.getString("dyeName");
      this._dyeItemId = set.getInteger("dyeItemId");
      this._str = set.getInteger("str");
      this._con = set.getInteger("con");
      this._dex = set.getInteger("dex");
      this._int = set.getInteger("int");
      this._men = set.getInteger("men");
      this._wit = set.getInteger("wit");
      this._wear_fee = set.getInteger("wear_fee");
      this._wear_count = set.getInteger("wear_count");
      this._cancel_fee = set.getInteger("cancel_fee");
      this._cancel_count = set.getInteger("cancel_count");
      this._wear_class = new ArrayList<>();
      this._skillList = new ArrayList<>();
   }

   public int getDyeId() {
      return this._dyeId;
   }

   public String getDyeName() {
      return this._dyeName;
   }

   public int getDyeItemId() {
      return this._dyeItemId;
   }

   public int getStatSTR() {
      return this._str;
   }

   public int getStatCON() {
      return this._con;
   }

   public int getStatDEX() {
      return this._dex;
   }

   public int getStatINT() {
      return this._int;
   }

   public int getStatMEN() {
      return this._men;
   }

   public int getStatWIT() {
      return this._wit;
   }

   public int getWearFee() {
      return this._wear_fee;
   }

   public int getWearCount() {
      return this._wear_count;
   }

   public int getCancelFee() {
      return this._cancel_fee;
   }

   public int getCancelCount() {
      return this._cancel_count;
   }

   public List<ClassId> getAllowedWearClass() {
      return this._wear_class;
   }

   public boolean isAllowedClass(ClassId c) {
      return this._wear_class.contains(c);
   }

   public void setWearClassIds(List<ClassId> wearClassIds) {
      this._wear_class.addAll(wearClassIds);
   }

   public void setSkills(List<Skill> skillList) {
      this._skillList.addAll(skillList);
   }

   public List<Skill> getSkillList() {
      return this._skillList;
   }
}
