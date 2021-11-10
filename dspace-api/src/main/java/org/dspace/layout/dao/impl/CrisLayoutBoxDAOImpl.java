/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.layout.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.EntityType;
import org.dspace.content.EntityType_;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.layout.CrisLayoutBox;
import org.dspace.layout.CrisLayoutBox_;
import org.dspace.layout.dao.CrisLayoutBoxDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Database Access Object implementation class for the CrisLayoutBox object
 * 
 * @author Danilo Di Nuzzo (danilo.dinuzzo at 4science.it)
 *
 */
public class CrisLayoutBoxDAOImpl extends AbstractHibernateDAO<CrisLayoutBox> implements CrisLayoutBoxDAO {

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Override
    public List<CrisLayoutBox> findByEntityType(
            Context context, String entityType, Integer tabId, Integer limit, Integer offset)
            throws SQLException {
        CriteriaBuilder cb = getCriteriaBuilder(context);
        CriteriaQuery<CrisLayoutBox> query = cb.createQuery(CrisLayoutBox.class);
        Root<CrisLayoutBox> boxRoot = query.from(CrisLayoutBox.class);
        boxRoot.fetch(CrisLayoutBox_.entitytype, JoinType.LEFT);
        // Initialize dynamic predicates list
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(boxRoot.get(CrisLayoutBox_.entitytype).get(EntityType_.LABEL), entityType));

        // Set filter if tabId parameter isn't null
//        if (tabId != null) {
//            Join<CrisLayoutBox, CrisLayoutTab2Box> tabs = boxRoot.join(CrisLayoutBox_.TAB2BOX);
//            predicates.add(cb.equal(tabs.get(CrisLayoutTab2Box_.ID)
//                    .get(CrisLayoutTab2BoxId_.CRIS_LAYOUT_TAB_ID), tabId));
//            query.orderBy(cb.asc(tabs.get(CrisLayoutTab2Box_.POSITION)));
//        }

        Predicate[] predicateArray = new Predicate[predicates.size()];
        predicates.toArray(predicateArray);
        // Set where condition and orderBy
        query.select(boxRoot)
            .where(predicateArray);
        TypedQuery<CrisLayoutBox> exQuery = getHibernateSession(context).createQuery(query);
        // If present set pagination filter
        if ( limit != null && offset != null ) {
            exQuery.setFirstResult(offset).setMaxResults(limit);
        }
        return exQuery.getResultList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dspace.layout.dao.CrisLayoutBoxDAO#findByShortname(org.dspace.core.
     * Context, java.lang.String, java.lang.String)
     */
    @Override
    public CrisLayoutBox findByShortname(Context context, Integer entityTypeId, String shortname) throws SQLException {
        CriteriaBuilder cb = getCriteriaBuilder(context);
        CriteriaQuery<CrisLayoutBox> query = cb.createQuery(CrisLayoutBox.class);
        Root<CrisLayoutBox> boxRoot = query.from(CrisLayoutBox.class);
        Join<CrisLayoutBox, EntityType> join = boxRoot.join(CrisLayoutBox_.entitytype);
        query.where(
                cb.and(
                        cb.equal(boxRoot.get(CrisLayoutBox_.SHORTNAME), shortname),
                        cb.equal(join.get(EntityType_.id), entityTypeId)));
        TypedQuery<CrisLayoutBox> tq = getHibernateSession(context).createQuery(query);
        return tq.getSingleResult();
    }
    @Override
    public List<CrisLayoutBox> findBoxesWithEntityAndType(Context context,
                                                          String entity, String type) throws SQLException {
        CriteriaBuilder cb = getCriteriaBuilder(context);
        CriteriaQuery<CrisLayoutBox> cq = cb.createQuery(CrisLayoutBox.class);
        Root<CrisLayoutBox> boxRoot = cq.from(CrisLayoutBox.class);
        boxRoot.fetch(CrisLayoutBox_.entitytype, JoinType.LEFT);
        cq.where(cb.and(cb.equal(boxRoot.get(CrisLayoutBox_.entitytype)
                .get(EntityType_.LABEL), entity)),cb.equal(boxRoot.get(CrisLayoutBox_.type), type));
        TypedQuery<CrisLayoutBox> query = getHibernateSession(context).createQuery(cq);
        return query.getResultList();
    }
}
