# Issue Haven't Solve In SampleExtActionClear

1. **It has zero awareness of teammates**
    - overlapping identical target
    - no deconfliction, "someone already there, no need me'

2. **Completely dumb what to clear , only know how**
    - zero decision making about priority
    - blindly trust whatever `setTarget` tells it
    - if road detector/ target allocator weak, maybe have bad plan with perfect precision

3. **Doesnt send anything**
    - no `messageManager.addMessage(...)` anywhere
    - everything roads it clear is invisible to police station and other agent

4. **It's blind to fire**
    - `calc` dont know is this road near a burning building, maybe dont walk through
    - `needRest` only react on damage taken , not about to happen

5. **The switch(si.getMode()) has no default**
    - If getMode() ever returns anything outside the three expected cases, pathPlanning stays null and the very next call to pathPlanning.getResult(...) in calc() throws an NPE with zero explanation. You have no defensive fallback and no log message telling you why the agent just died.

6. **Zero vector deadlock**
    - cutTowards

7. **No validation on config default**
    - `developData.getInteger(..., 3)` and `(..., 100)`
    - if typo in key name, silent fallback to the default with zero warning.
    - error never know, because nothing logs "using default, config key not found."

8. **calcRest doesn't check if the refuge is actually reachable or usable**
    - No check for whether that refuge is on fire, overcrowded, or blocked. If the nearest refuge is compromised, the agent still walks straight at it.

9. **No Test**
    - Not unit tests, not even a manual harness. The only feedback loop is running the entire simulation and eyeballing whether a dot on a map moves.

10. **Undefined blockades get silently skipped forever, with no follow-up.**
    - `!blockade.isApexesDefined()` just does continue — there's no mechanism to say "I know something's there, get closer to actually perceive its shape." If a blockade's apexes never resolve, that blockade is invisible to this class permanently, and nothing flags that as a problem.