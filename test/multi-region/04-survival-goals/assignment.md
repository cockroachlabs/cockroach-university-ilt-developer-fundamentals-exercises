---
slug: survival-goals
id: zcaiyx3godzn
type: challenge
title: 03 Improve resilience by adding Region survival goal
teaser: Practice adding Region survival goal to database and observe the new behavior
notes:
- type: text
  contents: |-
    In this challenge, you'll learn how to:
    * Implement survival goals using CockroachDB survival goals syntax
    * Configure and test region survival settings
    * Verify failover behavior in multi-region deployments
tabs:
- id: c7ahafzll8t6
  title: Terminal 1
  type: terminal
  hostname: ilt-lab-vm
- id: 2bgoyu8coi2j
  title: Terminal 2
  type: terminal
  hostname: ilt-lab-vm
difficulty: ""
enhanced_loading: null
---

# Region Survival Goal

In this challenge, you'll learn how to increase MovR's multi-region database resilience by adding a **region survival goal** and testing the resilience through an outage simulation.

## Learning Objectives

By completing this exercise, you've learned:
- How to configure region survival goals in CockroachDB
- The impact of survival goals on voting configurations
- How CockroachDB handles region failures
- The automatic recovery process when failed regions come back online


## The Story
===
Recall, the MovR application uses three databases as part of its architecture: `movr_users`, `movr_rides`, and `movr_vehicles` - one database for each microservice. As part of the multi-region migration project, these databases are configured as multi-region databases across 3 different regions.

The ride service is vital to the core MovR customer experience. An outage of the rides service would lead to catastrophic service degradation for customers, affecting both riders trying to start trips and those trying to complete them.

In this exercise, you'll configure the `movr_rides` database to survive region failures.

## 1. Check the Current Zone Configuration
===

First, let's examine the current zone configuration for the `movr_rides` database:

1. Open [button label="Terminal 1" background="#6935ff"](tab-0) and connect to the cluster:
```sql,run
cockroach sql --certs-dir=/root/certs --host=haproxy --port=26257
```

2. Check the zone configuration:

```sql,run
SHOW ZONE CONFIGURATION FROM DATABASE movr_rides;
```

**How many voting replicas is this database configured for?**

## 2. Update the Region Survival Goal
===

Update the movr_rides database survival goal to `REGION` using this syntax:

```sql,run
ALTER DATABASE movr_rides SURVIVE REGION FAILURE;
```

After making this change, inspect the zone configuration again and note the differences, particularly:
- `num_replicas`
- `num_voters`
- `constraints`
- `voter_constraints`
- `lease_preferences`

Pay special attention to the `num_voters` parameter - **it should have changed from 3 to 5 after setting the REGION survival goal.**

## 3. Prepare for Testing
===

1. First, let's modify the suspect timeout setting to speed up our failover testing:

```sql,run
SET CLUSTER SETTING server.time_until_store_dead = '1m15s';
```

2. Now let's add some test data to verify our database remains functional throughout the region failure:
```bash,run
-- Insert some test rides
INSERT INTO movr_rides.rides (vehicle_id, user_email, start_ts)
VALUES
  (gen_random_uuid(), 'alice@example.com', now()),
  (gen_random_uuid(), 'bob@example.com', now()),
  (gen_random_uuid(), 'carol@example.com', now());

-- Insert some test events
INSERT INTO movr_rides.events (event_type, event_data)
VALUES
  ('ride_started', '{"location": "downtown", "battery_level": 0.9}'::json),
  ('ride_ended', '{"distance": 1.2, "duration": 10}'::json);

-- Verify our data
SELECT user_email, start_ts FROM movr_rides.rides ORDER BY start_ts DESC LIMIT 3;
SELECT event_type, ts FROM movr_rides.events ORDER BY ts DESC LIMIT 2;
```

## 3. Simulate region failure
===

Let's verify that the database can survive a region failure.

1. First, open [button label="Terminal 1" background="#6935ff"](tab-0), and check where the leaseholder is located:
```sql,run
with range_info AS (SHOW RANGES FROM TABLE movr_rides.rides WITH DETAILS)
SELECT range_id,lease_holder,lease_holder_locality,replicas,replica_localities from range_info;
```

**On which node is the leaseholder?**

2. We'll simulate a full region outage by stopping all nodes in the primary region. The nodes are distributed as follows:

| Region | Nodes |
|--------|--------------|
| us-east (PRIMARY)| node1, node2, node3 |
| us-central | node4, node5, node6 |
| us-west | node7, node8, node9 |

Open [button label="Terminal 2" background="#6935ff"](tab-1) and stop each node in the primary region:
```bash,run
ssh node1 pkill cockroach
ssh node2 pkill cockroach
ssh node3 pkill cockroach
```

