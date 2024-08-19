package edu.hit.fmpmm.domain.neo4j.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@org.springframework.data.neo4j.core.schema.Node("Goal")
public class Goal extends Node {
    @Id
    @GeneratedValue
    private String id;

    @Property("name")
    private String name;

    @CompositeProperty(prefix = "pro", delimiter = "_")
    private Map<String, Object> otherProperties = new HashMap<>();

    public Goal() {
    }

    public Goal(String name, Map<String, Object> otherProperties) {
        this.name = name;
        this.otherProperties = otherProperties;
    }

    @Relationship(type = "operation", direction = Relationship.Direction.OUTGOING)
    private List<Operation> operations = new ArrayList<>();
}
