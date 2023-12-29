package l2e.gameserver.model.actor.templates.reflection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class ReflectionWorld {
   private Reflection _reflection = null;
   private int _templateId = -1;
   private final List<Integer> _allowed = new CopyOnWriteArrayList<>();
   private final AtomicInteger _status = new AtomicInteger();
   private boolean _isLocked = false;
   private int _tag = -1;

   public List<Integer> getAllowed() {
      return this._allowed;
   }

   public void removeAllowed(int id) {
      this._allowed.remove(this._allowed.indexOf(id));
   }

   public void addAllowed(int id) {
      this._allowed.add(id);
   }

   public boolean isAllowed(int id) {
      return this._allowed.contains(id);
   }

   public void setReflection(Reflection reflection) {
      this._reflection = reflection;
   }

   public Reflection getReflection() {
      return this._reflection;
   }

   public int getReflectionId() {
      return this._reflection.getId();
   }

   public void setTemplateId(int templateId) {
      this._templateId = templateId;
   }

   public int getTemplateId() {
      return this._templateId;
   }

   public int getStatus() {
      return this._status.get();
   }

   public boolean isStatus(int status) {
      return this._status.get() == status;
   }

   public void setStatus(int status) {
      this._status.set(status);
   }

   public void incStatus() {
      this._status.incrementAndGet();
   }

   public void setTag(int tag) {
      this._tag = tag;
   }

   public int getTag() {
      return this._tag;
   }

   public void setIsLocked(boolean isLocked) {
      this._isLocked = isLocked;
   }

   public boolean isLocked() {
      return this._isLocked;
   }

   public void onDeath(Creature killer, Creature victim) {
      if (victim != null && victim.isPlayer() && this._reflection != null) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_EXPELLED_IN_S1);
         sm.addNumber(this._reflection.getEjectTime());
         victim.getActingPlayer().sendPacket(sm);
         this._reflection.addEjectDeadTask(victim.getActingPlayer());
      }
   }
}