3. After stopping the primary region nodes, open [button label="Terminal 1" background="#6935ff"](tab-0) verify that the leaseholder has moved to an available region:
```sql,run
with range_info AS (SHOW RANGES FROM TABLE movr_rides.rides WITH DETAILS)
SELECT range_id,lease_holder,lease_holder_locality,replicas,replica_localities from range_info;
```

You should see a new `lease_holder_locality` in either `us-central` or `us-west`.

> [!NOTE]
> It might take a few moments for the lease holder to move. If you don't see the change immediately, wait a few seconds and check again.

## 4. Verify database availability during region outage
===
After shutting down the primary region nodes, let's verify our data is still accessible and we can continue to perform operations:

```sql,run
-- Check our existing data
SELECT user_email, start_ts FROM movr_rides.rides ORDER BY start_ts DESC LIMIT 3;
SELECT event_type, ts FROM movr_rides.events ORDER BY ts DESC LIMIT 2;

-- Add new data to verify write operations
INSERT INTO movr_rides.rides (vehicle_id, user_email, start_ts)
VALUES (gen_random_uuid(), 'dave@example.com', now());

INSERT INTO movr_rides.events (event_type, event_data)
VALUES ('ride_started', '{"location": "uptown", "battery_level": 0.95}'::json);

-- Verify our new data
SELECT user_email, start_ts FROM movr_rides.rides WHERE user_email = 'dave@example.com';
SELECT event_type, ts FROM movr_rides.events ORDER BY ts DESC LIMIT 1;
```
## Bring primary region back online

Now, let's test recovery by bringing the primary region back online. For the us-east region nodes (1-3), we'll use zones a, b, and c:

1. Open [button label="Terminal 2" background="#6935ff"](tab-1) and start all nodes in `us-east`:
```bash,run
# Define the zones array
ZONES=("a" "b" "c")
US_EAST_NODES=(node1 node2 node3)

# Start each node in the us-east region with its corresponding zone
for i in "${!US_EAST_NODES[@]}"; do
    node="${US_EAST_NODES[i]}"
    zone="${ZONES[i]}"
    ssh ${node} "nohup cockroach start \
        --certs-dir=/root/certs \
        --advertise-addr=${node}:26257 \
        --http-addr=0.0.0.0:8080 \
        --join=node1:26257,node2:26257,node3:26257 \
        --locality=region=us-east,zone=${zone} \
        --cache=.25 \
        --max-sql-memory=.25 \
        --background \
        > /tmp/start_${node}.out 2> /tmp/start_${node}.err < /dev/null"
done
```

2. Verify all nodes are back online:
```bash,run
cockroach node status --certs-dir=/root/certs --host=haproxy --format=tsv | awk -F'\t' '{
    gsub(",", "|", $7);
    print $1","$2","$7","$8","$9
}' | column -t -s','
```

3. Open [button label="Terminal 1" background="#6935ff"](tab-0) and confirm that the **leaseholder returns to the primary region**:
```sql,run
with range_info AS (SHOW RANGES FROM TABLE movr_rides.rides WITH DETAILS)
SELECT range_id,lease_holder,lease_holder_locality,replicas,replica_localities from range_info;
```

> [!NOTE]
> Remember it might take up to a minute to see the change.

4. Perform one final post-recovery verification:

```sql,run
-- Verify all our data survived
SELECT COUNT(*) FROM movr_rides.rides;
SELECT COUNT(*) FROM movr_rides.events;

-- Add one final test record
INSERT INTO movr_rides.rides (vehicle_id, user_email, start_ts)
VALUES (gen_random_uuid(), 'eve@example.com', now());

-- Verify we can see all our test data
SELECT user_email, start_ts
FROM movr_rides.rides
ORDER BY start_ts DESC
LIMIT 5;
```

## Summary
===
In this exercise, you:

**Configured Region Survival**
- Set up REGION survival goals for the `movr_rides` database
- Verified configuration changes including the increase from 3 to 5 voters
- Modified the suspect timeout for faster failover testing

**Tested Data Availability**
- Added test records to both `rides` and `events` tables
- Verified data was readable before simulating region failure
- Successfully performed reads and writes during the outage
- Confirmed all data remained intact after recovery

**Managed Region Failure**
- Simulated a region outage by stopping all nodes in the primary region
- Observed automatic leaseholder movement to an available region
- Successfully restarted the primary region
- Verified the leaseholder returned to the primary region

This exercise demonstrated how CockroachDB maintains availability and consistency even when an entire region fails, showing that:
- The database remains fully operational during a region outage
- Both read and write operations continue to work
- Data remains consistent throughout the failure and recovery process
- The system automatically rebalances when the region comes back online

> [!NOTE]
> - The increase from 3 to 5 voters ensures the cluster can maintain quorum even if an entire region fails
> - Watch how quickly CockroachDB detects node failure and moves the leaseholder to maintain availability
> - When the primary region comes back online, CockroachDB automatically rebalances to optimize for locality preferences

