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
@org.springframework.data.neo4j.core.schema.Node("Action")
public class Action extends Node implements HasAfterNode {
    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    public Action() {
    }

    public Action(String name) {
        this.name = name;
    }

    @Relationship(type = "after", direction = Relationship.Direction.OUTGOING)
    private List<Action> afters = new ArrayList<>();

    @Relationship(type = "has_parameter", direction = Relationship.Direction.OUTGOING)
    private List<Parameter> hasParameters = new ArrayList<>();

    @Override
    public List<HasAfterNode> customGetAfters() {
        List<Action> actions = getAfters();
        return new ArrayList<>(actions);
    }

    @Override
    public String customGetId() {
        return getId();
    }
}
