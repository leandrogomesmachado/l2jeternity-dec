package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.listener.talk.ChatListener;
import l2e.gameserver.model.actor.Player;

public class ChatEvent implements EventListener {
   private String _text;
   private Player _origin;
   private String _target;
   private ChatListener.ChatTargetType _targetType;

   public String getText() {
      return this._text;
   }

   public void setText(String text) {
      this._text = text;
   }

   public Player getOrigin() {
      return this._origin;
   }

   public void setOrigin(Player origin) {
      this._origin = origin;
   }

   public String getTarget() {
      return this._target;
   }

   public void setTarget(String target) {
      this._target = target;
   }

   public ChatListener.ChatTargetType getTargetType() {
      return this._targetType;
   }

   public void setTargetType(ChatListener.ChatTargetType targetType) {
      this._targetType = targetType;
   }
}
