package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Instance;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstanceRepository extends Neo4jRepository<Instance, String> {

    Instance findInstanceByName(String name);

    @Query("MATCH (i:Instance) WHERE i.pro_code=$code RETURN i")
    Instance findInstanceByCode(String code);

    @Query("MATCH (if:Instance)-[:include]->(is:Instance) WHERE ID(is)=toInteger($id) RETURN if")
    List<Instance> findInstancesWitchInclude(@Param("id") String id);

    @Query("MATCH (if:Instance)-[:include]->(is:Instance) WHERE ID(if)=toInteger($id) RETURN is")
    List<Instance> findIncludeSons(@Param("id") String id);

    @Query("MATCH (i:Instance)-[:achieve_goal]->(g:Goal) WHERE ID(g)=toInteger($id) RETURN i")
    List<Instance> findInstancesWhichAchieveGoal(@Param("id") String id);

    @Query("MATCH (i:Instance)-[:has_capability]->(c:Instance) WHERE ID(c)=toInteger($id) RETURN i")
    List<Instance> findCapabilityOwners(@Param("id") String id);

    @Query("MATCH (i:Instance)-[:has_capability]->(c:Instance) WHERE ID(i)=toInteger($id) RETURN c")
    List<Instance> findCapabilityInstancesByOwner(@Param("id") String id);

    @Query("MATCH (c:Capability)-[:has_instance]->(i:Instance) WHERE ID(c)=toInteger($id) RETURN i")
    List<Instance> findCapabilityInstancesByHasInstance(@Param("id") String id);
}
