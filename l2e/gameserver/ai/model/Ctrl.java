package l2e.gameserver.ai.model;

import l2e.gameserver.model.actor.Creature;

public interface Ctrl {
   Creature getActor();

   CtrlIntention getIntention();

   Creature getAttackTarget();

   void setIntention(CtrlIntention var1);

   void setIntention(CtrlIntention var1, Object var2);

   void setIntention(CtrlIntention var1, Object var2, Object var3);

   void notifyEvent(CtrlEvent var1);

   void notifyEvent(CtrlEvent var1, Object var2);

   void notifyEvent(CtrlEvent var1, Object var2, Object var3);
}
