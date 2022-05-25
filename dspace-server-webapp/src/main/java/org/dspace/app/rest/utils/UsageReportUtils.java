/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.app.rest.model.UsageReportCategoryRest;
import org.dspace.app.rest.model.UsageReportRest;
import org.dspace.app.rest.statistics.StatisticsReportsConfiguration;
import org.dspace.app.rest.statistics.UsageReportGenerator;
import org.dspace.content.Bitstream;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.Site;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/**
 * This is the Service dealing with the {@link UsageReportRest} logic
 *
 * @author Maria Verdonck (Atmire) on 08/06/2020
 */
@Component
public class UsageReportUtils {

    @Autowired
    private StatisticsReportsConfiguration configuration;

    public static final String TOTAL_VISITS_REPORT_ID = "TotalVisits";
    public static final String TOP_ITEMS_REPORT_ID = "TopItems";
    public static final String TOTAL_VISITS_REPORT_ID_RELATION_PERSON_RESEARCHOUTPUTS
        = "TotalVisitsPersonResearchoutputs";
    public static final String TOTAL_VISITS_REPORT_ID_RELATION_PERSON_PROJECTS
        = "TotalVisitsPersonProjects";
    public static final String TOTAL_VISITS_REPORT_ID_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TotalVisitsOrgUnitRppublications";
    public static final String TOTAL_VISITS_REPORT_ID_RELATION_ORGUNIT_PROJECTS
        = "TotalVisitsOrgUnitRpprojects";
    public static final String TOP_ITEMS_REPORT_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS = "TopItemsOrgUnitRppublications";
    public static final String TOTAL_DOWNLOADS_REPORT_ID_RELATION_PERSON_RESEARCHOUTPUTS
        = "TotalDownloadsPersonResearchoutputs";
    public static final String TOTAL_DOWNLOADS_REPORT_ID_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TotalDownloadsOrgUnitRppublications";
    public static final String TOTAL_VISITS_PER_MONTH_REPORT_ID_RELATION_PERSON_RESEARCHOUTPUTS
        = "TotalVisitPerPeriodPersonResearchoutputs";
    public static final String TOTAL_VISITS_PER_MONTH_REPORT_ID_RELATION_PERSON_PROJECTS
        = "TotalVisitPerPeriodPersonProjects";
    public static final String TOTAL_VISITS_PER_MONTH_REPORT_ID_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TotalVisitPerPeriodOrgUnitRppublications";
    public static final String TOTAL_VISITS_PER_MONTH_REPORT_ID = "TotalVisitsPerMonth";
    public static final String TOTAL_DOWNLOADS_REPORT_ID = "TotalDownloads";
    public static final String TOP_COUNTRIES_REPORT_ID = "TopCountries";
    public static final String TOP_COUNTRIES_REPORT_ID_RELATION_PERSON_RESEARCHOUTPUTS
        = "TopCountriesPersonResearchoutputs";
    public static final String TOP_COUNTRIES_REPORT_ID_RELATION_PERSON_PROJECTS
        = "TopCountriesPersonProjects";
    public static final String TOP_COUNTRIES_REPORT_ID_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TopCountriesOrgUnitRppublications";
    public static final String TOP_CITIES_REPORT_ID = "TopCities";
    public static final String TOP_CONTINENTS_REPORT_ID = "TopContinents";
    public static final String TOP_CATEGORIES_REPORT_ID = "TopCategories";
    public static final String TOP_CITIES_REPORT_ID_RELATION_PERSON_PROJECTS =
        "TopCitiesPersonProjects";
    public static final String TOP_CITIES_REPORT_ID_RELATION_PERSON_RESEARCHOUTPUTS
        = "TopCitiesPersonResearchoutputs";
    public static final String TOP_CITIES_REPORT_ID_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TopCitiesOrgUnitRppublications";
    public static final String TOTAL_VISITS_TOTAL_DOWNLOADS = "TotalVisitsAndDownloads";
    public static final String TOTAL_VISITS_TOTAL_DOWNLOADS_RELATION_PERSON_RESEARCHOUTPUTS
        = "TotalViewsDownloadsPersonResearchoutputs";
    public static final String TOTAL_VISITS_TOTAL_DOWNLOADS_RELATION_ORGUNIT_RP_RESEARCHOUTPUTS
        = "TotalViewsDownloadsOrgUnitRppublications";

    public static final String TOP_DOWNLOAD_CONTINENTS_REPORT_ID = "TopDownloadsContinents";
    public static final String TOP_DOWNLOAD_COUNTRIES_REPORT_ID = "TopDownloadsCountries";
    public static final String TOP_DOWNLOAD_CITIES_REPORT_ID = "TopDownloadsCities";
    public static final String TOTAL_DOWNLOAD_PER_MONTH_REPORT_ID = "TotalDownloadsPerMonth";

