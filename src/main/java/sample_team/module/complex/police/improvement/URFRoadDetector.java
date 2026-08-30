package sample_team.module.complex.police.improvement;


import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.develop.DevelopData;


import adf.core.agent.communication.MessageManager;

import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.module.algorithm.PathPlanning;
import adf.core.component.module.complex.RoadDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;


public class URFRoadDetector extends RoadDetector{

    private PathPlanning pathPlanning;
    private EntityID result;

    private Map<EntityID, Collection<EntityID>> neighbourGraph;
    private Set<EntityID> allRoads;
    private Set<EntityID> refugeRoads;
    private int maxNumberOfNeighbours;
    private boolean initialised;

    private Set<EntityID> seenRoads;
    private Set<EntityID> blockedRoads;
    private Set<EntityID> clearedRoads;

    private static final int TARGET_PATIENCE = 30;
    private int targetChosenAt;

    public URFRoadDetector(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData){
        super(ai,wi,si,moduleManager,developData);

        this.pathPlanning = moduleManager.getModule(
            "URFRoadDetector.PathPlanning",
            "adf.impl.module.algorithm.DijkstraPathPlanning"
        );
        registerModule(this.pathPlanning);

        this.result= null;

        this.neighbourGraph = new HashMap<>();
        this.allRoads = new HashSet<>();
        this.refugeRoads = new HashSet<>();
        this.maxNumberOfNeighbours = 1;
        this.initialised = false;


        this.seenRoads = new HashSet<>();
        this.blockedRoads = new HashSet<>();
        this.clearedRoads = new HashSet<>();
    }

    /*
        SET allRoads 
        SET maxNumberOfNeighbours

    */
    private void buildStaticMap(){
        if (this.initialised) return;

        Collection<StandardEntity> allEntities = this.worldInfo.getAllEntities();

        // SET allRoads , Road extends Area extends StandardEntity
        for(StandardEntity entity : allEntities){
            if (!(entity instanceof Area)) {continue;}

            Area area = (Area) entity;
            List<EntityID> neighbours = area.getNeighbours();
            EntityID areaEntityID = area.getID();
            this.neighbourGraph.put(areaEntityID, new ArrayList<>(area.getNeighbours()));

            if(area instanceof Road){
                this.allRoads.add(areaEntityID);
                if(neighbours.size() > this.maxNumberOfNeighbours){
                    this.maxNumberOfNeighbours = neighbours.size();
                }
            }
        }

        Collection<EntityID> allRefugees = this.worldInfo.getEntityIDsOfType(StandardEntityURN.REFUGE);

        for(EntityID refugeID : allRefugees){
            Collection<EntityID> neighbours = this.neighbourGraph.get(refugeID);
            if(neighbours == null){continue;}

            for(EntityID neighbourID : neighbours){
                if(this.allRoads.contains(neighbourID)){
                    this.refugeRoads.add(neighbourID);
                }
            }
        }


        this.initialised = true;

        System.out.println(
            "URF MAP roads = " + this.allRoads.size() + "\n" +
            "refugeRoads = " + this.refugeRoads.size() + "\n" +
            "maxNumberOfNeighbours = " + this.maxNumberOfNeighbours
            );
    }

    @Override
    public EntityID getTarget() {
        return this.result;
    }

    @Override
    public RoadDetector calc() {
        this.buildStaticMap();
        EntityID position = this.agentInfo.getPosition();

        StandardEntity here = this.worldInfo.getEntity(position);
        if (here instanceof Road && this.isStillBlocked((Road) here)) {
            this.result = position;
            return this;
        }

        if (this.result != null) {
            boolean expired =
                (this.agentInfo.getTime() - this.targetChosenAt) > TARGET_PATIENCE;
            if (!this.isDone(this.result) && !expired) {
                return this;
            }
            if (expired) {
                this.blockedRoads.remove(this.result);
                this.seenRoads.add(this.result);
            }
            this.result = null;
        }

        Set<EntityID> visited = new HashSet<>();
        visited.add(position);
        this.result = this.sweep(Collections.singletonList(position), visited);

        if (this.result != null) {
            this.targetChosenAt = this.agentInfo.getTime();
        }

        System.out.println("URF_OBS t=" + this.agentInfo.getTime()
            + " pos=" + this.agentInfo.getPosition().getValue()
            + " target=" + (this.result == null ? "null" : this.result.getValue())
            + " seen=" + this.seenRoads.size()
            + " blocked=" + this.blockedRoads.size()
            + " cleared=" + this.clearedRoads.size());

        return this;
    }

