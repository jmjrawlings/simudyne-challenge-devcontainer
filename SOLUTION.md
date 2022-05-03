# SOLUTION: Simudyne Hunger Games

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

### v1: Group together all runs

Group together all the different segments comprising a `step()` in the `Factory` class.

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

### v2: Disable Unnecessary Loggers
In addition to changes of v1, disable Loggers from everywhere except `Factory` class. This should in principle free up a little more additional resources.

### v3: Discretization and Split

#### Discretization
Discretize means that now each tick will be `K` times faster, where `K` is a parameter that is chosen so that there is a good balance between the end result (as measured by the output KPIs in the console) and run time. The benchmark is the number of products done with no discretization applied `(K=1, NUMPRODUCTSDONE.HIGH=1.32K)`

Originally I tried `K`=0.1/0.002223 = `45ms`. The assumption was that since each product is 0.1 meters and the speed is 0.002223 m/s, I could consider this as a basic discrete step.

The problem with using `K=45ms` is that the cycle time of a product in a machine is 10ms, which means some products will take this amount of time to be machined. As a result, machines will not be able to notify upstream conveyors after 10ms (they would need to wait until the whole 45ms step is over). Therefore, the factory will have a lower throughput. This was observed in the Console `(K=45, NUMPRODUCTSDONE.HIGH=903)`. Hence, we should at least reduce to `K=10 ms`.

However, using `K=10ms` still did not produce an acceptable throughput `(K=10, NUMPRODUCTSDONE.HIGH=1.21K)`. That's because  basic 45ms that it takes for a product to take the slot of a previous product will sill produce multiples of 5. Hence, we came up with `K=5ms` which produces a throughput that is quite close to the benchmark `(K=5, NUMPRODUCTSDONE.HIGH=1.26K)` and reduces the execution time roughly 1/5.

**NOTE:** When using discretization, you should now reduce the Model lenght (ticks) by `K`. So, for `K=5` insted of using 10,000 ticks if we use 2,000 ticks.

#### Split

In this version we additionally introduce parallelization of two calls: `Machine.receiveProductForWork()` // `Conveyor.advanceAllProducts()` using `split()`. The intuition is that these two steps can be run independently and therefore in parallel, which should result in further reduction in run time.

        run(
                // 1. machine finishes product -> push downstream AND flag upstream that you have space (must be in 1 func)
                Machine.pushDownstreamAndFlagUpstream(),

                // 2. conveyors: get products from upstream machine. Push out oldest if downstream is free (must be in 1 func)
                Conveyor.receiveProductAndPushOutOldest(),
                // 3. machine receives product from upstream for next tick
                Split.create(Machine.receiveProductForWork(),
                Conveyor.advanceAllProducts()),
                Conveyor.addNewProducts()
        );

## Results
Pasted below are the run times experienced on my computer (Apple M1 Pro 16GB Memory) and docker configuration (`VARIANT=16-bullseye`, with the following allocated resources: 6 CPUs, 14GB RAM, 3GB Swap, 200GB Disk image size).

The reduction goal in run time was "~50%".

### v0: Baseline (10,000 ticks)
    2022-05-02 10:51:24,993 [INFO] [Simudyne-akka.actor.default-dispatcher-12] org.example.models.factory - Total Run time = 11879
    2022-05-02 10:53:20,701 [INFO] [Simudyne-akka.actor.default-dispatcher-6] org.example.models.factory - Total Run time = 8938
    2022-05-02 10:55:11,118 [INFO] [Simudyne-akka.actor.default-dispatcher-7] org.example.models.factory - Total Run time = 9102
    2022-05-02 10:57:53,346 [INFO] [Simudyne-akka.actor.default-dispatcher-11] org.example.models.factory - Total Run time = 8431

**Average:** 9587.5 ms

### v1: Group together all runs (10,000 ticks)
    2022-05-02 11:01:29,751 [INFO] [Simudyne-akka.actor.default-dispatcher-6] factory - Total Run time = 6305
    2022-05-02 11:03:53,962 [INFO] [Simudyne-akka.actor.default-dispatcher-12] factory - Total Run time = 5523
    2022-05-02 11:06:17,350 [INFO] [Simudyne-akka.actor.default-dispatcher-12] factory - Total Run time = 4553
    2022-05-02 11:08:17,317 [INFO] [Simudyne-akka.actor.default-dispatcher-13] factory - Total Run time = 5059

**Average:** 5360 ms

**Run Time reduction vs baseline:** 44.1%

### v2: Disable Unnecessary Loggers (10,000 ticks)
    2022-05-02 11:14:42,646 [INFO] [Simudyne-akka.actor.default-dispatcher-10] factory - Total Run time = 5808
    2022-05-02 11:18:33,369 [INFO] [Simudyne-akka.actor.default-dispatcher-6] factory - Total Run time = 4430
    2022-05-02 11:20:22,861 [INFO] [Simudyne-akka.actor.default-dispatcher-5] factory - Total Run time = 5034
    2022-05-02 11:38:14,455 [INFO] [Simudyne-akka.actor.default-dispatcher-12] factory - Total Run time = 4619

**Average:** 4972.8 ms

**Run Time reduction vs baseline:** 48.13%%

### v3: Discretization and Split (K=5, 2,000 ticks)
        2022-05-03 05:52:41,745 [INFO] [Simudyne-akka.actor.default-dispatcher-16] factory - Total Run time = 1395
        2022-05-03 05:55:22,883 [INFO] [Simudyne-akka.actor.default-dispatcher-9] factory - Total Run time = 1386
        2022-05-03 05:56:01,509 [INFO] [Simudyne-akka.actor.default-dispatcher-10] factory - Total Run time = 1328
        2022-05-03 05:56:16,074 [INFO] [Simudyne-akka.actor.default-dispatcher-5] factory - Total Run time = 1344

**Average:** 1363.3ms

**Run Time reduction vs baseline:** 85.78%