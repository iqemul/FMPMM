package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Operation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends Neo4jRepository<Operation, String> {
    @Query("MATCH (g:Goal)-[:operation]->(o:Operation) WHERE ID(g)=toInteger($id) RETURN o")
    public List<Operation> findOperationsByGoal(@Param("id") String id);
}
