package com.example.airefiner.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefinedContentProcessorTest {

    private RefinedContentProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new RefinedContentProcessor();
    }

    @Test
    void shouldReturnFallbackForNull() {
        assertEquals("No summary available", processor.process(null));
    }

    @Test
    void shouldReturnFallbackForBlank() {
        assertEquals("No summary available", processor.process("   \n\t"));
    }

    @Test
    void shouldTrimAndCollapseWhitespace() {
        String input = "   This   is   a   test   ";
        String expected = "This is a test";
        assertEquals(expected, processor.process(input));
    }

    @Test
    void shouldRemoveSimpleMarkdownArtifacts() {
        String input = "*Bold* #Header `code` >quote";
        String expected = "Bold Header code quote";
        assertEquals(expected, processor.process(input));
    }

    @Test
    void shouldLeaveValidContentUntouched() {
        String input = "A well‑formed summary without markdown.";
        assertEquals(input, processor.process(input));
    }
}
