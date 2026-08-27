package sample_team.module.complex;

// Action
import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.fire.ActionExtinguish;
import adf.core.agent.fire.ActionRefill;
import adf.core.agent.fire.ActionRescue;


/* Big 6 data */
import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;


/* Communication */
import adf.core.agent.communication.standard.bundle.information.MessageFireBrigade;
import adf.core.agent.communication.standard.bundle.information.MessageBuilding;
import adf.core.component.communication.CommunicationMessage;


/* Precompute Data */
import adf.core.agent.precompute.PrecomputeData;


/* ExtAction and PathPlanning */
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;


/* Containers */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/* Exception Handling */
import rescuecore2.config.NoSuchConfigOptionException;


/* Geometry */
import rescuecore2.misc.geometry.Point2D;


/* Area Entities */
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Hydrant;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Edge;

/* Human Entities */
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;

/* Base Types Entities */
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardEntityConstants;


import rescuecore2.worldmodel.EntityID;
import sample_team.module.complex.SampleExtActionClear;


public class SampleExtActionFireFighting extends ExtAction{

  // LOGGER
  private static final Logger LOGGER = Logger.getLogger(SampleExtActionClear.class.getName());

  // CONFIG
  private static final String CONFIG_PREFIX = "sample_team.module.complex.SampleExtActionFireFighting.";
  private static final String PATH_PLANNING_KEY = "SampleExtActionFireFighting.PathPlanning";
  private static final String DEFAULT_PATH_PLANNING = "adf.impl.module.algorithm.DijkstraPathPlanning";

  private EntityID target = null;


  private final PathPlanning pathPlanning;
  private MessageManager messageManager;

  private enum TaskKind {EXTINGUISH,RESCUE,EXTINGUISH_N_RESCUE,GET_CLOSE_TO_BUILDING,NONE}
  private TaskKind taskKind = TaskKind.NONE;

  public SampleExtActionFireFighting(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);

    int maxWater    = si.getFireTankMaximum();
    int refillRate  = si.getFireTankRefillRate();
    int hydrantRate = si.getFireTankRefillHydrantRate();
    int maxPerShot  = si.getFireExtinguishMaxSum(); // One cycle how much water spray is allowed
    int maxDistance = si.getFireExtinguishMaxDistance();

