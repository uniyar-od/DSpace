package org.dspace.ref.compliance.rules;

import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.ref.compliance.definition.model.Value;
import org.dspace.ref.compliance.result.RuleComplianceResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class DateRangeSmallerThanRuleTest {

    @Mock
    private Context context;

    @Mock
    private Item item;

    private Value value;

    @Before
    public void setUp() {
        Metadatum from = new Metadatum();
        from.value = "2016-01-01T11:21:13Z";
        when(item.getMetadata(eq("test"), eq("case"), eq("from"), anyString())).thenReturn(new Metadatum[] {from});

        Metadatum to = new Metadatum();
        to.value = "2016-04-04T10:21:13Z";
        when(item.getMetadata(eq("test"), eq("case"), eq("to"), anyString())).thenReturn(new Metadatum[] {to});

        when(item.getMetadata(eq("test"), eq("case"), eq("invalid"), anyString())).thenReturn(new Metadatum[] {});

        Metadatum invalid2 = new Metadatum();
        invalid2.value = "2016-03-04TBARZ";
        when(item.getMetadata(eq("test"), eq("case"), eq("invalid2"), anyString())).thenReturn(new Metadatum[] {invalid2});

        value = new Value();
    }


    @Test
    public void testDoValidationValid() throws Exception {
        value.setValue("4");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.to", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(true, result.isCompliant());
    }

    @Test
    public void testDoValidationInvalid() throws Exception {
        value.setValue("3");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.to", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }

    @Test
    public void testDoValidationNoNumbericThreshold() throws Exception {
        value.setValue("Foobar");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.to", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }

    @Test
    public void testDoValidationNoThreshold() throws Exception {
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.to", "test range", null);

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }

    @Test
    public void testDoValidationNoDateRange() throws Exception {
        value.setValue("4");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }

    @Test
    public void testDoValidationInvalidDateRange() throws Exception {
        value.setValue("4");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.invalid", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }

    @Test
    public void testDoValidationInvalidDateRange2() throws Exception {
        value.setValue("4");
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule("test.case.from", "test.case.invalid2", "test range", Arrays.asList(value));

        RuleComplianceResult result = rule.validate(context, item);

        assertEquals(false, result.isCompliant());
    }
}