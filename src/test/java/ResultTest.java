import hu.levente.prog3.util.Result;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ellenőrzi a {@link Result.Success} és {@link Result.Failure} példányok típushelyességét a tárolt érékek, illetve
 * hibák visszanyerését valamint a callback metódusokat is.
 * */
public class ResultTest {

    @Test
    void testSuccess() {
        Result<String, Integer> result = Result.success("Sikeres");

        assertInstanceOf(Result.Success.class, result);
        assertEquals("Sikeres", ((Result.Success<String, Integer>) result).value());

        AtomicReference<String> capturedValue = new AtomicReference<>();
        AtomicBoolean failureCalled = new AtomicBoolean(false);

        result.ifSuccess(capturedValue::set);
        result.ifFailure(err -> failureCalled.set(true));

        assertEquals("Sikeres", capturedValue.get());
        assertFalse(failureCalled.get());
    }

    @Test
    void testFailure() {
        IllegalStateException exception = new IllegalStateException("Hiba");
        Result<String, Exception> result = Result.failure(exception);

        assertInstanceOf(Result.Failure.class, result);
        assertSame(exception, ((Result.Failure<String, Exception>) result).error());

        AtomicBoolean successCalled = new AtomicBoolean(false);
        AtomicReference<Exception> failureCalled = new AtomicReference<>();

        result.ifSuccess(val -> successCalled.set(true));
        result.ifFailure(failureCalled::set);

        assertFalse(successCalled.get());
        assertSame(exception, failureCalled.get());
    }
}
