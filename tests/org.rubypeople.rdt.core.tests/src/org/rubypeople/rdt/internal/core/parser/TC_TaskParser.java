package org.rubypeople.rdt.internal.core.parser;


import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.resources.IMarker;
import org.rubypeople.rdt.core.RubyCore;

public class TC_TaskParser extends TestCase {

	private static final String BASIC_MESSAGE      = "#TASK test";
	private static final String BASIC_MESSAGE_EXPECTED = "TASK test";
	private static final String ALTERNATE_MESSAGE  = "#CHORE test";
	private static final String ALTERNATE_MESSAGE_EXPECTED  = "CHORE test";
    private Map preferences;
    private TaskParser parser;

    public void setUp() {
        preferences = new HashMap();
        preferences.put(RubyCore.COMPILER_TASK_TAGS, "TASK");

        parser = new TaskParser(preferences);
    }

    public void testSimpleTag() {
        List tasks = parseTasks("TASK", BASIC_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED, 1, 1, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
	}

    public void testIndentedTag() {
        List tasks = parseTasks("TASK", "x"+BASIC_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED, 2, 1, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testCaseInsensitive() {
        preferences.put(RubyCore.COMPILER_TASK_CASE_SENSITIVE, RubyCore.DISABLED);
        List tasks = parseTasks("TASK", "x"+BASIC_MESSAGE.toLowerCase());
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED.toLowerCase(), 2, 1, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testLineEndingCRLF() {
        List tasks = parseTasks("TASK", "\r\n" + BASIC_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED, 3, 2, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testLineEndingLF() {
        List tasks = parseTasks("TASK", "\n" + BASIC_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED, 2, 2, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testLineEndingCR() {
        List tasks = parseTasks("TASK", "\r" + BASIC_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(BASIC_MESSAGE_EXPECTED, 2, 2, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testLoadFromReader() throws Exception {
        StringReader reader = new StringReader(BASIC_MESSAGE + '\n');
        preferences.put(RubyCore.COMPILER_TASK_TAGS, "TASK");
        parser = new TaskParser(preferences);
        
        List tasks = parser.getTasks(reader); 

        assertEquals(1, tasks.size());
        assertTask("TASK test", 1, 1, IMarker.PRIORITY_NORMAL, (TaskTag) tasks.get(0));
    }

    public void testDifferentTag() {
        List tasks = parseTasks("TASK,CHORE", ALTERNATE_MESSAGE);
        assertEquals(1, tasks.size());
        assertTask(ALTERNATE_MESSAGE_EXPECTED, 1, 1, IMarker.PRIORITY_HIGH, (TaskTag) tasks.get(0));
    }
    
    public void testDoNotMatch() {
    	final String LINE = "puts 'TASK'";
    	List tasks = parseTasks("TASK", LINE);
    	assertEquals("Should not match " + LINE, 0, tasks.size());
    }

    private List parseTasks(String validTags, String input) {
        preferences.put(RubyCore.COMPILER_TASK_TAGS, validTags);
        parser = new TaskParser(preferences);

        List tasks = parser.getTasks(input);
        return tasks;
    }

    private void assertTask(String expectedMessage, int expectedStart, int expectedLineNumber, int expectedPriority, TaskTag actualTask) {
        assertEquals("Text",        expectedMessage,                  actualTask.getMessage());
        assertEquals("Start",       expectedStart,                    actualTask.getSourceStart());
        assertEquals("End",         expectedStart + expectedMessage.length(), actualTask.getSourceEnd());
        assertEquals("Line number", expectedLineNumber,               actualTask.getSourceLineNumber());
        assertEquals("Priority",    expectedPriority,          actualTask.getPriority());
    }
}
