package org.bid;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertiesLoaderTest {

    PropertiesLoader pl;

    @Before
    public void setUp() {
        pl = new PropertiesLoader();
    }

    @Test
    public void testAddition() {
        assertEquals(pl.user, "GojoSatoru");
    }

}