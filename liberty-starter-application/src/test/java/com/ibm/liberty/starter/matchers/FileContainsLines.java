/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.liberty.starter.matchers;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.Matchers.containsInRelativeOrder;

/**
 * A matcher that matches the lines in a file using a delegate matcher.
 */
public class FileContainsLines extends BaseMatcher<File> implements Matcher<File> {

    private final Matcher<Iterable<? extends String>> lineMatchingDelegate;

    public FileContainsLines(Matcher<Iterable<? extends String>> lineMatchingDelegate) {
        super();
        this.lineMatchingDelegate = lineMatchingDelegate;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a file with lines that are an ");
        lineMatchingDelegate.describeTo(description);
    }

    @Override
    public final boolean matches(Object file) {
        List<String> lines = readLinesSafely(file, Description.NONE);
        if (lines == null) {
            return false;
        } else {
            return lineMatchingDelegate.matches(lines);
        }
    }

    @Override
    public void describeMismatch(Object file, Description description) {
        List<String> lines = readLinesSafely(file, description);
        if (lines != null) {
            lineMatchingDelegate.describeMismatch(lines, description);
            description.appendText(" in file ");
            description.appendValue(file);
            description.appendText(" with lines ");
            description.appendValue(readLinesSafely(file, Description.NONE));
        }
    }

    /**
     * Safely read the lines in a {@link File} and if unable to do so add a reason to the <code>mismatchDescription</code> and return <code>null</code>
     */
    private List<String> readLinesSafely(Object file, Description mismatchDescription) {
        if (file == null) {
            mismatchDescription.appendText("was null");
            return null;
        }
        if (!(file instanceof File)) {
            mismatchDescription.appendText("was a " + file.getClass().getName() + " and not a file");
            return null;
        }
        try {
            return Files.readAllLines(((File) file).toPath());
        } catch (IOException e) {
            e.printStackTrace();
            mismatchDescription.appendText("was unable to read file due to the IOException: " + e.getMessage());
            return null;
        }
    }

    /**
     * <p>Creates a matcher for {@link File}s that matches when a single pass over the lines in the File that each satisfying the corresponding matcher in the
     * specified matchers, in the same relative order. For example:</p>
     * <code>
     * // File containing "line1\nline2\nline3\nline4\line5"<br/>
     * File file = new File(someFile.txt);<br/>
     * assertThat(file, containsLinesInRelativeOrder(equalTo("line2"), equalTo("line4")));</code>
     * 
     * @param lineMatchers the lines that must be contained within the File in the same relative order
     */
    @SafeVarargs
    public static Matcher<File> containsLinesInRelativeOrder(Matcher<String>... lineMatchers) {
        return new FileContainsLines(containsInRelativeOrder(lineMatchers));
    }

}