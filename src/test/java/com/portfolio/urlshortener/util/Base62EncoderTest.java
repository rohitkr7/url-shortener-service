package com.portfolio.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Base62EncoderTest {

    @Test
    @DisplayName("Should return '0' for zero and negative inputs")
    void testZeroAndNegativeValues() {
        assertEquals("0", Base62Encoder.encode(0));
        assertEquals("0", Base62Encoder.encode(-1));
        assertEquals("0", Base62Encoder.encode(-100));
    }

    @Test
    @DisplayName("Should correctly encode single-digit boundary values")
    void testSingleDigitBoundaries() {
        assertEquals("1", Base62Encoder.encode(1));
        assertEquals("9", Base62Encoder.encode(9));
        assertEquals("a", Base62Encoder.encode(10));
        assertEquals("z", Base62Encoder.encode(35));
        assertEquals("A", Base62Encoder.encode(36));
        assertEquals("Z", Base62Encoder.encode(61));
    }

    @Test
    @DisplayName("Should correctly encode multi-digit rollover values")
    void testMultiDigitRollovers() {
        assertEquals("10", Base62Encoder.encode(62));
        assertEquals("11", Base62Encoder.encode(63));
        assertEquals("4c92", Base62Encoder.encode(1000000));
    }
}
