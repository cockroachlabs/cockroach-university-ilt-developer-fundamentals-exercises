---
slug: init-cluster
id: wq4qg35bcafz
type: challenge
title: 01 Initialize multi-region cluster
teaser: Initialize a 9 node multi-region cluster
notes:
- type: text
  contents: Practice initializing a multi-region cluster
tabs:
- id: ujlhere96t1f
  title: terminal
  type: terminal
  hostname: ilt-lab-vm
- id: 7erwy7j9cl1w
  title: DB Console
  type: service
  hostname: node1
  path: '#/overview/list'
  port: 8443
difficulty: ""
enhanced_loading: null
---

# **Configuring a Multi-Region Cluster**

Before you can create multi-region databases in CockroachDB, you must first have a properly configured multi-region cluster. This is a crucial foundation that requires setting the `region` attribute in node localities during cluster initialization.

## **Why Multi-Region Clusters Matter**

A multi-region cluster is the foundation for CockroachDB's advanced multi-region capabilities. When you configure a cluster with region information:
- CockroachDB can make intelligent decisions about data placement
- The cluster understands geographic boundaries for replica placement
- You unlock access to high-level multi-region abstractions in SQL
- The system can optimize for regional latency and survival goals

Without properly configured region localities, you cannot:
- Create multi-region databases
- Set regional survivability goals
- Use regional table placement
- Optimize for global performance

## **Learning Objectives**
After completing this exercise, you will be able to:
- Deploy a multi-region CockroachDB cluster
- Configure proper region and zone localities
- Verify multi-region capability
- Prepare your cluster for multi-region database creation

## **Set up Localities for a Multi-Region Cluster**
===

This exercise involves setting up a 9-node CockroachDB cluster distributed across three regions: `us-east`, `us-central`, and `us-west`. Each region contains three availability zones: `a`, `b`, and `c`. The locality hierarchy of `region` and `zone` provides the geographic awareness needed for advanced multi-region features.

1. **Node Distribution**:
   - **us-east**: `node1`, `node2`, `node3`
   - **us-central**: `node4`, `node5`, `node6`
   - **us-west**: `node7`, `node8`, `node9`

2. **Environment Variables**:
   Define node groups for each region.

   ```bash,run
   export US_EAST_NODES=("node1" "node2" "node3")
   export US_CENTRAL_NODES=("node4" "node5" "node6")
   export US_WEST_NODES=("node7" "node8" "node9")
   export REGIONS=("us-east" "us-central" "us-west")
   ```

3. **Availability Zones**:
   Assign each node to an availability zone (`a`, `b`, `c`), cycling through zones.

   ```bash,run
   export ZONES=("a" "b" "c")
   ```

## **Start Nodes in the Cluster**
===

Each set of nodes will be started with locality flags, region, and zone assignments.

### **1. Start `US East` Nodes**

Run the following loop to start the `us-east` nodes with their specific localities:

```bash,run
for i in "${!US_EAST_NODES[@]}"; do
    node="${US_EAST_NODES[i]}"
    zone="${ZONES[i]}"
    ssh ${node} "nohup cockroach start \
      --certs-dir=/root/certs \
      --advertise-addr=${node}:26257 \
      --http-addr=0.0.0.0:8443 \
      --join=node1:26257,node2:26257,node3:26257 \
      --locality=region=us-east,zone=${zone} \
      --cache=.25 \
      --max-sql-memory=.25 \
      --background \
      > /tmp/start_${node}.out 2> /tmp/start_${node}.err < /dev/null"
done
```

### **2. Start `US Central` Nodes**

Repeat the process for `us-central` nodes:

```bash,run
for i in "${!US_CENTRAL_NODES[@]}"; do
    node="${US_CENTRAL_NODES[i]}"
    zone="${ZONES[i]}"
    ssh ${node} "nohup cockroach start \
      --certs-dir=/root/certs \
      --advertise-addr=${node}:26257 \
      --http-addr=0.0.0.0:8443 \
      --join=node1:26257,node2:26257,node3:26257,node4:26257,node5:26257 \
      --locality=region=us-central,zone=${zone} \
      --cache=.25 \
      --max-sql-memory=.25 \
      --background \
      > /tmp/start_${node}.out 2> /tmp/start_${node}.err < /dev/null"
done
```

### **3. Start `US West` Nodes**

Start the `us-west` nodes:

```bash,run
for i in "${!US_WEST_NODES[@]}"; do
    node="${US_WEST_NODES[i]}"
    zone="${ZONES[i]}"
    ssh ${node} "nohup cockroach start \
      --certs-dir=/root/certs \
      --advertise-addr=${node}:26257 \
      --http-addr=0.0.0.0:8443 \
      --join=node1:26257,node2:26257,node3:26257,node4:26257,node5:26257,node6:26257 \
      --locality=region=us-west,zone=${zone} \
      --cache=.25 \
      --max-sql-memory=.25 \
      --background \
      > /tmp/start_${node}.out 2> /tmp/start_${node}.err < /dev/null"
done
```

## **Initialize the cluster**
===

1. Run this command to initialize the cluster:
```bash,run
cockroach init --certs-dir=/root/certs --host=node1:26257
```

2. Run the following to configure HAProxy:

```bash,run
# Generate HAProxy configuration file for 9-node cluster
cockroach gen haproxy \
    --certs-dir=/root/certs \
    --host=node1 \
    --port=26257 \
    --out=/tmp/haproxy.cfg

# Transfer HAProxy config to the HAProxy node
scp /tmp/haproxy.cfg haproxy:/etc/haproxy/haproxy.cfg

# Restart or start HAProxy on the HAProxy node
ssh haproxy "sudo systemctl restart haproxy || sudo systemctl start haproxy"

```

3. Create a users for DB Console:
```bash,run
cockroach sql --certs-dir=/root/certs --host=haproxy --execute $'CREATE USER demo with password \'movr\';'

cockroach sql --certs-dir=/root/certs --host=haproxy --execute $'ALTER USER root with password \'pass\';'
```

## **Verify the Cluster**
===

1. **View all regions** assigned to this cluster:
   ```bash,run
   cockroach sql --certs-dir=/root/certs --host=haproxy:26257 -e "SHOW REGIONS FROM CLUSTER"
   ```

2. **Check Localities**:
   Verify that all nodes have been assigned the correct locality:
   ```bash,run
   cockroach node status --certs-dir=/root/certs --host=haproxy:26257
   ```

## **Summary**
===
In this exercise, you:

**Built Multi-Region Foundation**
- Deployed a 9-node CockroachDB cluster across three geographic regions
- Configured proper region and zone localities for each node
- Set up HAProxy load balancing for cluster access
- Verified all nodes joined with correct locality settings

**Configured Locality Hierarchy**
- Established region-level organization (us-east, us-central, us-west)
- Implemented zone-level distribution (a, b, c) within each region
- Created the geographic topology needed for advanced features
- Verified region recognition at the cluster level

**Learned Best Practices**
- Used environment variables for organized node management
- Implemented consistent locality naming conventions
- Applied proper join-string configuration for node coordination
- Followed systematic node startup procedures

This foundational setup is critical because:
- Region localities enable CockroachDB's multi-region features
- Proper configuration unlocks advanced survivability options
- Geographic awareness enables intelligent data placement
- Future exercises will build on this regional architecture

