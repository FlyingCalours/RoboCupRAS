# ClearGeometry, explained

Notes on the `rescuecore2` geometry package and why `ClearGeometry` is shaped
the way it is. Written against `roborescue/rcrs-server` and
`roborescue/adf-core-java` (master, August 2026).

---

## 1. The units and the coordinate space

Everything in RCRS world coordinates is an **integer number of millimetres**.
A road is typically a few thousand mm across; `clearRepairDistance` is usually
around 10000 mm. Two consequences that matter:

- Distances are large numbers, so a "tolerance" of `1000.0` in the original
  `samePoint` is one metre, not one millimetre. That is a deliberate slop, not
  a rounding fudge.
- Entity coordinates come back as `int`, but every geometry class works in
  `double`. Every cut point therefore ends with a cast back to `int` when it
  goes into `ActionClear`. Sub-millimetre precision is discarded, and that is
  fine.

---

## 2. The four `rescuecore2` types you actually need

All in `rescuecore2.misc.geometry`.

### `Point2D`

A position. Immutable, `double` fields.

```java
Point2D p = new Point2D(x, y);
double x = p.getX();
Vector2D v = p.minus(other);   // p - other, as a direction
```

The only method that matters here is `minus`. Note the direction of the
subtraction: `a.minus(b)` is the vector **from b to a**. Getting this backwards
makes the agent cut in exactly the wrong direction, and the symptom (agent
clears away from the exit forever) looks nothing like a sign error.

### `Vector2D`

A direction with a magnitude. Not a position — it has no origin.

```java
double len  = v.getLength();
Vector2D u  = v.normalised();        // length 1, same bearing
Vector2D s  = u.scale(clearDistance);// length clearDistance, same bearing
```

`normalised()` divides both components by `getLength()`. **If the length is
zero this divides by zero**, and IEEE floating point hands back `NaN` rather
than throwing. See section 4.

### `Line2D`

A segment, stored as an origin point plus a direction vector.

```java
Point2D start = line.getOrigin();
Point2D end   = line.getEndPoint();
```

Note `getEndPoint()` is computed (origin + direction), not stored.

### `GeometryTools2D`

Static helpers. The ones relevant to police and fire work:

| Method | What it gives you |
| --- | --- |
| `vertexArrayToPoints(int[])` | flat `{x0,y0,x1,y1,...}` to `List<Point2D>` |
| `pointsToLines(List<Point2D>, boolean close)` | points to edges; `close=true` joins last back to first |
| `intersects(Line2D, Line2D)` | segment intersection |
| `getDistance(Point2D, Point2D)` | plain distance |
| `getDistance(Line2D, Point2D)` | distance from a point to a *segment* |
| `getClosestPointOnSegment(Line2D, Point2D)` | the nearest point on an edge |
| `isPointInsidePolygon(Point2D, List<Point2D>)` | containment |
| `computeCentroid(List<Point2D>)` | polygon centre of area |

The last four are unused by the police agent but do most of the work for the
fire brigade — see section 7.

---

## 3. Why `ClearGeometry` is a separate class

Two reasons, and the second one is the important one.

**It has no dependencies on the simulation.** No `WorldInfo`, no `AgentInfo`,
no `EntityID`. Every method takes plain numbers and returns plain numbers. That
is what makes `ClearGeometryTest` possible: it runs in milliseconds with no
kernel, no network, no map. Issue 9 in your list ("no test") is not really
fixable while the geometry is tangled up with the world model, because you
cannot construct a `PoliceForce` without a running simulation. Pulling the maths
out is the precondition for testing it.

**It separates "what is true about these shapes" from "what should the agent
do".** `SampleExtActionClear` decides policy — which blockade, whether to yield,
whether to rest. `ClearGeometry` only answers questions of fact. When the agent
misbehaves you can now ask which of the two is wrong, instead of reading one
400-line method.

---

## 4. `cutPoint` — the core conversion

```java
public static Point2D cutPoint(double agentX, double agentY,
                               double aimX, double aimY, int clearDistance)
```

### The problem it solves

The modern server takes `AKClearArea(x, y)`. The agent cuts a corridor of
`clearRepairDistance` length from itself towards `(x, y)`. So the kernel wants a
*point*, but what the agent actually knows is a *bearing*: "towards the exit I
am trying to use", "towards the nearest chunk of rubble".

`cutPoint` converts one into the other:

1. `aim - agent` gives the direction, with whatever magnitude it happened to
   have.
2. `normalised()` throws the magnitude away, keeping only the bearing.
3. `scale(clearDistance)` gives it the magnitude the kernel expects.
4. Add it back onto the agent's position to get an absolute point.

### Why step 2 and 3 are not redundant

