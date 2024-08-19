package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Action;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends Neo4jRepository<Action, String> {
    Action findActionByName(String name);
}
