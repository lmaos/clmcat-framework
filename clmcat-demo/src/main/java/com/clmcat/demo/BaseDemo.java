package com.clmcat.demo;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;
import com.clmcat.basics.commons.util.Base36;
import com.clmcat.basics.commons.util.Base62;


import java.util.concurrent.TimeUnit;

public class BaseDemo {

    public static void main(String[] args) {
        long timeMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365);
        CustomSnowflake snowflake = SnowflakeCustomBuilder.builder()
                .add(0)
                .add("timeStrategy", 42, TimeStrategy.millisecond(timeMillis))
                .add(10, MachineStrategy.autoByIp())
                .add(11, SequenceStrategy.create(), "timeStrategy").build();


        for (int i = 0; i < 100; i++) {
            long nextId = snowflake.nextId();
            System.out.println(nextId + "," + Base36.encode(nextId) + "," + Base62.encode(nextId));
        }


        System.out.println(Base36.encode(999999999 ));
    }
}
