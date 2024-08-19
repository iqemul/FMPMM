package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Goal;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends Neo4jRepository<Goal, String> {

    @Query("MATCH (i:Instance)-[:achieve_goal]->(g:Goal) WHERE ID(i)=toInteger($id) RETURN g")
    List<Goal> findGoalsByInstance(@Param("id") String id);
}
