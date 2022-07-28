/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022 International Association of Oil and Gas Producers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.iogp.gigs.generator;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.NoSuchElementException;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



/**
 * Container for a {@code GIGSTestDataset} file, which is read from a CSV file.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class DataParser {
    /**
     * Path to the root directory of data.
     * Shall be specified by a {@code GIGS_DATA} environment variable.
     */
    private static final String PATH_TO_DATA = System.getenv("GIGS_DATA");

    /**
     * The character used as column separator.
     */
    private static final char COLUMN_SEPARATOR = '\t';

    /**
     * The separator for elements in a list.
     */
    private static final char LIST_ELEMENT_SEPARATOR = ';';

    /**
     * The character to use as a separator between the lower and the upper value in a range.
     * Example: {@code "16362-16398"}.
     */
    private static final char RANGE_SEPARATOR = '-';

    /**
     * The character to use for specifying a step after a range.
     * Example: {@code "16362-16398 +2"}.
     */
    private static final char STEP_PREFIX = '+';

    /**
     * The character used for quoting strings. The column separator
     * can be used as an ordinary character inside the quoted string.
     *
     * Quotes that need to be escaped shall be doubled.
     */
    private static final char QUOTE = '"';

    /**
     * The {@code GIGSTestDataset} content.
     */
    private final List<Object[]> content;

    /**
     * The current row. This is the value of {@link #content} at index {@link #cursor}.
     */
    private Object[] currentRow;

    /**
     * The current cursor position.
     */
    private int cursor = -1;

    /**
     * Loads the data from the given file.
     *
     * @param series        the series to which the test belong.
     * @param file          the file name, without path.
     * @param columnTypes   the type of each column. The only legal values at this time are
     *                      {@link String}, {@link Integer}, {@link Double} and {@link Boolean}.
     *                      The {@code null} value means that a column will not be used.
     * @throws IOException if an error occurred while reading the test data.
     */
    public DataParser(final Series series, final String file, final Class<?>... columnTypes) throws IOException {
        if (PATH_TO_DATA == null) {
            throw new IllegalStateException("The GIGS_DATA environment variable "
                    + "must be set to the root directory of GIGSTestDataset.");
        }
        final Path path = Paths.get(PATH_TO_DATA).resolve("GIGSTestDatasetFiles")
                .resolve(series.directory).resolve("ASCII").resolve(file);
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            content = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = trim(line, 0, line.length());
                if (!line.isBlank() && line.charAt(0) != '#') {
                    content.add(parseRow(line, columnTypes));
                }
            }
        }
    }

    /**
     * Creates a parser with the given content.
     * Used for testing purpose only.
     *
     * @param  content  the rows.
     */
    DataParser(final List<Object[]> content) {
        this.content = content;
    }

    /**
     * Returns a sub-string with leading and trailing white spaces removed.
     * This method does not remove tabulations, because they are used as column separator.
     *
     * @param  item   the string to trim.
     * @param  start  index of the first character to keep, ignoring spaces.
     * @param  end    index after the last character to keep, ignoring spaces.
     * @return {@code item.substring(start, end)} with spaces trimmed.
     */
    private static String trim(final String item, int start, int end) {
        int c;
        while (end > 0 && Character.isSpaceChar(c = item.codePointBefore(end))) {
            end -= Character.charCount(c);
        }
        while (start < end && Character.isSpaceChar(c = item.codePointAt(start))) {
            start += Character.charCount(c);
        }
        return item.substring(start, end);
    }

    /**
     * Parses a single row. The given line must be non-empty.
     *
     * @param  line          the line to parse.
     * @param  columnTypes   the type of each column.
     * @return values as objects of the types specified in {@code columnTypes}.
     * @throws IOException if an error occurred while reading the test data.
     */
    static Object[] parseRow(String line, final Class<?>... columnTypes) throws IOException {
        final Object[] row = new Object[columnTypes.length];
        for (int i=0; i<columnTypes.length; i++) {
            /*
             * Find the start index and end index of substring to parse.
             * If the part begin with a opening quote, we will search for
             * the closing quote before to search for the column separator.
             */
            int skip=0, end=0;
            if (line.charAt(0) == QUOTE) {
                while (true) {
                    if ((end = line.indexOf(QUOTE, end+1)) < 0) {
                        throw new IOException("Unbalanced quote.");
                    }
                    if (end+1 >= line.length() || line.charAt(end+1) != QUOTE) {
                        break;
                    }
                    // If we reach this point, the quote has been escaped.
                    line = line.substring(0, end) + line.substring(end+1);
                }
                skip = 1;                               // Skip the quotes.
            }
            end = line.indexOf(COLUMN_SEPARATOR, end);
            if (end < 0) {
                end = line.length();
            }
            String part = trim(line, 0, end);
            if (skip != 0) {
                part = trim(line, skip, part.length() - skip);
            }
            if (!part.isEmpty()) {
                final Class<?> type = columnTypes[i];
                if (type != null) {
                    final Object value;
                    if (type == String.class) {
                        value = part;
                    } else if (type == Integer.class) {
                        value = Integer.valueOf(part);
                    } else if (type == Double.class) {
                        value = part.equalsIgnoreCase("NULL") ? Double.NaN : Double.valueOf(part);
                    } else if (type == Boolean.class) {
                        value = Boolean.valueOf(part);
                    } else {
                        throw new IOException("Unsupported column type: " + type);
                    }
                    row[i] = value;
                }
            }
            if (++end >= line.length()) {
                break;
            }
            line = trim(line, end, line.length());
        }
        return row;
    }

    /**
     * Appends columns computed using the given function. The functions will receive in argument the numerical value
     * in the column identified by {@code columnOfCode}. This is usually column 0, which contains the EPSG code.
     *
     * @param columnOfCode  the column of the integer value to give to functions.
     * @param functions     the functions computing values of new columns.
     */
    public void appendColumns(final int columnOfCode, final IntFunction<?>... functions) {
        final int size = content.size();
        for (int i=0; i<size; i++) {
            Object[] row = content.get(i);
            final int code = (Integer) row[columnOfCode];
            int n = row.length;
            row = Arrays.copyOf(row, n + functions.length);
            for (final IntFunction<?> function : functions) {
                row[n++] = function.apply(code);
            }
            content.set(i, row);
        }
    }

    /**
     * Moves the cursor to the next row and returns {@code true} on success,
     * or {@code false} if there is no more row to iterate.
     *
     * @return {@code true} if this parser moved to next row, or {@code false} if there is no more rows.
     */
    public boolean next() {
        if (++cursor < content.size()) {
            currentRow = content.get(cursor);
            return true;
        } else {
            currentRow = null;
            return false;
        }
    }

    /**
     * Returns the value in the current row at the given column.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column, or {@code null} if none.
     * @throws NoSuchElementException if there is currently no active row.
     */
    private Object getValue(final int column) throws NoSuchElementException {
        if (currentRow != null) {
            return currentRow[column];
        }
        throw new NoSuchElementException("No active row.");
    }

    /**
     * Returns the value in the given column as a string.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column, or {@code null} if none.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not a string.
     */
    public String getString(final int column) {
        return (String) getValue(column);
    }

    /**
     * Returns the value in the given column as a list of strings.
     * The original data is assumed to be a semi-colon separated list.
     *
     * @param  column  the column from which to get the value.
     * @return the values in the given column, or an empty array if none.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not a string.
     */
    public String[] getStrings(final int column) {
        final String data = getString(column);
        final List<String> elements = new ArrayList<>(4);
        if (data != null) {
            int lower = 0;
            int upper = data.indexOf(LIST_ELEMENT_SEPARATOR);
            boolean stop = false;
            do {
                if (upper < 0) {
                    upper = data.length();
                    stop = true;
                }
                elements.add(trim(data, lower, upper));
                lower = upper + 1;
                upper = data.indexOf(LIST_ELEMENT_SEPARATOR, lower);
            } while (!stop);
        }
        return elements.toArray(String[]::new);
    }

    /**
     * Returns the value in the given column as an integer.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not an integer.
     */
    public OptionalInt getIntOptional(final int column) {
        final Integer value = (Integer) getValue(column);
        return  (value != null) ? OptionalInt.of(value) : OptionalInt.empty();
    }

    /**
     * Returns the value in the given column as an integer.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not an integer.
     * @throws NullPointerException if there is no value in the given column.
     */
    public int getInt(final int column) {
        return (Integer) getValue(column);
    }

    /**
     * Returns the value in the given column as a sequence of integers.
     * If the original data is a string, then it is assumed to be a semi-colon
     * separated list of values or range of values. Example:
     *
     * <pre>16261-16299; 16070-16089; 16099</pre>
     *
     * @param  column  the column from which to get the values.
     * @return the sequence of integer values in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not a sequence of integers.
     */
    public int[] getInts(final int column) {
        {   // For keeping variable in local scope.
            final Object value = getValue(column);
            if (value instanceof int[]) {
                return (int[]) value;
            }
            if (value instanceof Integer) {
                return new int[] {(Integer) value};
            }
        }
        final String[] strings = getStrings(column);
        int[] values = new int[strings.length];
        int count = 0;
        for (final String element : strings) {
            final int lower, upper, step;
            final int upperAt = element.indexOf(RANGE_SEPARATOR);
            if (upperAt <= 0) {
                // We have a single number, no range.
                lower = upper = Integer.parseInt(element);
                step = 1;
            } else {
                // We have a range of values. Get the upper value.
                lower = Integer.parseInt(trim(element, 0, upperAt));
                int stepAt = element.indexOf(STEP_PREFIX, upperAt);
                if (stepAt < 0) {
                    stepAt = element.length();
                    step = 1;
                } else {
                    step = Integer.parseInt(trim(element, stepAt+1, element.length()));
                }
                upper = Integer.parseInt(trim(element, upperAt+1, stepAt));
            }
            /*
             * At this point, we have all information to add in the array.
             * First, we must ensure that the array as a suffisient capacity.
             */
            final int length = count + (upper - lower) / step + 1;
            if (length > values.length) {
                values = Arrays.copyOf(values, Math.max(values.length*2, length));
            }
            for (int value = lower; value <= upper; value += step) {
                values[count++] = value;
            }
        }
        return Arrays.copyOf(values, count);
    }

    /**
     * Returns the value in the given column as a double.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column, or {@code Double#NaN} if none.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not a double.
     */
    public double getDouble(final int column) {
        final Double value = (Double) getValue(column);
        return (value != null) ? value : Double.NaN;
    }

    /**
     * Returns the value in the given column as a boolean.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws ClassCastException if the value in the given column is not a boolean.
     * @throws NullPointerException if there is no value in the given column.
     */
    public boolean getBoolean(final int column) {
        return (Boolean) getValue(column);
    }

    /**
     * Returns the value in the given column as a definition source.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws IllegalArgumentException if the value in the given column is not a definition source.
     * @throws NullPointerException if there is no value in the given column.
     */
    public DefinitionSource getSource(final int column) {
        return DefinitionSource.parse(getString(column));
    }

    /**
     * Returns the value in the given column as a geodetic CRS type.
     *
     * @param  column  the column from which to get the value.
     * @return the value in the given column.
     * @throws NoSuchElementException if there is currently no active row.
     * @throws IllegalArgumentException if the value in the given column is not a geodetic CRS type.
     * @throws NullPointerException if there is no value in the given column.
     */
    public GeodeticCrsType getCrsType(final int column) {
        return GeodeticCrsType.parse(getString(column));
    }

    /**
     * Groups together records having similar properties. The EPSG or GIGS code is replaced by an array of codes.
     * The criteria for grouping lines are:
     *
     * <ul>
     *   <li>Must be consecutive lines.</li>
     *   <li>Lines must not have been grouped in a previous iteration.</li>
     *   <li>The value in the column identifier by {@code columnOfConstant} must be equal in all lines.</li>
     *   <li>Splitting the value in the column identified by {@code columnOfName} must result equals arrays.</li>
     * </ul>
     *
     * @param columnOfCode       the column of EPSG or GIGS codes to replace by arrays of codes.
     * @param columnOfConstants  the column of the properties which must be constant in a group.
     * @param columnOfName       the column of names which must match a given pattern.
     * @param namePatterns       patterns to use for splitting a name in parts: a base name and a suffix.
     */
    final void regroup(final int columnOfCode, final int[] columnOfConstants, final int columnOfName, final String... namePatterns) {
        for (final String namePattern : namePatterns) {
            final Pattern pattern = Pattern.compile(namePattern);
            for (int start = 0; start < content.size(); start++) {
                final Object[] row = content.get(start);
                if (row[columnOfCode] instanceof Integer) {
                    final String[] parts = pattern.split(row[columnOfName].toString());
                    final String nameBase = parts[0];
                    int end = start;
compare:            while (++end < content.size()) {
                        final Object[] nextRow = content.get(end);
                        if (!(nextRow[columnOfCode] instanceof Integer)) {
                            break;
                        }
                        for (final int i : columnOfConstants) {
                            if (!Objects.equals(row[i], nextRow[i])) {
                                break compare;
                            }
                        }
                        if (!Arrays.equals(parts, pattern.split(nextRow[columnOfName].toString()))) {
                            break;
                        }
                    }
                    final int count = end - start;
                    if (count > 1) {
                        final int[] codes = new int[count];
                        for (int i=0; i<count; i++) {
                            codes[i] = (Integer) content.get(start + i)[columnOfCode];
                        }
                        final Object[] copy = row.clone();
                        Arrays.fill(row, null);
                        row[columnOfCode] = codes;
                        row[columnOfName] = nameBase.trim();
                        for (final int i : columnOfConstants) {
                            row[i] = copy[i];
                        }
                        content.subList(start+1, end).clear();
                    }
                }
            }
        }
    }

    /**
     * Returns a string representation of the currently active row.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder("DataParser[");
        if (currentRow == null) {
            buffer.append("no active row");
        } else {
            buffer.append(cursor);
            String separator = ": ";
            for (final Object value : currentRow) {
                buffer.append(separator).append(value);
                separator = ", ";
            }
        }
        return buffer.append(']').toString();
    }
}
