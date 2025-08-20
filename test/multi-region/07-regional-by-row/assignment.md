---
slug: regional-by-row
id: eyralgljhgqh
type: challenge
title: 06 Optimize User Data Access with Regional by Row Tables
teaser: Learn how to implement row-level optimization for geographically distributed
  data
notes:
- type: text
  contents: |-
    In this challenge, you'll learn how to:
    * Configure regional by row tables
    * Optimize data access based on geographic location
    * Verify row-level locality settings
    * Monitor leaseholder distribution
tabs:
- id: fom6x8qhfext
  title: Terminal
  type: terminal
  hostname: ilt-lab-vm
difficulty: basic
enhanced_loading: null
---

# Regional By Row Tables

In this challenge, you'll help MovR optimize their user service by implementing regional by row tables to improve performance for geographically distributed users.

## Learning Objectives

By completing this exercise, you'll learn:
- How to configure regional by row tables
- When to use row-level optimization
- How to verify row-level locality settings
- Best practices for geographically distributed data

## The Story
===

MovR is growing rapidly across the US with plans to expand into EU and APAC regions. The user service currently stores all profile data in a single table with users spread across different regions. The team has identified that user data is primarily accessed from the user's home region.

The `movr_users` database is already configured with these multi-region settings:
```sql,nocopy
ALTER DATABASE movr_users SET PRIMARY REGION "us-east";
ALTER DATABASE movr_users ADD REGION "us-central";
ALTER DATABASE movr_users ADD REGION "us-west";
ALTER DATABASE movr_users SURVIVE REGION FAILURE;
```

## Examine Current Configuration
===

1. Open [button label="Terminal 1" background="#6935ff"](tab-0) and connect to the database:
```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy --port=26257
```

2. Run the following command to look at the current users table structure:

```sql,run
SHOW COLUMNS FROM movr_users.users;
```

## Check Current Leaseholder Location
===

Let's see where our data is currently being served from:

```sql,run
WITH user_info AS (SHOW RANGES FROM TABLE movr_users.users WITH DETAILS)
SELECT range_id, lease_holder, lease_holder_locality
FROM user_info;
```

You should see output showing leaseholders in the primary region.

## Configure Regional By Row
===

Now let's optimize the table for row-level locality:

```sql,run
ALTER TABLE movr_users.users SET LOCALITY REGIONAL BY ROW;
```

> [!NOTE]
> This command adds a `crdb_region` column that determines where each row's leaseholder should be located.

## Verify Table Structure
===

Let's check the modified table structure:

```sql,run
SHOW COLUMNS FROM movr_users.users;
```

Notice the new `crdb_region` column that was automatically added.

## Load Sample User Data
===

Let's add some geographically distributed users:

```sql,run
INSERT INTO movr_users.users (
    email,
    city,
    last_name,
    first_name,
    phone_numbers,
    crdb_region
) VALUES
    ('user1@example.com', 'New York', 'Smith', 'John', ARRAY['202-555-0101'], 'us-east'),
    ('user2@example.com', 'San Francisco', 'Garcia', 'Maria', ARRAY['951-555-0102'], 'us-west'),
    ('user3@example.com', 'Chicago', 'Johnson', 'James', ARRAY['312-555-0103'], 'us-central');
```

## Verify Regional Distribution
===

1. First, take a look at the newly inserted data.

```sql,run
  SELECT crdb_region,
    email,
    city,
    last_name,
    first_name,
    phone_numbers
  FROM movr_users.users LIMIT 10;
```

2, Next, let's check how our ranges are distributed among leaseholders:

```sql,run
WITH user_info AS (SHOW RANGES FROM TABLE movr_users.users WITH DETAILS)
SELECT range_id, lease_holder, lease_holder_locality
FROM user_info;
```

> [!WARNING]
> Wait 30-60 seconds after the locality change to see the full effect of the redistribution.

## Summary
===

In this exercise, you:

**Configured Row-Level Optimization**
- Modified an existing table to use regional by row settings
- Added the `crdb_region` column for locality control
- Loaded geographically distributed test data
- Verified row-level locality settings

**Key Learnings**
- Regional by row tables optimize data access at the row level
- Each row can have its own optimized location
- The `crdb_region` column controls row locality
- Leaseholders are distributed based on row settings

> [!NOTE]
> - Use regional by row when data access patterns align with geographic regions
> - Monitor leaseholder distribution to verify proper configuration
> - Consider data access patterns when choosing region values
