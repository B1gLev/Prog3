import hu.levente.prog3.parser.SourceCursor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ellenőrzi a határétékeket, az előretekintést és a visszatekintést is, valamint a szövegrészek pontos kinyerését.
 * */
public class SourceCursorTest {

    @Test
    void testNullAndEmptySource() {
        // Null bemenettel nem dobhat kivételt
        SourceCursor sourceCursor = new SourceCursor(null);
        assertTrue(sourceCursor.isAtEnd());
        assertEquals('\0', sourceCursor.peek());
        assertEquals('\0', sourceCursor.peekNext());
        assertEquals('\0', sourceCursor.advance());
        assertEquals('\0', sourceCursor.previous());
        assertEquals(0, sourceCursor.pos());

        SourceCursor sourceCursor2 = new SourceCursor("");
        assertTrue(sourceCursor2.isAtEnd());
        assertEquals('\0', sourceCursor2.peek());
    }

    @Test
    void testSubstringAndTraverse() {
        SourceCursor sourceCursor = new SourceCursor("abc");

        // Kezdőállapot ellenörzése az első karakter előtt
        assertFalse(sourceCursor.isAtEnd());
        assertEquals('a', sourceCursor.peek());
        assertEquals('b', sourceCursor.peekNext());
        assertEquals('\0', sourceCursor.previous());

        // 'a' utána a kurzor 'b' karakterre lép
        assertEquals('a', sourceCursor.advance());
        assertEquals(1, sourceCursor.pos());
        assertEquals('a', sourceCursor.previous());
        assertEquals('b', sourceCursor.peek());
        assertEquals('c', sourceCursor.peekNext());

        // 'b' után az utolsó karakternél a 'c' utána már '\0'-t ad
        assertEquals('b', sourceCursor.advance());
        assertEquals('c', sourceCursor.peek());
        assertEquals('\0', sourceCursor.peekNext());

        // Az eddig bejárt szövegrész kinyerése
        assertEquals("ab", sourceCursor.extractSubstring(0));

        // Az utolsó karakter után eléri az EOF-ot
        assertEquals('c', sourceCursor.advance());
        assertTrue(sourceCursor.isAtEnd());
        assertEquals('\0', sourceCursor.peek());
        assertEquals('\0', sourceCursor.peekNext());
        assertEquals("abc", sourceCursor.extractSubstring(0));
    }
}