    private void observeRoad(Road road) {
        EntityID id = road.getID();
        this.seenRoads.add(id);
        if (!road.isBlockadesDefined()) {
            return;
        }

        if (road.getBlockades().isEmpty()) {
            this.blockedRoads.remove(id);
            this.clearedRoads.add(id);
        }
        else {
            this.clearedRoads.remove(id);
            this.blockedRoads.add(id);
        }
    }

    @Override
    public RoadDetector updateInfo(MessageManager messageManager) {
        super.updateInfo(messageManager);
        if (this.getCountUpdateInfo() >= 2) {
            return this;
        }
        this.buildStaticMap();

        for (EntityID id : this.worldInfo.getChanged().getChangedEntities()) {
            StandardEntity entity = this.worldInfo.getEntity(id);
            if (entity instanceof Road) {
                this.observeRoad((Road) entity);
            }
            else if (entity instanceof Blockade) {
                Blockade blockade = (Blockade) entity;
                if (blockade.isPositionDefined()) {
                    EntityID roadID = blockade.getPosition();
                    this.seenRoads.add(roadID);
                    this.clearedRoads.remove(roadID);
                    this.blockedRoads.add(roadID);
                }
            }
        }

        StandardEntity here = this.worldInfo.getEntity(this.agentInfo.getPosition());
        if (here instanceof Area) {
            if (here instanceof Road) {
                    this.observeRoad((Road) here);
            }

            for (EntityID neighbourID : ((Area) here).getNeighbours()) {
                StandardEntity neighbour = this.worldInfo.getEntity(neighbourID);

                if (neighbour instanceof Road) {
                    this.observeRoad((Road) neighbour);
                }
            }
        }

        /*
        System.out.println(
            "URF_OBS t=" + this.agentInfo.getTime()
            + " seen=" + this.seenRoads.size()
            + " blocked=" + this.blockedRoads.size()
            + " cleared=" + this.clearedRoads.size());
        */
        return this;
    }


    private EntityID sweep(List<EntityID> ring, Set<EntityID> visited) {
        if (ring.isEmpty()) {
            return null;
        }

        List<EntityID> blockedHere = new ArrayList<>();
        List<EntityID> unknownHere = new ArrayList<>();
        List<EntityID> nextRing = new ArrayList<>();

        for (EntityID areaID : ring) {
            if (this.allRoads.contains(areaID)) {
                if (this.blockedRoads.contains(areaID)) {
                blockedHere.add(areaID);
                } 
                else if (!this.seenRoads.contains(areaID)) {
                    unknownHere.add(areaID);
                }
            }

            Collection<EntityID> neighbours = this.neighbourGraph.get(areaID);

            if (neighbours == null) {
                continue;
            }

            for (EntityID neighbourID : neighbours) {
                if (visited.add(neighbourID)) {
                    nextRing.add(neighbourID);
                }
            }
        }

        if (!blockedHere.isEmpty()) {
            return this.pickOne(blockedHere);
        }
        if (!unknownHere.isEmpty()) {
            return this.pickOne(unknownHere);
        }
        return this.sweep(nextRing, visited);
    }

    private EntityID pickOne(List<EntityID> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        List<EntityID> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
        int offset = Math.abs(this.agentInfo.getID().getValue()) % sorted.size();
        return sorted.get(offset);
    }

    private boolean isDone(EntityID roadID) {
        StandardEntity entity = this.worldInfo.getEntity(roadID);
        if (!(entity instanceof Road)) {
            return true;
        }

        Road road = (Road) entity;

        if (!road.isBlockadesDefined()) {
            return false;
        }

        return road.getBlockades().isEmpty();
    }

    private boolean isStillBlocked(Road road) {
        return road.isBlockadesDefined() && !road.getBlockades().isEmpty();
    }

    
}
