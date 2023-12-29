package l2e.gameserver.ai.character;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Vehicle;
import l2e.gameserver.model.skills.Skill;

public abstract class VehicleAI extends CharacterAI {
   public VehicleAI(Vehicle accessor) {
      super(accessor);
   }

   @Override
   protected void onIntentionAttack(Creature target) {
   }

   @Override
   protected void onIntentionCast(Skill skill, GameObject target) {
   }

   @Override
   protected void onIntentionFollow(Creature target) {
   }

   @Override
   protected void onIntentionPickUp(GameObject item) {
   }

   @Override
   protected void onIntentionInteract(GameObject object) {
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
   }

   @Override
   protected void onEvtAggression(Creature target, int aggro) {
   }

   @Override
   protected void onEvtStunned(Creature attacker) {
   }

   @Override
   protected void onEvtSleeping(Creature attacker) {
   }

   @Override
   protected void onEvtRooted(Creature attacker) {
   }

   @Override
   protected void onEvtForgetObject(GameObject object) {
   }

   @Override
   protected void onEvtCancel() {
   }

   @Override
   protected void onEvtDead(Creature killer) {
   }

   @Override
   protected void onEvtFakeDeath() {
   }

   @Override
   protected void onEvtFinishCasting() {
   }

   @Override
   protected void clientActionFailed() {
   }

   @Override
   protected void moveToPawn(GameObject pawn, int offset) {
   }

   @Override
   protected void clientStoppedMoving() {
   }
}
