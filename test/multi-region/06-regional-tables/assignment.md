---
slug: regional-tables
id: jhje7yojb6xs
type: challenge
title: 05 Optimizing Regional Table Performance
teaser: Learn how to optimize table performance for specific regions using table locality
  settings
notes:
- type: text
  contents: |-
    In this challenge, you'll learn how to:
    * Configure table locality for optimal regional performance
    * Verify leaseholder locations
    * Optimize table performance for specific geographic regions
tabs:
- id: z4emebidktms
  title: Terminal 1
  type: terminal
  hostname: ilt-lab-vm
difficulty: basic
enhanced_loading: null
---

# Regional Table Optimization

In this challenge, you'll help MovR optimize their new VIP rates feature for San Francisco users by configuring table locality, even though the database's primary region is in us-east.

## Learning Objectives

By completing this exercise, you'll learn:
- How to configure table locality independently from database primary region
- How to verify leaseholder locations
- How leaseholder placement affects regional performance
- Best practices for optimizing table performance for specific regions

## The Story
===

MovR is launching a new personalized VIP rates feature, starting with a trial in San Francisco. While the `movr_pricing` database's primary region is in us-east, the team needs to ensure fast, responsive performance for their West Coast beta users.

The database is already configured with these multi-region settings:
```sql,nocopy
ALTER DATABASE movr_pricing SET PRIMARY REGION "us-east";
ALTER DATABASE movr_pricing ADD REGION "us-central";
ALTER DATABASE movr_pricing ADD REGION "us-west";
ALTER DATABASE movr_pricing SURVIVE REGION FAILURE;
```

## 1. Create the VIP Rates Table
===

1. Open [button label="Terminal 1" background="#6935ff"](tab-0) and connect to the database:
```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy --port=26257
```

2. Create the table that will store our VIP rates:

```sql,run
CREATE TABLE movr_pricing.vip_rates (
    rate_code STRING PRIMARY KEY,
    market STRING NOT NULL,
    description STRING NOT NULL,
    creation_time TIMESTAMP NOT NULL
);
```

## 2. Check Current Leaseholder Location
===

Let's verify where the leaseholder is currently located:

```sql,run
WITH vip_info AS (SHOW RANGES FROM TABLE movr_pricing.vip_rates WITH DETAILS)
SELECT range_id,lease_holder,lease_holder_locality FROM vip_info;
```

> [!NOTE]
> You should see the leaseholder in us-east (the primary region) by default.

## 3. Optimize for West Coast Performance
===

Now let's configure the table locality to optimize for San Francisco users:

```sql,run
ALTER TABLE movr_pricing.vip_rates SET LOCALITY REGIONAL BY TABLE IN "us-west";
```

## 4. Verify the Configuration
===

Check that the leaseholder has moved to the new region:

```sql,run
WITH vip_info AS (SHOW RANGES FROM TABLE movr_pricing.vip_rates WITH DETAILS)
SELECT range_id,lease_holder,lease_holder_locality FROM vip_info;
```

> [!NOTE]
> After a few moments, you should see the leaseholder move to us-west, which will provide better performance for San Francisco users. **This could take up to a minute to see**

## 5. Compare Default vs Regional Table Settings
===

Next, we will create a standard pricing table to compare the leaseholder location with our VIP rates table:

```sql,run
CREATE TABLE movr_pricing.standard_rates (
    rate_code STRING PRIMARY KEY,
    market STRING NOT NULL,
    base_rate DECIMAL NOT NULL,
    description STRING NOT NULL,
    creation_time TIMESTAMP NOT NULL
);
```

Let's look at both tables' leaseholder locations side by side:

```sql,run
WITH
  standard_info AS (
    SELECT
      'standard_rates' as table_name,
      range_id,
      lease_holder,
      lease_holder_locality
    FROM [SHOW RANGES FROM TABLE movr_pricing.standard_rates WITH DETAILS]
  ),
  vip_info AS (
    SELECT
      'vip_rates' as table_name,
      range_id,
      lease_holder,
      lease_holder_locality
    FROM [SHOW RANGES FROM TABLE movr_pricing.vip_rates WITH DETAILS]
  )
SELECT * FROM standard_info
UNION ALL
SELECT * FROM vip_info
ORDER BY table_name;
```

> [!NOTE]
> You'll notice that:
> - **After about a minute** the `standard_rates` table leaseholder shows up in `us-east` (the primary region).
> - The `vip_rates` table leaseholder is in `us-west` (optimized for San Francisco users)
> - Both tables are in the same database but can have different regional optimization settings

### This demonstrates how:
1. Tables in the same database can have different regional optimizations
2. You can target performance improvements for specific features (like the VIP program) without affecting other tables
3. Default settings remain appropriate for tables that don't need region-specific optimization

## Summary
===

In this exercise, you:

**Created and Configured Multiple Tables**
- Created a VIP rates table optimized for West Coast users
- Created a standard rates table using default settings
- Compared leaseholder locations between tables
- Demonstrated how different tables in the same database can have different regional optimizations

**Key Learnings**
- Tables can have different locality settings from their parent database
- Multiple tables in the same database can have different regional optimizations
- Leaseholder location significantly impacts regional performance
- REGIONAL BY TABLE allows fine-grained control over table optimization
- Default settings remain appropriate for tables without specific regional requirements

> [!NOTE]
> - Consider table locality when you need to optimize performance for specific regions
> - Not all tables need custom regional settings - use them strategically
> - You can mix default and custom regional settings within the same database
> - Monitor leaseholder locations to verify your configuration is working as intended
