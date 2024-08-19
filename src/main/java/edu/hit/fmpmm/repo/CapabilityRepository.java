package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Capability;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapabilityRepository extends Neo4jRepository<Capability, String> {
    Capability findCapabilityByName(String name);
}
