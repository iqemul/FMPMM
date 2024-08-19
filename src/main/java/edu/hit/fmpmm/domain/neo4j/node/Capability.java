package edu.hit.fmpmm.domain.neo4j.node;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@org.springframework.data.neo4j.core.schema.Node("Capability")
public class Capability extends Node implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @Property
    private String name;

    public Capability() {
    }

    public Capability(String name) {
        this.name = name;
    }

    @Relationship(type = "has_instance", direction = Relationship.Direction.OUTGOING)
    private List<Instance> hasInstances = new ArrayList<>();

    @Relationship(type = "include", direction = Relationship.Direction.OUTGOING)
    private List<Capability> includes = new ArrayList<>();
}