    /**
     * Get list of usage reports that are applicable to the DSO (of given UUID)
     *
     * @param context   DSpace context
     * @param dso       DSpaceObject we want all available usage reports of
     * @param category  if not null, limit the reports to the ones included in the specified category
     * @return List of usage reports, applicable to the given DSO
     */
    public List<UsageReportRest> getUsageReportsOfDSO(Context context,
                                                      DSpaceObject dso, String category,
                                                      String startDate, String endDate)
        throws SQLException, ParseException, SolrServerException, IOException {
        List<UsageReportCategoryRest> categories = configuration.getCategories(dso);
        List<String> reportIds = new ArrayList();
        List<UsageReportRest> reports = new ArrayList();
        for (UsageReportCategoryRest cat : categories) {
            if (category == null || StringUtils.equals(cat.getId(), category)) {
                for (Entry<String, UsageReportGenerator> entry : cat.getReports().entrySet()) {
                    if (!reportIds.contains(entry.getKey())) {
                        reportIds.add(entry.getKey());
                        reports.add(createUsageReport(context, dso, entry.getKey(), startDate, endDate));
                    }
                }
            }
        }
        return reports;
    }

    private List<String> getReports(Context context, DSpaceObject dso, String category) {
        List<String> reports = new ArrayList();
        if (dso instanceof Site) {
            reports.add(TOTAL_VISITS_REPORT_ID);
        } else {
            reports.add(TOTAL_VISITS_REPORT_ID);
            reports.add(TOTAL_VISITS_PER_MONTH_REPORT_ID);
            reports.add(TOP_COUNTRIES_REPORT_ID);
            reports.add(TOP_CITIES_REPORT_ID);
        }
        if (dso instanceof Item || dso instanceof Bitstream) {
            reports.add(TOTAL_DOWNLOADS_REPORT_ID);
            reports.add(TOTAL_VISITS_TOTAL_DOWNLOADS);
        }
        return reports;
    }

    /**
     * Get list of usage reports categories that are applicable to the DSO (of given UUID)
     *
     * @param context the DSpace Context
     * @param dso     DSpaceObject we want all available usage reports categories of
     *
     * @return List of usage reports categories, applicable to the given DSO
     */
    public List<UsageReportCategoryRest> getUsageReportsCategoriesOfDSO(Context context, DSpaceObject dso)
            throws SQLException, ParseException, SolrServerException, IOException {
        return configuration.getCategories(dso);
    }

    /**
     * Creates the stat different stat usage report based on the report id.
     * If the report id or the object uuid is invalid, an exception is thrown.
     *
     * @param context  DSpace context
     * @param dso     DSpace object we want a stat usage report on
     * @param reportId Type of usage report requested
     * @return Rest object containing the stat usage report, see {@link UsageReportRest}
     */
    public UsageReportRest createUsageReport(Context context, DSpaceObject dso, String reportId)
            throws ParseException, SolrServerException, IOException, SQLException {
        UsageReportGenerator generator = configuration.getReportGenerator(dso, reportId);
        if (generator != null) {
            UsageReportRest usageReportRest = generator.createUsageReport(context, dso, null, null);
            usageReportRest.setId(dso.getID() + "_" + reportId);
            usageReportRest.setReportType(generator.getReportType());
            usageReportRest.setViewMode(generator.getViewMode());
            return usageReportRest;
        } else {
            throw new ResourceNotFoundException("The given report id can't be resolved: " + reportId + "; "
                    + "available reports: TotalVisits, TotalVisitsPerMonth, "
                    + "TotalDownloads, TopCountries, TopCities");
        }
    }
    public UsageReportRest createUsageReport(Context context, DSpaceObject dso, String reportId,
                                             String startDate, String endDate)
            throws ParseException, SolrServerException, IOException, SQLException {
        UsageReportGenerator generator = configuration.getReportGenerator(dso, reportId);
        if (generator != null) {
            UsageReportRest usageReportRest = generator.createUsageReport(context, dso, startDate, endDate);
            usageReportRest.setId(dso.getID() + "_" + reportId);
            usageReportRest.setReportType(generator.getReportType());
            usageReportRest.setViewMode(generator.getViewMode());
            return usageReportRest;
        } else {
            throw new ResourceNotFoundException("The given report id can't be resolved: " + reportId + "; "
                    + "available reports: TotalVisits, TotalVisitsPerMonth, "
                    + "TotalDownloads, TopCountries, TopCities");
        }
    }
    public boolean categoryExists(DSpaceObject dso, String category) {
        List<UsageReportCategoryRest> categories = configuration.getCategories(dso);
        if (categories != null) {
            return categories.stream().anyMatch(x -> StringUtils.equals(category, x.getId()));
        }
        return false;
    }

}
