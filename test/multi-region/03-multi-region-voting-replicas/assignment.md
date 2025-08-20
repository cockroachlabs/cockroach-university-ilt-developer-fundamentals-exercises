---
slug: multi-region-voting-replicas
id: f0xpy7qrjlvk
type: challenge
title: 02b Create multi-region database
teaser: Observe voting and non-voting replicas when creating multi-region database
notes:
- type: text
  contents: Take a closer look at voting and non-voting replicas.
tabs:
- id: xrfuqmg6g5np
  title: terminal
  type: terminal
  hostname: ilt-lab-vm
difficulty: ""
enhanced_loading: null
---

# Adding Additional Regions to MovR Databases

In this challenge, you'll learn how to expand MovR's multi-region database configuration by adding regions to additional databases and understand how voting and non-voting replicas work.

## Objectives

In this challenge you will:
- Add additional regions to MovR's databases
- Observe how voting and non-voting replicas are impacted by using multi-region abstractions
- Learn about zone configurations and replica distribution

## The Story

After successfully creating a multi-region setup for the `movr_rides` database, it's time to extend the same configuration to `movr_users` and `movr_vehicles` databases. This will ensure consistent data access and availability characteristics across all MovR's databases.

[diagram showing multi-region setup with replicas]

## Starting Point

Your CockroachDB cluster is already running with the following setup:
- Three regions: us-east, us-central, and us-west
- The `movr_rides` database configured as multi-region from the previous exercise
- The `movr_users` and `movr_vehicles` databases ready to be configured

## Steps

### 1. Inspect Current Zone Configuration

Connect to the cluster:

```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy
```

Let's first examine the current zone configuration for the `movr_users` database:

```sql,run
SHOW ZONE CONFIGURATION FROM DATABASE movr_users;
```

You should see output showing basic configuration:
```nocopy
     target     |              raw_config_sql
----------------+-------------------------------------------
  RANGE default | ALTER RANGE default CONFIGURE ZONE USING
                |     range_min_bytes = 134217728,
                |     range_max_bytes = 536870912,
                |     gc.ttlseconds = 90000,
                |     num_replicas = 3,
                |     constraints = '[]',
                |     lease_preferences = '[]'
```

### 2. Update Database Configurations

Add the same multi-region configuration to both `movr_users` and `movr_vehicles` databases:

1. First, set the primary region to us-east (since majority of users are in NYC):
```sql,run
ALTER DATABASE movr_users PRIMARY REGION "us-east";
ALTER DATABASE movr_vehicles PRIMARY REGION "us-east";
```

2. Add the remaining regions:
```sql,run
ALTER DATABASE movr_users ADD REGION "us-central";
ALTER DATABASE movr_users ADD REGION "us-west";

ALTER DATABASE movr_vehicles ADD REGION "us-central";
ALTER DATABASE movr_vehicles ADD REGION "us-west";
```

### 3. Verify Configuration

Check the updated zone configuration:

```sql,run
SHOW ZONE CONFIGURATION FROM DATABASE movr_users;
```

You should see changes including:
- `num_replicas` increased to 5
- `num_voters` set to 3
- `voter_constraints` showing all voting replicas in us-east (primary region)
- Region-specific constraints

### 4. Review All Databases

Examine the final configuration of all databases:

```sql,run
SELECT database_name, primary_region, regions FROM [SHOW DATABASES] WHERE database_name ILIKE '%movr_%';
```

You should see the following output:

```nocopy
  database_name | primary_region |           regions
----------------+----------------+-------------------------------
  movr_rides    | us-east        | {us-central,us-east,us-west}
  movr_users    | us-east        | {us-central,us-east,us-west}
  movr_vehicles | us-east        | {us-central,us-east,us-west}
```

Look for:
- Primary region set to `us-east` for all `movr` databases
- All three regions listed in the regions column for each database

## Summary
===

In this exercise, you:

**Configured Multi-Region Databases**
- Set primary regions for movr_users and movr_vehicles databases
- Added two other regions to complete the multi-region setup
- Verified all three regions (us-east, us-central, us-west) were properly configured

**Observed Replica Types**
- Observed how adding regions impacts replica distribution
- Verified the automatic configuration of voting and non-voting replicas
- Saw how the primary region influences voter replica placement

**Explored Zone Configurations**
- Examined zone configuration changes after adding regions
- Observed the increase in replica count from 3 to 5
- Verified voter constraints showing proper replica placement

This exercise demonstrated how CockroachDB's multi-region abstractions automatically handle complex configuration details, including:
- Proper placement of voting replicas in the primary region
- Distribution of non-voting replicas across non-primary regions
- Automatic adjustment of zone configurations
- Consistent configuration across multiple databases

> [!NOTE]
> - Use `SHOW RANGES` to see detailed replica distribution
> - The primary region should match where the majority of users are located
> - Remember that voter replicas are automatically placed in the primary region


