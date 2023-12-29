package l2e.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.commons.util.Broadcast;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.data.DocumentParser;
import l2e.gameserver.instancemanager.tasks.StartMovingTask;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.WalkInfo;
import l2e.gameserver.model.WalkRoute;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.instance.MinionInstance;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.tasks.npc.walker.ArrivedTask;
import l2e.gameserver.model.actor.templates.NpcWalkerTemplate;
import l2e.gameserver.model.holders.NpcRoutesHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class WalkingManager extends DocumentParser {
   public static final byte REPEAT_GO_BACK = 0;
   public static final byte REPEAT_GO_FIRST = 1;
   public static final byte REPEAT_TELE_FIRST = 2;
   public static final byte REPEAT_RANDOM = 3;
   private final Map<String, WalkRoute> _routes = new HashMap<>();
   private final Map<Integer, WalkInfo> _activeRoutes = new HashMap<>();
   private final Map<Integer, NpcRoutesHolder> _routesToAttach = new HashMap<>();

   protected WalkingManager() {
      this.load();
   }

   @Override
   public final void load() {
      this.parseDatapackFile("data/stats/npcs/routes.xml");
      this._log.info(this.getClass().getSimpleName() + ": Loaded " + this._routes.size() + " walking routes.");
   }

   @Override
   protected void reloadDocument() {
   }

   @Override
   protected void parseDocument() {
      Node n = this.getCurrentDocument().getFirstChild();

      for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
         if (d.getNodeName().equals("route")) {
            String routeName = parseString(d.getAttributes(), "name");
            boolean repeat = parseBoolean(d.getAttributes(), "repeat");
            String repeatStyle = d.getAttributes().getNamedItem("repeatStyle").getNodeValue();
            byte repeatType;
            if (repeatStyle.equalsIgnoreCase("back")) {
               repeatType = 0;
            } else if (repeatStyle.equalsIgnoreCase("cycle")) {
               repeatType = 1;
            } else if (repeatStyle.equalsIgnoreCase("conveyor")) {
               repeatType = 2;
            } else if (repeatStyle.equalsIgnoreCase("random")) {
               repeatType = 3;
            } else {
               repeatType = -1;
            }

            List<NpcWalkerTemplate> list = new ArrayList<>();

            for(Node r = d.getFirstChild(); r != null; r = r.getNextSibling()) {
               if (r.getNodeName().equals("point")) {
                  NamedNodeMap attrs = r.getAttributes();
                  int x = parseInt(attrs, "X");
                  int y = parseInt(attrs, "Y");
                  int z = parseInt(attrs, "Z");
                  int delay = parseInt(attrs, "delay");
                  String chatString = null;
                  NpcStringId npcString = null;
                  Node node = attrs.getNamedItem("string");
                  if (node != null) {
                     chatString = node.getNodeValue();
                  } else {
                     node = attrs.getNamedItem("npcString");
                     if (node != null) {
                        npcString = NpcStringId.getNpcStringId(node.getNodeValue());
                        if (npcString == null) {
                           this._log.warning(this.getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
                           continue;
                        }
                     } else {
                        node = attrs.getNamedItem("npcStringId");
                        if (node != null) {
                           npcString = NpcStringId.getNpcStringId(Integer.parseInt(node.getNodeValue()));
                           if (npcString == null) {
                              this._log.warning(this.getClass().getSimpleName() + ": Unknown npcstring '" + node.getNodeValue() + ".");
                              continue;
                           }
                        }
                     }
                  }

                  list.add(new NpcWalkerTemplate(0, npcString, chatString, x, y, z, delay, parseBoolean(attrs, "run")));
               } else if (r.getNodeName().equals("target")) {
                  NamedNodeMap attrs = r.getAttributes();

                  try {
                     int npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                     int x = 0;
                     int y = 0;
                     int z = 0;
                     x = Integer.parseInt(attrs.getNamedItem("spawnX").getNodeValue());
                     y = Integer.parseInt(attrs.getNamedItem("spawnY").getNodeValue());
                     z = Integer.parseInt(attrs.getNamedItem("spawnZ").getNodeValue());
                     NpcRoutesHolder holder = this._routesToAttach.containsKey(npcId) ? this._routesToAttach.get(npcId) : new NpcRoutesHolder();
                     holder.addRoute(routeName, new Location(x, y, z));
                     this._routesToAttach.put(npcId, holder);
                  } catch (Exception var17) {
                     this._log.warning("Walking Manager: Error in target definition for route : " + routeName);
                  }
               }
            }

            this._routes.put(routeName, new WalkRoute(routeName, list, repeat, false, repeatType));
         }
      }
   }

   public boolean isOnWalk(Npc npc) {
      MonsterInstance monster = null;
      if (npc.isMonster()) {
         if (((MonsterInstance)npc).getLeader() == null) {
            monster = (MonsterInstance)npc;
         } else {
            monster = ((MinionInstance)npc).getLeader();
         }
      }

      if ((monster == null || this.isRegistered(monster)) && this.isRegistered(npc)) {
         WalkInfo walk = monster != null ? this._activeRoutes.get(monster.getObjectId()) : this._activeRoutes.get(npc.getObjectId());
         return !walk.isStoppedByAttack() && !walk.isSuspended();
      } else {
         return false;
      }
   }

   public WalkRoute getRoute(String route) {
      return this._routes.get(route);
   }

   public boolean isRegistered(Npc npc) {
      return this._activeRoutes.containsKey(npc.getObjectId());
   }

   public String getRouteName(Npc npc) {
      return this._activeRoutes.containsKey(npc.getObjectId()) ? this._activeRoutes.get(npc.getObjectId()).getRoute().getName() : "";
   }

   public void startMoving(Npc npc, String routeName) {
      if (this._routes.containsKey(routeName) && npc != null && !npc.isDead()) {
         if (!this._activeRoutes.containsKey(npc.getObjectId())) {
            if (npc.getAI().getIntention() != CtrlIntention.ACTIVE && npc.getAI().getIntention() != CtrlIntention.IDLE) {
               ThreadPoolManager.getInstance().schedule(new StartMovingTask(npc, routeName), 60000L);
            } else {
               WalkInfo walk = new WalkInfo(routeName);
               if (npc.isDebug()) {
                  walk.setLastAction(System.currentTimeMillis());
               }

               NpcWalkerTemplate node = walk.getCurrentNode();
               if (Util.checkIfInRange(40, node.getMoveX(), node.getMoveY(), node.getMoveZ(), npc, false)) {
                  walk.calculateNextNode(npc);
                  node = walk.getCurrentNode();
               }

               npc.setIsRunning(node.getRunning());
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
               walk.setWalkCheckTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new StartMovingTask(npc, routeName), 60000L, 60000L));
               this._activeRoutes.put(npc.getObjectId(), walk);
            }
         } else if (this._activeRoutes.containsKey(npc.getObjectId())
            && (npc.getAI().getIntention() == CtrlIntention.ACTIVE || npc.getAI().getIntention() == CtrlIntention.IDLE)) {
            WalkInfo walk = this._activeRoutes.get(npc.getObjectId());
            if (walk == null) {
               return;
            }

            if (walk.isBlocked() || walk.isSuspended()) {
               return;
            }

            walk.setBlocked(true);
            NpcWalkerTemplate node = walk.getCurrentNode();
            npc.setIsRunning(node.getRunning());
            npc.getAI().setIntention(CtrlIntention.MOVING, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
            walk.setBlocked(false);
            walk.setStoppedByAttack(false);
         }
      }
   }

   public synchronized void cancelMoving(Npc npc) {
      if (this._activeRoutes.containsKey(npc.getObjectId())) {
         WalkInfo walk = this._activeRoutes.remove(npc.getObjectId());
         walk.getWalkCheckTask().cancel(true);
      }
   }

   public void resumeMoving(Npc npc) {
      if (this._activeRoutes.containsKey(npc.getObjectId())) {
         WalkInfo walk = this._activeRoutes.get(npc.getObjectId());
         walk.setSuspended(false);
         walk.setStoppedByAttack(false);
         this.startMoving(npc, walk.getRoute().getName());
      }
   }

   public void stopMoving(Npc npc, boolean suspend, boolean stoppedByAttack) {
      MonsterInstance monster = null;
      if (npc.isMonster()) {
         if (((MonsterInstance)npc).getLeader() == null) {
            monster = (MonsterInstance)npc;
         } else {
            monster = ((MinionInstance)npc).getLeader();
         }
      }

      if ((monster == null || this.isRegistered(monster)) && this.isRegistered(npc)) {
         WalkInfo walk = monster != null ? this._activeRoutes.get(monster.getObjectId()) : this._activeRoutes.get(npc.getObjectId());
         walk.setSuspended(suspend);
         walk.setStoppedByAttack(stoppedByAttack);
         if (monster != null) {
            monster.stopMove(null);
            monster.getAI().setIntention(CtrlIntention.ACTIVE);
         } else {
            npc.stopMove(null);
            npc.getAI().setIntention(CtrlIntention.ACTIVE);
         }
      }
   }

   public void onArrived(Npc npc) {
      if (this._activeRoutes.containsKey(npc.getObjectId())) {
         if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_NODE_ARRIVED) != null) {
            for(Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_NODE_ARRIVED)) {
               quest.notifyNodeArrived(npc);
            }
         }

         WalkInfo walk = this._activeRoutes.get(npc.getObjectId());
         if (walk.getCurrentNodeId() >= 0 && walk.getCurrentNodeId() < walk.getRoute().getNodesCount()) {
            NpcWalkerTemplate node = walk.getRoute().getNodeList().get(walk.getCurrentNodeId());
            if (npc.isInsideRadius(node.getMoveX(), node.getMoveY(), 10, false)) {
               walk.calculateNextNode(npc);
               int delay = node.getDelay();
               walk.setBlocked(true);
               if (node.getNpcString() != null) {
                  Broadcast.toKnownPlayers(npc, new NpcSay(npc, 22, node.getNpcString()));
               } else {
                  String text = node.getChatText();
                  if (text != null && !text.isEmpty()) {
                     Broadcast.toKnownPlayers(npc, new NpcSay(npc, 22, text));
                  }
               }

               if (npc.isDebug()) {
                  walk.setLastAction(System.currentTimeMillis());
               }

               ThreadPoolManager.getInstance().schedule(new ArrivedTask(npc, walk), 100L + (long)delay * 1000L);
            }
         }
      }
   }

   public void onDeath(Npc npc) {
      this.cancelMoving(npc);
   }

   public void onSpawn(Npc npc) {
      if (this._routesToAttach.containsKey(npc.getId())) {
         String routeName = this._routesToAttach.get(npc.getId()).getRouteName(npc);
         if (!routeName.isEmpty()) {
            this.startMoving(npc, routeName);
         }
      }
   }

   public static final WalkingManager getInstance() {
      return WalkingManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final WalkingManager _instance = new WalkingManager();
   }
}