This is the part that looks like pointless work. It is not. The aim point can be
anywhere — an edge midpoint 500 mm away, a blockade vertex 40000 mm away. If you
passed the aim straight to `ActionClear` you would get inconsistent behaviour:
a near aim under-reaches, a far aim asks the kernel for a corridor it will not
grant. Normalising then rescaling makes **the aim's distance irrelevant and only
its bearing significant**. That invariant is what
`cutPointIsIndependentOfHowFarTheAimIs` in the test file pins down.

### The `null` return

```java
if (direction.getLength() < EPSILON) {
  return null;
}
```

`EPSILON` is 1.0 mm. Below that, the agent is effectively standing on its own
aim point and there is no meaningful bearing to extract.

Returning `null` here is a design choice worth defending in your report. The
alternatives were:

- **Throw.** Wrong: this is a normal, expected situation in a simulation, not a
  programming error.
- **Return the agent's own position.** Wrong: it silently produces a clear
  command that does nothing, and the agent looks busy while achieving nothing.
- **Pick a fallback direction inside `cutPoint`.** Wrong for a different reason:
  choosing a fallback is *policy*, and policy belongs in the ExtAction. What
  counts as a good escape direction depends on the road's neighbours, which
  `ClearGeometry` deliberately cannot see.

So `null` means "this question has no answer, you decide what to do about it".
`SampleExtActionClear.escapeDegenerate` is where that decision lives.

### What the original code did

```java
if (direction.getLength() == 0) {
  int nudgeX = (int) (agentX + clearDistance);
  int nudgeY = (int) agentY;
  ...
}
```

Two bugs stacked on top of each other:

1. `== 0` only catches an exact zero. A direction of length 0.0001 mm passes the
   check, gets normalised, and produces coordinates in the millions. The kernel
   discards the command and you get no error. `< EPSILON` catches the whole
   degenerate neighbourhood, not just the exact point.
2. `agentX + clearDistance` is always due east. If the thing wedging the agent
   happens to be east — which is exactly the case when the agent got stuck
   driving east — the nudge pushes it into the same wall every single cycle,
   forever.

---

## 5. `sweepPoint` — the escape hatch

```java
public static Point2D sweepPoint(double agentX, double agentY,
                                 int clearDistance, int attempt)
```

When there is no usable aim at all, the agent still has to do *something*. This
returns a point on a circle of radius `clearDistance` around the agent, at a
bearing of `attempt * 60°`.

The `attempt` counter is the whole point. A fixed bearing that fails once will
fail identically every cycle, because nothing about the situation changed. By
incrementing `sweepAttempt` each time the deadlock detector fires,
consecutive stuck cycles try 0°, 60°, 120°, and so on — six distinct directions
before it repeats. Something in that set almost always frees the agent.

Sixty degrees is a compromise: small enough to cover the circle, large enough
that consecutive attempts are meaningfully different rather than both hitting
the same obstacle.

The invariant is that every sweep point sits exactly on the clear radius, which
`sweepPointsStayOnTheClearRadius` checks for twelve consecutive attempts,
including the wrap past 360°.

---

## 6. `segmentHitsPolygon` — "is this thing actually in my way"

```java
public static boolean segmentHitsPolygon(double fromX, double fromY,
                                         double toX, double toY, int[] apexes)
```

### The pipeline

`Blockade.getApexes()` returns a flat `int[]` in the form
`{x0, y0, x1, y1, x2, y2, ...}`. There is no polygon type and no
"does this polygon block me" call. You build the test yourself:

```java
List<Point2D> points = GeometryTools2D.vertexArrayToPoints(apexes);
List<Line2D>  edges  = GeometryTools2D.pointsToLines(points, true);
```

The `true` is load-bearing. Without it the last apex is never joined back to the
first, leaving the polygon open along one edge — and a segment entering through
exactly that gap reports no intersection. The blockade would be invisible from
one specific approach angle only, which is a spectacularly annoying bug to
reproduce.

Then each edge is tested against the agent's intended line of travel. Any
intersection means the blockade is on the path.

### Why the guard clause matters more than the maths

```java
if (!isUsablePolygon(apexes)) {
  return false;
}
```

`isUsablePolygon` requires a non-null array, at least 6 ints (three points), and
an even length. In a live simulation, blockade apexes are frequently
undefined — the agent knows something is there but has not perceived its shape.
The original code checked `isApexesDefined()` at the call site, but that flag
being true does not guarantee the array is well-formed. Pushing the check down
into the geometry means no caller can crash it, and `undefinedApexesNeverThrow`
records that as a promise rather than an accident.

Note that "not in the way" is the safe answer for a malformed polygon. It leads
the agent to drive on and discover the truth by bumping into things, which is
recoverable. The opposite default would have the agent clearing at phantoms.

### Why `java.awt.geom.Line2D.linesIntersect` and not `GeometryTools2D.intersects`

Both work. `GeometryTools2D.intersects(Line2D, Line2D)` is the native option and
would remove the `java.awt` import.

