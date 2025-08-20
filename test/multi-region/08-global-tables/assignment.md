---
slug: global-tables
id: ns2mzsrhopjt
type: challenge
title: 07 Implement Global Tables for Promotion Codes
teaser: Learn how to use global tables for data that needs fast reads from all regions
notes:
- type: text
  contents: |-
    In this challenge, you'll learn how to:
    * Identify use cases for global tables
    * Configure global table settings
    * Verify global read optimizations
tabs:
- id: viktdr0bumzt
  title: Terminal
  type: terminal
  hostname: ilt-lab-vm
difficulty: basic
enhanced_loading: null
---

# Global Tables for Promotion Codes

In this challenge, you'll help MovR implement a promotions feature that provides fast read access from any region.

## Learning Objectives

By completing this exercise, you'll learn:
- When to use global tables
- How to configure global table settings
- How to verify global read optimizations
- Best practices for globally accessed data

## The Story
===

MovR is implementing a new pricing engine service that includes promotional codes. Their requirements:
- Promotions run for 1 week, planned 4 weeks in advance
- New codes are created only once a month
- Users from any region need fast read access to codes
- Initial promotion codes are not region-specific

The `movr_pricing` database already has these multi-region settings:
```sql
ALTER DATABASE movr_pricing SET PRIMARY REGION "us-east";
ALTER DATABASE movr_pricing ADD REGION "us-central";
ALTER DATABASE movr_pricing ADD REGION "us-west";
ALTER DATABASE movr_pricing SURVIVE REGION FAILURE;
```

## 1. Create the Promotions Table
===

1. Open [button label="Terminal" background="#6935ff"](tab-0) and connect to the database:
```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy --port=26257
```

2. Create a table to store promotion codes:

```sql,run
CREATE TABLE movr_pricing.promo_codes (
    code STRING,
    description STRING,
    creation_time TIMESTAMPTZ,
    expiration_time TIMESTAMPTZ,
    rules JSONB
);
```

## 2. Configure Global Table Settings
===

By default, the table inherits the database's regional settings. Let's optimize it for global reads:

```sql,run
ALTER TABLE movr_pricing.promo_codes SET LOCALITY GLOBAL;
```

> [!NOTE]
> Global tables are optimized for fast reads from any region, making them perfect for data that rarely changes but is frequently read from multiple regions.

## 3. Verify Configuration
===

Check that the table is properly configured:

```sql,run
SHOW ZONE CONFIGURATION FOR TABLE movr_pricing.promo_codes;
```

Look for these key settings in the output:
- `global_reads = true`
- `num_replicas = 5`
- `num_voters = 5`

> [!NOTE]
> Note the number of voters is 5. You can see that this is a table with `REGION` survival goal.
>
> Use this command to see the survival goals for each of the `movr` tables:
> ```sql,run
> SELECT database_name, primary_region, regions, survival_goal FROM [SHOW DATABASES] WHERE
>                        database_name ILIKE '%movr_%';
> ```

## Summary
===

In this exercise, you:

**Created and Configured a Global Table**
- Created the promo_codes table for the pricing service
- Modified table locality to optimize for global reads
- Verified global read configurations

**Key Learnings**
- Global tables provide fast reads from all regions
- Best for data that changes infrequently
- Ideal for reference data needed across regions
- Requires more storage due to full replication

> [!NOTE]
> - Use global tables when data needs to be read quickly from multiple regions
> - Perfect for reference data that changes infrequently
> - Consider storage implications of full replication