    this.pathPlanning = moduleManager.getModule(PATH_PLANNING_KEY, DEFAULT_PATH_PLANNING);

  }


  // ---------------------------------------------------------------- lifecycle

  @Override
  public ExtAction precompute(PrecomputeData precomputeData) {

    super.precompute(precomputeData);

    if (this.getCountPrecompute() >= 2) {
      return this;
    }

    this.pathPlanning.precompute(precomputeData);

    this.readKernelTime();

    return this;
  }


  @Override
  public ExtAction resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    this.readKernelTime();
    return this;
  }


  @Override
  public ExtAction preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    this.readKernelTime();
    return this;
  }


  @Override
  public ExtAction updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    // Stashed before the guard so calc() always has this cycle's manager.
    this.messageManager = messageManager;
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    this.readTeammateReports(messageManager);
    return this;
  }


  private void readKernelTime() {
    try {
      this.kernelTime = this.scenarioInfo.getKernelTimesteps();
    } catch (NoSuchConfigOptionException e) {
      LOGGER.warning("WARNING !!! NoSuchConfigurationException Caught");
      this.kernelTime = -1;
    }
  }


  @Override
  public ExtAction setTarget(EntityID target) {

    boolean haveHumanBuried = false;
    boolean haveBuildingOnFire = false;
    EntityID theHumanPosition = null;

    this.target = null;

    if (target == null) {

      this.taskKind = TaskKind.NONE;

      return this;
    }

    StandardEntity entity = this.worldInfo.getEntity(target);


    // GET human position entity
    // NOT this.target = position because not guarantee human inside building
    if (entity instanceof Human){

      Human theHuman = (Human) entity;

      if (theHuman.isPositionDefined()){

        theHumanPosition = theHuman.getPosition();
        entity = this.worldInfo.getEntity(theHumanPosition);
        haveHumanBuried = true;
      }

      // Human with no position ?
      // Because you dont know where is the Human , just ignore
      else entity = null;
    }

    // One line but checking 2 things
    // IF human defined above, check IF it's inside building
    // IF no human but pure building ONLY
    if (entity instanceof Building) {
      Building building = (Building) entity;

      // IF Fieryness not defined -> get target
      // IF Fieryness not defined meant not close enough so you can't see
      // Can't see -> Don't know fire or not -> GET close to it
      if(!building.isFierynessDefined()){

        this.target = building.getID();
        this.taskKind = TaskKind.GET_CLOSE_TO_BUILDING;
        return this;
      }

      else if(building.isOnFire()){

        this.target = building.getID();
        haveBuildingOnFire = true;
      }
    }

    if(haveHumanBuried && haveBuildingOnFire){
      this.taskKind = TaskKind.EXTINGUISH_N_RESCUE;
      return this;
    }

    else if(haveHumanBuried && !haveBuildingOnFire){
      this.target = theHumanPosition;
      this.taskKind = TaskKind.RESCUE;
      return this;
    }

    else if(!haveHumanBuried && haveBuildingOnFire){
      this.taskKind = TaskKind.EXTINGUISH;
      return this;
    }

    else {
      this.taskKind = TaskKind.NONE;
      return this;
    }

    // NOTE : STUCK Tracking should add here.
  }

  @Override
  public ExtAction calc(){
    this.result=null;

    if(!(this.agentInfo.me() instanceof FireBrigade)){
      return this;
    }
    
    FireBrigade fireBrigade = (FireBrigade) this.agentInfo.me();
    EntityID myPosition = fireBrigade.getPosition();

    if(this.needRest(fireBrigade)){
      Action rest = this.calcRest(fireBrigade);
      if (rest != null) {
        this.result = rest;
        this.report(fireBrigade, rest, null);
        return this;
      }
    }

    








    if(this.target == null){
      return this;
    }

    StandardEntity myPositionEntity = this.worldInfo.getEntity(myPosition);
    StandardEntity myTargetEntity = this.worldInfo.getEntity(this.target);

    if(!(myPositionEntity instanceof Area) || !(myTargetEntity instanceof Area)){
      return this;
    }

    List<EntityID> path = null;
    EntityID nextArea = null;
    if (!myPosition.equals(this.target)){
      path = this.pathPlanning.getResult(myPosition,this.target);
      nextArea = nextStep(path, myPosition);
    }


    


  }

  public EntityID nextStep(List<EntityID> path, EntityID position){
    if(path==null || path.isEmpty()){
      return null; // null because empty
    }

    int index = path.indexOf(position);

    if(index>=0){
      if(index+1 < path.size()){
        return path.get(index+1);
      }

      else {return null;} // null because at destination
    }

    return path.get(0); // Because current location not on path , so go to first element of path
  }

  // ------------------------------------------------------------------ resting

  private boolean needRest(Human agent) {
    if (!agent.isHPDefined() || !agent.isDamageDefined()) {
      return false;
    }
    int hp = agent.getHP();
    int damage = agent.getDamage();
    if (hp == 0 || damage == 0) {
      return false;
    }
    if (this.kernelTime == -1) {
      this.readKernelTime();
    }

    // Standing next to a fire means the damage rate is about to rise,
    // so break off earlier than the flat threshold would.
    int threshold = this.thresholdRest;
    if (agent.isPositionDefined() && this.isNearFire(agent.getPosition())) {
      threshold = Math.max(1, threshold / 2);
    }

    int survivableCycles = (hp / damage) + ((hp % damage) != 0 ? 1 : 0);
    return damage >= threshold
        || (survivableCycles + this.agentInfo.getTime()) < this.kernelTime;
  }


  /**
   * Picks a refuge that is not on fire, checks the planner can actually
   * reach it, and clears its way there rather than walking into rubble.
   */
  private Action calcRest(FireBrigade fireBrigade) {
    EntityID position = fireBrigade.getPosition();
    Collection<EntityID> refuges = this.worldInfo
        .getEntityIDsOfType(StandardEntityURN.REFUGE);
    if (refuges.isEmpty()) {
      return null;
    }

    List<EntityID> safe = new ArrayList<>();
    for (EntityID id : refuges) {
      StandardEntity entity = this.worldInfo.getEntity(id);
      if (entity instanceof Area && this.isBurning((Area) entity)) {
        continue;
      }
      safe.add(id);
    }
    if (safe.isEmpty()) {
      LOGGER.warning("every known refuge is burning; heading to the nearest "
          + "one anyway because standing in the street is worse");
      safe.addAll(refuges);
    }

    if (safe.contains(position)) {
      return new ActionRest();
    }

    this.pathPlanning.setFrom(position);
    this.pathPlanning.setDestination(safe);
    List<EntityID> path = this.pathPlanning.calc().getResult();
    if (path == null || path.isEmpty()) {
      // No route. Returning null lets calc() carry on with the firefighting job
      // instead of standing still and bleeding out.
      LOGGER.fine("no reachable refuge from " + position
          + "; continuing to clear");
      return null;
    }

    return new ActionMove(path);
  }

  // -------------------------------------------------------------- fire safety

  /** A burning area, whether it is a plain building or a refuge. */
  private boolean isBurning(Area area) {
    if (!(area instanceof Building)) {
      return false;
    }
    Building building = (Building) area;
    return building.isFierynessDefined()
        && Building.BURNING.contains(building.getFierynessEnum());
  }

  /** True if the area, or anything it opens onto, is alight. */
  private boolean isNearFire(EntityID areaID) {
    if (areaID == null) {
      return false;
    }
    StandardEntity entity = this.worldInfo.getEntity(areaID);
    if (!(entity instanceof Area)) {
      return false;
    }
    Area area = (Area) entity;
    if (this.isBurning(area)) {
      return true;
    }
    for (EntityID neighbour : area.getNeighbours()) {
      StandardEntity next = this.worldInfo.getEntity(neighbour);
      if (next instanceof Area && this.isBurning((Area) next)) {
        return true;
      }
    }
    return false;
  }


  // ------------------------------------------------------------ communication

  /** Say what we are doing, so allocators and the station can react. */
  private void report(FireBrigade fireBrigade, Action action, EntityID reportTarget) {
    if (this.messageManager == null) {
      return;
    }
    int kind;
    if (action instanceof ActionExtinguish) {
      kind = MessageFireBrigade.ActionExtinguish;
    } 
    
    else if (action instanceof ActionRefill){
      kind = MessageFireBrigade.ActionRefill;
    }

    else if (action instanceof ActionRescue){
      kind = MessageFireBrigade.ActionRescue;
    }

    else if (action instanceof ActionRest) {
      kind = MessageFireBrigade.ACTION_REST;
    } else {
      kind = MessageFireBrigade.ACTION_MOVE;
    }
    this.messageManager
        .addMessage(new MessageFireBrigade(true, fireBrigade, kind, reportTarget));
  }


}
