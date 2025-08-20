---
slug: stale-follower-reads
id: iyqxsemhizwy
type: challenge
title: 04 Implement Stale Follower Reads for Low-Latency Access
teaser: Learn how to use follower reads to optimize read performance in multi-region
  deployments
notes:
- type: text
  contents: |-
    In this challenge, you'll learn how to:
    * Configure and implement stale follower reads
    * Monitor follower read metrics in the DB Console
    * Optimize read performance for geographically distributed users
    * Verify follower read behavior using built-in metrics
tabs:
- id: zf90xv9hbhe3
  title: Terminal
  type: terminal
  hostname: ilt-lab-vm
- id: uwhdtm86osjk
  title: DB Console
  type: service
  hostname: node1
  path: '#/overview/list'
  port: 8443
difficulty: basic
enhanced_loading: null
---

# Stale Follower Reads for Low-Latency Access

In this challenge, you'll help MovR's customer service team implement stale follower reads to improve query performance when accessing vehicle location history data.

## Learning Objectives

By completing this exercise, you'll learn:
- How to use stale follower reads for low-latency data access
- When to use follower reads vs. real-time reads
- How to verify follower read behavior using metrics
- Best practices for implementing follower reads

## The Story
===

MovR's customer service team is distributed across the United States from coast to coast. They frequently need to access vehicle location history to resolve customer issues. The data is stored in the `movr_vehicles` database with these multi-region settings:

```sql,nocopy
ALTER DATABASE movr_vehicles SET PRIMARY REGION "us-east";
ALTER DATABASE movr_vehicles ADD REGION "us-central";
ALTER DATABASE movr_vehicles ADD REGION "us-west";
ALTER DATABASE movr_vehicles SURVIVE REGION FAILURE;
```

The `location_history` table receives constant updates as vehicles move around. While real-time data access is important for some features, customer service representatives can often work with slightly stale data to troubleshoot past issues.

## 1. Access Vehicle Location History
===
1. Open [button label="Terminal 1" background="#6935ff"](tab-0) and connect to the database:
```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy --port=26257
```

2. Let's start by looking at the current query for accessing location history:

```sql,run
SELECT ts, longitude, latitude
FROM movr_vehicles.location_history
WHERE vehicle_id = 'e92dbdff-ee44-47ae-9ce3-b9b4eb1f7ea6';
```

## 2. Implement Follower Reads
===

Now let's modify the query to use follower reads for better performance:

```sql,run
SELECT ts, longitude, latitude
FROM movr_vehicles.location_history
AS OF SYSTEM TIME follower_read_timestamp()
WHERE vehicle_id = 'e92dbdff-ee44-47ae-9ce3-b9b4eb1f7ea6';
```

> [!NOTE]
> The `AS OF SYSTEM TIME` clause must come before the `WHERE` clause. The `follower_read_timestamp()` function automatically selects an appropriate timestamp for stale reads.

## 3. Monitor Follower Read Metrics
===

Let's verify our follower reads are working:

1. Open the [button label="DB Console" background="#6935ff"](tab-1) and click "Advanced Debug" in the navigation bar

> [!NOTE]
> The login details are username -> "root" password -> "pass"

[!Advanced Debug Screenshot](../assets/02-create-multi-region-db/advanced-debug.png)

2. Under Reports, select "Custom Time Series Chart"

[!Time Series Chart Link](../assets/02-create-multi-region-db/custom-chart.png)

3. Add the metric `follower_reads.success_count` to track follower read operations

[!Add metric](../assets/02-create-multi-region-db/add-metric.png)

4. Return to the [button label="Terminal" background="#6935ff"](tab-0) and run your follower read query multiple times to generate metrics:

```sql,run
-- Run this several times to generate metrics
SELECT ts, longitude, latitude
FROM movr_vehicles.location_history
AS OF SYSTEM TIME follower_read_timestamp()
WHERE vehicle_id = 'e92dbdff-ee44-47ae-9ce3-b9b4eb1f7ea6';
```

> [!NOTE]
> Run the query 10-15 times to see the follower read count increase in the metrics chart.

1. Open [button label="DB Console" background="#6935ff"](tab-1) to view the updated chart.

## Summary
===

In this exercise, you:

**Implemented Follower Reads**
- Modified existing queries to use stale follower reads
- Used the `follower_read_timestamp()` function for automatic timestamp selection
- Verified follower read behavior through metrics

**Monitored Performance**
- Set up custom metrics in the DB Console
- Tracked follower read success counts
- Observed the impact of follower reads on query performance

**Key Learnings**
- Follower reads provide lower latency for read operations
- Slightly stale data can be acceptable for many use cases
- Monitoring helps verify follower read behavior
- The `AS OF SYSTEM TIME` clause position is important

> [!NOTE]
> - Consider follower reads when real-time data isn't critical
> - Monitor follower read metrics to ensure they're working as expected
> - Remember the correct syntax order for follower read queries
