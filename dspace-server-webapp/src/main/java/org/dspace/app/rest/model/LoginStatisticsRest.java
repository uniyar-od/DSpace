/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import org.dspace.app.rest.RestResourceController;

/**
 * Model a LOGIN statistics for a single EPerson.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class LoginStatisticsRest extends BaseObjectRest<String> {

    public static final String NAME = "login";
    public static final String CATEGORY = RestModel.STATISTICS;

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

}
