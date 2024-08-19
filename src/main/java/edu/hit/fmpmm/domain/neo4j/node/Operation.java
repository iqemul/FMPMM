package edu.hit.fmpmm.domain.neo4j.node;

import edu.hit.fmpmm.domain.neo4j.HasAfterNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
@Data
@EqualsAndHashCode(callSuper = false)
@org.springframework.data.neo4j.core.schema.Node("Operation")
public class Operation extends Node implements HasAfterNode {
    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;
    @Property("obj")
    private String obj;  // 操作对象的类型

    @Relationship(type = "after", direction = Relationship.Direction.OUTGOING)
    private List<Operation> afters = new ArrayList<>();

    @Relationship(type = "has_action", direction = Relationship.Direction.OUTGOING)
    private List<Action> hasActions = new ArrayList<>();

    @Override
    public List<HasAfterNode> customGetAfters() {
        List<Operation> operations = getAfters();
        return new ArrayList<>(operations);
    }

    @Override
    public String customGetId() {
        return getId();
    }
}
