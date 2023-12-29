package l2e.gameserver.model.actor.templates.daily;

import java.util.Map;

public class DailyTaskTemplate {
   private final int _id;
   private final String _type;
   private final String _sort;
   private final String _name;
   private final String _image;
   private final String _descr;
   private Map<Integer, Long> _rewards;
   private int _npcId;
   private int _npcCount;
   private int _questId;
   private int _reflectionId;
   private int _pvpCount;
   private int _pkCount;
   private int _olyMatchCount;
   private int _eventsCount;
   private boolean _isSiegeFort;
   private boolean _isSiegeCastle;

   public DailyTaskTemplate(int id, String type, String sort, String name, String descr, String image) {
      this._id = id;
      this._type = type;
      this._sort = sort;
      this._name = name;
      this._descr = descr;
      this._image = image;
   }

   public int getId() {
      return this._id;
   }

   public String getType() {
      return this._type;
   }

   public String getSort() {
      return this._sort;
   }

   public String getName() {
      return this._name;
   }

   public String getImage() {
      return this._image;
   }

   public String getDescr() {
      return this._descr;
   }

   public void setRewards(Map<Integer, Long> rewards) {
      this._rewards = rewards;
   }

   public Map<Integer, Long> getRewards() {
      return this._rewards;
   }

   public void setNpcId(int npcId) {
      this._npcId = npcId;
   }

   public int getNpcId() {
      return this._npcId;
   }

   public void setNpcCount(int count) {
      this._npcCount = count;
   }

   public int getNpcCount() {
      return this._npcCount;
   }

   public void setQuestId(int questId) {
      this._questId = questId;
   }

   public int getQuestId() {
      return this._questId;
   }

   public void setReflectionId(int reflectionId) {
      this._reflectionId = reflectionId;
   }

   public int getReflectionId() {
      return this._reflectionId;
   }

   public void setPvpCount(int pvpCount) {
      this._pvpCount = pvpCount;
   }

   public int getPvpCount() {
      return this._pvpCount;
   }

   public void setPkCount(int pkCount) {
      this._pkCount = pkCount;
   }

   public int getPkCount() {
      return this._pkCount;
   }

   public void setOlyMatchCount(int count) {
      this._olyMatchCount = count;
   }

   public int getOlyMatchCount() {
      return this._olyMatchCount;
   }

   public void setEventsCount(int count) {
      this._eventsCount = count;
   }

   public int getEventsCount() {
      return this._eventsCount;
   }

   public void setSiegeFort(boolean fort) {
      this._isSiegeFort = fort;
   }

   public boolean getSiegeFort() {
      return this._isSiegeFort;
   }

   public void setSiegeCastle(boolean castle) {
      this._isSiegeCastle = castle;
   }

   public boolean getSiegeCastle() {
      return this._isSiegeCastle;
   }
}
