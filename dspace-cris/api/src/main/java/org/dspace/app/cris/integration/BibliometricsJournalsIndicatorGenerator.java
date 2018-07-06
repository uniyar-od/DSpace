package org.dspace.app.cris.integration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.cris.model.ResearchObject;
import org.dspace.app.cris.model.jdyna.DynamicNestedObject;
import org.dspace.app.cris.model.jdyna.DynamicNestedProperty;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.content.integration.defaultvalues.DefaultValuesBean;
import org.dspace.content.integration.defaultvalues.EnhancedValuesGenerator;
import org.dspace.content.integration.defaultvalues.FulltextPermissionGenerator;

public class BibliometricsJournalsIndicatorGenerator implements EnhancedValuesGenerator {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(FulltextPermissionGenerator.class);

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy");

    private String metadataItemToCheckYear;
    private String metadataNestedToCheckYear;
    private String metadataIndicatorValue;
    private String metadataIndicatorNested;
    private ApplicationService applicationService;

    @Override
    public DefaultValuesBean generateValues(Item item, String schema, String element, String qualifier, String value) {
        DefaultValuesBean result = new DefaultValuesBean();
        String values = "none";
        try {
            result.setLanguage("en");
            result.setMetadataSchema("item");
            result.setMetadataElement("journals");
            result.setMetadataQualifier(getMetadataIndicatorValue());
            List<IMetadataValue> journals = item.getMetadata(schema, element, qualifier, Item.ANY);
            values = buildIndicatorsFromNested(values, journals, item.getMetadata(getMetadataItemToCheckYear()));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        result.setValues(values);
        return result;
    }

    private String buildIndicatorsFromNested(String values, List<IMetadataValue> journals, String year)
            throws ParseException {
        for (IMetadataValue mm : journals) {
            if (StringUtils.isNotBlank(mm.getAuthority())) {
                ResearchObject ro = applicationService.getEntityByCrisId(mm.getAuthority(), ResearchObject.class);
                List<DynamicNestedObject> dnos = applicationService.getNestedObjectsByParentIDAndShortname(ro.getId(),
                        getMetadataIndicatorNested(), DynamicNestedObject.class);

                int rowSelected = 0;
                Date selectedYear = null;
                if (StringUtils.isNotBlank(year)) {
                    Date rowYearToCheck = format.parse(year);

                    List<Date> datesMinor = new ArrayList<Date>();
                    List<Date> datesMajor = new ArrayList<Date>();
                    // check target year
                    external: for (DynamicNestedObject dno : dnos) {

                        Map<String, List<DynamicNestedProperty>> rowsNested = dno.getAnagrafica4view();
                        internal: for (DynamicNestedProperty row : rowsNested.get(getMetadataNestedToCheckYear())) {

                            String rowYear = row.getValue().toString();
                            Date rowDateYear = format.parse(rowYear);
                            int currentYearComparition = rowDateYear.compareTo(rowYearToCheck);
                            if (currentYearComparition <= 0) {
                                datesMinor.add(rowDateYear);
                            } else {
                                datesMajor.add(rowDateYear);
                            }

                        }
                    }

                    Date closestMinor = null;
                    if(datesMinor!=null && !datesMinor.isEmpty()) {
                        closestMinor = Collections.min(datesMinor, new Comparator<Date>() {
                            public int compare(Date d1, Date d2) {
                                long diff1 = Math.abs(d1.getTime() - rowYearToCheck.getTime());
                                long diff2 = Math.abs(d2.getTime() - rowYearToCheck.getTime());
                                return Long.compare(diff1, diff2);
                            }
                        });
                    }
                    if(closestMinor!=null) {
                        selectedYear = closestMinor;
                    }
                    else if(datesMajor!=null && !datesMajor.isEmpty()) {
                        Date closestMajor = Collections.min(datesMajor, new Comparator<Date>() {
                            public int compare(Date d1, Date d2) {
                                long diff1 = Math.abs(d1.getTime() - rowYearToCheck.getTime());
                                long diff2 = Math.abs(d2.getTime() - rowYearToCheck.getTime());
                                return Long.compare(diff1, diff2);
                            }
                        });
                        if(closestMajor!=null) {
                            selectedYear = closestMajor;
                        }
                    }
      

                    external: for (DynamicNestedObject dno : dnos) {

                        Map<String, List<DynamicNestedProperty>> rowsNested = dno.getAnagrafica4view();
                        internal: for (DynamicNestedProperty row : rowsNested.get(getMetadataNestedToCheckYear())) {

                            String rowYear = row.getValue().toString();
                            Date rowDateYear = format.parse(rowYear);
                            if (rowDateYear.equals(selectedYear)) {
                                datesMinor.add(rowDateYear);
                                rowSelected = dno.getPositionDef();
                            } 
                        }
                    }
                    
                }

                // retrieve indicators
                external: for (DynamicNestedObject dno : dnos) {
                    Map<String, List<DynamicNestedProperty>> rowsNested = dno.getAnagrafica4view();
                    internal: for (DynamicNestedProperty row : rowsNested.get(getMetadataIndicatorValue())) {
                        String rowIndicator = row.getValue().toString();
                        int currentRow = dno.getPositionDef();
                        if (rowSelected == currentRow) {
                            values = rowIndicator;
                            break external;
                        }
                    }
                }
            }
        }
        return values;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public String getMetadataItemToCheckYear() {
        return metadataItemToCheckYear;
    }

    public void setMetadataItemToCheckYear(String metadataItemToCheckYear) {
        this.metadataItemToCheckYear = metadataItemToCheckYear;
    }

    public String getMetadataNestedToCheckYear() {
        return metadataNestedToCheckYear;
    }

    public void setMetadataNestedToCheckYear(String metadataNestedToCheckYear) {
        this.metadataNestedToCheckYear = metadataNestedToCheckYear;
    }

    public String getMetadataIndicatorValue() {
        return metadataIndicatorValue;
    }

    public void setMetadataIndicatorValue(String metadataIndicatorValue) {
        this.metadataIndicatorValue = metadataIndicatorValue;
    }

    public String getMetadataIndicatorNested() {
        return metadataIndicatorNested;
    }

    public void setMetadataIndicatorNested(String metadataIndicatorNested) {
        this.metadataIndicatorNested = metadataIndicatorNested;
    }
}
