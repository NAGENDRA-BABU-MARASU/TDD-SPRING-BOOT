package com.learnings.ticketapi.repository.impl;

import com.learnings.ticketapi.model.Agent;
import com.learnings.ticketapi.model.Status;
import com.learnings.ticketapi.model.Ticket;
import com.learnings.ticketapi.repository.TicketFilterRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketFilterRepositoryImpl implements TicketFilterRepository {

    public static final String STATUS_FIELD = "status";
    public static final String CREATED_DATE_FIELD = "createdDate";
    public static final String NAME_FIELD = "name";
    public static final String ASSIGNED_AGENT_FIELD = "assignedAgent";
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Ticket> findWithFilters(List<Status> statuses, LocalDateTime startDate, LocalDateTime endDate, String assignedAgent) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> ticketRoot = query.from(Ticket.class);
        Join<Ticket, Agent> agentJoin = ticketRoot.join(ASSIGNED_AGENT_FIELD, JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(statuses, startDate, endDate, assignedAgent, ticketRoot, cb, agentJoin);

        query.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(query).getResultList();
    }

    private List<Predicate> buildPredicates(List<Status> statuses, LocalDateTime startDate, LocalDateTime endDate, String assignedAgent, Root<Ticket> ticketRoot, CriteriaBuilder cb, Join<Ticket, Agent> agentJoin) {
        List<Predicate> predicates = new ArrayList<>();

        if(statuses != null && !statuses.isEmpty()) {
            predicates.add(ticketRoot.get(STATUS_FIELD).in(statuses));
        }

        if(startDate != null && endDate != null) {
            predicates.add(cb.between(ticketRoot.get(CREATED_DATE_FIELD), startDate, endDate));
        }
        else if( startDate != null ) {
            predicates.add(cb.greaterThanOrEqualTo(ticketRoot.get(CREATED_DATE_FIELD), startDate));
        }
        else if(endDate != null ) {
            predicates.add(cb.lessThanOrEqualTo(ticketRoot.get(CREATED_DATE_FIELD), endDate));
        }

        if(assignedAgent != null && !assignedAgent.trim().isEmpty()) {
            predicates.add(cb.equal(agentJoin.get(NAME_FIELD), assignedAgent));
        }
        return predicates;
    }
}
