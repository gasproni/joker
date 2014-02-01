package com.asprotunity.joker.internal.protocol;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonCallEncoderTest {

    private JsonCallEncoder jsonCallEncoder;

    @Before
    public void setUp() throws Exception {
        jsonCallEncoder = new JsonCallEncoder(JsonParserBuilder.build(null));
    }

    @Test
    public void convertsServiceCallToJsonAndBack() {

        Object[] callParameters = new Object[]{"aString", 1, true};

        String jsonCallParameters = jsonCallEncoder.encode(callParameters);

        assertThat(jsonCallParameters, is("[\"aString\",1,true]"));

        Object[] fromJsonCallParameters =
                jsonCallEncoder.decode(jsonCallParameters, new Type[]{String.class, int.class, boolean.class});

        assertThat(fromJsonCallParameters, equalTo(callParameters));
    }


    @Test
    public void convertsCallResultToJsonAndBack() {

        Double[] callResult = new Double[]{1.0, 2.0};

        String jsonCallResult = jsonCallEncoder.encode(callResult);

        assertThat(jsonCallResult, is("[1.0,2.0]"));

        Double[] fromJsonCallResult = jsonCallEncoder.decode(jsonCallResult, Double[].class);
        assertThat(callResult, equalTo(fromJsonCallResult));

    }
}