The AWT version is used because it is a single static call on primitives with
well-defined behaviour for collinear and endpoint-touching cases, which is
exactly the situation when a blockade edge lies flat along the road edge the
agent is aiming at. `GeometryTools2D` builds on `getSegmentIntersectionPoint`,
which returns `null` for parallel lines — correct, but it means collinear
overlap is handled by a different method (`overlaps`) that you would have to
call separately.

If your assignment rubric prefers no AWT dependency, swapping is a two-line
change and the tests will tell you immediately whether behaviour shifted. That
is the other thing tests buy you.

---

## 7. Transferring this to the fire brigade

The instinct is that fire brigades need none of this, because
`ActionExtinguish` takes a building ID and a water amount rather than
coordinates:

```java
new ActionExtinguish(EntityID targetID, int maxPower)
```

No point, no vector, no cut. That is true of the **command**. It is not true of
the **decision that precedes the command**, and that is where the same geometry
comes back.

### What changes: the constraint moves from direction to distance

The police problem is *directional*: the agent is in contact with the rubble and
only has to decide which way to cut.

The fire problem is *positional*: the agent can only extinguish a building it is
close enough to. `ScenarioInfo` exposes the limit:

```java
int maxDistance = scenarioInfo.getFireExtinguishMaxDistance();
int maxWater    = scenarioInfo.getFireExtinguishMaxSum();
```

So the police agent asks "which bearing", and the fire brigade asks "which
standing position". Both are answered with the same three operations.

**Important caveat.** For deciding whether the kernel will *accept* the command,
use `worldInfo.getDistance(agentID, buildingID)` rather than rolling your own
distance. That call goes through the same entity-location logic the simulator
uses, so your prediction matches the server's ruling. Use the polygon geometry
below to decide where to *move*, not to second-guess whether a command is legal.

### Method-by-method transfer

| `ClearGeometry` | Fire brigade equivalent |
| --- | --- |
| `cutPoint` | **Standoff point.** Same three steps, opposite sign. Aim the vector from the fire *towards* the agent, scale it to slightly less than `maxExtinguishDistance`, and you get a position that is in range without being inside the fire. Pass it as `new ActionMove(path, x, y)`. |
| the `null` return | **Identical bug, identical guard.** If the agent is standing on the building's centre, `fire - agent` has zero length and normalising gives `NaN`. This is *more* likely for fire brigades than for police, because agents routinely stand inside or on top of the building they are fighting. |
| `sweepPoint` | **Repositioning when the standoff spot is unreachable.** Rotate the bearing and try another side of the building. A fire brigade wedged behind a burning row has the same structural problem as a police agent wedged on a blockade edge: doing the same thing again will not help. |
| `segmentHitsPolygon` | **Route blockage detection.** A fire brigade *cannot clear*, so detecting that its route is blocked is more urgent than for the police, not less. The same test tells it to reroute and to broadcast a `MessageRoad` so a police agent gets sent. |
| `nearestApexIndex` | Superseded. Use `GeometryTools2D.getDistance(Line2D edge, Point2D agent)` over the building's edges instead of its vertices. For a large building the nearest *wall* can be well inside extinguish range while the nearest *corner* is not — vertex distance systematically overestimates. |
| `isUsablePolygon` | Same guard, same reason. Building apexes can be undefined for a building the agent has only heard about over the radio. |

### One thing with no police analogue

Fire spreads, so the fire brigade has a *cluster* problem the police do not:
several burning buildings that should be fought as one front rather than
individually. `GeometryTools2D.computeCentroid(List<Point2D>)` over the centres
of a burning cluster gives you an aim point for the group, and the standoff
calculation then runs against that centroid rather than a single building.
Attacking the edge of a cluster generally beats attacking its hottest building,
because the goal is to stop propagation.

### One thing that transfers but is not geometry

The repeat-detection in `cutTowards` — same command, same place, N times, so do
something else — is a pattern, not a geometric fact. The fire brigade version is
"I have poured water on this building for N cycles and its fieryness has not
dropped", which usually means the tank is empty or the building is beyond
saving. Same counter, same threshold-then-change-behaviour structure, different
sensor. Worth writing once and reusing.

### Suggested refactor if you do all three agent types

Rename `ClearGeometry` to something like `RescueGeometry` and add:

```java
public static Point2D standoffPoint(double agentX, double agentY,
                                    double targetX, double targetY,
                                    int maxRange, double safetyMargin)

public static double distanceToPolygon(double x, double y, int[] apexes)
```

`standoffPoint` is `cutPoint` with the vector reversed and the scale set to
`maxRange * safetyMargin`. `distanceToPolygon` wraps
`GeometryTools2D.getDistance(Line2D, Point2D)` over the closed edge loop. Both
are testable the same way, with no simulator running, and both are shared by the
police (approach) and the fire brigade (standoff).

The ambulance team reuses `distanceToPolygon` and the route-blockage test but
needs little else — its hard problems are triage ordering and buriedness
estimation, which are not geometric at all.
