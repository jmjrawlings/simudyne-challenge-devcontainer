# Simudyne Hunger Games

## Baseline
There is a significant variation in total run time for executions using the exact environment, same code, scenario and settings. This is very strange because the code uses a seed to generate randomness, so in principle it should have a deterministic run time. It happens both using it in Docker and from my local machine.

To work around this problem, I ran each code variation several consecutive times. Normally the first time you get a longer run time, and after that it tends to converge. I run each variation 4 times.

I used a long model length (10,000 ticks) instead of the default 60 to try to reduce that variation even more.

Each run was executed using `mvn clean compile exec:java`.

The values seen in the Console at the last step (10,000) pretty much remain constant:

**QUEUELENGTH**

*MEAN:* 5.95K

*HIGH:* 6K

*LOW:* 0

**NUMPRODUCTSDONE**

*MEAN:* 662.88

*HIGH:* 1.32K

*LOW:* 0

## Changes made

### v1

Group together all the different basic Segments comprising a `step()` in the `Factory` class.

Instead of this:

        run(
                // 1. machine finishes product -> push downstream AND flag upstream that you have space (must be in 1 func)
                Machine.pushDownstreamAndFlagUpstream(),

                // 2. conveyors: get products from upstream machine. Push out oldest if downstream is free (must be in 1 func)
                Conveyor.receiveProductAndPushOutOldest(),
                // 3. machine receives product from upstream for next tick
                Machine.receiveProductForWork()
        );

        run( // after adjusting conveyor queues, move all products by conveyor speed
                Conveyor.advanceAllProducts()
        );

        run( // let new products enter the system regularly
                Conveyor.addNewProducts()
        );

We do this:

        run(
                // 1. machine finishes product -> push downstream AND flag upstream that you have space (must be in 1 func)
                Machine.pushDownstreamAndFlagUpstream(),
                // 2. conveyors: get products from upstream machine. Push out oldest if downstream is free (must be in 1 func)
                Conveyor.receiveProductAndPushOutOldest(),
                // 3. machine receives product from upstream for next tick
                Machine.receiveProductForWork(),
                Conveyor.advanceAllProducts(),
                Conveyor.addNewProducts()
        );

### v2
In addition to changes of v1, disable Loggers from everywhere except `Factory` class. This should in principle free up a little more additional resources.

## Results
Pasted below are the run times experienced on my computer (Apple M1 Pro 16GB Memory) and docker configuration (`VARIANT=16-bullseye`, with the following allocated resources: 6 CPUs, 14GB RAM, 3GB Swap, 200GB Disk image size).

The reduction in run time is **48.13%** (the goal was "~50% FIRST").

### Baseline
    2022-05-02 10:51:24,993 [INFO] [Simudyne-akka.actor.default-dispatcher-12] org.example.models.factory - Total Run time = 11879
    2022-05-02 10:53:20,701 [INFO] [Simudyne-akka.actor.default-dispatcher-6] org.example.models.factory - Total Run time = 8938
    2022-05-02 10:55:11,118 [INFO] [Simudyne-akka.actor.default-dispatcher-7] org.example.models.factory - Total Run time = 9102
    2022-05-02 10:57:53,346 [INFO] [Simudyne-akka.actor.default-dispatcher-11] org.example.models.factory - Total Run time = 8431

**Average:** 9587.5 ms

### Modified v1 (just grouping together all runs)
    2022-05-02 11:01:29,751 [INFO] [Simudyne-akka.actor.default-dispatcher-6] org.example.models.factory - Total Run time = 6305
    2022-05-02 11:03:53,962 [INFO] [Simudyne-akka.actor.default-dispatcher-12] org.example.models.factory - Total Run time = 5523
    2022-05-02 11:06:17,350 [INFO] [Simudyne-akka.actor.default-dispatcher-12] org.example.models.factory - Total Run time = 4553
    2022-05-02 11:08:17,317 [INFO] [Simudyne-akka.actor.default-dispatcher-13] org.example.models.factory - Total Run time = 5059

**Average:** 5360 ms

### Modified v2 (disable Loggers from everywhere except Factory class)
    2022-05-02 11:14:42,646 [INFO] [Simudyne-akka.actor.default-dispatcher-10] org.example.models.factory - Total Run time = 5808
    2022-05-02 11:18:33,369 [INFO] [Simudyne-akka.actor.default-dispatcher-6] org.example.models.factory - Total Run time = 4430
    2022-05-02 11:20:22,861 [INFO] [Simudyne-akka.actor.default-dispatcher-5] org.example.models.factory - Total Run time = 5034
    2022-05-02 11:38:14,455 [INFO] [Simudyne-akka.actor.default-dispatcher-12] org.example.models.factory - Total Run time = 4619

**Average:** 4972.8 ms
