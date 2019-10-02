/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.webui.cris.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dspace.app.webui.cris.metrics.ItemMetricsDTO;
import org.dspace.services.RequestService;
import org.dspace.utils.DSpace;

import it.cilea.osd.jdyna.components.IBeanSubComponent;
import it.cilea.osd.jdyna.components.IComponent;

public class BibliometricsConfigurerComponent<IBC extends IBeanSubComponent>
        implements IComponent
{

    /** log4j logger */
    private static Logger log = Logger
            .getLogger(BibliometricsConfigurerComponent.class);

    private Map<String, IBC> types = new HashMap<String, IBC>();

    private String shortName;

    @Override
    public long count(HttpServletRequest request, String type, Integer id)
    {
        try
        {
            if(request == null) {
                RequestService requestService = new DSpace().getServiceManager().getServiceByName(RequestService.class.getName(), RequestService.class);
                if(requestService != null && requestService.getCurrentRequest() != null) {
                    request = requestService.getCurrentRequest().getHttpServletRequest();
                } else {
                    return -1;
                }
            }

            HashMap extra = (HashMap)request.getAttribute("extra");
            HashMap<String, ItemMetricsDTO> metrics = (HashMap<String, ItemMetricsDTO>)extra.get("metrics");
            for (String metricType : (ArrayList<String>)extra.get("metricTypes")) {
                if (metrics.get(metricType) != null && metrics.get(metricType).counter != null && metrics.get(metricType).counter > 0) {
                    return metrics.get(metricType).counter.longValue();
                }
            }
        }
        catch (Exception ex)
        {
            if(log.isDebugEnabled()) {
                log.error(ex.getMessage(), ex);
            }
        }
        return -1;
    }

    @Override
    public void evalute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        /** Data are retrieved by the processor {@link MetricsCrisHomeProcessor} */
    }

    @Override
    public List sublinks(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }

    public void setTypes(Map<String, IBC> types)
    {
        this.types = types;
    }

    @Override
    public Map<String, IBC> getTypes() {
        return types;
    }

    @Override
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

}
