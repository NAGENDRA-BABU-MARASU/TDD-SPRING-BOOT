package com.learnings.ticketapi.repository;

import com.learnings.ticketapi.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
}
