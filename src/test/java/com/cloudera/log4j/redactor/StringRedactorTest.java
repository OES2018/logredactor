package com.cloudera.log4j.redactor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import junit.framework.Assert;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringRedactorTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private String resourcePath;
  private static final String MESSAGE = "This string is not redacted";

  private String readFile(String file) throws IOException {
    BufferedReader reader = new BufferedReader( new FileReader(file));
    String line;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    while( ( line = reader.readLine() ) != null ) {
      stringBuilder.append( line );
      stringBuilder.append( ls );
    }

    return stringBuilder.toString();
  }

  @Before
  public void setUp() throws Exception {
    URL resourceUrl = getClass().getResource("/good-1.json");
    File resourceFile = new File(resourceUrl.toURI());
    resourcePath = resourceFile.getParent();
  }

  @Test
  public void testFileDNE() throws Exception {
    final String fileName = "thisfiledoesnotexist.json";
    thrown.expect(FileNotFoundException.class);
    thrown.expectMessage(fileName);
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testFileIsDir() throws Exception {
    final String fileName = "/tmp";
    thrown.expect(FileNotFoundException.class);
    thrown.expectMessage(fileName);
    thrown.expectMessage("directory");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNotJSON() throws Exception {
    final String fileName = resourcePath + "/non-json.json";
    thrown.expect(JsonParseException.class);
    thrown.expectMessage("#");
    thrown.expectMessage("Unexpected");
    thrown.expectMessage(fileName);
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNotJsonString() throws Exception {
    final String json = readFile(resourcePath + "/non-json.json");
    thrown.expect(JsonParseException.class);
    thrown.expectMessage("#");
    thrown.expectMessage("Unexpected");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testNoVersion() throws Exception {
    final String fileName = resourcePath + "/no-version.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("No version specified");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNoVersionString() throws Exception {
    final String json = readFile(resourcePath + "/no-version.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("No version specified");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testUnknownVersion() throws Exception {
    final String fileName = resourcePath + "/unknown-version.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Unknown version");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testUnknownVersionString() throws Exception {
    final String json = readFile(resourcePath + "/unknown-version.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Unknown version");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testAlphaVersion() throws Exception {
    final String fileName = resourcePath + "/alpha-version.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("string");
    thrown.expectMessage(fileName);
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testAlphaVersionString() throws Exception {
    final String json = readFile(resourcePath + "/alpha-version.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("string");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testNoSearch() throws Exception {
    final String fileName = resourcePath + "/no-search.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("search");
    thrown.expectMessage("cannot be empty");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNoSearchString() throws Exception {
    final String json = readFile(resourcePath + "/no-search.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("search");
    thrown.expectMessage("cannot be empty");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testNoReplace() throws Exception {
    final String fileName = resourcePath + "/no-replace.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("replace");
    thrown.expectMessage("cannot be empty");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNoReplaceString() throws Exception {
    final String json = readFile(resourcePath + "/no-replace.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("replace");
    thrown.expectMessage("cannot be empty");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testNoBrace() throws Exception {
    final String fileName = resourcePath + "/no-brace.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Can not instantiate");
    thrown.expectMessage("no single-String constructor/factory");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testNoBraceString() throws Exception {
    final String json = readFile(resourcePath + "/no-brace.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Can not instantiate");
    thrown.expectMessage("no single-String constructor/factory");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testBadRegex() throws Exception {
    final String fileName = resourcePath + "/bad-regex.json";
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Unclosed character class");
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
  }

  @Test
  public void testBadRegexString() throws Exception {
    final String json = readFile(resourcePath + "/bad-regex.json");
    thrown.expect(JsonMappingException.class);
    thrown.expectMessage("Unclosed character class");
    StringRedactor sr = StringRedactor.createFromJsonString(json);
  }

  @Test
  public void testEmptyFile() throws Exception {
    final String fileName = resourcePath + "/empty.json";
    StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
    String result = sr.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
    sr = StringRedactor.createFromJsonFile(null);
    result = sr.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
  }

  @Test
  public void testEmptyString() throws Exception {
    StringRedactor sr = StringRedactor.createFromJsonString("");
    String result = sr.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
    sr = StringRedactor.createFromJsonString(null);
    result = sr.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
  }

  @Test
  public void testEmptyRules() throws Exception {
    final String fileName = resourcePath + "/empty-rules.json";
    final String json = readFile(fileName);
    StringRedactor srf = StringRedactor.createFromJsonFile(fileName);
    StringRedactor srj = StringRedactor.createFromJsonString(json);
    String result = srf.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
    result = srj.redact(MESSAGE);
    Assert.assertEquals(MESSAGE, result);
  }

  @Test
  public void testBasicGood1() throws Exception {
    final String fileName = resourcePath + "/good-1.json";
    final String json = readFile(fileName);
    StringRedactor srf = StringRedactor.createFromJsonFile(fileName);
    StringRedactor srj = StringRedactor.createFromJsonString(json);
    String redacted = srf.redact("Hello, world");
    Assert.assertEquals("Hxllx, wxrld", redacted);
    redacted = srj.redact("Hello, world");
    Assert.assertEquals("Hxllx, wxrld", redacted);
  }

  @Test
  public void testExtraAttr() throws Exception {
    final String fileName = resourcePath + "/extra-attr.json";
    final String json = readFile(fileName);
    StringRedactor srf = StringRedactor.createFromJsonFile(fileName);
    StringRedactor srj = StringRedactor.createFromJsonString(json);
    String redacted = srf.redact("Hello, world");
    Assert.assertEquals("Hxllx, wxrld", redacted);
    redacted = srj.redact("Hello, world");
    Assert.assertEquals("Hxllx, wxrld", redacted);
  }

  @Test
  public void testRealRules() throws Exception {
    final String fileName = resourcePath + "/real-1.json";
    final String json = readFile(fileName);
    StringRedactor srf = StringRedactor.createFromJsonFile(fileName);
    StringRedactor srj = StringRedactor.createFromJsonString(json);

    List<String[]> tests = new ArrayList<String[]>();
    // tests are a list of {"input", "expected"} pairs.
    tests.add(new String[]{"Hello, world", "Hello, world"});
    tests.add(new String[]{"CC 1234-2345-3456-4576", "CC XXXX-XXXX-XXXX-XXXX"});
    tests.add(new String[]{"CC 1234234534654576", "CC XXXXXXXXXXXXXXXX"});
    tests.add(new String[]{"CC 1234,2345,3456,4576", "CC XXXX-XXXX-XXXX-XXXX"});
    tests.add(new String[]{"SSN 123-45-6789", "SSN XXX-XX-XXXX"});
    tests.add(new String[]{"SSN 123456789", "SSN XXXXXXXXX"});
    tests.add(new String[]{"My password=Hello123", "My password=xxxxx"});
    tests.add(new String[]{"Host www.cloudera.com", "Host HOSTNAME.REDACTED"});
    tests.add(new String[]{"www.c1-foo.org rules!", "HOSTNAME.REDACTED rules!"});
    tests.add(new String[]{"IP1 8.8.8.8", "IP1 0.0.0.0"});
    tests.add(new String[]{"IP2 192.168.0.1", "IP2 0.0.0.0"});
    tests.add(new String[]{"My email is myoder@cloudera.com",
            "My email is email@redacted.host"});
    tests.add(new String[]{"hello.world@ex.x-1.fr is interesting",
            "email@redacted.host is interesting"});
    tests.add(new String[]{"Multi 1234-2345-3456-4567\nLine 123-45-6789",
            "Multi XXXX-XXXX-XXXX-XXXX\nLine XXX-XX-XXXX"});

    String redacted;
    for (String[] test : tests) {
      redacted = srf.redact(test[0]);
      Assert.assertEquals("Failed (f) redacting: " + test[0], test[1], redacted);
      redacted = srj.redact(test[0]);
      Assert.assertEquals("Failed (s) redacting: " + test[0], test[1], redacted);
    }
  }

  @Test
  public void testHugeRules() throws Exception {
    final String fileName = resourcePath + "/huge-1.json";
    final String json = readFile(fileName);
    StringRedactor srf = StringRedactor.createFromJsonFile(fileName);
    StringRedactor srj = StringRedactor.createFromJsonString(json);
    String redacted = srf.redact(MESSAGE);
    Assert.assertEquals("This string is not redadted", redacted);
    redacted = srj.redact(MESSAGE);
    Assert.assertEquals("This string is not redadted", redacted);
  }

  private int multithreadedErrors;

  @Test
  public void testMultithreading() throws Exception {
    String fileName = resourcePath + "/numbers.json";  // [0-9] -> #
    final StringRedactor sr = StringRedactor.createFromJsonFile(fileName);
    String redacted = sr.redact("asdf1234fdas666 H3ll0 w0rld");
    Assert.assertEquals("asdf####fdas### H#ll# w#rld", redacted);

    multithreadedErrors = 0;
    Thread[] threads = new Thread[50];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          Pattern numberRegex = Pattern.compile("[0-9]");
          Matcher numberMatcher = numberRegex.matcher("");
          for (int j = 0; j < 500; j++) {
            String original = RandomStringUtils.random(2048);
            String redacted = sr.redact(original);
            numberMatcher.reset(redacted);
            boolean found = numberMatcher.find();
            if (found) {
              synchronized (StringRedactorTest.this) {
                // Assertions here don't stop or fail the whole test
                // So increment this; the main thread will see it and die.
                multithreadedErrors++;
              }
            }
            Assert.assertTrue("Found numbers; orig: " + original +
                    "\n redacted: " + redacted, !found);
          }
        }
      });
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
    Assert.assertEquals("Hit some errors", multithreadedErrors, 0);
  }
}