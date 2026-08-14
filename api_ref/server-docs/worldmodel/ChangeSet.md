# ChangeSet.java

`rescuecore2.worldmodel.ChangeSet`

```
ChangeSet();
ChangeSet(ChangeSet other);
```

**ChangeSet** is **what the agent perceived this tick** : the set of entities whose properties changed inside the agent's vision range. `AgentInfo.getChanged()` returns it, and `WorldInfo.getChanged()` is the ADF wrapper. It is the raw material of every "did I just see something new?" decision.

```java
ChangeSet changed = agentInfo.getChanged();
for (EntityID id : changed.getChangedEntities()) {
    StandardEntity entity = worldInfo.getEntity(id);
    ...
}
```

- What did I **see** ?
  - [getChangedEntities()](#getchangedentities)
  - [getChangedProperties()](#getchangedproperties)
  - [getChangedProperty()](#getchangedproperty)
  - [getEntityURN()](#getentityurn)
  - [getDeletedEntities()](#getdeletedentities)

- **Building a change set**
  - [addChange()](#addchange)
  - [addAll()](#addall)
  - [entityDeleted()](#entitydeleted)
  - [merge()](#merge)

## <a id="getchangedentities"></a>getChangedEntities()

```java
Set<EntityID> getChangedEntities();
```
Get every entity that changed this tick.

**Parameters :**
- None

**Returns :**
- `Set<EntityID>` : The changed entity IDs.

---

## <a id="getchangedproperties"></a>getChangedProperties()

```java
Set<Property> getChangedProperties(EntityID e);
```
Get the properties that changed for one entity.

**Parameters :**
- `e` : The entity ID.

**Returns :**
- `Set<Property>` : The changed properties.

---

## <a id="getchangedproperty"></a>getChangedProperty()

```java
Property getChangedProperty(EntityID e, int urn);
```
Get one changed property by its numeric URN (see `StandardPropertyURN`).

**Parameters :**
- `e` : The entity ID.
- `urn` : The property URN id.

**Returns :**
- `Property` : The property, or `null`.

---

## <a id="getentityurn"></a>getEntityURN()

```java
int getEntityURN(EntityID id);
```
Get the type URN of a changed entity, without looking it up in the world model.

**Parameters :**
- `id` : The entity ID.

**Returns :**
- `int` : Numeric entity URN.

---

## <a id="getdeletedentities"></a>getDeletedEntities()

```java
Set<EntityID> getDeletedEntities();
```
Get entities that disappeared — this is how a police force learns a blockade it was clearing is gone.

**Parameters :**
- None

**Returns :**
- `Set<EntityID>` : Deleted entity IDs.

---

## <a id="addchange"></a>addChange()

```java
void addChange(Entity e, Property p);
void addChange(EntityID e, int urn, Property p);
```
Record a property change.

**Parameters :**
- `e` : The entity (or its ID).
- `urn` : The entity URN when only the ID is given.
- `p` : The changed property.

**Returns :**
- `void`

---

## <a id="addall"></a>addAll()

```java
void addAll(Collection<? extends Entity> c);
```
Record every property of every given entity as changed.

**Parameters :**
- `c` : The entities to add.

**Returns :**
- `void`

---

## <a id="entitydeleted"></a>entityDeleted()

```java
void entityDeleted(EntityID e);
```
Record that an entity no longer exists.

**Parameters :**
- `e` : The deleted entity ID.

**Returns :**
- `void`

---

## <a id="merge"></a>merge()

```java
void merge(ChangeSet other);
```
Merge another change set into this one.

**Parameters :**
- `other` : The change set to merge.

**Returns :**
- `void`

---
