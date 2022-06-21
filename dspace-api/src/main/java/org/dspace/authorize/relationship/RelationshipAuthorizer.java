/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authorize.relationship;

import org.dspace.content.Item;
import org.dspace.content.RelationshipType;
import org.dspace.core.Context;

/**
 * Interface for classes that check if a relationship of a specific type can be
 * created between two items.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public interface RelationshipAuthorizer {

    /**
     * Check if a relationship of the given type can be created between the leftItem
     * and the rigthItem.
     *
     * @param  context          The DSpace context
     * @param  relationshipType the type of the relationship to be created
     * @param  leftItem         the left item of the relationship
     * @param  rightItem        the right item of the relationship
     * @return                  true if a relationship can be created, false
     *                          otherwise
     */
    boolean isRelationshipCreatable(Context context, RelationshipType relationshipType, Item leftItem, Item rightItem);

}
