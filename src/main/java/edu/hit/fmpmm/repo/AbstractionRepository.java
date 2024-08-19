package edu.hit.fmpmm.repo;

import edu.hit.fmpmm.domain.neo4j.node.Abstraction;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbstractionRepository extends Neo4jRepository<Abstraction, String> {

    Abstraction findAbstractionByName(String name);

    @Query("MATCH (af:Abstraction)-[:include]->(as:Abstraction) WHERE ID(as)=toInteger($id) RETURN af")
    List<Abstraction> findAbstractionWhichInclude(@Param("id") String id);  // 查找与此节点具有include关系的头节点

    @Query("MATCH (a:Abstraction)-[:has_instance]->(i:Instance) WHERE ID(i)=toInteger($id) RETURN a")
    List<Abstraction> findAbstractionsWhichHasInstance(@Param("id") String id);  // 查某实例的抽象概念
}  // 这是用来对Abstraction类型的节点进行操作的
